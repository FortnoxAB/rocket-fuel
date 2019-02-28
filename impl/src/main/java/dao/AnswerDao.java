package dao;


import api.Answer;
import rx.Observable;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;

public interface AnswerDao {

    @Update("UPDATE answer " +
            "SET accepted=true WHERE id=:answerId AND user_id=:userId")
    Observable<Integer> markAsAnswered(long userId, long answerId);

    @Query("SELECT answer.id, answer.user_id, answer.answer, answer.created_at, answer.accepted, answer.title, answer.votes , answer.slack_id, \"user\".\"name\"  as created_by from answer \n" +
            "INNER JOIN \"user\" on \"user\".id = answer.user_id \n" +
            "where user_id=:userId AND question_id=:questionId\n")
    Observable<Answer> getAnswers(long userId, long questionId);

    @Query("SELECT answer.id, answer.user_id, answer.answer, answer.created_at, answer.accepted, answer.title, answer.votes , answer.slack_id, \"user\".\"name\"  as created_by from answer \n" +
        "INNER JOIN \"user\" on \"user\".id = answer.user_id \n" +
        "where question_id=:questionId\n")
    Observable<Answer> getAnswers(long questionId);

    @Query("SELECT answer.id, answer.user_id, answer.answer, answer.created_at, answer.accepted, answer.title, answer.votes , answer.slack_id, \"user\".\"name\"  as created_by from answer \n" +
        "INNER JOIN \"user\" on \"user\".id = answer.user_id \n" +
        "where slack_id=:slackId\n")
    Observable<Answer> getAnswer(String slackId);

    @Update("INSERT INTO answer\n" +
            "(answer, title, votes, created_at, accepted, question_id, user_id, slack_id)\n" +
            "VALUES(:answer.answer, :answer.title, 0, NOW(), false, :questionId, :userId, :answer.slackId);\n")
    Observable<Void> createAnswer(long userId, long questionId, Answer answer);


    @Update("UPDATE answer " +
            "SET answer=:answer.answer, title=:answer.title, votes=:answer.votes, accepted=:answer.accepted " +
            "WHERE answer.id=:answerId AND question_id=:questionId AND answer.user_id=:userId")
    Observable<Void> updateAnswer(long userId, long questionId, long answerId, Answer answer);

    @Update("UPDATE answer set votes=votes+1 where slack_id = :threadId")
    Observable<Void> upVoteAnswer(String threadId);

    @Update(value = "UPDATE answer set votes=votes-1 where slack_id = :threadId")
    Observable<Void> downVoteAnswer(String threadId);
}
