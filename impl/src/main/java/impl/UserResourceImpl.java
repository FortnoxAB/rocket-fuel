package impl;

import api.User;
import api.UserResource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.UserDao;
import rx.Observable;

@Singleton
public class UserResourceImpl implements UserResource {

    private final UserDao userDao;

    @Inject
    public UserResourceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Observable<Void> createUser(long userId, User user) {
        return this.userDao.upsertUser(userId, user);
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
