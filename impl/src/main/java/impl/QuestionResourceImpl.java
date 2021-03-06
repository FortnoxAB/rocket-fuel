package impl;

import api.Question;
import api.QuestionResource;
import api.Tag;
import api.auth.Auth;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.QuestionDao;
import dao.QuestionVote;
import dao.QuestionVoteDao;
import dao.TagDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;
import se.fortnox.reactivewizard.CollectionOptions;
import se.fortnox.reactivewizard.db.transactions.DaoTransactions;
import se.fortnox.reactivewizard.jaxrs.WebException;
import slack.SlackConfig;
import slack.SlackResource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static rx.Observable.empty;
import static rx.Observable.error;
import static rx.Observable.just;
import static se.fortnox.reactivewizard.util.rx.RxUtils.exception;

@Singleton
public class QuestionResourceImpl implements QuestionResource {

    private static final Logger LOG = LoggerFactory.getLogger(QuestionResourceImpl.class);
    public static final  String FAILED_TO_SEARCH_FOR_QUESTIONS             = "failed.to.search.for.questions";
    public static final  String QUESTION_NOT_FOUND                         = "question.not.found";
    public static final  String FAILED_TO_GET_QUESTIONS_FROM_DATABASE      = "failed.to.get.questions.from.database";
    public static final  String FAILED_TO_GET_QUESTION_FROM_DATABASE       = "failed.to.get.question.from.database";
    public static final  String FAILED_TO_UPDATE_QUESTION_TO_DATABASE      = "failed.to.update.question.to.database";
    public static final  String NOT_OWNER_OF_QUESTION                      = "not.owner.of.question";
    public static final  String FAILED_TO_DELETE_QUESTION                  = "failed.to.delete.question";
    public static final  String INVALID_VOTE                               = "invalid.vote";
    public static final  String FAILED_TO_GET_LATEST_QUESTIONS             = "failed.to.get.latest.questions";
    public static final  String FAILED_TO_GET_POPULAR_QUESTIONS            = "failed.to.get.popular.questions";
    public static final  String FAILED_TO_GET_POPULAR_UNANSWERED_QUESTIONS = "failed.to.get.popular.unanswered.questions";
    public static final  String FAILED_TO_GET_RECENTLY_ACCEPTED_QUESTIONS  = "failed.to.get.recently.accepted.questions";
    public static final  String FAILED_TO_ADD_QUESTION_TO_DATABASE         = "failed.to.add.question.to.database";
    public static final  String FAILED_TO_UPDATE_TAGS_ON_QUESTION          = "failed.to.update.tags.on.question";

    private final QuestionDao       questionDao;
    private final QuestionVoteDao   questionVoteDao;
    private final SlackResource     slackResource;
    private final SlackConfig       slackConfig;
    private final ApplicationConfig applicationConfig;
    private final TagDao            tagDao;
    private final DaoTransactions daoTransactions;

    @Inject
    public QuestionResourceImpl(QuestionDao questionDao, QuestionVoteDao questionVoteDao,
                                SlackResource slackResource, SlackConfig slackConfig, ApplicationConfig applicationConfig,
                                TagDao tagDao, DaoTransactions daoTransactions) {
        this.questionDao = questionDao;
        this.questionVoteDao = questionVoteDao;
        this.slackResource = slackResource;
        this.slackConfig = slackConfig;
        this.applicationConfig = applicationConfig;
        this.tagDao = tagDao;
        this.daoTransactions = daoTransactions;
    }

    @Override
    public Observable<Question> getQuestionBySlackThreadId(String slackThreadId) {
        return this.questionDao.getQuestionBySlackThreadId(slackThreadId).switchIfEmpty(
            exception(() -> new WebException(NOT_FOUND, QUESTION_NOT_FOUND)));
    }

    @Override
    public Observable<Question> getQuestionById(long questionId) {
        return this.questionDao.getQuestion(questionId).switchIfEmpty(
            exception(() -> new WebException(NOT_FOUND, QUESTION_NOT_FOUND)));
    }

