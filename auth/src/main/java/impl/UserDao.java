package impl;

import rx.Observable;

public interface UserDao {
    Observable<Integer> upsertUser(Object user);
}
