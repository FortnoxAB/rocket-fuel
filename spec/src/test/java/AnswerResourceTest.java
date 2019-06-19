import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.UserResource;
import api.auth.Auth;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.ArrayList;
import java.util.List;

import static impl.AnswerResourceImpl.ERROR_NOT_OWNER_OF_QUESTION;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class AnswerResourceTest {
    private static QuestionResource questionResource;
    private static AnswerResource   answerResource;

    @ClassRule
    public static  PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();
    private static TestSetup           testSetup;
    private static UserResource        userResource;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        answerResource = testSetup.getInjector().getInstance(AnswerResource.class);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
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
    public void markBothQuestionAndAnswerAsAccepted() {

        Auth questioner = newUser();

        Question question = new Question();
        question.setTitle("Question");
        question.setQuestion("Question");

        Question returnedQuestion = questionResource.postQuestion(questioner, question).toBlocking().singleOrDefault(null);
        assertThat(returnedQuestion).isNotNull();

        Auth answerer = newUser();

        Answer answer = new Answer();
        answer.setAnswer("test");
        Answer returnedAnswer = answerResource.answerQuestion(answerer, answer, returnedQuestion.getId()).toBlocking().singleOrDefault(null);
        assertThat(returnedAnswer).isNotNull();

        answerResource.markAsAcceptedAnswer(questioner, returnedAnswer.getId()).toBlocking().singleOrDefault(null);

        List<Answer> answers = answerResource.getAnswers(returnedQuestion.getId()).toBlocking().singleOrDefault(new ArrayList<>());
        assertThat(answers.size()).isEqualTo(1);
        assertThat(answers.get(0).isAccepted()).isTrue();

        Question questionFromDb = questionResource.getQuestionById(returnedQuestion.getId()).toBlocking().singleOrDefault(null);
        assertThat(questionFromDb.isAnswerAccepted()).isTrue();
    }

    @Test
    public void userCantAcceptAnswerToOtherUsersQuestion() {

        Question question = new Question();
        question.setTitle("Question");
        question.setQuestion("Question");

        Question returnedQuestion = questionResource.postQuestion(newUser(), question).toBlocking().singleOrDefault(null);
        assertThat(returnedQuestion).isNotNull();

        Answer answer = new Answer();
        answer.setAnswer("test");
        Answer returnedAnswer = answerResource.answerQuestion(newUser(), answer, returnedQuestion.getId()).toBlocking().singleOrDefault(null);

        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> answerResource.markAsAcceptedAnswer(newUser(), returnedAnswer.getId()).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertThat(e.getStatus()).isEqualTo(BAD_REQUEST);
                assertThat(e.getError()).isEqualTo(ERROR_NOT_OWNER_OF_QUESTION);
            });
    }

    private Auth newUser() {
        return new MockAuth(TestSetup.insertUser(userResource).getId());
    }

}
