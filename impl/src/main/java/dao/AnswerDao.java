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
                                "a.title," +
                                "a.votes, " +
                                "a.slack_id, " +
                                "a.question_id, ";

    @Update(
        "UPDATE answer " +
        "SET accepted=true " +
        "WHERE id=:answerId"
    )
    Observable<Integer> markAsAccepted(long answerId);

    @Query(
        SELECT_ANSWER +
            "\"user\".\"name\" AS created_by " +
        "FROM answer a " +
        "INNER JOIN \"user\" on \"user\".id = a.user_id " +
        "WHERE user_id=:userId AND question_id=:questionId"
    )
    Observable<Answer> getAnswers(long userId, long questionId);

    @Query(
        SELECT_ANSWER +
            "\"user\".\"name\" AS created_by " +
        "FROM answer a " +
        "INNER JOIN \"user\" on \"user\".id = a.user_id " +
        "WHERE question_id=:questionId " +
        "ORDER BY a.accepted desc, a.votes desc, a.created_at desc"
    )
    Observable<Answer> getAnswers(long questionId);


    @Query(
        SELECT_ANSWER +
            "\"user\".\"name\" AS created_by " +
        "FROM answer a " +
        "INNER JOIN \"user\" on \"user\".id = a.user_id " +
        "WHERE slack_id=:slackId"
    )
    Observable<Answer> getAnswer(String slackId);

    @Update(
        "INSERT INTO answer " +
            "(" +
                "answer, " +
                "title, " +
                "votes, " +
                "created_at, " +
                "accepted, " +
                "question_id, " +
                "user_id, " +
                "slack_id" +
            ")" +
            "VALUES(" +
                ":answer.answer, " +
                ":answer.title, " +
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
            "answer=:answer.answer, " +
            "title=:answer.title, " +
            "votes=:answer.votes, " +
            "accepted=:answer.accepted " +
        "WHERE " +
            "answer.id=:answerId " +
            "AND question_id=:questionId " +
            "AND answer.user_id=:userId")
    Observable<Void> updateAnswer(long userId, long questionId, long answerId, Answer answer);

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
        "FROM answer a " +
        "INNER JOIN \"user\" u on u.id = a.user_id " +
        "INNER JOIN question q on q.id = a.question_id " +
        "WHERE a.id=:id")
    Observable<AnswerInternal> getAnswerById(long id);
}
