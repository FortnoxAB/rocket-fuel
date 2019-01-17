package dao;

import api.User;
import rx.Observable;
import se.fortnox.reactivewizard.db.Query;


public interface UserDao {
    //TODO add upsert, we need to add a unique constrain here.
    Observable<Void> upsertUser(Long userId, User user);

    @Query(value = "SELECT * from \"user\" where email = :email", maxLimit = 1)
    Observable<User> getUserByEmail(String email);

    @Query(value = "SELECT * from \"user\" where id = :userId", maxLimit = 1)
    Observable<User> getUserById(long userId);
}
