package impl;

import api.Question;
import api.QuestionResource;
import api.User;
import api.UserResource;
import api.auth.Auth;
import dao.TagDao;
import org.apache.log4j.Appender;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import rx.Observable;
import se.fortnox.reactivewizard.CollectionOptions;
import se.fortnox.reactivewizard.db.GeneratedKey;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;
import se.fortnox.reactivewizard.test.LoggingMockUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static impl.TestSetup.insertUser;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TagsTest {
    public static final LocalDateTime CURRENT = now();
    public static final LocalDateTime A_DAY_AGO = CURRENT.minusDays(1);
    public static final LocalDateTime A_MONTH_AGO = CURRENT.minusMonths(1);

    private static QuestionResource questionResource;
    private static UserResource userResource;
    private static TestDao testDao;
    private static TagDao tagDao;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup testSetup;
    private static Appender appender;
    private static ApplicationConfig applicationConfig;

    private CollectionOptions options;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        testDao = testSetup.getInjector().getInstance(TestDao.class);
        tagDao = testSetup.getInjector().getInstance(TagDao.class);
        applicationConfig = new ApplicationConfig();
        applicationConfig.setBaseUrl("deployed.fuel.com");
    }

    @Before
    public void beforeEach() throws Exception {
        options = new CollectionOptions();
        testSetup.setupDatabase();
        appender = LoggingMockUtil.createMockedLogAppender(QuestionResourceImpl.class);
    }

    @After
    public void afterEach() throws Exception {
        testSetup.clearDatabase();
        LoggingMockUtil.destroyMockedAppender(appender, QuestionResourceImpl.class);
    }

    @Test
    public void shouldRemoveUnusedTagsWhenSavingQuestionWithNewTags() {
        User createdUser = insertUser(userResource);
        Auth mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());

        // given a question with tags exist
        Question question = TestSetup.getQuestion("my question title", "my question", Set.of("tag1", "tag2"));
        Question storedQuestion = questionResource.createQuestion(mockAuth, question).test().awaitTerminalEvent().assertNoErrors().getOnNextEvents().get(0);

        // when we update that question with new tags making "tag2" unreferenced
        Question editedQuestion = TestSetup.getQuestion("my question title updated", "my question updated", Set.of("tag1", "tag3"));
        Question updatedQuestion = questionResource.updateQuestion(mockAuth, storedQuestion.getId(), editedQuestion).single().test().awaitTerminalEvent().assertNoErrors().getOnNextEvents().get(0);

        // then the question should have the updated tags
        assertThat(updatedQuestion.getTags()).containsExactlyInAnyOrder("tag1", "tag3");

        // and there should be no unused tags
        Integer aggregatedTagMinimumUsage = testDao.aggregatedTagMinimumUsage().toBlocking().single();
        assertThat(aggregatedTagMinimumUsage).as("Unreferenced tag still exists").isEqualTo(1);
        assertNotNull(updatedQuestion.getId());
    }

    @Test
    public void shouldBePossibleToAddQuestionWithPreexistingTagAndFetchIt() {
        // Given
        User createdUser = insertUser(userResource);
        Long tagId = testDao.createTag("tag1").map(GeneratedKey::getKey).test().awaitTerminalEvent().assertNoErrors().getOnNextEvents().get(0);
        assertThat(tagId).isGreaterThan(0);

        // when question is created
        Question question = TestSetup.getQuestion("my question title", "my question", Set.of("tag1", "tag2"));
        Auth mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());

        // when we create the question
        questionResource.createQuestion(mockAuth, question).test().awaitTerminalEvent().assertNoErrors();

        // then the question should be returned when asking for the users questions
        List<Question> questions = questionResource.getQuestions(createdUser.getId(), null)
            .test()
            .awaitTerminalEvent()
            .assertNoErrors()
            .getOnNextEvents()
            .get(0);
        assertEquals(1, questions.size());
        Question insertedQuestion = questions.get(0);
        assertEquals("my question title", insertedQuestion.getTitle());
        assertEquals("my question", insertedQuestion.getQuestion());
        assertEquals(question.getBounty(), insertedQuestion.getBounty());
        assertEquals(createdUser.getId(), insertedQuestion.getUserId());
        assertThat(insertedQuestion.getTags()).containsExactlyInAnyOrder("tag1", "tag2");
        assertNotNull(insertedQuestion.getId());
    }

    private interface TestDao {
        @Update("UPDATE question " +
            "SET created_at=:createdAt " +
            "WHERE question.id=:question.id")
        Observable<Integer> setCreatedAt(Question question, LocalDateTime createdAt);

        @Update("INSERT INTO tag (label) VALUES (:label) RETURNING id")
        Observable<GeneratedKey<Long>> createTag(String label);

        @Query("SELECT min(usages) from tag_usage")
        Observable<Integer> aggregatedTagMinimumUsage();
    }
}
