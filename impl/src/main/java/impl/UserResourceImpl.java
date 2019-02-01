package impl;

import api.ApplicationAuthenticator;
import api.ApplicationToken;
import api.Auth;
import api.User;
import api.UserResource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.UserDao;
import rx.Observable;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

import static rx.Observable.just;

@Singleton
public class UserResourceImpl implements UserResource {

    private final ResponseHeaderHolder responseHeaderHolder;

    private final UserDao userDao;

    private final ApplicationAuthenticator applicationAuthenticator;

    @Inject
    public UserResourceImpl(UserDao userDao, ResponseHeaderHolder responseHeaderHolder, ApplicationAuthenticator applicationAuthenticator) {
        this.userDao = userDao;
        this.responseHeaderHolder = responseHeaderHolder;
        this.applicationAuthenticator = applicationAuthenticator;
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

    @Override
    public Observable<ApplicationToken> generateToken(@NotNull String openIdToken) {
    	// get user by email ( how does one get

	   // applicationAuthenticator.validate(openIdToken);
        ApplicationToken applicationToken = applicationAuthenticator.create(openIdToken, 2);
        responseHeaderHolder.addHeaders(applicationToken, new HashMap<String, Object>() {{
            put("Set-Cookie", "applicationToken=" + applicationToken.getApplicationToken() + "; path=/; domain=" + "localhost" + ";");
        }});
        return just(applicationToken);
    }

}
