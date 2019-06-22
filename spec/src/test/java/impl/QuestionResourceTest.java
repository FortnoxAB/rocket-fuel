package impl;

import api.*;
import api.auth.Auth;
import dao.QuestionDao;
import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;
import rx.Observable;
import rx.observers.AssertableSubscriber;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuestionResourceTest {
    private static QuestionResource     questionResource;
    private static UserResource         userResource;

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
    public void shouldThrowErrorWhenServerIsDown() {
        QuestionDao questionDao = mock(QuestionDao.class);
        QuestionResourceImpl questionResource = new QuestionResourceImpl(questionDao);
        when(questionDao.getLatestQuestions(any())).thenReturn(Observable.error(new SQLException()));

        try {
            questionResource.getLatestQuestion(null).toBlocking().single();
            fail("Should have thrown exception");
        } catch(WebException e) {
            assertThat(e.getError()).isEqualTo("failed.to.get.latest.questions");
        }
    }


    @Test
    public void shouldBePossibleToGetQuestionBySlackThreadId() {

        User createdUser = TestSetup.insertUser(userResource);
        Auth mockAuth = new MockAuth(createdUser.getId());

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

    private void generateQuestions(int questionsToGenerate) {
        User createdUser = TestSetup.insertUser(userResource);
        Auth mockAuth    = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());

        for (int i = 1; i <= questionsToGenerate; i++) {
            Question question = TestSetup.getQuestion("my question title " + i, "my question");
            questionResource.createQuestion(mockAuth, question).toBlocking().singleOrDefault(null);
        }
    }

    @Test
    public void shouldListLatest5Questions() {
        int limit = 5;
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
}
