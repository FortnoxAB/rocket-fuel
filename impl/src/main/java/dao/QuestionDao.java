package dao;

import api.Question;
import rx.Observable;
import se.fortnox.reactivewizard.CollectionOptions;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;


public interface QuestionDao {

    @Query("SELECT question.id, question.question, answer_accepted, question.title, question.bounty, question.votes, question.created_at, question,user_id,  \"user\".name as created_by FROM question  INNER JOIN \"user\" on \"user\".id = question.user_id WHERE question.user_id=:userId")
    Observable<Question> getQuestions(long userId, CollectionOptions collectionOptions);

    @Update("INSERT INTO question (question, title, bounty, votes, created_at, user_id, slack_thread_id) VALUES(:question.question, :question.title, :question.bounty, 0, NOW(), :userId, :question.slackThreadId)")
    Observable<Void> addQuestion(long userId, Question question);

    @Update("UPDATE question " +
            "SET question=:question.question, bounty=:question.bounty, title=:question.title, votes=:question.votes " +
            "WHERE question.id=:questionId AND question.user_id=:userId")
    Observable<Integer> updateQuestion(long userId, long questionId, Question question);

    @Query("SELECT id, question, title, bounty, votes, answer_accepted, created_at, user_id, slack_thread_id FROM question WHERE user_id = :userId AND id=:questionId")
    Observable<Question> getQuestion(long userId, long questionId);

    @Update("UPDATE question " +
            "SET answer_accepted=true WHERE question.id=:questionId AND question.user_id=:userId")
    Observable<Integer> markAsAnswered(long userId, long questionId);

    @Query("SELECT id, question, title, bounty, votes, answer_accepted, created_at, user_id, slack_thread_id FROM question WHERE slack_thread_id = :slackThreadId")
    Observable<Question> getQuestionBySlackThreadId(String slackThreadId);
}
