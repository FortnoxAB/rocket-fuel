package dao;

import rx.Observable;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;

public interface QuestionVoteDao {

    @Query(
        "SELECT " +
            "question_vote.user_id, " +
            "question_vote.question_id, " +
            "question_vote.value " +
        "FROM " +
            "question_vote " +
        "WHERE " +
            "question_vote.user_id = :userId AND " +
            "question_vote.question_id = :questionId"
    )
    Observable<QuestionVote> findVote(long userId, long questionId);

    @Update(
        "INSERT INTO " +
            "question_vote " +
        "(" +
            "user_id, " +
            "question_id, " +
            "value, " +
            "created_at" +
        ")" +
        "VALUES" +
        "(" +
            ":vote.userId, " +
            ":vote.questionId, " +
            ":vote.value, " +
            "NOW()" +
        ")"
    )
    Observable<Integer> createVote(QuestionVote vote);

    @Update(
        "DELETE FROM " +
            "question_vote " +
        "WHERE " +
            "user_id = :userId AND " +
            "question_id = :questionId"
    )
    Observable<Integer> deleteVote(long userId, long questionId);
}
