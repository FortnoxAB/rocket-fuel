package dao;

import api.Question;
import rx.Observable;
import se.fortnox.reactivewizard.db.Query;

public interface QuestionSearchDao {

    @Query("SELECT DISTINCT " +
            "question.id, " +
            "question.question, " +
            "answer_accepted, " +
            "question.title, " +
            "question.bounty, " +
            "question.votes, " +
            "question.created_at, " +
            "question, " +
            "question.user_id, " +
            "\"user\".name as created_by " +
            "FROM " +
            "question " +
            "INNER JOIN " +
            "\"user\" on \"user\".id = question.user_id " +
            "LEFT JOIN  " +
            "answer on answer.question_id = question.id and answer.user_id = question.user_id " +
            "WHERE  " +
            "question.title like ('%' || :search || '%') " +
            "or " +
            "question.question like ('%' || :search || '%') " +
            "or " +
            "answer.title  like ('%' || :search || '%') " +
            "order by  " +
            "question.votes desc, " +
            "question.created_at desc")
    Observable<Question> getQuestions(String search);
}
