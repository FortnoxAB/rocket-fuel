package impl.dao;

import api.User;
import rx.Observable;
import se.fortnox.reactivewizard.db.Query;


public interface UserDao {

    @Query(value = "SELECT * from \"user\" where id = :userId", maxLimit = 1)
    Observable<User> getUser(long userId);
}
