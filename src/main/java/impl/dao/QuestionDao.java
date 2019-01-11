package impl.dao;

import api.Question;
import rx.Observable;
import se.fortnox.reactivewizard.CollectionOptions;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;


public interface QuestionDao {

    @Query("SELECT question.id, question.question, question.title, question.bounty, question.votes, question.created_at, question,user_id,  \"user\".name as created_by FROM question  INNER JOIN \"user\" on \"user\".id = question.user_id ")
    Observable<Question> getQuestions(CollectionOptions collectionOptions);

    @Update("INSERT INTO public.question (question, title, bounty, votes, created_at, user_id) VALUES(:question.question, :question.title, 0, 0, NOW(), :userId)")
    Observable<Void> addQuestion(long userId, Question question);

    Observable<Integer> updateQuestion(Question question);

    @Query("SELECT id, question, title, bounty, votes, created_at, user_id FROM question WHERE id = :id")
    Observable<Question> getQuestion(long id);

    Observable<Integer> markAsAnswered(long questionId);
}
