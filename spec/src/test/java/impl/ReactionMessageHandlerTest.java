package impl;

import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.User;
import api.UserResource;
import api.auth.Auth;
import com.google.gson.JsonObject;
import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;
import rx.observers.AssertableSubscriber;
import slack.ReactionMessageHandler;
import slack.SlackResource;

import static org.assertj.core.api.Assertions.assertThat;

public class ReactionMessageHandlerTest {

    private static QuestionResource       questionResource;
    private static AnswerResource         answerResource;
    private static UserResource           userResource;
    private static TestSetup              testSetup;
    private static ReactionMessageHandler reactionMessageHandler;

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

        userResource = testSetup.getInjector().getInstance(UserResource.class);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        reactionMessageHandler = testSetup.getInjector().getInstance(ReactionMessageHandler.class);
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
    public void testVotesOnQuestion() {
        Question question = TestSetup.getQuestion("whatever", "whatever");
        //Given
        User user                = TestSetup.insertUser(userResource);

        long   currentTimeMillis = System.currentTimeMillis();
        String questionId        = String.valueOf(currentTimeMillis);

        question.setSlackId(questionId);

        questionResource.createQuestion(as(user), question).toBlocking().singleOrDefault(null);

        Question questionBySlackThreadId = questionResource.getQuestionBySlackThreadId(questionId).toBlocking().singleOrDefault(null);
        assertThat(questionBySlackThreadId.getVotes()).isEqualTo(0);

        //When
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "reaction_added");
        jsonObject.addProperty("reaction", "+1");
        JsonObject item = new JsonObject();
        item.addProperty("ts", questionId);
        jsonObject.add("item", item);

        //Then
        assertThat(reactionMessageHandler.shouldHandle("reaction_added", jsonObject)).isTrue();

        AssertableSubscriber<Void> test = reactionMessageHandler.handleMessage(jsonObject).test();
        test.awaitTerminalEvent();

        questionBySlackThreadId = questionResource.getQuestionBySlackThreadId(questionId).toBlocking().single();
        assertThat(questionBySlackThreadId.getVotes()).isEqualTo(1);


        //Remove reaction on question
        jsonObject.addProperty("type", "reaction_removed");
        assertThat(reactionMessageHandler.shouldHandle("reaction_removed", jsonObject)).isTrue();
        test = reactionMessageHandler.handleMessage(jsonObject).test();
        test.awaitTerminalEvent();

        questionBySlackThreadId = questionResource.getQuestionBySlackThreadId(questionId).toBlocking().single();
        assertThat(questionBySlackThreadId.getVotes()).isEqualTo(0);

        //remvove reaction on question already with votes 0
        jsonObject.addProperty("type", "reaction_added");
        jsonObject.addProperty("reaction", "-1");
        test = reactionMessageHandler.handleMessage(jsonObject).test();
        test.awaitTerminalEvent();

        questionBySlackThreadId = questionResource.getQuestionBySlackThreadId(questionId).toBlocking().single();
        assertThat(questionBySlackThreadId.getVotes()).isEqualTo(-1);
    }

    @Test
    public void testVotesOnAnswer() {
        Question question = TestSetup.getQuestion("whatever", "whatever");
        //Given
        User user                = TestSetup.insertUser(userResource);

        long   currentTimeMillis = System.currentTimeMillis();
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
        answer.setTitle("The title of the answer");
        answer.setSlackId(String.valueOf(currentTimeMillis+1));
        AssertableSubscriber<Answer> voidAssertableSubscriber = answerResource.answerQuestion(as(user), answer, questionBySlackThreadId.getId()).test().awaitTerminalEvent();
        voidAssertableSubscriber.assertNoErrors();
        Answer storedAnswer = answerResource.getAnswers(questionBySlackThreadId.getId()).toBlocking().first().get(0);
        assertThat(storedAnswer.getVotes()).isEqualTo(0);


        //When
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "reaction_added");
        jsonObject.addProperty("reaction", "+1");

        JsonObject item = new JsonObject();
        item.addProperty("ts", answer.getSlackId());
        jsonObject.add("item", item);

        //Then
        assertThat(reactionMessageHandler.shouldHandle("reaction_added", jsonObject)).isTrue();

        AssertableSubscriber<Void> test = reactionMessageHandler.handleMessage(jsonObject).test();
        test.awaitTerminalEvent();

        Answer upvotedAnswer = answerResource.getAnswerBySlackId(answer.getSlackId()).toBlocking().single();
        assertThat(upvotedAnswer.getVotes()).isEqualTo(1);

        //Remove reaction on answer
        jsonObject.addProperty("type", "reaction_removed");
        assertThat(reactionMessageHandler.shouldHandle("reaction_removed", jsonObject)).isTrue();
        test = reactionMessageHandler.handleMessage(jsonObject).test();
        test.awaitTerminalEvent();

        Answer downVotedAnswer = answerResource.getAnswerBySlackId(answer.getSlackId()).toBlocking().single();
        assertThat(downVotedAnswer.getVotes()).isEqualTo(0);


        jsonObject.addProperty("type", "reaction_added");
        jsonObject.addProperty("reaction", "-1");
        //remvove reaction on question already with votes 0
        test = reactionMessageHandler.handleMessage(jsonObject).test();
        test.awaitTerminalEvent();

        downVotedAnswer = answerResource.getAnswerBySlackId(answer.getSlackId()).toBlocking().single();
        assertThat(downVotedAnswer.getVotes()).isEqualTo(-1);
    }



    private Auth as(User user) {
        Auth auth = new Auth();
        auth.setUserId(user.getId());
        return auth;
    }

}
