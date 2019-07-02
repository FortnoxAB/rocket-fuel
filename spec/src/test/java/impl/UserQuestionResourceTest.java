package impl;

import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.User;
import api.UserQuestionResource;
import api.UserResource;
import api.auth.Auth;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.testcontainers.containers.PostgreSQLContainer;
import se.fortnox.reactivewizard.jaxrs.WebException;
import slack.SlackResource;

import java.util.List;

import static impl.UserQuestionResourceImpl.QUESTION_NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static rx.Observable.empty;

public class UserQuestionResourceTest {

    private static UserQuestionResource userQuestionResource;
    private static QuestionResource     questionResource;
    private static UserResource         userResource;
    private static AnswerResource       answerResource;


    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup testSetup;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer, binder -> binder.bind(SlackResource.class).toInstance(Mockito.mock(SlackResource.class, new org.mockito.stubbing.Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return empty();
            }
        })));
        userResource = testSetup.getInjector().getInstance(UserResource.class);
        userQuestionResource = testSetup.getInjector().getInstance(UserQuestionResource.class);
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
    public void shouldBePossibleToAddQuestionAndFetchIt() {

        User createdUser = TestSetup.insertUser(userResource);

        // when question is created
        Question question = TestSetup.getQuestion("my question title", "my question");
        Auth mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());

        // when we create the question
        questionResource.createQuestion(mockAuth, question).toBlocking().singleOrDefault(null);

        // then the question should be returned when asking for the users questions
        List<Question> questions = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single();
        assertEquals(1, questions.size());

        Question insertedQuestion = questions.get(0);
        assertEquals("my question title", insertedQuestion.getTitle());
        assertEquals("my question", insertedQuestion.getQuestion());
        assertEquals(question.getBounty(),insertedQuestion.getBounty());
        assertEquals(createdUser.getId(), insertedQuestion.getUserId());
        assertNotNull(insertedQuestion.getId());
    }

    @Test
    public void shouldBePossibleToGetQuestionById() {

        User createdUser = TestSetup.insertUser(userResource);

        // when question is inserted
        Question question = TestSetup.getQuestion("my question title", "my question");
        Auth     mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());
        questionResource.createQuestion(mockAuth, question).toBlocking().singleOrDefault(null);
        List<Question> questions = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single();
        assertEquals(1, questions.size());

        // then the question should be returned when asking for the specific question
        Question selectedQuestion = userQuestionResource.getQuestion(createdUser.getId(), questions.get(0).getId()).toBlocking().single();

        assertEquals("my question title", selectedQuestion.getTitle());
        assertEquals("my question", selectedQuestion.getQuestion());
        assertEquals(Integer.valueOf(300),selectedQuestion.getBounty());
        assertEquals(createdUser.getId(), selectedQuestion.getUserId());
    }



    @Test
    public void shouldBePossibleToUpdateQuestion() {

        User createdUser = TestSetup.insertUser(userResource);

        // when question is inserted
        Question question = TestSetup.getQuestion("my question title", "my question");

        Auth     mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());
        questionResource.createQuestion(mockAuth, question).toBlocking().singleOrDefault(null);

        Question storedQuestion = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single().get(0);
        storedQuestion.setBounty(400);
        storedQuestion.setTitle("new title");
        storedQuestion.setQuestion("new question body");
        userQuestionResource.updateQuestion(mockAuth, storedQuestion.getId(), storedQuestion).toBlocking().singleOrDefault(null);
        List<Question> questions = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single();
        assertEquals(1, questions.size());

        Question updatedQuestion = questions.get(0);
        assertEquals("new title", updatedQuestion.getTitle());
        assertEquals("new question body", updatedQuestion.getQuestion());
        assertEquals(question.getBounty(), updatedQuestion.getBounty());
        assertEquals(createdUser.getId(), updatedQuestion.getUserId());
    }

    @Test
    public void shouldOnyReturnQuestionsForTheSpecifiedUser() {

        User otherUser = TestSetup.insertUser(userResource);
        User ourUser = TestSetup.insertUser(userResource);

        // when questions are inserted for different users
        Question ourQuestion = TestSetup.getQuestion("our users question title", "our users question");

        // when question is inserted
        Question questionForOtherUser = TestSetup.getQuestion("other users question title", "other users question");

        Auth auth = new MockAuth(ourUser.getId());
        questionResource.createQuestion(auth, ourQuestion).toBlocking().singleOrDefault(null);
        Auth authOtherUser = new MockAuth(otherUser.getId());
        questionResource.createQuestion(authOtherUser, questionForOtherUser).toBlocking().singleOrDefault(null);

        // then only questions for our user should be returned
        List<Question> questions = userQuestionResource.getQuestions(ourUser.getId()).toBlocking().single();
        assertEquals(1, questions.size());

        Question insertedQuestion = questions.get(0);
        assertEquals("our users question title", insertedQuestion.getTitle());
        assertEquals(ourUser.getId(), insertedQuestion.getUserId());
    }

    @Test
    public void shouldBePossibleToDeleteQuestion() {

        User createdUser = TestSetup.insertUser(userResource);

        Auth mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());

        // given questions exists
        Question questionToInsert = TestSetup.getQuestion("my question title", "my question");

        Question questionToInsert2 = TestSetup.getQuestion("my question title2", "my question2");

        questionResource.createQuestion(mockAuth, questionToInsert).toBlocking().singleOrDefault(null);
        questionResource.createQuestion(mockAuth, questionToInsert2).toBlocking().singleOrDefault(null);

        // and the questions has answers
        List<Question> questions = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single();

        Answer answer = new Answer();
        answer.setAnswer("just a answer");
        answerResource.answerQuestion(mockAuth, answer, questions.get(0).getId()).toBlocking().singleOrDefault(null);
        answerResource.answerQuestion(mockAuth, answer, questions.get(1).getId()).toBlocking().singleOrDefault(null);

        List<Question> questionsSaved = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single();

        // when we deletes a question
        userQuestionResource.deleteQuestion(mockAuth,questionsSaved.get(0).getId()).toBlocking().singleOrDefault(null);

        // only the question we want to delete should be removed
        List<Question> remaningQuestions = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single();
        assertThat(remaningQuestions.size()).isEqualTo(1);
        assertThat(remaningQuestions.get(0).getTitle()).isEqualTo("my question title2");

        // and the answers should be deleted as well for the deleted question
        List<Answer> answersForTheNonDeletedQuestion = answerResource.getAnswers(questionsSaved.get(1).getId()).toBlocking().singleOrDefault(null);
        assertThat(answersForTheNonDeletedQuestion).isNotEmpty();

        List<Answer> answersForTheDeletedQuestion = answerResource.getAnswers(questionsSaved.get(0).getId()).toBlocking().singleOrDefault(null);
        assertThat(answersForTheDeletedQuestion).isEmpty();
    }

    @Test
    public void shouldNotDeleteQuestionThatDoesNotExist() {
        User createdUser = TestSetup.insertUser(userResource);

        Auth auth = new MockAuth(createdUser.getId());
        long nonExistingQuestionId = 12;
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> userQuestionResource.deleteQuestion(auth,  nonExistingQuestionId).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertEquals(NOT_FOUND, e.getStatus());
                assertEquals(QUESTION_NOT_FOUND, e.getError());
            });
    }
}
