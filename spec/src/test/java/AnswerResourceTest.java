import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.User;
import api.UserResource;
import api.auth.Auth;
import dao.AnswerDao;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class AnswerResourceTest {
    private static QuestionResource questionResource;
    private static AnswerResource   answerResource;
    private static AnswerDao answerDao;

    @ClassRule
    public static  PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();
    private static TestSetup           testSetup;
    private static UserResource userResource;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        answerResource = testSetup.getInjector().getInstance(AnswerResource.class);
        userResource = testSetup.getInjector().getInstance(UserResource.class);

        answerDao = testSetup.getInjector().getInstance(AnswerDao.class);

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
        User user = TestSetup.insertUser(userResource);

        Auth  auth = new MockAuth(user.getId());

        Question question = new Question();
        question.setTitle("Question");
        question.setQuestion("Question");

        Question returnedQuestion = questionResource.postQuestion(auth, question).toBlocking().singleOrDefault(null);
        assertThat(returnedQuestion).isNotNull();

        Answer answer = new Answer();
        answer.setAnswer("test");
        Answer returnedAnswer = answerResource.answerQuestion(auth, answer, returnedQuestion.getId()).toBlocking().singleOrDefault(null);
        assertThat(returnedAnswer).isNotNull();

        answerResource.markAsAcceptedAnswer(auth, returnedAnswer.getId()).toBlocking().singleOrDefault(null);

        List<Answer> answers = answerResource.getAnswers(returnedQuestion.getId()).toBlocking().singleOrDefault(new ArrayList<>());
        assertThat(answers.size()).isEqualTo(1);
        assertThat(answers.get(0).isAccepted()).isTrue();

        Question questionFromDb = questionResource.getQuestionById(returnedQuestion.getId()).toBlocking().singleOrDefault(null);
        assertThat(questionFromDb.isAnswerAccepted()).isTrue();
    }


}
