package impl;

import rx.Observable;
import se.fortnox.reactivewizard.db.Query;

public interface AuthDao {

    @Query(value = "SELECT id from \"user\" where email = :email", maxLimit = 1)
    Observable<Long> getUserId(String email);
}
