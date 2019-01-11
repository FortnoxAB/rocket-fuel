package impl.dao;


import api.Answer;
import rx.Observable;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;

import java.util.List;

public interface AnswerDao {

    @Update("asd")
    rx.Observable<Integer> markAsAnswered(long answerId);

    @Query("SELECT answer.id, answer.user_id, answer.answer, answer.created_at, answer.accepted, answer.title, answer.votes , \"user\".\"name\"  as created_by from answer \n" +
            "INNER JOIN \"user\" on \"user\".id = answer.user_id \n" +
            "where user_id=:userId AND question_id=:questionId\n")
    Observable<Answer> getAnswers(long userId, long questionId);

    @Update("INSERT INTO answer\n" +
            "(answer, title, votes, created_at, accepted, question_id, user_id)\n" +
            "VALUES(:answer.answer, :answer.title, 0, NOW(), false, :questionId, :userId);\n")
    Observable<Void> createAnswer(long userId, long questionId, Answer answer);


    @Update("UPDATE answer\n" +
            "SET answer.answer=':answer.answer', title=:answer.title, votes=:answer.votes, accepted=:answer.accepted\n" +
            "WHERE answer.id=:answerId AND question_id=:questionId")
    Observable<Void> updateAnswer(long userId, long questionId, long answerId, Answer answer);
}
