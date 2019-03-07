package impl;

import api.User;
import api.UserResource;
import api.auth.ApplicationToken;
import api.auth.Auth;
import auth.openid.ImmutableOpenIdToken;
import auth.openid.OpenIdValidator;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.UserDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

import static rx.Observable.error;

@Singleton
public class UserResourceImpl implements UserResource {

    private final Logger LOG = LoggerFactory.getLogger(UserResourceImpl.class);

    private final ResponseHeaderHolder responseHeaderHolder;

    private final UserDao userDao;

    private final OpenIdValidator openIdValidator;

    private final ApplicationTokenCreator applicationTokenCreator;

    @Inject
    public UserResourceImpl(UserDao userDao, ResponseHeaderHolder responseHeaderHolder, OpenIdValidator openIdValidator, ApplicationTokenCreator applicationTokenCreator) {
        this.userDao = userDao;
        this.responseHeaderHolder = responseHeaderHolder;
        this.openIdValidator = openIdValidator;
        this.applicationTokenCreator = applicationTokenCreator;
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
    public Observable<User> getUserByEmail(String email) {
        return this.userDao.getUserByEmail(email)
                .switchIfEmpty(error(new WebException(HttpResponseStatus.NOT_FOUND)));
    }

    @Override
    public Observable<User> getUserById(long userId) {
        return this.userDao.getUserById(userId)
                .switchIfEmpty(error(new WebException(HttpResponseStatus.NOT_FOUND)));
    }

    @Override
    public Observable<User> generateToken(@NotNull String openIdToken) {
        final ImmutableOpenIdToken validOpenId = openIdValidator.validate(openIdToken);
        return userDao.getUserByEmail(validOpenId.email)
                .onErrorResumeNext((t) -> error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed to search for user", t)))
                .single()
                .onErrorResumeNext((t) -> addUserToDatabase(validOpenId))
                .map((user) -> {
            ApplicationToken applicationToken = applicationTokenCreator.createApplicationToken(validOpenId, user.getId());
            addAsCookie(applicationToken);
            return user;
        });

    }

    private Observable<User> addUserToDatabase(ImmutableOpenIdToken validOpenId) {
        User user = new User();
        user.setName(validOpenId.name);
        user.setEmail(validOpenId.email);
        return userDao.insertUser(user).flatMap((ignore) -> userDao.getUserByEmail(validOpenId.email))
                .onErrorResumeNext((t) -> error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed to add user to the database", t)));
    }

    private void addAsCookie(final ApplicationToken applicationToken) {
        responseHeaderHolder.addHeaders(applicationToken, new HashMap<String, Object>() {{
            put("Set-Cookie", "application=" + applicationToken.getApplicationToken() + "; path=/; domain=" + "localhost" + ";");
        }});
    }

}
