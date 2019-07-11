package impl;

import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.User;
import api.UserResource;
import api.auth.Auth;
import dao.QuestionDao;
import dao.QuestionVoteDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import rx.Observable;
import rx.observers.AssertableSubscriber;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.sql.SQLException;
import java.util.List;

import static impl.QuestionResourceImpl.FAILED_TO_SEARCH_FOR_QUESTIONS;
import static impl.TestSetup.insertUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static rx.Observable.error;

public class QuestionResourceTest {
    private static QuestionResource questionResource;
    private static AnswerResource   answerResource;
    private static UserResource     userResource;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup testSetup;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        answerResource = testSetup.getInjector().getInstance(AnswerResource.class);
    }

    @After
    public void afterEach() throws Exception {
        testSetup.clearDatabase();
    }

    @Before
    public void beforeEach() throws Exception {
        testSetup.setupDatabase();
    }

    @Test
    public void shouldThrowErrorWhenServerIsDown() {
        QuestionDao          questionDao      = mock(QuestionDao.class);
        QuestionResourceImpl questionResource = new QuestionResourceImpl(questionDao);
        when(questionDao.getLatestQuestions(any())).thenReturn(error(new SQLException()));

        try {
            questionResource.getLatestQuestion(null).toBlocking().single();
            fail("Should have thrown exception");
        } catch (WebException e) {
            assertThat(e.getError()).isEqualTo("failed.to.get.latest.questions");
        }
    }

    @Test
    public void shouldBePossibleToGetQuestionBySlackThreadId() {

        User createdUser = TestSetup.insertUser(userResource);
        Auth mockAuth    = newAuth();

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
        List<Question> questions = questionResource.getLatestQuestion(null).toBlocking().single();
        assertEquals(10, questions.size());
    }

    @Test
    public void shouldOnlySearchForQuestion() {
        Auth mockAuth = createUserAndAuth();
        // given a question and a answer
        Question question = createQuestionAndAnswer(mockAuth);

        // when searching with a query that matches the body of the answer
        List<Question> questions = questionResource.getQuestionsBySearchQuery("body").toBlocking().single();

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
        List<Question> questions = questionResource.getQuestionsBySearchQuery("Answer body").toBlocking().single();

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
        Answer answer = TestSetup.getAnswer(answerBody);
        answerResource.answerQuestion(mockAuth, answer, questionId).toBlocking().singleOrDefault(null);
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
        List<Question> questions = questionResource.getQuestionsBySearchQuery("no results for this query").toBlocking().single();

        // then no questions should be returned
        assertThat(questions).isEmpty();
    }

    @Test
    public void shouldReturnEmptyIfSearchQueryIsEmpty() {
        Auth mockAuth = createUserAndAuth();

        // given a question and a answer
        createQuestionAndAnswer(mockAuth);

        // when searching with non matching query
        List<Question> questions = questionResource.getQuestionsBySearchQuery("").toBlocking().single();

        // then no questions should be returned
        assertThat(questions).isEmpty();
    }

    @Test
    public void shouldReturnEmptyIfSearchQueryIsNull() {
        Auth mockAuth = createUserAndAuth();

        // given a question and a answer
        createQuestionAndAnswer(mockAuth);

        // when searching with no query
        List<Question> questions = questionResource.getQuestionsBySearchQuery(null).toBlocking().single();

        // then no questions should be returned
        assertThat(questions).isEmpty();
    }

    @Test
    public void shouldBePossibleToSearchForAnswersThatAreNotCreatedByTheQuestionOwner() {
        // given two users
        Auth firstUserAuth = createUserAndAuth();
        Auth secondUserAuth = createUserAndAuth();

        // and answers created both by the question owner and the other user
        long questionId = createQuestion(firstUserAuth, "Question?","Question?").getId();
        createAnswer(firstUserAuth, questionId, "1");
        Answer answerNotCreatedByOwner = createAnswer(secondUserAuth, questionId, "2");

        // when searching by the answer that is not created by the question owner
        List<Question> searchResult = questionResource.getQuestionsBySearchQuery(answerNotCreatedByOwner.getAnswer()).toBlocking().single();

        // then the question should be returned
        assertThat(searchResult.size()).isEqualTo(1);
    }

    @Test
    public void shouldReturnErrorIfQueryFails() {
        // given that the query will fail
        QuestionDao questionDao = mock(QuestionDao.class);
        when(questionDao.getQuestions(anyString())).thenReturn(error(new WebException()));
        QuestionResource questionResource = new QuestionResourceImpl(questionDao);

        // when searching
        Observable<List<Question>> questions = questionResource.getQuestionsBySearchQuery("explode");

        // we should get a exception back
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> questions.toBlocking().single())
            .satisfies(e -> {
                assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getStatus());
                assertEquals(FAILED_TO_SEARCH_FOR_QUESTIONS, e.getError());
            });
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

    @Test
    public void shouldListLatest5Questions() {
        int limit               = 5;
        int questionsToGenerate = 10;

        generateQuestions(questionsToGenerate);

        List<Question> questions = questionResource.getLatestQuestion(limit).toBlocking().single();
        assertEquals(limit, questions.size());

        for (int i = 0; i < limit; i++) {
            Question insertedQuestion = questions.get(i);
            assertEquals("my question title " + (questionsToGenerate - i), insertedQuestion.getTitle());
            assertEquals("my question", insertedQuestion.getQuestion());
        }
    }


    private Auth newAuth() {
        User createdUser = insertUser(userResource);
        return new MockAuth(createdUser.getId());
    }
}
