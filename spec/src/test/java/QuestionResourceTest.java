import api.Question;
import api.QuestionResource;
import api.User;
import api.UserResource;
import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;
import se.fortnox.reactivewizard.CollectionOptions;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class QuestionResourceTest {

    private static QuestionResource questionResource;
    private static UserResource userResource;


    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup testSetup;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
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

        User createdUser = insertUser();

        // when question is inserted
        Question question = new Question();
        question.setAnswerAccepted(false);
        question.setBounty(300);
        question.setTitle("my question title");
        question.setVotes(3);
        question.setQuestion("my question");

        questionResource.postQuestion(createdUser.getId(), question).toBlocking().singleOrDefault(null);

        // then the question should be returned when asking for the users questions
        List<Question> questions = questionResource.getQuestions(createdUser.getId(), new CollectionOptions()).toBlocking().single();
        assertEquals(1, questions.size());

        Question insertedQuestion = questions.get(0);
        assertEquals("my question title", insertedQuestion.getTitle());
        assertEquals("my question", insertedQuestion.getQuestion());
        assertEquals(new Integer(300),insertedQuestion.getBounty());
        assertEquals(createdUser.getId(), insertedQuestion.getUserId());
    }

    @Test
    public void shouldBePossibleToGetQuestionById() {

        User createdUser = insertUser();

        // when question is inserted
        Question question = new Question();
        question.setAnswerAccepted(false);
        question.setBounty(300);
        question.setTitle("my question title");
        question.setVotes(3);
        question.setQuestion("my question");

        questionResource.postQuestion(createdUser.getId(), question).toBlocking().singleOrDefault(null);
        List<Question> questions = questionResource.getQuestions(createdUser.getId(), new CollectionOptions()).toBlocking().single();
        assertEquals(1, questions.size());

        // then the question should be returned when asking for the specific question
        Question selectedQuestion = questionResource.getQuestion(createdUser.getId(), questions.get(0).getId()).toBlocking().single();

        assertEquals("my question title", selectedQuestion.getTitle());
        assertEquals("my question", selectedQuestion.getQuestion());
        assertEquals(new Integer(300),selectedQuestion.getBounty());
        assertEquals(createdUser.getId(), selectedQuestion.getUserId());
    }


    @Test
    public void shouldBePossibleToUpdateQuestion() {

        User createdUser = insertUser();

        // when question is inserted
        Question question = new Question();
        question.setAnswerAccepted(false);
        question.setBounty(300);
        question.setTitle("my question title");
        question.setVotes(3);
        question.setQuestion("my question");

        questionResource.postQuestion(createdUser.getId(), question).toBlocking().singleOrDefault(null);

        long newQuestionId = questionResource.getQuestions(createdUser.getId(), new CollectionOptions()).toBlocking().single().get(0).getId();
        question.setBounty(400);
        question.setTitle("new title");
        question.setQuestion("new question body");
        questionResource.updateQuestion(createdUser.getId(), newQuestionId,  question).toBlocking().singleOrDefault(null);
        List<Question> questions = questionResource.getQuestions(createdUser.getId(), new CollectionOptions()).toBlocking().single();
        assertEquals(1, questions.size());

        Question updatedQuestion = questions.get(0);
        assertEquals("new title", updatedQuestion.getTitle());
        assertEquals("new question body", updatedQuestion.getQuestion());
        assertEquals(new Integer(400),updatedQuestion.getBounty());
        assertEquals(createdUser.getId(), updatedQuestion.getUserId());
    }

    @Test
    public void shouldOnyReturnQuestionsForTheSpecifiedUser() {

        User otherUser = insertUser();
        User ourUser = insertUser();


        // when questions are inserted for different users
        Question ourQuestion = new Question();
        ourQuestion.setAnswerAccepted(false);
        ourQuestion.setBounty(300);
        ourQuestion.setTitle("our users question title");
        ourQuestion.setVotes(3);
        ourQuestion.setQuestion("our users question");

        // when question is inserted
        Question questionForOtherUser = new Question();
        questionForOtherUser.setAnswerAccepted(false);
        questionForOtherUser.setBounty(300);
        questionForOtherUser.setTitle("other users question title");
        questionForOtherUser.setVotes(3);
        questionForOtherUser.setQuestion("other users question");

        questionResource.postQuestion(ourUser.getId(), ourQuestion).toBlocking().singleOrDefault(null);
        questionResource.postQuestion(otherUser.getId(), questionForOtherUser).toBlocking().singleOrDefault(null);

        // then only questions for our user should be returned
        List<Question> questions = questionResource.getQuestions(ourUser.getId(), new CollectionOptions()).toBlocking().single();
        assertEquals(1, questions.size());

        Question insertedQuestion = questions.get(0);
        assertEquals("our users question title", insertedQuestion.getTitle());
        assertEquals(ourUser.getId(), insertedQuestion.getUserId());
    }


    private User insertUser() {
        final String generatedEmail = UUID.randomUUID().toString()+"@fortnox.se";
        User user = new User();
        user.setEmail(generatedEmail);
        user.setName("Test Subject");
        user.setVendorId("vendorId");
        userResource.createUser(user).toBlocking().single();
        return userResource.getUserByEmail(generatedEmail).toBlocking().single();
    }

}
