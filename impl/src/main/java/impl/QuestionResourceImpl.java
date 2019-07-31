package impl;

import api.Question;
import api.QuestionResource;
import api.auth.Auth;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.QuestionDao;
import dao.QuestionVote;
import dao.QuestionVoteDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static java.util.Collections.emptyList;
import static rx.Observable.error;
import static rx.Observable.just;
import static se.fortnox.reactivewizard.util.rx.RxUtils.exception;

@Singleton
public class QuestionResourceImpl implements QuestionResource {

    private static final Logger LOG = LoggerFactory.getLogger(QuestionResourceImpl.class);
    public static final String FAILED_TO_SEARCH_FOR_QUESTIONS = "failed.to.search.for.questions";
    public static final String QUESTION_NOT_FOUND                    = "question.not.found";
    public static final String FAILED_TO_GET_QUESTIONS_FROM_DATABASE = "failed.to.get.questions.from.database";
    public static final String FAILED_TO_GET_QUESTION_FROM_DATABASE  = "failed.to.get.question.from.database";
    public static final String FAILED_TO_UPDATE_QUESTION_TO_DATABASE = "failed.to.update.question.to.database";
    public static final String NOT_OWNER_OF_QUESTION                 = "not.owner.of.question";
    public static final String FAILED_TO_DELETE_QUESTION             = "failed.to.delete.question";
    public static final        String INVALID_VOTE                          = "invalid.vote";


    private final QuestionDao     questionDao;
    private final QuestionVoteDao questionVoteDao;

    @Inject
    public QuestionResourceImpl(QuestionDao questionDao, QuestionVoteDao questionVoteDao) {
        this.questionDao = questionDao;
        this.questionVoteDao = questionVoteDao;
    }

    @Override
    public Observable<Question> getQuestionBySlackThreadId(String slackThreadId) {
        return this.questionDao.getQuestionBySlackThreadId(slackThreadId).switchIfEmpty(
            exception(() -> new WebException(HttpResponseStatus.NOT_FOUND, "not.found")));
    }

	@Override
	public Observable<Question> getQuestionById(long questionId) {
      return this.questionDao.getQuestion(questionId).switchIfEmpty(
        exception(() -> new WebException(HttpResponseStatus.NOT_FOUND, "not.found")));
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
    public Observable<List<Question>> getLatestQuestion(Integer limit) {
        if (limit == null) {
            limit = 10;
        }
        return this.questionDao.getLatestQuestions(limit).toList().onErrorResumeNext(e ->
            error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed.to.get.latest.questions", e))
        );
    }

    @Override
    public Observable<Question> createQuestion(Auth auth, Question question) {
        return this.questionDao
          .addQuestion(auth.getUserId(), question)
            .map(longGeneratedKey -> {
                question.setId(longGeneratedKey.getKey());
                return question;
            })
          .onErrorResumeNext(throwable -> error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed.to.add.question.to.database", throwable)));
    }

    @Override
    public Observable<List<Question>> getQuestionsBySearchQuery(String searchQuery, Integer limit) {
        if (limit == null) {
            limit = 50;
        }
        if (isNullOrEmpty(searchQuery)) {
            return just(emptyList());
        }
        return questionDao.getQuestions(searchQuery, limit)
            .onErrorResumeNext(e -> {
                LOG.error("failed to search for questions with search query: [" + searchQuery + "]");
                return error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, FAILED_TO_SEARCH_FOR_QUESTIONS ,e));
            }).toList();
    }

    @Override
    public Observable<List<Question>> getQuestions(long userId) {
        return this.questionDao
            .getQuestions(userId).toList()
            .onErrorResumeNext(throwable ->
                error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTIONS_FROM_DATABASE, throwable)));
    }

    @Override
    public Observable<Question> updateQuestion(Auth auth, long questionId, Question question) {
        return questionDao.getQuestion(questionId)
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE, throwable)))
            .switchIfEmpty(exception(() -> new WebException(NOT_FOUND, QUESTION_NOT_FOUND)))
            .flatMap(storedQuestion -> {
                if (auth.getUserId() != storedQuestion.getUserId()) {
                    return error(new WebException(FORBIDDEN, NOT_OWNER_OF_QUESTION));
                }
                return questionDao.updateQuestion(auth.getUserId(), questionId, question)
                    .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_UPDATE_QUESTION_TO_DATABASE, throwable)))
                    .flatMap(ignore -> questionDao.getQuestion(questionId)
                        .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE, throwable))))
                    .switchIfEmpty(exception(() -> new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE)));
            });
    }

    @Override
    public Observable<Void> deleteQuestion(Auth auth, long questionId) {
        return questionDao.getQuestion(questionId)
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE, throwable)))
            .switchIfEmpty(exception(() -> new WebException(NOT_FOUND, QUESTION_NOT_FOUND)))
            .flatMap(storedAnswer -> {
                if (auth.getUserId() != storedAnswer.getUserId()) {
                    return error(new WebException(FORBIDDEN, NOT_OWNER_OF_QUESTION));
                }
                return questionDao.deleteQuestion(auth.getUserId(), questionId)
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


}
