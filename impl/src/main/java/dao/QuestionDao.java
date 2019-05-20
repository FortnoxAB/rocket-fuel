package dao;

import api.Question;
import rx.Observable;
import se.fortnox.reactivewizard.CollectionOptions;
import se.fortnox.reactivewizard.db.GeneratedKey;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;


public interface QuestionDao {

    @Query("SELECT question.id, question.question, answer_accepted, question.title, question.bounty, question.votes, question.created_at, question,user_id, question.slack_id, \"user\".name as created_by FROM question  INNER JOIN \"user\" on \"user\".id = question.user_id WHERE question.user_id=:userId")
    Observable<Question> getQuestions(long userId, CollectionOptions collectionOptions);

    @Update("INSERT INTO question (question, title, bounty, votes, created_at, user_id, slack_id) " +
      " VALUES(:question.question, :question.title, :question.bounty, 0, NOW(), :userId, :question.slackId)")
    Observable<GeneratedKey<Long>> addQuestion(long userId, Question question);

    @Update("UPDATE question " +
            "SET question=:question.question, bounty=:question.bounty, title=:question.title, votes=:question.votes " +
            "WHERE question.id=:questionId AND question.user_id=:userId")
    Observable<Integer> updateQuestion(long userId, long questionId, Question question);

    @Query("SELECT id, question, title, bounty, votes, answer_accepted, created_at, user_id, slack_id FROM question WHERE user_id = :userId AND id=:questionId")
    Observable<Question> getQuestion(long userId, long questionId);

    @Query("SELECT question.id, question.question, question.title, question.bounty, question.votes, question.answer_accepted, question.created_at, question.slack_id, u.name as created_by, u.id AS user_id FROM question LEFT JOIN \"user\" u on u.id = question.user_id WHERE question.id=:questionId")
    Observable<Question> getQuestionById(long questionId);

    @Update("UPDATE question " +
            "SET answer_accepted=true WHERE question.id=:questionId AND question.user_id=:userId")
    Observable<Integer> markAsAnswered(long userId, long questionId);

    @Query("SELECT id, question, title, bounty, votes, answer_accepted, created_at, user_id, slack_id FROM question WHERE slack_id = :slackId")
    Observable<Question> getQuestionBySlackThreadId(String slackId);

    @Update("UPDATE question set votes=votes+1 where slack_id = :threadId")
    Observable<Void> upVoteQuestion(String threadId);

    @Update(value = "UPDATE question set votes=votes-1 where slack_id = :threadId")
    Observable<Void> downVoteQuestion(String threadId);
}
