package impl;

import api.Auth;
import api.User;
import api.UserResource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.UserDao;
import rx.Observable;

import static rx.Observable.just;

@Singleton
public class UserResourceImpl implements UserResource {

    private final UserDao userDao;

    @Inject
    public UserResourceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Observable<String> getCurrent(Auth auth) {
        return just(Long.valueOf(auth.getUserId()).toString());
    }

    @Override
    public Observable<Integer> createUser(User user) {
        return this.userDao.insertUser(user);
    }

    @Override
    public Observable<User> getUserByEmail(String email) {
        return this.userDao.getUserByEmail(email);
    }

    @Override
    public Observable<User> getUserById(long userId) {
        return this.userDao.getUserById(userId);
    }
}
