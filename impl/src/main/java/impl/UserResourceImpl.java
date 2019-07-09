package impl;

import api.User;
import api.UserResource;
import api.auth.ApplicationToken;
import api.auth.Auth;
import auth.application.ApplicationTokenConfig;
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
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;
import static rx.Observable.defer;
import static rx.Observable.error;
import static rx.Observable.just;
import static se.fortnox.reactivewizard.util.rx.RxUtils.exception;

@Singleton
public class UserResourceImpl implements UserResource {


    private static final Logger LOG = LoggerFactory.getLogger(UserResourceImpl.class);
    public static final int SESSION_MAX_AGE_SECONDS = 3600;
    public static final String FAILED_TO_UPDATE_USER_NAME_OR_PICTURE = "failed.to.update.user.name.or.picture";
    public static final String FAILED_TO_SEARCH_FOR_USER = "failed.to.search.for.user";
    public static final String DEFAULT_PICTURE_URL =  "https://via.placeholder.com/96";

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
                .switchIfEmpty(exception(() -> new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "current.user.not.found")));
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
                    return addUserToDatabase("Added from slack", email, null);
                } else {
                    return error(new WebException(HttpResponseStatus.NOT_FOUND));
                }
            }));
    }

    @Override
    public Observable<User> getUserById(long userId) {
        return this.userDao.getUserById(userId)
                .switchIfEmpty(exception(() -> new WebException(HttpResponseStatus.NOT_FOUND)));
    }

    @Override
    public Observable<User> signIn(@NotNull String openIdToken) {
        return openIdValidator.validate(openIdToken).flatMap(validOpenId ->
                userDao.getUserByEmail(validOpenId.email)
                .onErrorResumeNext(throwable -> error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, FAILED_TO_SEARCH_FOR_USER, throwable)))
                .switchIfEmpty(addUserToDatabase(validOpenId.name, validOpenId.email, validOpenId.picture))
                .flatMap(user -> updateUserNameAndPicture(user, validOpenId))
                .map(user -> addApplicationTokenToHeader(validOpenId, user)));
    }

    private User addApplicationTokenToHeader(ImmutableOpenIdToken validOpenId, User user) {
        ApplicationToken applicationToken = applicationTokenCreator.createApplicationToken(validOpenId, user.getId());
        addAsCookie(applicationToken, user);
        return user;
    }

    private Observable<User> updateUserNameAndPicture(User user, ImmutableOpenIdToken openId) {
        final String picture = getPicture(openId.picture);
        if(!hasUpdatedNameOrPicture(user, openId.name, picture)) {
            return just(user);
        }

        return userDao.updateUser(user.getId(), openId.name, picture)
            .doOnError(e -> LOG.error("failed to update user with id: {} with the following updated name: {} and updated picture: {}",
                user.getId(),
                openId.name,
                openId.picture))
            .onErrorResumeNext(e -> error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, FAILED_TO_UPDATE_USER_NAME_OR_PICTURE, e)))
            .map(e -> {
                user.setName(openId.name);
                user.setPicture(picture);
                return user;
            });
    }

    private String getPicture(String picture) {
        if(isNullOrEmpty(picture)) {
            return DEFAULT_PICTURE_URL;
        }
        return picture;
    }

    private boolean hasUpdatedNameOrPicture(User user, String openIdName, String openIdPicture) {
        if(!Objects.equals(user.getName(), openIdName)) {
            return true;
        }
        if(!Objects.equals(user.getPicture(), openIdPicture)) {
            return true;
        }
        return false;
    }


    private Observable<User> addUserToDatabase(String name, String email, String picture) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPicture(picture);
        return userDao.insertUser(user).flatMap(ignore -> userDao.getUserByEmail(email))
                .onErrorResumeNext(t -> error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed.to.add.user.to.the.database", t)));
    }

    private void addAsCookie(final ApplicationToken applicationToken, User user) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Set-Cookie", formatCookie(applicationToken.getApplicationToken(), SESSION_MAX_AGE_SECONDS, applicationTokenConfig.getDomain()));
        responseHeaderHolder.addHeaders(user, headers);
    }

    @Override
    public Observable<Long> signOut(Auth auth) {
        int expireNow = 0;
        Map<String, Object> headers = new HashMap<>();
        headers.put("Set-Cookie", formatCookie("", expireNow, applicationTokenConfig.getDomain()));
        responseHeaderHolder.addHeaders(auth.getUserId(), headers);
        return Observable.just(auth.getUserId());
    }

    private static String formatCookie(String applicationToken, int maxAge, String domain){
        return String.format("application=%s; Path=/; Max-Age=%d; Domain=%s; SameSite=Strict; HttpOnly; Secure;", applicationToken, maxAge, domain);
    }

}
