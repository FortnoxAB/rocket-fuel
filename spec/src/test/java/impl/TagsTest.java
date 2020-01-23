package impl;

import api.Question;
import api.QuestionResource;
import api.Tag;
import api.TagResource;
import api.User;
import api.UserResource;
import dao.QuestionDao;
import dao.QuestionVoteDao;
import dao.TagDao;
import org.apache.log4j.Appender;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import rx.Observable;
import se.fortnox.reactivewizard.db.GeneratedKey;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;
import se.fortnox.reactivewizard.db.transactions.DaoTransactions;
import se.fortnox.reactivewizard.test.LoggingMockUtil;
import se.fortnox.reactivewizard.validation.ValidationFailedException;
import slack.SlackConfig;
import slack.SlackResource;

import java.util.Collections;
import java.util.List;

import static impl.TestSetup.insertUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TagsTest {
    private static QuestionResource questionResource;
    private static UserResource     userResource;
    private static TestDao          testDao;
    private static TagResource      tagResource;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup         testSetup;
    private static Appender          appender;
    private static ApplicationConfig applicationConfig;
    private        MockAuth          mockAuth;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        testDao = testSetup.getInjector().getInstance(TestDao.class);
        tagResource = testSetup.getInjector().getInstance(TagResource.class);

        applicationConfig = new ApplicationConfig();
        applicationConfig.setBaseUrl("duringtest.example.org");
    }

    @Before
    public void beforeEach() throws Exception {
        testSetup.setupDatabase();
        User createdUser = insertUser(userResource);
        mockAuth = new MockAuth(createdUser.getId());
        appender = LoggingMockUtil.createMockedLogAppender(QuestionResourceImpl.class);
    }

    @After
    public void afterEach() throws Exception {
        testSetup.clearDatabase();
        LoggingMockUtil.destroyMockedAppender(appender, QuestionResourceImpl.class);
    }

    @Test
    public void shouldBeAbleToCreateQuestionWithMissingTags() {
        // when a question is created with no tags
        Question question       = TestSetup.getQuestion("my question title", "my question");
        Question storedQuestion = questionResource.createQuestion(mockAuth, question).toBlocking().single();

        // then no tags should exist on the question
        assertThat(storedQuestion.getTags()).isEmpty();
    }

    @Test
    public void shouldBeAbleToCreateQuestionWithEmptyTags() {
        // when a question is created with empty tags
        Question question       = TestSetup.getQuestion("my question title", "my question", Collections.emptyList());
        Question storedQuestion = questionResource.createQuestion(mockAuth, question).toBlocking().single();

        // then no tags should exist on the question
        assertThat(storedQuestion.getTags()).isEmpty();
    }

    @Test
    public void shouldBeAbleToCreateQuestionWithTags() {
        // when a question is created with tags
        Question question       = TestSetup.getQuestion("my question title", "my question", List.of("tag-1", "tag_2", "tag3"));
        Question storedQuestion = questionResource.createQuestion(mockAuth, question).toBlocking().single();

        // then these should be returned
        assertThat(storedQuestion.getTags())
            .extracting(Tag::getLabel)
            .containsExactlyInAnyOrder("tag-1", "tag_2", "tag3");
    }

    @Test
    public void shouldBeAbleToLookupTags() {
        // given we have questions with some distinct tags
        Observable.just(
            TestSetup.getQuestion("my question title", "my question", List.of("alpha", "beta")),
            TestSetup.getQuestion("my other question title", "my other question", List.of("alpha", "alpaca", "charlie"))
        )
            .concatMap(question -> questionResource.createQuestion(mockAuth, question))
            .toList()
            .toBlocking()
            .single();

        // when a search is made for a partial tag from one of the questions
        List<Tag> foundTags = tagResource.queryTags("alp").toBlocking().single();

        // then
        assertThat(foundTags)
            .extracting(Tag::getLabel)
            .hasSize(2)
            .containsExactly("alpha", "alpaca");
    }

    @Test
    public void shouldRemoveUnusedTagsWhenSavingQuestionWithNewTags() {
        // given a question with tags exist
        Question question       = TestSetup.getQuestion("my question title", "my question", List.of("tag1", "tag2"));
        Question storedQuestion = questionResource.createQuestion(mockAuth, question).toBlocking().single();

        // when we update that question with new tags making "tag2" unreferenced
        Question editedQuestion  = TestSetup.getQuestion("my question title updated", "my question updated", List.of("tag1", "tag3"));
        Question updatedQuestion = questionResource.updateQuestion(mockAuth, storedQuestion.getId(), editedQuestion).single().toBlocking().single();

        // then the question should have the updated tags
        assertThat(updatedQuestion.getTags())
            .extracting(Tag::getLabel)
            .containsExactlyInAnyOrder("tag1", "tag3");

        // and there should be no unused tags
        Integer aggregatedTagMinimumUsage = testDao.aggregatedTagMinimumUsage().toBlocking().single();
        assertThat(aggregatedTagMinimumUsage).as("Unreferenced tag still exists").isEqualTo(1);
    }

    @Test
    public void shouldCreateUniqueTags() {
        Question question = TestSetup.getQuestion("title", "body", List.of("tag1", "tag2", "tag2"));
        questionResource.createQuestion(mockAuth, question).toBlocking().single();

        List<Tag> single = testDao.getAllTags().toList().toBlocking().single();
        assertThat(single)
            .extracting(Tag::getLabel)
            .containsExactlyInAnyOrder("tag1", "tag2");
    }

    @Test
    public void shouldBePossibleToAddQuestionWithPreexistingTagAndFetchIt() {
        // given we have an existing tag
        testDao.createTag("tag1").map(GeneratedKey::getKey).toBlocking().single();

        // when question is created
        Question question       = TestSetup.getQuestion("my question title", "my question", List.of("tag1", "tag2"));
        Question storedQuestion = questionResource.createQuestion(mockAuth, question).toBlocking().single();

        // then the question should have a correct set of tags
        assertThat(storedQuestion.getTags())
            .extracting(Tag::getLabel)
            .containsExactlyInAnyOrder("tag1", "tag2");
    }

    @Test
    public void shouldRemoveDanglingTagsWhenQuestionIsDeleted() {
        // Given a question with tags
        Question question = TestSetup.getQuestion("my question title", "my question", List.of("tag1", "tag2"));
        questionResource.createQuestion(mockAuth, question).toBlocking().single();
        List<Tag> tagsInStorage = testDao.getAllTags().toList().toBlocking().single();
        assertThat(tagsInStorage)
            .extracting(Tag::getLabel)
            .containsExactlyInAnyOrder("tag1", "tag2");

        // When the question is removed
        questionResource.deleteQuestion(mockAuth, question.getId()).toBlocking().singleOrDefault(null);

        // Then no tags should exist
        tagsInStorage = testDao.getAllTags().toList().toBlocking().single();
        assertThat(tagsInStorage)
            .hasSize(0);
    }

    @Test
    public void shouldRemoveAllTagsFromQuestion() {
        // Given a question with tags
        Question question       = TestSetup.getQuestion("my question title", "my question", List.of("tag1", "tag2"));
        Question storedQuestion = questionResource.createQuestion(mockAuth, question).toBlocking().single();

        // when tags are removed from the question
        question.setTags(Collections.emptyList());
        questionResource.updateQuestion(mockAuth, storedQuestion.getId(), question).toBlocking().single();

        // then no tags should exist on the question
        storedQuestion = questionResource.getQuestion(mockAuth, storedQuestion.getId()).toBlocking().single();
        assertThat(storedQuestion.getTags()).isEmpty();
    }

    @Test
    public void shouldKeepTagsOnQuestion() {
        // Given a question with tags
        Question question       = TestSetup.getQuestion("my question title", "my question", List.of("tag1", "tag2"));
        Question storedQuestion = questionResource.createQuestion(mockAuth, question).toBlocking().single();

        // when tags are missing
        question.setTags(null);
        questionResource.updateQuestion(mockAuth, storedQuestion.getId(), question).toBlocking().single();

        // then tags should remain
        storedQuestion = questionResource.getQuestion(mockAuth, storedQuestion.getId()).toBlocking().single();
        assertThat(storedQuestion.getTags())
            .extracting(Tag::getLabel)
            .containsExactlyInAnyOrder("tag1", "tag2");
    }

    @Test
    public void shouldStoreLowercaseTags() {
        // Given we are not validating request data
        QuestionDao      questionDao                   = testSetup.getInjector().getInstance(QuestionDao.class);
        QuestionVoteDao  questionVoteDao               = testSetup.getInjector().getInstance(QuestionVoteDao.class);
        SlackResource    slackResource                 = testSetup.getInjector().getInstance(SlackResource.class);
        SlackConfig      slackConfig                   = testSetup.getInjector().getInstance(SlackConfig.class);
        TagDao           tagDao                        = testSetup.getInjector().getInstance(TagDao.class);
        DaoTransactions  daoTransactions               = testSetup.getInjector().getInstance(DaoTransactions.class);
        QuestionResource nonValidatingQuestionResource = new QuestionResourceImpl(questionDao, questionVoteDao, slackResource, slackConfig, applicationConfig, tagDao, daoTransactions);

        // when a request is made to create a question with mixed case tags
        Question question       = TestSetup.getQuestion("my question title", "my question", List.of("Tag1", "tAG2"));
        Question storedQuestion = nonValidatingQuestionResource.createQuestion(mockAuth, question).toBlocking().single();

        // then tags should have been stored lowercased
        storedQuestion = questionResource.getQuestion(mockAuth, storedQuestion.getId()).toBlocking().single();
        assertThat(storedQuestion.getTags())
            .extracting(Tag::getLabel)
            .containsExactlyInAnyOrder("tag1", "tag2");
    }

    @Test
    public void shouldValidateTags() {
        Question question = TestSetup.getQuestion("my question title", "my question", List.of("tag 1", "tag-@", " tag3", "tag4 ", " tag5 "));
        assertThatExceptionOfType(ValidationFailedException.class)
            .isThrownBy(() -> questionResource.createQuestion(mockAuth, question).toBlocking().single())
            .satisfies(e -> {
                assertThat(e.getFields())
                    .allSatisfy(fieldError -> {
                        assertThat(fieldError.getError()).isEqualTo("validation.pattern");
                        assertThat(fieldError.getField()).matches("tags\\[\\d+\\]\\.label");
                    });
            });
    }

    private interface TestDao {
        @Update("INSERT INTO tag (label) VALUES (:label) RETURNING id")
        Observable<GeneratedKey<Long>> createTag(String label);

        @Query("SELECT min(usages) from tag_usage")
        Observable<Integer> aggregatedTagMinimumUsage();

        @Query("SELECT id, label FROM tag")
        Observable<Tag> getAllTags();
    }
}
