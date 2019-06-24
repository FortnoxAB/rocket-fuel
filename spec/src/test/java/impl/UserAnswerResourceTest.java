package impl;

import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
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

public class UserAnswerResourceTest {

    private static UserQuestionResource userQuestionResource;
    private static QuestionResource     questionResource;
    private static UserResource         userResource;
    private static UserAnswerResource   userAnswerResource;
    private static AnswerResource answerResource;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup testSetup;


    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
        userQuestionResource = testSetup.getInjector().getInstance(UserQuestionResource.class);
        userAnswerResource = testSetup.getInjector().getInstance(UserAnswerResource.class);
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
    public void shouldAddAnswerToQuestion() {

        User createdUser = createUser();
        Question question = createQuestion(createdUser);

        Answer answer = new Answer();
        answer.setTitle("this is an answer title");
        answer.setAnswer("this is the body of the answer");

        Auth auth = new MockAuth(createdUser.getId());
        answerResource.answerQuestion(auth, answer, question.getId()).toBlocking().singleOrDefault(null);

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

        answerResource.answerQuestion(auth, answer, question.getId()).toBlocking().singleOrDefault(null);

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
        answerResource.answerQuestion(new MockAuth(createdUser.getId()), answer, question.getId()).toBlocking().singleOrDefault(null);
        answerResource.answerQuestion(new MockAuth(createdUserTwo.getId()), answerForUserTwo, questionForUserTwo.getId()).toBlocking().singleOrDefault(null);

        Answer createdAnswer = getFirstAnswer(createdUser, question);
        assertFalse(createdAnswer.isAccepted());
        assertEquals("this is the body of the answer", createdAnswer.getAnswer());
        assertEquals("this is an answer title", createdAnswer.getTitle());
        assertEquals(Integer.valueOf(0), createdAnswer.getVotes());
        assertEquals("Test Subject", createdAnswer.getCreatedBy());
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

        questionResource.postQuestion(new MockAuth(createdUser.getId()), question).toBlocking().singleOrDefault(null);

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
