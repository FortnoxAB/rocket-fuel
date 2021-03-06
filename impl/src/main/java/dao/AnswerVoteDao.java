package dao;

import rx.Observable;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;

public interface AnswerVoteDao {

    @Query(
        "SELECT " +
            "answer_vote.user_id, " +
            "answer_vote.answer_id, " +
            "answer_vote.value " +
        "FROM " +
            "answer_vote " +
        "WHERE " +
            "answer_vote.user_id = :userId AND " +
            "answer_vote.answer_id = :answerId"
    )
    Observable<AnswerVote> findVote(long userId, long answerId);

    @Update(
        "INSERT INTO " +
            "answer_vote " +
        "(" +
            "user_id, " +
            "answer_id, " +
            "value, " +
            "created_at" +
        ")" +
        "VALUES" +
        "(" +
            ":vote.userId, " +
            ":vote.answerId, " +
            ":vote.value, " +
            "NOW()" +
        ")"
    )
    Observable<Integer> createVote(AnswerVote vote);

    @Update(
        "DELETE FROM " +
            "answer_vote " +
        "WHERE " +
            "user_id = :userId AND " +
            "answer_id = :answerId"
    )
    Observable<Integer> deleteVote(long userId, long answerId);
}
