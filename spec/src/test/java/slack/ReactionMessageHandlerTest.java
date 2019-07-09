package slack;

import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.User;
import api.UserResource;
import api.auth.Auth;
import com.github.seratch.jslack.api.model.Message;
import com.google.gson.JsonObject;
import impl.TestSetup;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import rx.observers.AssertableSubscriber;

import static impl.TestSetup.insertUser;
import static java.lang.System.currentTimeMillis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static rx.Observable.just;
import static slack.ReactionMessageHandler.CHANNEL;
import static slack.ReactionMessageHandler.ITEM;
import static slack.ReactionMessageHandler.MESSAGE;
import static slack.ReactionMessageHandler.REACTION;
import static slack.ReactionMessageHandler.REACTION_ADDED;
import static slack.ReactionMessageHandler.REACTION_REMOVED;
import static slack.ReactionMessageHandler.THREAD;
import static slack.ReactionMessageHandler.TYPE;

public class ReactionMessageHandlerTest {

    private static QuestionResource       questionResource;
    private static AnswerResource         answerResource;
    private static UserResource           userResource;
    private static TestSetup              testSetup;
    private static ReactionMessageHandler reactionMessageHandler;
    private static SlackResource          slackResource;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);

        userResource = testSetup.getInjector().getInstance(UserResource.class);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        reactionMessageHandler = testSetup.getInjector().getInstance(ReactionMessageHandler.class);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        answerResource = testSetup.getInjector().getInstance(AnswerResource.class);
        slackResource = testSetup.getInjector().getInstance(SlackResource.class);
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
    public void testVotesOnQuestion() {
        Question question = TestSetup.getQuestion("whatever", "whatever");
        //Given
        User user = insertUser(userResource);

        long   currentTimeMillis = currentTimeMillis();
        String questionId        = String.valueOf(currentTimeMillis);

        question.setSlackId(questionId);

        questionResource.createQuestion(as(user), question).toBlocking().singleOrDefault(null);

        Question questionBySlackThreadId = questionResource.getQuestionBySlackThreadId(questionId).toBlocking().singleOrDefault(null);
        assertThat(questionBySlackThreadId.getVotes()).isEqualTo(0);

        String channel = "someChannel";
        mockSlackUserAndMessage(user, question.getSlackId(), channel);

        //When
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TYPE, "reaction_added");
        jsonObject.addProperty(REACTION, "+1");
        JsonObject item = new JsonObject();
        item.addProperty(THREAD, questionId);
        item.addProperty(TYPE, MESSAGE);
        item.addProperty(CHANNEL, channel);
        jsonObject.add(ITEM, item);

        //Then
        assertThat(reactionMessageHandler.shouldHandle("reaction_added", jsonObject)).isTrue();

        AssertableSubscriber<Void> test = reactionMessageHandler.handleMessage(jsonObject).test();
        test.awaitTerminalEvent().assertNoErrors();

        questionBySlackThreadId = questionResource.getQuestionBySlackThreadId(questionId).toBlocking().single();
        assertThat(questionBySlackThreadId.getVotes()).isEqualTo(1);

        //Remove reaction on question
        jsonObject.addProperty("type", "reaction_removed");
        assertThat(reactionMessageHandler.shouldHandle("reaction_removed", jsonObject)).isTrue();
        test = reactionMessageHandler.handleMessage(jsonObject).test();
        test.awaitTerminalEvent().assertNoErrors();

        questionBySlackThreadId = questionResource.getQuestionBySlackThreadId(questionId).toBlocking().single();
        assertThat(questionBySlackThreadId.getVotes()).isEqualTo(0);

        //remvove reaction on question already with votes 0
        jsonObject.addProperty("type", "reaction_added");
        jsonObject.addProperty("reaction", "-1");
        test = reactionMessageHandler.handleMessage(jsonObject).test();
        test.awaitTerminalEvent().assertNoErrors();

        questionBySlackThreadId = questionResource.getQuestionBySlackThreadId(questionId).toBlocking().single();
        assertThat(questionBySlackThreadId.getVotes()).isEqualTo(-1);
    }

    @Test
    public void testVotesOnAnswer() {
        Question question = TestSetup.getQuestion("whatever", "whatever");
        //Given
        User user = insertUser(userResource);

        long   currentTimeMillis = currentTimeMillis();
        String questionId        = String.valueOf(currentTimeMillis);

        question.setSlackId(questionId);

        //Create and assert question in db
        questionResource.createQuestion(as(user), question).toBlocking().singleOrDefault(null);
        Question questionBySlackThreadId = questionResource.getQuestionBySlackThreadId(questionId).toBlocking().singleOrDefault(null);
        assertThat(questionBySlackThreadId.getVotes()).isEqualTo(0);

        //Create and get answer
        Answer answer = new Answer();
        answer.setAnswer("test");
        answer.setUserId(user.getId());
        answer.setSlackId(String.valueOf(currentTimeMillis + 1));
        answer.setSlackId(String.valueOf(currentTimeMillis + 1));
        AssertableSubscriber<Answer> voidAssertableSubscriber = answerResource.answerQuestion(as(user), answer, questionBySlackThreadId.getId()).test().awaitTerminalEvent();
        voidAssertableSubscriber.assertNoErrors();
        Answer storedAnswer = answerResource.getAnswers(as(user), questionBySlackThreadId.getId()).toBlocking().first().get(0);
        assertThat(storedAnswer.getVotes()).isEqualTo(0);

        String channel = "someChannel";
        mockSlackUserAndMessage(insertUser(userResource), answer.getSlackId(), channel);

        //When
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(TYPE, REACTION_ADDED);
        jsonObject.addProperty(REACTION, "+1");

        JsonObject item = new JsonObject();
        item.addProperty(TYPE, MESSAGE);
        item.addProperty(CHANNEL, channel);
        item.addProperty(THREAD, answer.getSlackId());
        jsonObject.add(ITEM, item);

        //Then
        assertThat(reactionMessageHandler.shouldHandle(REACTION_ADDED, jsonObject)).isTrue();

        AssertableSubscriber<Void> test = reactionMessageHandler.handleMessage(jsonObject).test();
        test.awaitTerminalEvent().assertNoErrors();

        Answer upvotedAnswer = answerResource.getAnswerBySlackId(answer.getSlackId()).toBlocking().single();
        assertThat(upvotedAnswer.getVotes()).isEqualTo(1);

        //Remove reaction on answer
        jsonObject.addProperty("type", REACTION_REMOVED);
        assertThat(reactionMessageHandler.shouldHandle(REACTION_REMOVED, jsonObject)).isTrue();
        test = reactionMessageHandler.handleMessage(jsonObject).test();
        test.awaitTerminalEvent().assertNoErrors();

        Answer downVotedAnswer = answerResource.getAnswerBySlackId(answer.getSlackId()).toBlocking().single();
        assertThat(downVotedAnswer.getVotes()).isEqualTo(0);

        jsonObject.addProperty("type", "reaction_added");
        jsonObject.addProperty("reaction", "-1");
        //remvove reaction on question already with votes 0
        test = reactionMessageHandler.handleMessage(jsonObject).test();
        test.awaitTerminalEvent().assertNoErrors();

        downVotedAnswer = answerResource.getAnswerBySlackId(answer.getSlackId()).toBlocking().single();
        assertThat(downVotedAnswer.getVotes()).isEqualTo(-1);
    }

    @Test
    public void shouldHandleShouldBeNullSafe() {

        assertThat(reactionMessageHandler.shouldHandle(null, null)).isFalse();
        assertThat(reactionMessageHandler.shouldHandle(REACTION_REMOVED, null)).isFalse();

        JsonObject jsonObject = new JsonObject();

        assertThat(reactionMessageHandler.shouldHandle(REACTION_REMOVED, jsonObject)).isFalse();

        jsonObject.add(ITEM, new JsonObject());
        assertThat(reactionMessageHandler.shouldHandle(REACTION_REMOVED, jsonObject)).isFalse();
    }

    private Auth as(User user) {
        Auth auth = new Auth();
        auth.setUserId(user.getId());
        return auth;
    }

    private void mockSlackUserAndMessage(User user, String slackId, String channel) {

        String slackUser = "someUser";

        when(slackResource.getUser(any())).thenReturn(just(user));

        when(slackResource.getMessageFromSlack(channel, slackId))
            .thenAnswer(i -> {
                Message message = new Message();
                message.setUser(slackUser);
                return just(message);
            });
    }
}
