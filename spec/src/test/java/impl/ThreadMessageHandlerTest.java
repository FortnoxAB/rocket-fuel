package impl;

import api.Answer;
import api.Question;
import api.QuestionResource;
import api.User;
import api.UserAnswerResource;
import api.UserResource;
import com.github.seratch.jslack.api.model.Message;
import com.google.gson.JsonObject;
import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;
import slack.SlackResource;
import slack.ThreadMessageHandler;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.empty;
import static rx.Observable.just;

public class ThreadMessageHandlerTest {
    private static QuestionResource     questionResource;
    private static UserAnswerResource   userAnswerResource;
    private static UserResource         userResource;
    private static TestSetup            testSetup;
    private static ThreadMessageHandler threadMessageHandler;
    private static SlackResource        slackResourceMock;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();



    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer, new AbstractModule() {
            @Override
            protected void configure() {
                SlackResource slackResource = Mockito.mock(SlackResource.class);
                binder().bind(SlackResource.class).toInstance(slackResource);
            }
        });

        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
        threadMessageHandler = testSetup.getInjector().getInstance(ThreadMessageHandler.class);

        slackResourceMock = testSetup.getInjector().getInstance(SlackResource.class);
        userAnswerResource = testSetup.getInjector().getInstance(UserAnswerResource.class);
    }

    @After
    public void afterEach() throws Exception {
        testSetup.clearDatabase();
        reset(slackResourceMock);
    }

    @Before
    public void beforeEach() throws Exception {
        testSetup.setupDatabase();
        when(slackResourceMock.postMessageToSlack(anyString(), anyString(), anyString())).thenReturn(empty());
    }


    @Test
    public void shouldCreateNewThreadWhenNewThreadIsCreatedInSlack() {

        //Given
        User user = TestSetup.insertUser(userResource);
        User originalMessageUser = TestSetup.insertUser(userResource);

        long questionIdLong = System.currentTimeMillis();
        String questionId = String.valueOf(System.currentTimeMillis());

        Message message = new Message();
        message.setUser("original_message_user");
        message.setTs(questionId);
        message.setText("a clever question");

        when(slackResourceMock.getUserEmail("user_id")).thenReturn(just(user.getEmail()));
        when(slackResourceMock.getUserEmail("original_message_user")).thenReturn(just(originalMessageUser.getEmail()));
        when(slackResourceMock.getMessageFromSlack(eq("channel"), eq(message.getTs()))).thenReturn(just(message));


        //When
        JsonObject slackMessage = new JsonObject();

        slackMessage.addProperty("thread_ts", questionId);
        slackMessage.addProperty("ts", questionId+1);
        slackMessage.addProperty("channel", "channel");
        slackMessage.addProperty("user", "user_id");
        slackMessage.addProperty("text", "an answer to your question");

        threadMessageHandler.handleMessage(slackMessage).toBlocking().singleOrDefault(null);

        //Then
        //Make sure the message is posted to slack
        verify(slackResourceMock).postMessageToSlack(
                eq(slackMessage.get("channel").getAsString()),
                eq("This looks like an interesting conversation, added it to rocket-fuel"),
                eq(slackMessage.get("thread_ts").getAsString()));

        //Make sure the question is created connected to the user connected to the slack user email
        Question whateverQuestion = questionResource.getQuestionBySlackThreadId(questionId).toBlocking().singleOrDefault(null);
        assertNotNull(whateverQuestion);
        assertThat(whateverQuestion.getUserId()).isEqualTo(originalMessageUser.getId());

        //Make sure the answer is also created
        List<Answer> answers = userAnswerResource.getAnswers(user.getId(), whateverQuestion.getId()).toBlocking().singleOrDefault(null);
        assertThat(answers).isNotNull();
        assertThat(answers.get(0).getSlackId()).isNotNull();

        //When second message in thread
        JsonObject messageNo2 = new JsonObject();

        messageNo2.addProperty("thread_ts", questionId);
        messageNo2.addProperty("ts", String.valueOf(questionIdLong+1));
        messageNo2.addProperty("channel", "channel");
        messageNo2.addProperty("user", "user_id");
        messageNo2.addProperty("text", "yet another answer");

        threadMessageHandler.handleMessage(messageNo2).toBlocking().singleOrDefault(null);

        answers = userAnswerResource.getAnswers(user.getId(), whateverQuestion.getId()).toBlocking().singleOrDefault(null);
        assertThat(answers).isNotNull();
        assertThat(answers).hasSize(2);
        assertThat(answers.get(1).getSlackId()).isNotNull();
        assertThat(answers.get(0).getId()).isNotNull();
        assertThat(answers.get(1).getId()).isNotNull();

        //No sending to slack the second time. Verifying only single invocation after second call to handleMessage
        verify(slackResourceMock, times(1)).postMessageToSlack(anyString(), anyString(), anyString());
    }
}
