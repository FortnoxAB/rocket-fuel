package dao;

import api.Answer;
import rx.Observable;
import se.fortnox.reactivewizard.db.GeneratedKey;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;

public interface AnswerDao {

    @Update(
        "UPDATE answer " +
        "SET accepted_at=NOW() " +
        "WHERE id=:answerId"
    )
    Observable<Integer> markAsAccepted(long answerId);

    @Query(
        "SELECT " +
            "answer.id, " +
            "answer.user_id, " +
            "answer.answer, " +
            "answer.created_at, " +
            "answer.accepted_at, " +
            "\"user\".picture, " +
            "answer.slack_id, " +
            "answer.question_id, " +
            "\"user\".name AS created_by, " +
            "(SELECT COALESCE(SUM(answer_vote.value), 0) FROM answer_vote WHERE answer_vote.answer_id = answer.id) AS votes " +
        "FROM answer " +
            "INNER JOIN \"user\" on \"user\".id = answer.user_id " +
        "WHERE user_id=:userId AND question_id=:questionId"
    )
    Observable<Answer> getAnswers(long userId, long questionId);

    @Query(
        "SELECT " +
            "answer.id, " +
            "answer.user_id, " +
            "answer.answer, " +
            "answer.created_at, " +
            "answer.accepted_at, " +
            "\"user\".picture, " +
            "answer.slack_id, " +
            "answer.question_id, " +
            "\"user\".name AS created_by, " +
            "(SELECT COALESCE(SUM(answer_vote.value), 0) " +
                "FROM answer_vote " +
                "WHERE answer_vote.answer_id = answer.id) AS votes, " +
            "(SELECT COALESCE(SUM(answer_vote.value), 0) " +
                "FROM answer_vote " +
                "WHERE answer_vote.answer_id = answer.id AND answer_vote.user_id = :userId) AS current_user_vote " +
        "FROM answer " +
            "INNER JOIN \"user\" on \"user\".id = answer.user_id " +
        "WHERE question_id=:questionId " +
        "ORDER BY answer.accepted_at desc, \"votes\" desc, answer.created_at desc"
    )
    Observable<Answer> getAnswersWithUserVotes(long userId, long questionId);


    @Query(
        "SELECT " +
            "answer.id, " +
            "answer.user_id, " +
            "answer.answer, " +
            "answer.created_at, " +
            "answer.accepted_at, " +
            "\"user\".picture, " +
            "answer.slack_id, " +
            "answer.question_id, " +
            "\"user\".name AS created_by, " +
            "(SELECT COALESCE(SUM(answer_vote.value), 0) FROM answer_vote WHERE answer_vote.answer_id = answer.id) AS votes " +
        "FROM answer " +
            "INNER JOIN \"user\" on \"user\".id = answer.user_id " +
        "WHERE slack_id=:slackId"
    )
    Observable<Answer> getAnswer(String slackId);

    @Update(
        "INSERT INTO answer " +
            "(" +
                "answer, " +
                "created_at, " +
                "question_id, " +
                "user_id, " +
                "slack_id" +
            ")" +
            "VALUES(" +
                ":answer.answer, " +
                "NOW(), " +
                ":questionId, " +
                ":userId, " +
                ":answer.slackId" +
            ")")
    Observable<GeneratedKey<Long>> createAnswer(long userId, long questionId, Answer answer);


    @Update(
        "UPDATE answer SET " +
            "answer=:answer.answer, " +
            "accepted_at=:answer.acceptedAt " +
        "WHERE " +
            "answer.id=:answerId " +
            "AND answer.user_id=:userId")
    Observable<Void> updateAnswer(long userId, long answerId, Answer answer);

    @Query(
        "SELECT " +
            "answer.id, " +
            "answer.user_id, " +
            "answer.answer, " +
            "answer.created_at, " +
            "answer.accepted_at, " +
            "\"user\".picture, " +
            "answer.slack_id, " +
            "answer.question_id, " +
            "\"user\".name AS created_by, " +
            "question.user_id AS \"question.user_id\", " +
            "(SELECT COALESCE(SUM(answer_vote.value), 0) FROM answer_vote WHERE answer_vote.answer_id = answer.id) AS votes " +
        "FROM answer " +
            "INNER JOIN \"user\" on \"user\".id = answer.user_id " +
            "INNER JOIN question on question.id = answer.question_id " +
        "WHERE answer.id=:id")
    Observable<AnswerInternal> getAnswerById(long id);

    @Update(
        "DELETE FROM " +
            "answer " +
        "WHERE " +
            "answer.user_id = :userId AND answer.id = :answerId")
    Observable<Void> deleteAnswer(long userId, long answerId);
}
