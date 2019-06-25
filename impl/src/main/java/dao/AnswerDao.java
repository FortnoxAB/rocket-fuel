package dao;


import api.Answer;
import rx.Observable;
import se.fortnox.reactivewizard.db.GeneratedKey;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;

public interface AnswerDao {

    @Update("UPDATE answer " +
            "SET accepted=true WHERE id=:answerId")
    Observable<Integer> markAsAccepted(long answerId);

    @Query("SELECT answer.id, answer.user_id, answer.answer, answer.created_at, answer.accepted, answer.title, answer.votes , answer.slack_id, \"user\".picture, \"user\".\"name\"  AS created_by FROM answer \n" +
            "INNER JOIN \"user\" on \"user\".id = answer.user_id \n" +
            "WHERE user_id=:userId AND question_id=:questionId\n")
    Observable<Answer> getAnswers(long userId, long questionId);

    @Query("SELECT answer.id, answer.user_id, answer.answer, answer.created_at, answer.accepted, answer.title, answer.votes , answer.slack_id, \"user\".picture, \"user\".\"name\"  AS created_by FROM answer \n" +
        "INNER JOIN \"user\" on \"user\".id = answer.user_id \n" +
        "where question_id=:questionId order by answer.accepted desc, answer.votes desc, answer.created_at desc")
    Observable<Answer> getAnswers(long questionId);


    @Query("SELECT answer.id, answer.user_id, answer.answer, answer.created_at, answer.accepted, answer.title, answer.votes , answer.slack_id, \"user\".picture, \"user\".\"name\"  AS created_by FROM answer \n" +
        "INNER JOIN \"user\" on \"user\".id = answer.user_id \n" +
        "WHERE slack_id=:slackId\n")
    Observable<Answer> getAnswer(String slackId);

    @Query("SELECT question_id FROM answer WHERE id=:answerId")
    Observable<Long> getQuestionIdByAnswer(Long answerId);

    @Update("INSERT INTO answer\n" +
            "(answer, title, votes, created_at, accepted, question_id, user_id, slack_id)\n" +
            "VALUES(:answer.answer, :answer.title, 0, NOW(), false, :questionId, :userId, :answer.slackId);\n")
    Observable<GeneratedKey<Long>> createAnswer(long userId, long questionId, Answer answer);


    @Update("UPDATE answer " +
            "SET answer=:answer.answer, title=:answer.title, votes=:answer.votes, accepted=:answer.accepted " +
            "WHERE answer.id=:answerId AND question_id=:questionId AND answer.user_id=:userId")
    Observable<Void> updateAnswer(long userId, long questionId, long answerId, Answer answer);

    @Update("UPDATE answer SET votes=votes+1 WHERE slack_id = :threadId")
    Observable<Void> upVoteAnswer(String threadId);

    @Update(value = "UPDATE answer SET votes=votes-1 WHERE slack_id = :threadId")
    Observable<Void> downVoteAnswer(String threadId);
}