    @Override
    public Observable<Question> getQuestion(Auth auth, long questionId) {
        return this.questionDao
            .getQuestion(auth.getUserId(), questionId)
            .onErrorResumeNext(throwable ->
                error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE, throwable)))
            .switchIfEmpty(exception(() -> new WebException(NOT_FOUND, QUESTION_NOT_FOUND)));
    }

    @Override
    public Observable<List<Question>> getLatestQuestions(CollectionOptions options) {
        return handleError(questionDao.getLatestQuestions(options), FAILED_TO_GET_LATEST_QUESTIONS);
    }

    @Override
    public Observable<List<Question>> getPopularQuestions(CollectionOptions options) {
        return handleError(questionDao.getPopularQuestions(options), FAILED_TO_GET_POPULAR_QUESTIONS);
    }

    @Override
    public Observable<List<Question>> getPopularUnansweredQuestions(CollectionOptions options) {
        return handleError(questionDao.getPopularUnansweredQuestions(options), FAILED_TO_GET_POPULAR_UNANSWERED_QUESTIONS);
    }

    @Override
    public Observable<List<Question>> getRecentlyAcceptedQuestions(CollectionOptions options) {
        return handleError(questionDao.getRecentlyAcceptedQuestions(options), FAILED_TO_GET_RECENTLY_ACCEPTED_QUESTIONS);
    }

    @Override
    public Observable<Question> createQuestion(Auth auth, Question question) {
        return this.questionDao
            .addQuestion(auth.getUserId(), question)
            .map(longGeneratedKey -> {
                question.setId(longGeneratedKey.getKey());
                return question;
            })
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_ADD_QUESTION_TO_DATABASE, throwable)))
            .concatMap(savedQuestion -> {
                return slackResource.postMessageToSlack(slackConfig.getFeedChannel(), notificationMessage(question))
                    .onErrorResumeNext(e -> {
                        LOG.error("failed to notify by slack that question has been added", e);
                        return empty();
                    })
                    .cast(Question.class)
                    .switchIfEmpty(just(savedQuestion));
            })
            .concatMap(savedQuestion -> {
                return associateTagsWithCreatedQuestion(savedQuestion, question.getTags())
                    .cast(Question.class)
                    .switchIfEmpty(
                        questionDao.getQuestion(savedQuestion.getId())
                            .onErrorResumeNext(throwable ->
                                error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE, throwable)))
                    );

            });
    }

    private Observable<Void> associateTagsWithCreatedQuestion(Question question, List<Tag> tags) {
        if (tags == null) {
            return Observable.empty();
        }
        List<String> lowerCasedTags = tags
            .stream()
            .map(tag -> tag.getLabel().toLowerCase())
            .collect(Collectors.toList());
        List<Observable<Integer>> daoCalls = lowerCasedTags
            .stream()
            .map(tagDao::mergeTag)
            .collect(Collectors.toList());
        daoCalls.add(tagDao.associateTagsWithQuestion(question.getId(), lowerCasedTags));
        daoCalls.add(tagDao.deleteUnusedTags());
        return daoTransactions
            .executeTransaction(daoCalls)
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_UPDATE_TAGS_ON_QUESTION, throwable)));
    }

    private List<LayoutBlock> notificationMessage(Question question) {
        return asList(SectionBlock.builder()
                .text(markdownText("A new question: *%s* was submitted.", question.getTitle()))
                .build(),
            SectionBlock.builder()
                .text(markdownText("Head over to %s to view the question.", questionUrl(question.getId())))
                .build());
    }

    private String questionUrl(long questionId) {
        return "<" + applicationConfig.getBaseUrl() + "/question/" + questionId + "|rocket-fuel>";
    }

    private static MarkdownTextObject markdownText(String string, String... args) {
        return MarkdownTextObject.builder()
            .text(String.format(string, args))
            .build();
    }

    @Override
    public Observable<List<Question>> getQuestionsBySearchQuery(String searchQuery, CollectionOptions options) {
        if (isNullOrEmpty(searchQuery)) {
            return just(emptyList());
        }
        QuestionSearchOptions questionSearchOptions = QuestionSearchOptions.from(searchQuery);
        return questionDao.getQuestions(questionSearchOptions, options)
            .onErrorResumeNext(e -> {
                LOG.error("failed to search for questions with search query: [{}]", searchQuery);
                return error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_SEARCH_FOR_QUESTIONS, e));
            }).toList();
    }

    @Override
    public Observable<List<Question>> getQuestions(long userId, CollectionOptions options) {
        return handleError(questionDao.getQuestions(userId, options), FAILED_TO_GET_QUESTIONS_FROM_DATABASE);
    }

    @Override
    public Observable<Question> updateQuestion(Auth auth, long questionId, Question question) {
        return questionDao.getQuestion(questionId)
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE, throwable)))
            .switchIfEmpty(exception(() -> new WebException(NOT_FOUND, QUESTION_NOT_FOUND)))
            .concatMap(storedQuestion -> {
                if (auth.getUserId() != storedQuestion.getUserId()) {
                    return error(new WebException(FORBIDDEN, NOT_OWNER_OF_QUESTION));
                }
                List<Observable<Integer>> daoCalls = new ArrayList<>();
                daoCalls.add(questionDao.updateQuestion(auth.getUserId(), questionId, question));
                if(question.getTags() != null) { // Null means we shouldn't touch existing tags
                    List<String> labels = question
                        .getTags()
                        .stream()
                        .map(tag -> tag.getLabel().toLowerCase())
                        .collect(Collectors.toList());
                    for (Tag tag : question.getTags()) {
                        daoCalls.add(tagDao.mergeTag(tag.getLabel()));
                    }
                    daoCalls.add(tagDao.removeTagAssociationFromQuestion(storedQuestion.getId()));
                    daoCalls.add(tagDao.associateTagsWithQuestion(storedQuestion.getId(), labels));
                    daoCalls.add(tagDao.deleteUnusedTags());
                }

                return daoTransactions
                    .executeTransaction(daoCalls)
                    .ignoreElements()
                    .cast(Question.class)
                    .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_UPDATE_QUESTION_TO_DATABASE, throwable)))
                    .concatWith(
                        questionDao.getQuestion(storedQuestion.getId())
                            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE, throwable)))
                            .switchIfEmpty(exception(() -> new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE)))
                    )
                    .last();
            });
    }

    @Override
    public Observable<Void> deleteQuestion(Auth auth, long questionId) {
        return questionDao.getQuestion(questionId)
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE, throwable)))
            .switchIfEmpty(exception(() -> new WebException(NOT_FOUND, QUESTION_NOT_FOUND)))
            .concatMap(storedQuestion -> {
                if (auth.getUserId() != storedQuestion.getUserId()) {
                    return error(new WebException(FORBIDDEN, NOT_OWNER_OF_QUESTION));
                }
                List<Observable<Integer>> daoCalls = asList(
                    questionDao.deleteQuestion(auth.getUserId(), questionId),
                    tagDao.deleteUnusedTags()
                );
                return daoTransactions
                    .executeTransaction(daoCalls)
                    .ignoreElements()
                    .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_DELETE_QUESTION, throwable)));
            });
    }

    @Override
    public Observable<Void> upVoteQuestion(Auth auth, long questionId) {
        return handleVote(new QuestionVote(auth.getUserId(), questionId, 1));
    }

    @Override
    public Observable<Void> downVoteQuestion(Auth auth, long questionId) {
        return handleVote(new QuestionVote(auth.getUserId(), questionId, -1));
    }

    private Observable<Void> handleVote(QuestionVote newVote) {
        return questionDao.getQuestion(newVote.getQuestionId())
            .flatMap(validateQuestionAndGetExistingVote(newVote))
            .flatMap(validateVoteAndRemoveIfZero(newVote))
            .switchIfEmpty(questionVoteDao.createVote(newVote).map(i -> newVote))
            .ignoreElements()
            .cast(Void.class);
    }

    private Func1<Question, Observable<QuestionVote>> validateQuestionAndGetExistingVote(QuestionVote newVote) {
        return question -> {
            if (question.getUserId() == newVote.getUserId()) { // no voting for your own question
                return error(new WebException(BAD_REQUEST, INVALID_VOTE));
            }
            return questionVoteDao.findVote(newVote.getUserId(), newVote.getQuestionId());
        };
    }

    private Func1<QuestionVote, Observable<QuestionVote>> validateVoteAndRemoveIfZero(QuestionVote newVote) {
        return existingVote -> {
            int totalVote = newVote.getValue() + existingVote.getValue();
            if (totalVote == 0) {
                return questionVoteDao.deleteVote(existingVote.getUserId(), existingVote.getQuestionId())
                    .map(i -> existingVote);
            }
            return error(new WebException(BAD_REQUEST, INVALID_VOTE));
        };
    }

    private static Observable<List<Question>> handleError(Observable<Question> questions, String errorCode) {
        return questions.toList()
            .onErrorResumeNext(e ->
                error(new WebException(INTERNAL_SERVER_ERROR, errorCode, e))
            );
    }
}
