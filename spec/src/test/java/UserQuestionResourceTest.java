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
import org.testcontainers.containers.PostgreSQLContainer;
import rx.observers.AssertableSubscriber;
import se.fortnox.reactivewizard.CollectionOptions;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;


public class UserQuestionResourceTest {

    private static UserQuestionResource userQuestionResource;
    private static QuestionResource     questionResource;
    private static UserResource         userResource;


    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup testSetup;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
        userQuestionResource = testSetup.getInjector().getInstance(UserQuestionResource.class);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
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
    public void shouldBePossibleToAddQuestionToUserAndFetchIt() {

        User createdUser = TestSetup.insertUser(userResource);

        // when question is inserted
        Question question = TestSetup.getQuestion("my question title", "my question");

        Auth mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());
        questionResource.postQuestion(mockAuth, question).toBlocking().singleOrDefault(null);

        // then the question should be returned when asking for the users questions
        List<Question> questions = userQuestionResource.getQuestions(createdUser.getId(), new CollectionOptions()).toBlocking().single();
        assertEquals(1, questions.size());

        Question insertedQuestion = questions.get(0);
        assertEquals("my question title", insertedQuestion.getTitle());
        assertEquals("my question", insertedQuestion.getQuestion());
        assertEquals(new Integer(300),insertedQuestion.getBounty());
        assertEquals(createdUser.getId(), insertedQuestion.getUserId());
    }

    @Test
    public void shouldBePossibleToGetQuestionById() {

        User createdUser = TestSetup.insertUser(userResource);

        // when question is inserted
        Question question = TestSetup.getQuestion("my question title", "my question");
        Auth     mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());
        questionResource.postQuestion(mockAuth, question).toBlocking().singleOrDefault(null);
        List<Question> questions = userQuestionResource.getQuestions(createdUser.getId(), new CollectionOptions()).toBlocking().single();
        assertEquals(1, questions.size());

        // then the question should be returned when asking for the specific question
        Question selectedQuestion = userQuestionResource.getQuestion(createdUser.getId(), questions.get(0).getId()).toBlocking().single();

        assertEquals("my question title", selectedQuestion.getTitle());
        assertEquals("my question", selectedQuestion.getQuestion());
        assertEquals(new Integer(300),selectedQuestion.getBounty());
        assertEquals(createdUser.getId(), selectedQuestion.getUserId());
    }

    @Test
    public void shouldBePossibleToGetQuestionBySlackThreadId() {

        User createdUser = TestSetup.insertUser(userResource);
        Auth mockAuth = new MockAuth(createdUser.getId());

        Question question      = TestSetup.getQuestion("my question title", "my question");
        String   slackThreadId = String.valueOf(System.currentTimeMillis());
        question.setSlackId(slackThreadId);

        questionResource.postQuestion(mockAuth, question).toBlocking().singleOrDefault(null);
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
    public void shouldBePossibleToUpdateQuestion() {

        User createdUser = TestSetup.insertUser(userResource);

        // when question is inserted
        Question question = TestSetup.getQuestion("my question title", "my question");

        Auth     mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());
        questionResource.postQuestion(mockAuth, question).toBlocking().singleOrDefault(null);

        long newQuestionId = userQuestionResource.getQuestions(createdUser.getId(), new CollectionOptions()).toBlocking().single().get(0).getId();
        question.setBounty(400);
        question.setTitle("new title");
        question.setQuestion("new question body");
        userQuestionResource.updateQuestion(mockAuth, newQuestionId, question).toBlocking().singleOrDefault(null);
        List<Question> questions = userQuestionResource.getQuestions(createdUser.getId(), new CollectionOptions()).toBlocking().single();
        assertEquals(1, questions.size());

        Question updatedQuestion = questions.get(0);
        assertEquals("new title", updatedQuestion.getTitle());
        assertEquals("new question body", updatedQuestion.getQuestion());
        assertEquals(new Integer(400),updatedQuestion.getBounty());
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
        questionResource.postQuestion(auth, ourQuestion).toBlocking().singleOrDefault(null);
        Auth authOtherUser = new MockAuth(otherUser.getId());
        questionResource.postQuestion(authOtherUser, questionForOtherUser).toBlocking().singleOrDefault(null);

        // then only questions for our user should be returned
        List<Question> questions = userQuestionResource.getQuestions(ourUser.getId(), new CollectionOptions()).toBlocking().single();
        assertEquals(1, questions.size());

        Question insertedQuestion = questions.get(0);
        assertEquals("our users question title", insertedQuestion.getTitle());
        assertEquals(ourUser.getId(), insertedQuestion.getUserId());
    }

}
