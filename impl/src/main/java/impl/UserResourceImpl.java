package impl;

import api.User;
import api.UserResource;
import api.auth.ApplicationToken;
import api.auth.Auth;
import auth.application.ApplicationTokenConfig;
import auth.openid.OpenIdValidator;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.UserDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

import static rx.Observable.defer;
import static rx.Observable.error;

@Singleton
public class UserResourceImpl implements UserResource {

    private final ResponseHeaderHolder responseHeaderHolder;
    private final UserDao userDao;
    private final OpenIdValidator openIdValidator;
    private final ApplicationTokenCreator applicationTokenCreator;
    private final ApplicationTokenConfig applicationTokenConfig;

    @Inject
    public UserResourceImpl(UserDao userDao, ResponseHeaderHolder responseHeaderHolder, OpenIdValidator openIdValidator, ApplicationTokenCreator applicationTokenCreator, ApplicationTokenConfig applicationTokenConfig) {
        this.userDao = userDao;
        this.responseHeaderHolder = responseHeaderHolder;
        this.openIdValidator = openIdValidator;
        this.applicationTokenCreator = applicationTokenCreator;
        this.applicationTokenConfig = applicationTokenConfig;
    }

    @Override
    public Observable<User> getCurrent(Auth auth) {
        return userDao.getUserById(auth.getUserId())
                .switchIfEmpty(error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "current user not found")));
    }

    @Override
    public Observable<Integer> createUser(Auth auth, User user) {
        return this.userDao.insertUser(user);
    }

    @Override
    public Observable<User> getUserByEmail(String email, boolean createIfMissing) {
        return this.userDao.getUserByEmail(email)
            .switchIfEmpty(defer(() -> {
                if(createIfMissing) {
                    return addUserToDatabase("Added from slack", email);
                } else {
                    return error(new WebException(HttpResponseStatus.NOT_FOUND));
                }
            }));
    }

    @Override
    public Observable<User> getUserById(long userId) {
        return this.userDao.getUserById(userId)
                .switchIfEmpty(error(new WebException(HttpResponseStatus.NOT_FOUND)));
    }

    @Override
    public Observable<User> generateToken(@NotNull String openIdToken) {
        return openIdValidator.validate(openIdToken).flatMap(validOpenId ->
                userDao.getUserByEmail(validOpenId.email)
                .onErrorResumeNext(t -> error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed to search for user", t)))
                .switchIfEmpty(addUserToDatabase(validOpenId.name, validOpenId.email))
                .map(user -> {
                            ApplicationToken applicationToken = applicationTokenCreator.createApplicationToken(validOpenId, user.getId());
                            addAsCookie(applicationToken, user);
                            return user;
                        }
                ));
    }

    private Observable<User> addUserToDatabase(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userDao.insertUser(user).flatMap(ignore -> userDao.getUserByEmail(email))
                .onErrorResumeNext((t) -> error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed to add user to the database", t)));
    }

    private void addAsCookie(final ApplicationToken applicationToken, User user) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Set-Cookie", "application=" + applicationToken.getApplicationToken() + "; path=/; domain=" + applicationTokenConfig.getDomain() + ";");
        responseHeaderHolder.addHeaders(user, headers);
    }

}
