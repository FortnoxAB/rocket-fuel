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
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.List;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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

        Answer answer = createAnswer();

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
        Answer answer = createAnswer();
        Auth auth = new MockAuth(createdUser.getId());

        answerResource.answerQuestion(auth, answer, question.getId()).toBlocking().singleOrDefault(null);

        Answer createdAnswer = getFirstAnswer(createdUser, question);

        Answer updatedAnswerInput = new Answer();
        updatedAnswerInput.setTitle("this is an updated title");
        updatedAnswerInput.setAnswer("this is an updated body of the answer");
        updatedAnswerInput.setVotes(3);
        updatedAnswerInput.setAccepted(false);

        userAnswerResource.updateAnswer(auth,  createdAnswer.getId(), updatedAnswerInput).toBlocking().singleOrDefault(null);

        Answer updatedAnswer = getFirstAnswer(createdUser, question);

        assertFalse(updatedAnswer.isAccepted());
        assertEquals("this is an updated title", updatedAnswer.getTitle());
        assertEquals("this is an updated body of the answer", updatedAnswer.getAnswer());
        assertEquals("Test Subject", updatedAnswer.getCreatedBy());
    }

    @Test
    public void shouldBeAbleToDeleteAnswer() {
        // given answers exists
        User createdUser = createUser();
        Question question = createQuestion(createdUser);

        Answer answer = createAnswer();
        Answer answer2 = createAnswer("title2","answer2");
        Auth auth = new MockAuth(createdUser.getId());

        answerResource.answerQuestion(auth, answer, question.getId()).toBlocking().singleOrDefault(null);
        answerResource.answerQuestion(auth, answer2, question.getId()).toBlocking().singleOrDefault(null);

        Answer createdAnswer = getFirstAnswer(createdUser, question);

        // when we delete a answer
        userAnswerResource.deleteAnswer(auth,  createdAnswer.getId()).toBlocking().singleOrDefault(null);

        // then only the answer we want to delete should be deleted
        List<Answer> remainingAnswers = userAnswerResource.getAnswers(createdUser.getId(), question.getId()).toBlocking().single();
        assertThat(remainingAnswers.size()).isEqualTo(1);
        assertThat(remainingAnswers.get(0).getTitle()).isEqualTo("title2");
    }

    @Test
    public void shouldNotDeleteAnswerThatDoesNotExist() {
        User createdUser = createUser();
        Auth auth = new MockAuth(createdUser.getId());
        long nonExistingAnswerId = 12;
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> userAnswerResource.deleteAnswer(auth,  nonExistingAnswerId).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertEquals(NOT_FOUND, e.getStatus());
                assertEquals("answer.not.found", e.getError());
            });
    }

    @Test
    public void shouldBeAbleToFetchAnswersForGivenUserAndQuestion() {

        User createdUser = createUser();
        Question question = createQuestion(createdUser);
        Answer answer = createAnswer();

        User createdUserTwo = createUser();

        Question questionForUserTwo = createQuestion(createdUserTwo);

        Answer answerForUserTwo = createAnswer();
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
        assertThat(createdAnswers).isNotEmpty();
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

        questionResource.createQuestion(new MockAuth(createdUser.getId()), question).toBlocking().singleOrDefault(null);

        // then the question should be returned when asking for the users questions
        List<Question> questions = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single();
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

    private static Answer createAnswer() {
        Answer answer = new Answer();
        answer.setTitle("this is an answer title");
        answer.setAnswer("this is the body of the answer");
        return answer;
    }
    private static Answer createAnswer(String title, String answerBody) {
        Answer answer = new Answer();
        answer.setTitle(title);
        answer.setAnswer(answerBody);
        return answer;
    }
}
