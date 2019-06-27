package dao;


import api.Answer;
import rx.Observable;
import se.fortnox.reactivewizard.db.GeneratedKey;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;

public interface AnswerDao {

    String SELECT_ANSWER = "SELECT " +
                                "a.id, " +
                                "a.user_id, " +
                                "a.answer, " +
                                "a.created_at, " +
                                "a.accepted, " +
                                "a.votes, " +
                                "u.picture, " +
                                "a.slack_id, " +
                                "a.question_id, ";

    String FROM_ANSWER = "FROM answer a " +
                            "INNER JOIN \"user\" u on u.id = a.user_id ";

    @Update(
        "UPDATE answer " +
        "SET accepted=true " +
        "WHERE id=:answerId"
    )
    Observable<Integer> markAsAccepted(long answerId);

    @Query(
        SELECT_ANSWER +
            "u.name AS created_by " +
        FROM_ANSWER +
        "WHERE user_id=:userId AND question_id=:questionId"
    )
    Observable<Answer> getAnswers(long userId, long questionId);

    @Query(
        SELECT_ANSWER +
            "u.name AS created_by " +
        FROM_ANSWER +
        "WHERE question_id=:questionId " +
        "ORDER BY a.accepted desc, a.votes desc, a.created_at desc"
    )
    Observable<Answer> getAnswers(long questionId);


    @Query(
        SELECT_ANSWER +
            "u.name AS created_by " +
        FROM_ANSWER +
        "WHERE slack_id=:slackId"
    )
    Observable<Answer> getAnswer(String slackId);

    @Update(
        "INSERT INTO answer " +
            "(" +
                "answer, " +
                "votes, " +
                "created_at, " +
                "accepted, " +
                "question_id, " +
                "user_id, " +
                "slack_id" +
            ")" +
            "VALUES(" +
                ":answer.answer, " +
                "0, " +
                "NOW(), " +
                "false, " +
                ":questionId, " +
                ":userId, " +
                ":answer.slackId" +
            ")")
    Observable<GeneratedKey<Long>> createAnswer(long userId, long questionId, Answer answer);


    @Update(
        "UPDATE answer SET " +
            "answer=:answer.answer " +
        "WHERE " +
            "answer.id=:answerId " +
            "AND answer.user_id=:userId")
    Observable<Void> updateAnswer(long userId, long answerId, Answer answer);

    @Update(
        "UPDATE answer SET " +
            "votes=votes+1 " +
        "WHERE " +
            "slack_id = :threadId")
    Observable<Void> upVoteAnswer(String threadId);

    @Update(
        "UPDATE answer " +
        "SET votes=votes-1 " +
        "WHERE " +
            "slack_id = :threadId")
    Observable<Void> downVoteAnswer(String threadId);

    @Query(
        SELECT_ANSWER +
            "u.name AS created_by, " +
            "q.user_id AS \"question.user_id\" " +
        FROM_ANSWER +
        "INNER JOIN question q on q.id = a.question_id " +
        "WHERE a.id=:id")
    Observable<AnswerInternal> getAnswerById(long id);

    @Update(
        "DELETE FROM " +
            "answer " +
        "WHERE " +
            "answer.user_id = :userId AND answer.id = :answerId")
    Observable<Void> deleteAnswer(long userId, long answerId);
}
