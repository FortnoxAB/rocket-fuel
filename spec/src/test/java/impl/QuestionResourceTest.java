package impl;

import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.User;
import api.UserResource;
import api.auth.Auth;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import dao.AnswerDao;
import dao.QuestionDao;
import dao.QuestionVoteDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.log4j.Appender;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.testcontainers.containers.PostgreSQLContainer;
import rx.Observable;
import rx.observers.AssertableSubscriber;
import se.fortnox.reactivewizard.db.GeneratedKey;
import se.fortnox.reactivewizard.jaxrs.WebException;
import se.fortnox.reactivewizard.test.LoggingMockUtil;
import slack.SlackConfig;
import slack.SlackResource;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static impl.QuestionResourceImpl.FAILED_TO_SEARCH_FOR_QUESTIONS;
import static impl.QuestionResourceImpl.INVALID_VOTE;
import static impl.QuestionResourceImpl.QUESTION_NOT_FOUND;
import static impl.TestSetup.getAnswer;
import static impl.TestSetup.getQuestion;
import static impl.TestSetup.insertUser;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.empty;
import static rx.Observable.error;
import static se.fortnox.reactivewizard.test.TestUtil.matches;
import static util.ObservableAssertions.assertThat;
import static util.ObservableAssertions.assertThatList;

public class QuestionResourceTest {

    public static final LocalDateTime CURRENT     = now();
    public static final LocalDateTime ONE_DAY_AGO = CURRENT.minusDays(1);
    public static final LocalDateTime A_MONTH_AGO = CURRENT.minusMonths(1);

