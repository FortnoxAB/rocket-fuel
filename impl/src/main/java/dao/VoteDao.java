package dao;

import rx.Observable;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;

public interface VoteDao {

    @Query(
        "SELECT " +
            "v.user_id, " +
            "v.answer_id, " +
            "v.value " +
        "FROM " +
            "answer_vote v " +
        "WHERE " +
            "v.user_id = :userId AND " +
            "v.answer_id = :answerId"
    )
    Observable<Vote> findVote(long userId, long answerId);

    @Update(
        "INSERT INTO " +
            "answer_vote " +
        "(" +
            "user_id, " +
            "answer_id, " +
            "value, " +
            "created_at, " +
            "modified_at" +
        ")" +
        "VALUES" +
        "(" +
            ":vote.userId, " +
            ":vote.answerId, " +
            ":vote.value, " +
            "NOW(), " +
            "NOW()" +
        ")"
    )
    Observable<Integer> createVote(Vote vote);


    @Update(
        "UPDATE answer_vote SET " +
            "value = :vote.value, " +
            "modified_at = NOW() " +
        "WHERE " +
            "user_id = :vote.userId AND " +
            "answer_id = :vote.answerId"
    )
    Observable<Integer> updateVote(Vote vote);

    @Update(
        "DELETE FROM " +
            "answer_vote " +
        "WHERE " +
            "user_id = :userId AND " +
            "answer_id = :answerId"
    )
    Observable<Integer> deleteVote(long userId, long answerId);
}
