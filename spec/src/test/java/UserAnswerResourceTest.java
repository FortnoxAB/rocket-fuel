import api.Answer;
import api.Question;
import api.User;
import api.UserAnswerResource;
import api.UserQuestionResource;
import api.UserResource;
import api.auth.Auth;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import se.fortnox.reactivewizard.CollectionOptions;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserAnswerResourceTest {

    private static UserQuestionResource userQuestionResource;
    private static UserResource         userResource;
    private static UserAnswerResource   userAnswerResource;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup testSetup;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
        userQuestionResource = testSetup.getInjector().getInstance(UserQuestionResource.class);
        userAnswerResource = testSetup.getInjector().getInstance(UserAnswerResource.class);
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
    public void shouldAddAnswerToQuestion() {

        User createdUser = createUser();
        Question question = createQuestion(createdUser);

        Answer answer = new Answer();
        answer.setTitle("this is an answer title");
        answer.setAnswer("this is the body of the answer");

        Auth auth = new MockAuth(createdUser.getId());
        userAnswerResource.createAnswer(auth, question.getId(), answer).toBlocking().singleOrDefault(null);

        Answer createdAnswer = getFirstAnswer(createdUser, question);
        assertFalse(createdAnswer.isAccepted());
        assertEquals("this is the body of the answer", createdAnswer.getAnswer());
        assertEquals("this is an answer title", createdAnswer.getTitle());
        assertEquals(Integer.valueOf(0), createdAnswer.getVotes());
        assertEquals("Test Subject", createdAnswer.getCreatedBy());
    }

    @Test
    public void shouldBeAbleToUpdateAnswer() {

        User createdUser = createUser();
        Question question = createQuestion(createdUser);

        Answer answer = new Answer();
        answer.setTitle("this is an answer title");
        answer.setAnswer("this is the body of the answer");
        Auth auth = new MockAuth(createdUser.getId());

        userAnswerResource.createAnswer(auth, question.getId(), answer).toBlocking().singleOrDefault(null);

        Answer createdAnswer = getFirstAnswer(createdUser, question);

        Answer updatedAnswerInput = new Answer();
        updatedAnswerInput.setTitle("this is an updated title");
        updatedAnswerInput.setAnswer("this is an updated body of the answer");
        updatedAnswerInput.setVotes(3);
        updatedAnswerInput.setAccepted(false);


        userAnswerResource.updateAnswer(auth, question.getId(), createdAnswer.getId(), updatedAnswerInput).toBlocking().singleOrDefault(null);

        Answer updatedAnswer = getFirstAnswer(createdUser, question);

        assertFalse(updatedAnswer.isAccepted());
        assertEquals("this is an updated title", updatedAnswer.getTitle());
        assertEquals("this is an updated body of the answer", updatedAnswer.getAnswer());
        assertEquals(Integer.valueOf(3), updatedAnswer.getVotes());
        assertEquals("Test Subject", updatedAnswer.getCreatedBy());
    }


    @Test
    public void shouldBeAbleToFetchAnswersForGivenUserAndQuestion() {

        User createdUser = createUser();
        Question question = createQuestion(createdUser);

        Answer answer = new Answer();
        answer.setTitle("this is an answer title");
        answer.setAnswer("this is the body of the answer");

        User createdUserTwo = createUser();

        Question questionForUserTwo = createQuestion(createdUserTwo);

        Answer answerForUserTwo = new Answer();
        answerForUserTwo.setTitle("this is an answer title");
        answerForUserTwo.setAnswer("this is the body of the answer");
        userAnswerResource.createAnswer(new MockAuth(createdUser.getId()), question.getId(), answer).toBlocking().singleOrDefault(null);
        userAnswerResource.createAnswer(new MockAuth(createdUserTwo.getId()), questionForUserTwo.getId(), answerForUserTwo).toBlocking().singleOrDefault(null);

        Answer createdAnswer = getFirstAnswer(createdUser, question);
        assertFalse(createdAnswer.isAccepted());
        assertEquals("this is the body of the answer", createdAnswer.getAnswer());
        assertEquals("this is an answer title", createdAnswer.getTitle());
        assertEquals(Integer.valueOf(0), createdAnswer.getVotes());
        assertEquals("Test Subject", createdAnswer.getCreatedBy());
    }

    @Test
    public void shouldMarkBothQuestionAndAnswerAsAccepted() {

        User createdUser = createUser();
        Question question = createQuestion(createdUser);

        Answer answer = new Answer();
        answer.setTitle("this is an answer title");
        answer.setAnswer("this is the body of the answer");

        userAnswerResource.createAnswer(new MockAuth(createdUser.getId()), question.getId(), answer).toBlocking().singleOrDefault(null);

        Answer createdAnswer = getFirstAnswer(createdUser, question);

        userAnswerResource.markAsAnswered(new MockAuth(createdUser.getId()), question.getId(), createdAnswer.getId()).toBlocking().singleOrDefault(null);

        Answer markedAsAccepted = getFirstAnswer(createdUser, question);

        Question markedAsAnswered = userQuestionResource.getQuestion(createdUser.getId(), createdAnswer.getId()).toBlocking().single();

        assertTrue(markedAsAccepted.isAccepted());
        assertTrue(markedAsAnswered.isAnswerAccepted());

    }

    private Answer getFirstAnswer(User createdUser, Question question) {
        List<Answer> createdAnswers = userAnswerResource.getAnswers(createdUser.getId(), question.getId()).toBlocking().single();
        assertEquals(1, createdAnswers.size());
        return createdAnswers.get(0);
    }

    private Question createQuestion(User createdUser) {
        // when question is inserted
        Question question = new Question();
        question.setAnswerAccepted(false);
        question.setBounty(300);
        question.setTitle("my question title");
        question.setVotes(3);
        question.setQuestion("my question");

        userQuestionResource.postQuestion(new MockAuth(createdUser.getId()), question).toBlocking().singleOrDefault(null);

        // then the question should be returned when asking for the users questions
        List<Question> questions = userQuestionResource.getQuestions(createdUser.getId(), new CollectionOptions()).toBlocking().single();
        assertEquals(1, questions.size());
        return questions.get(0);
    }

    private User createUser() {
        final String generatedEmail = UUID.randomUUID().toString() + "@fortnox.se";
        User user = new User();
        user.setEmail(generatedEmail);
        user.setName("Test Subject");
        userResource.createUser(null, user).toBlocking().single();
        return userResource.getUserByEmail(generatedEmail, false).toBlocking().single();
    }
}