    private static QuestionResource questionResource;
    private static AnswerResource   answerResource;
    private static UserResource     userResource;
    private static QuestionVoteDao  questionVoteDao;
    private static SlackResource    slackResource;
    private static QuestionDao      questionDao;
    private static AnswerDao        answerDao;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup         testSetup;
    private static Appender          appender;
    private static ApplicationConfig applicationConfig;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        answerResource = testSetup.getInjector().getInstance(AnswerResource.class);
        questionVoteDao = testSetup.getInjector().getInstance(QuestionVoteDao.class);
        questionDao = testSetup.getInjector().getInstance(QuestionDao.class);
        answerDao = testSetup.getInjector().getInstance(AnswerDao.class);
        slackResource = mock(SlackResource.class);
        applicationConfig = new ApplicationConfig();
        applicationConfig.setBaseUrl("deployed.fuel.com");
        when(slackResource.postMessageToSlack(anyString(), any())).thenReturn(empty());
    }

    @After
    public void afterEach() throws Exception {
        testSetup.clearDatabase();
        LoggingMockUtil.destroyMockedAppender(appender, QuestionResourceImpl.class);

    }

    @Before
    public void beforeEach() throws Exception {
        testSetup.setupDatabase();
        appender = LoggingMockUtil.createMockedLogAppender(QuestionResourceImpl.class);
    }

    @Test
    public void shouldThrowErrorWhenServerIsDown() {
        QuestionDao          questionDao      = mock(QuestionDao.class);
        QuestionResourceImpl questionResource = new QuestionResourceImpl(questionDao, questionVoteDao, slackResource, new SlackConfig(), applicationConfig);
        when(questionDao.getLatestQuestions(any())).thenReturn(error(new SQLException()));

        try {
            questionResource.getLatestQuestions(null).toBlocking().single();
            fail("Should have thrown exception");
        } catch (WebException e) {
            assertThat(e.getError()).isEqualTo("failed.to.get.latest.questions");
        }
    }

    @Test
    public void shouldBePossibleToGetQuestionBySlackThreadId() {

        TestSetup.insertUser(userResource);
        Auth mockAuth = newAuth();

        Question question      = TestSetup.getQuestion("my question title", "my question");
        String   slackThreadId = String.valueOf(System.currentTimeMillis());
        question.setSlackId(slackThreadId);

        questionResource.createQuestion(mockAuth, question).toBlocking().singleOrDefault(null);
        Question returnedQuestion = questionResource.getQuestionBySlackThreadId(slackThreadId).toBlocking().singleOrDefault(null);

        assertThat(returnedQuestion.getSlackId()).isEqualTo(slackThreadId);
    }

    @Test
    public void shouldReturnNotFoundWhenNoQuestionIsFoundForSlackThreadId() {

        AssertableSubscriber<Question> test = questionResource.getQuestionBySlackThreadId(String.valueOf(System.currentTimeMillis())).test();
        test.awaitTerminalEvent();

        test.assertError(WebException.class);
        assertThat(((WebException)test.getOnErrorEvents().get(0)).getError()).isEqualTo("not.found");
    }

    @Test
    public void shouldListLatest10QuestionsAsDefault() {
        generateQuestions(20);
        List<Question> questions = questionResource.getLatestQuestions(null).toBlocking().single();
        assertEquals(10, questions.size());
    }

    @Test
    public void shouldOnlySearchForQuestion() {
        Auth mockAuth = createUserAndAuth();
        // given a question and a answer
        Question question = createQuestionAndAnswer(mockAuth);

        // when searching with a query that matches the body of the answer
        List<Question> questions = questionResource.getQuestionsBySearchQuery("body", null).toBlocking().single();

        // then the question should be returned
        assertEquals(1, questions.size());
        assertThat(questions.get(0).getTitle()).isEqualTo(question.getTitle());
    }

    @Test
    public void shouldSearchCaseInsensitive() {
        Auth mockAuth = createUserAndAuth();

        // given a question and a answer
        Question question = createQuestionAndAnswer(mockAuth);

        // when searching with a query that matches the body of the answer
        List<Question> questions = questionResource.getQuestionsBySearchQuery("Answer body", null).toBlocking().single();

        // then the question should be returned that matches the answer (case insensitive )
        assertEquals(1, questions.size());
        assertThat(questions.get(0).getTitle()).isEqualTo(question.getTitle());
    }

    private Question createQuestionAndAnswer(Auth mockAuth) {
        // given a question
        Question question = createQuestion(mockAuth, "Question title", "Question");
        // and an answer
        createAnswer(mockAuth, question.getId(), "Answer body");
        return question;
    }

    private Answer createAnswer(Auth mockAuth, long questionId, String answerBody) {
        Answer answer = getAnswer(answerBody);
        answerResource.createAnswer(mockAuth, answer, questionId).toBlocking().singleOrDefault(null);
        return answer;
    }

    @NotNull
    private Question createQuestion(Auth mockAuth, String title, String body) {
        Question question = TestSetup.getQuestion(title, body);
        questionResource.createQuestion(mockAuth, question).toBlocking().singleOrDefault(null);
        return question;
    }

    @Test
    public void shouldReturnEmptyResultIfSearchQueryIsEmpty() {
        Auth mockAuth = createUserAndAuth();

        // given a question and a answer
        createQuestionAndAnswer(mockAuth);

        // when searching with non matching query
        List<Question> questions = questionResource.getQuestionsBySearchQuery("no results for this query", null).toBlocking().single();

        // then no questions should be returned
        assertThat(questions).isEmpty();
    }

    @Test
    public void shouldReturnEmptyIfSearchQueryIsEmpty() {
        Auth mockAuth = createUserAndAuth();

        // given a question and a answer
        createQuestionAndAnswer(mockAuth);

        // when searching with non matching query
        List<Question> questions = questionResource.getQuestionsBySearchQuery("", null).toBlocking().single();

        // then no questions should be returned
        assertThat(questions).isEmpty();
    }

    @Test
    public void shouldReturnEmptyIfSearchQueryIsNull() {
        Auth mockAuth = createUserAndAuth();

        // given a question and a answer
        createQuestionAndAnswer(mockAuth);

        // when searching with no query
        List<Question> questions = questionResource.getQuestionsBySearchQuery(null, null).toBlocking().single();

        // then no questions should be returned
        assertThat(questions).isEmpty();
    }

    @Test
    public void shouldBePossibleToSearchForAnswersThatAreNotCreatedByTheQuestionOwner() {
        // given two users
        Auth firstUserAuth  = createUserAndAuth();
        Auth secondUserAuth = createUserAndAuth();

        // and answers created both by the question owner and the other user
        long questionId = createQuestion(firstUserAuth, "Question?", "Question?").getId();
        createAnswer(firstUserAuth, questionId, "1");
        Answer answerNotCreatedByOwner = createAnswer(secondUserAuth, questionId, "2");

        // when searching by the answer that is not created by the question owner
        List<Question> searchResult = questionResource.getQuestionsBySearchQuery(answerNotCreatedByOwner.getAnswer(), null).toBlocking().single();

        // then the question should be returned
        assertThat(searchResult.size()).isEqualTo(1);
    }

    @Test
    public void shouldReturnErrorIfQueryFails() {
        // given that the query will fail
        QuestionDao questionDao = mock(QuestionDao.class);
        when(questionDao.getQuestions(anyString(), any())).thenReturn(error(new WebException()));
        QuestionResource questionResource = new QuestionResourceImpl(questionDao, questionVoteDao, slackResource, new SlackConfig(), applicationConfig);

        // when searching
        Observable<List<Question>> questions = questionResource.getQuestionsBySearchQuery("explode", null);

        // we should get a exception back
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> questions.toBlocking().single())
            .satisfies(e -> {
                assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getStatus());
                assertEquals(FAILED_TO_SEARCH_FOR_QUESTIONS, e.getError());
            });
    }

    @Test
    public void shouldBePossibleToAddQuestionAndFetchIt() {

        User createdUser = insertUser(userResource);

        // when question is created
        Question question = getQuestion("my question title", "my question");
        Auth     mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());

        // when we create the question
        questionResource.createQuestion(mockAuth, question).toBlocking().singleOrDefault(null);

        // then the question should be returned when asking for the users questions
        List<Question> questions = questionResource.getQuestions(createdUser.getId(), null).toBlocking().single();
        assertEquals(1, questions.size());

        Question insertedQuestion = questions.get(0);
        assertEquals("my question title", insertedQuestion.getTitle());
        assertEquals("my question", insertedQuestion.getQuestion());
        assertEquals(question.getBounty(), insertedQuestion.getBounty());
        assertEquals(createdUser.getId(), insertedQuestion.getUserId());
        assertNotNull(insertedQuestion.getId());
    }

    @Test
    public void shouldBePossibleToGetQuestionById() {

        // when question is inserted
        Question question = getQuestion("my question title", "my question");
        Auth     mockAuth = newAuth();
        mockAuth.setUserId(mockAuth.getUserId());
        questionResource.createQuestion(mockAuth, question).toBlocking().singleOrDefault(null);
        List<Question> questions = questionResource.getQuestions(mockAuth.getUserId(), null).toBlocking().single();
        assertEquals(1, questions.size());

        // then the question should be returned when asking for the specific question
        Question selectedQuestion = questionResource.getQuestion(newAuth(), questions.get(0).getId()).toBlocking().single();

        assertEquals("my question title", selectedQuestion.getTitle());
        assertEquals("my question", selectedQuestion.getQuestion());
        assertEquals(Integer.valueOf(300), selectedQuestion.getBounty());
        assertEquals(mockAuth.getUserId(), selectedQuestion.getUserId().longValue());
    }

    @Test
    public void shouldBePossibleToUpdateQuestion() {

        User createdUser = insertUser(userResource);

        // when question is inserted
        Question question = getQuestion("my question title", "my question");

        Auth mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());
        questionResource.createQuestion(mockAuth, question).toBlocking().singleOrDefault(null);

        Question storedQuestion = questionResource.getQuestions(createdUser.getId(), null).toBlocking().single().get(0);
        storedQuestion.setBounty(400);
        storedQuestion.setTitle("new title");
        storedQuestion.setQuestion("new question body");
        questionResource.updateQuestion(mockAuth, storedQuestion.getId(), storedQuestion).toBlocking().singleOrDefault(null);
        List<Question> questions = questionResource.getQuestions(createdUser.getId(), null).toBlocking().single();
        assertEquals(1, questions.size());

        Question updatedQuestion = questions.get(0);
        assertEquals("new title", updatedQuestion.getTitle());
        assertEquals("new question body", updatedQuestion.getQuestion());
        assertEquals(question.getBounty(), updatedQuestion.getBounty());
        assertEquals(createdUser.getId(), updatedQuestion.getUserId());
    }

    @Test
    public void shouldOnyReturnQuestionsForTheSpecifiedUser() {

        User otherUser = insertUser(userResource);
        User ourUser   = insertUser(userResource);

        // when questions are inserted for different users
        Question ourQuestion = getQuestion("our users question title", "our users question");

        // when question is inserted
        Question questionForOtherUser = getQuestion("other users question title", "other users question");

        Auth auth = new MockAuth(ourUser.getId());
        questionResource.createQuestion(auth, ourQuestion).toBlocking().singleOrDefault(null);
        Auth authOtherUser = new MockAuth(otherUser.getId());
        questionResource.createQuestion(authOtherUser, questionForOtherUser).toBlocking().singleOrDefault(null);

        // then only questions for our user should be returned
        List<Question> questions = questionResource.getQuestions(ourUser.getId(), null).toBlocking().single();
        assertEquals(1, questions.size());

        Question insertedQuestion = questions.get(0);
        assertEquals("our users question title", insertedQuestion.getTitle());
        assertEquals(ourUser.getId(), insertedQuestion.getUserId());
    }

    @Test
    public void shouldBePossibleToDeleteQuestion() {

        User createdUser = insertUser(userResource);

        Auth mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());

        // given questions exists
        Question questionToInsert = getQuestion("my question title", "my question");

        Question questionToInsert2 = getQuestion("my question title2", "my question2");

        questionResource.createQuestion(mockAuth, questionToInsert).toBlocking().singleOrDefault(null);
        questionResource.createQuestion(mockAuth, questionToInsert2).toBlocking().singleOrDefault(null);

        // and the questions has answers
        List<Question> questions = questionResource.getQuestions(createdUser.getId(), null).toBlocking().single();

        Answer answer = new Answer();
        answer.setAnswer("just a answer");
        answerResource.createAnswer(mockAuth, answer, questions.get(0).getId()).toBlocking().singleOrDefault(null);
        answerResource.createAnswer(mockAuth, answer, questions.get(1).getId()).toBlocking().singleOrDefault(null);

        List<Question> questionsSaved = questionResource.getQuestions(createdUser.getId(), null).toBlocking().single();

        // when we deletes a question
        questionResource.deleteQuestion(mockAuth, questionsSaved.get(0).getId()).toBlocking().singleOrDefault(null);

        // only the question we want to delete should be removed
        List<Question> remaningQuestions = questionResource.getQuestions(createdUser.getId(), null).toBlocking().single();
        assertThat(remaningQuestions.size()).isEqualTo(1);
        assertThat(remaningQuestions.get(0).getTitle()).isEqualTo("my question title2");

        // and the answers should be deleted as well for the deleted question
        List<Answer> answersForTheNonDeletedQuestion = answerResource.getAnswers(mockAuth, questionsSaved.get(1).getId()).toBlocking().singleOrDefault(null);
        assertThat(answersForTheNonDeletedQuestion).isNotEmpty();

        List<Answer> answersForTheDeletedQuestion = answerResource.getAnswers(mockAuth, questionsSaved.get(0).getId()).toBlocking().singleOrDefault(null);
        assertThat(answersForTheDeletedQuestion).isEmpty();
    }

    @Test
    public void shouldNotDeleteQuestionThatDoesNotExist() {
        User createdUser = insertUser(userResource);

        Auth auth                  = new MockAuth(createdUser.getId());
        long nonExistingQuestionId = 12;
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> questionResource.deleteQuestion(auth, nonExistingQuestionId).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertEquals(NOT_FOUND, e.getStatus());
                assertEquals(QUESTION_NOT_FOUND, e.getError());
            });
    }

    @Test
    public void shouldLogThatWeCouldNotSendSlackNotificationWhenQuestionIsCreated() {
        // given bad slack config
        Auth     auth     = new Auth();
        Question question = TestSetup.getQuestion("title", "body");
        when(slackResource.postMessageToSlack(eq("rocket-fuel"), any())).thenReturn(error(new SQLException("poff")));
        QuestionResource questionResource = new QuestionResourceImpl(questionDao, questionVoteDao, slackResource, new SlackConfig(), applicationConfig);

        // when we try to add the question to rocket fuel
        questionResource.createQuestion(auth, question).toBlocking().single();

        // it shall log that we could notify by slack that the question was added
        verify(appender).doAppend(matches(log -> {
            assertThat(log.getLevel().toString()).isEqualTo("ERROR");
            assertThat(log.getMessage().toString()).contains("failed to notify by slack that question has been added");
        }));
    }

    @Test
    public void shouldSendCorrectMessageToSlack() {
        // given that we have a question that we want to save
        Auth     auth     = new Auth();
        Question question = TestSetup.getQuestion("title of question?", "who does one do?");
        when(slackResource.postMessageToSlack(eq("rocket-fuel"), any())).thenReturn(empty());
        QuestionResource questionResource = new QuestionResourceImpl(questionDao, questionVoteDao, slackResource, new SlackConfig(), applicationConfig);

        // when we add the the question to rocket fuel
        questionResource.createQuestion(auth, question).toBlocking().single();
        ArgumentCaptor<List> mapArgumentCaptor = ArgumentCaptor.forClass(List.class);

        // then a message shall be sent through slack that a new question has been submitted.
        verify(slackResource, times(1)).postMessageToSlack(any(), mapArgumentCaptor.capture());
        SectionBlock header  = (SectionBlock)mapArgumentCaptor.getValue().get(0);
        SectionBlock content = (SectionBlock)mapArgumentCaptor.getValue().get(1);

        String headerText  = ((MarkdownTextObject)header.getText()).getText();
        String contentText = ((MarkdownTextObject)content.getText()).getText();
        assertThat(headerText).isEqualTo("A new question: *title of question?* was submitted.");
        assertThat(contentText).isEqualTo("Head over to <deployed.fuel.com/question/1|rocket-fuel> to view the question.");

    }

    @Test
    public void shouldYieldCorrectVotesWhenDownVotingAndUpVoting() {

        Auth auth1 = newAuth();
        Auth auth2 = newAuth();
        Auth auth3 = newAuth();

        Question question = questionResource.createQuestion(newAuth(), getQuestion("my question title", "my question")).toBlocking().singleOrDefault(null);

        voteAndAssertSuccess(questionResource::downVoteQuestion, auth1, question, -1);
        voteAndAssertFailure(questionResource::downVoteQuestion, auth1, question);
        voteAndAssertSuccess(questionResource::upVoteQuestion, auth1, question, 0);
        assertNoVote(auth1, question);
        voteAndAssertSuccess(questionResource::upVoteQuestion, auth1, question, 1);
        voteAndAssertFailure(questionResource::upVoteQuestion, auth1, question);

        voteAndAssertSuccess(questionResource::upVoteQuestion, auth2, question, 2);

        voteAndAssertSuccess(questionResource::upVoteQuestion, auth3, question, 3);

        voteAndAssertSuccess(questionResource::downVoteQuestion, auth2, question, 2);
        assertNoVote(auth2, question);

        voteAndAssertSuccess(questionResource::downVoteQuestion, auth3, question, 1);
        assertNoVote(auth2, question);
    }

    @Test
    public void shouldThrowErrorWhenUserVotesOnOwnAnswer() {

        Auth     auth     = newAuth();
        Question question = questionResource.createQuestion(auth, getQuestion("my question title", "my question")).toBlocking().singleOrDefault(null);

        voteAndAssertFailure(questionResource::downVoteQuestion, auth, question);
        voteAndAssertFailure(questionResource::upVoteQuestion, auth, question);
    }

    @Test
    public void shouldListLatest5Questions() {
        int limit               = 5;
        int questionsToGenerate = 10;

        generateQuestions(questionsToGenerate);

        assertThatList(questionResource.getLatestQuestions(limit))
            .hasExactlyOne()
            .hasSize(limit);
    }

    @Test
    public void shouldSortLatestQuestions() {
        List<Long> inExpectedOrder = asList(
            createQuestionWithoutVotes(CURRENT),
            createQuestionWithUnacceptedAnswer(CURRENT.minusMinutes(1), 3),
            createQuestionWithoutVotes(ONE_DAY_AGO),
            createQuestionWithoutAnswer(ONE_DAY_AGO.minusMinutes(1), 3),
            createQuestionWithoutVotes(A_MONTH_AGO)
        );
        assertOrder(questionResource::getLatestQuestions, inExpectedOrder);
    }

    @Test
    public void shouldSortPopularQuestions() {
        List<Long> inExpectedOrder = asList(
            createQuestionWithUnacceptedAnswer(CURRENT, 3),
            createQuestionWithoutAnswer(CURRENT.minusMinutes(1), 3),
            createQuestionWithUnacceptedAnswer(ONE_DAY_AGO, 3),
            createQuestionWithUnacceptedAnswer(A_MONTH_AGO, 3),
            createQuestionWithUnacceptedAnswer(CURRENT, 2),
            createQuestionWithUnacceptedAnswer(CURRENT, 1),
            createQuestionWithUnacceptedAnswer(CURRENT, 0)
        );
        assertOrder(questionResource::getPopularQuestions, inExpectedOrder);
    }

    @Test
    public void shouldSortPopularUnansweredQuestions() {
        List<Long> inExpectedOrder = asList(
            createQuestionWithoutAnswer(CURRENT, 3),
            createQuestionWithoutAnswer(ONE_DAY_AGO, 3),
            createQuestionWithoutAnswer(A_MONTH_AGO, 3),
            createQuestionWithoutAnswer(CURRENT, 2),
            createQuestionWithoutAnswer(CURRENT, 1)
        );

        // red herrings
        createQuestionWithUnacceptedAnswer(CURRENT, 3);
        createQuestionWithAcceptedAnswer(CURRENT);

        assertOrder(questionResource::getPopularUnansweredQuestions, inExpectedOrder);
    }

    @Test
    public void shouldSortRecentlyAcceptedQuestions() {
        List<Long> inExpectedOrder = asList(
            createQuestionWithAcceptedAnswer(CURRENT),
            createQuestionWithAcceptedAnswer(ONE_DAY_AGO),
            createQuestionWithAcceptedAnswer(A_MONTH_AGO)
        );

        // red herrings
        createQuestionWithUnacceptedAnswer(CURRENT, 3);
        createQuestionWithoutAnswer(CURRENT, 3);

        assertOrder(questionResource::getRecentlyAcceptedQuestions, inExpectedOrder);
    }

    private static void assertOrder(Function<Integer, Observable<List<Question>>> method, List<Long> inExpectedOrder) {
        assertThatList(method.apply(inExpectedOrder.size()))
            .hasExactlyOne()
            .extracting(Question::getId)
            .containsExactlyElementsOf(inExpectedOrder);
    }

    private Long createQuestionWithoutVotes(LocalDateTime created) {
        return createQuestion("a", created, null, 0, true);
    }

    private Long createQuestionWithUnacceptedAnswer(LocalDateTime created, int votes) {
        return createQuestion("a", created, null, votes, true);
    }

    private Long createQuestionWithoutAnswer(LocalDateTime created, int votes) {
        return createQuestion("a", created, null, votes, false);
    }

    private Long createQuestionWithAcceptedAnswer(LocalDateTime accepted) {
        return createQuestion("a", now(), accepted, 0, true);
    }

    private Long createQuestion(String title, LocalDateTime created, LocalDateTime accepted, int votes, boolean createUnAcceptedAnswer) {

        Auth mockAuth = new MockAuth(insertUser(userResource).getId());

        Question question = questionDao.addQuestion(mockAuth.getUserId(), getQuestion(title, RandomString.make()), created)
              .map(GeneratedKey::getKey)
              .flatMap(questionDao::getQuestion)
              .toBlocking().single();

        range(0, votes)
            .forEach(i -> assertThat(questionResource.upVoteQuestion(newAuth(), question.getId())).isEmpty());

        assertThat(questionResource.getQuestion(mockAuth, question.getId()))
            .hasExactlyOne()
            .satisfies(q -> assertThat(q.getVotes()).isEqualTo(votes));

        if(createUnAcceptedAnswer) {
            assertThat(answerResource.createAnswer(mockAuth, getAnswer(RandomString.make()), question.getId()))
                .hasExactlyOne();
        }

        if (nonNull(accepted)) {
            Answer acceptedAnswer = answerResource.createAnswer(mockAuth, getAnswer(RandomString.make()), question.getId()).toBlocking().firstOrDefault(null);
            acceptedAnswer.setAcceptedAt(accepted);
            assertThat(answerDao.updateAnswer(mockAuth.getUserId(), acceptedAnswer.getId(), acceptedAnswer)).isEmpty();
        }
        return question.getId();
    }

    private void voteAndAssertSuccess(BiFunction<Auth, Long, Observable<Void>> call, Auth auth, Question question, int expectedVotes) {
        call.apply(auth, question.getId()).test()
            .awaitTerminalEvent()
            .assertNoErrors();
        assertThat(questionResource.getQuestionById(question.getId()).test().awaitTerminalEvent().getOnNextEvents())
            .extracting(Question::getVotes)
            .containsExactly(expectedVotes);
    }

    private void voteAndAssertFailure(BiFunction<Auth, Long, Observable<Void>> call, Auth auth, Question question) {
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> call.apply(auth, question.getId()).toBlocking().single())
            .satisfies(e -> {
                assertThat(e).isInstanceOf(WebException.class);
                assertThat(e).hasFieldOrPropertyWithValue("status", BAD_REQUEST);
                assertThat(e).hasFieldOrPropertyWithValue("error", INVALID_VOTE);
            });
    }

    private void assertNoVote(Auth auth, Question answer) {
        questionVoteDao.findVote(auth.getUserId(), answer.getId()).test().awaitTerminalEvent()
            .assertNoValues();
    }

    private Auth newAuth() {
        User createdUser = insertUser(userResource);
        return new MockAuth(createdUser.getId());
    }

    private Auth createUserAndAuth() {
        User createdUser = TestSetup.insertUser(userResource);
        Auth mockAuth    = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());
        return mockAuth;
    }

    private void generateQuestions(int questionsToGenerate) {
        Auth mockAuth = createUserAndAuth();

        for (int i = 1; i <= questionsToGenerate; i++) {
            createQuestion(mockAuth, "my question title " + i, "my question");
        }
    }

}
