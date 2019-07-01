package impl;

import api.User;
import api.UserResource;
import api.auth.Auth;
import auth.application.ApplicationTokenConfig;
import auth.openid.ImmutableOpenIdToken;
import auth.openid.OpenIdValidator;
import dao.UserDao;
import dates.DateProvider;
import dates.DateProviderImpl;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.testcontainers.containers.PostgreSQLContainer;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import static impl.UserResourceImpl.DEFAULT_PICTURE_URL;
import static impl.UserResourceImpl.FAILED_TO_UPDATE_USER_NAME_OR_PICTURE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.error;
import static rx.Observable.just;

public class UserResourceTest {
    private static final String OPEN_ID_TOKEN = "eyJhbGciOiJIUzI1NiIsImtpZCI6ImIxNWEyYjhmN2E2YjNmNmJjMDhiYzFjNTZhODg0MTBlMTQ2ZDAxZmQiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiamVwcDMiLCJwaWN0dXJlIjoidXJsdG9waWN0dXJlIiwiZW1haWwiOiJqZXNwZXIubGFoZGV2aXJ0YUBnbWFpbC5jb20iLCJpYXQiOjE1NDg0MTc2NDcsImV4cCI6MTY0ODQyMTI0N30.WH70YBPaMFg1QtTaZddikcslsN2C5sxm4oQSOVIt_PU";
    private static UserResource userResource;
    private static OpenIdValidator openIdValidator;
    private static ResponseHeaderHolder responseHeaderHolder;
    private static ApplicationTokenConfig applicationTokenConfig;
    private static ApplicationTokenCreator applicationTokenCreator;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup testSetup;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        UserDao userDao = testSetup.getInjector().getInstance(UserDao.class);
        responseHeaderHolder = mock(ResponseHeaderHolder.class);
        openIdValidator = mock(OpenIdValidator.class);
        applicationTokenConfig = new ApplicationTokenConfig();
        applicationTokenConfig.setSecret("my-test-secret-that-is-valid");
        applicationTokenConfig.setDomain(".rocket-fuel");
        DateProvider dateProvider = new DateProviderImpl();
        applicationTokenCreator = new ApplicationTokenCreator(applicationTokenConfig, dateProvider);

        userResource = new UserResourceImpl(userDao, responseHeaderHolder, openIdValidator, applicationTokenCreator, applicationTokenConfig);
    }

    @After
    public void afterEach() throws Exception {
        testSetup.clearDatabase();
    }

    @Before
    public void beforeEach() throws Exception {
        reset(responseHeaderHolder);
        testSetup.setupDatabase();
    }


    @Test
    public void shouldCreateUserInDatabaseIfFirstTimeLogin() {
        // given
        ImmutableOpenIdToken openIdObject = new ImmutableOpenIdToken("jeppe", "jeppe@email.com", "pictureUrl");
        when(openIdValidator.validate(any())).thenReturn(just(openIdObject));

        // when
        User returnedUser = userResource.generateToken(OPEN_ID_TOKEN).toBlocking().singleOrDefault(null);

        // then
        assertNotNull(returnedUser);
        User insertedUser = userResource.getUserByEmail("jeppe@email.com", false).toBlocking().singleOrDefault(null);
        assertEquals("jeppe", insertedUser.getName());
    }

    @Test
    public void shouldFetchExistingUserInDatabaseIfReturningUser() {
        // given
        User user = insertUser();
        ImmutableOpenIdToken openIdObject = new ImmutableOpenIdToken(user.getName(), user.getEmail(), user.getPicture());
        when(openIdValidator.validate(any())).thenReturn(just(openIdObject));

        // when
        User returnedUser = userResource.generateToken(OPEN_ID_TOKEN).toBlocking().singleOrDefault(null);

        // then
        assertNotNull(returnedUser);
        User insertedUser = userResource.getUserByEmail(user.getEmail(), false).toBlocking().singleOrDefault(null);
        assertEquals(user.getName(), insertedUser.getName());
        assertEquals(user.getPicture(), insertedUser.getPicture());
    }

    @Test
    public void shouldAddCookieToHeader() {
        // given
        User user = insertUser();
        ImmutableOpenIdToken openIdObject = new ImmutableOpenIdToken(user.getName(), user.getEmail(), "pictureUrl");
        when(openIdValidator.validate(any())).thenReturn(just(openIdObject));

        // when
        User returnedUser = userResource.generateToken(OPEN_ID_TOKEN).toBlocking().singleOrDefault(null);

        // then
        ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        assertNotNull(returnedUser);
        verify(responseHeaderHolder, times(1)).addHeaders(any(), mapArgumentCaptor.capture());
        String setCookieHeader = (String) mapArgumentCaptor.getValue().get("Set-Cookie");
        assertTrue(setCookieHeader.contains("application="));
        assertTrue(setCookieHeader.contains("application="));
        assertTrue(setCookieHeader.contains("; path=/; domain=.rocket-fuel;"));

    }

    @Test
    public void shouldUpdateExistingUserIfNewPictureIsProvided() {
        // given that the user already exists in rocket fuel
        User user = insertUser();

        // and that the user has changed profile picture since last visit
        ImmutableOpenIdToken openId = new ImmutableOpenIdToken(user.getName(), user.getEmail(), "new_picture.png");
        when(openIdValidator.validate(any())).thenReturn(just(openId));

        // when the user signs in
        User returnedUser = userResource.generateToken(OPEN_ID_TOKEN).toBlocking().singleOrDefault(null);

        // then the user returned shall have the updated picture url
        assertThat(returnedUser.getPicture()).isEqualTo(openId.picture);

        // and the user in the database shall be updated with the new picture url
        User storedUser = userResource.getUserById(returnedUser.getId()).toBlocking().single();
        assertThat(storedUser.getPicture()).isEqualTo(openId.picture);
    }

    @Test
    public void shouldDefaultToDefaultImageIfUserGotNoImage() {
        // given that the user already exists in rocket fuel
        User user = insertUser();

        // and that the user has removed the profile picture from last visit
        ImmutableOpenIdToken openId = new ImmutableOpenIdToken(user.getName(), user.getEmail(), null);
        when(openIdValidator.validate(any())).thenReturn(just(openId));

        // when the user signs in
        User returnedUser = userResource.generateToken(OPEN_ID_TOKEN).toBlocking().singleOrDefault(null);

        // then the user returned shall have the updated picture url
        assertThat(returnedUser.getPicture()).isEqualTo(DEFAULT_PICTURE_URL);

        // and the user in the database shall be updated with the new picture url
        User storedUser = userResource.getUserById(returnedUser.getId()).toBlocking().single();
        assertThat(storedUser.getPicture()).isEqualTo(DEFAULT_PICTURE_URL);
    }

    @Test
    public void shouldUpdateExistingUserIfNewNameIsProvided() {
        // given that the user already exists in rocket fuel
        User user = insertUser();

        // and that the user has changed name since last visit
        ImmutableOpenIdToken openId = new ImmutableOpenIdToken("Arnold", user.getEmail(), user.getPicture());
        when(openIdValidator.validate(any())).thenReturn(just(openId));

        // when the user signs in
        User returnedUser = userResource.generateToken(OPEN_ID_TOKEN).toBlocking().singleOrDefault(null);

        // then the user returned shall have the updated name
        assertThat(returnedUser.getName()).isEqualTo(openId.name);

        // and the user in the database shall be updated with the new name
        User storedUser = userResource.getUserById(returnedUser.getId()).toBlocking().single();
        assertThat(storedUser.getName()).isEqualTo(openId.name);
    }

    @Test
    public void shouldThrowInternalServerErrorIfUpdateOfUserFails() {
        // given that the user already exists in rocket fuel
        User user = insertUser();
        UserDao userDaoMock = mock(UserDao.class);
        when(userDaoMock.getUserByEmail(anyString())).thenReturn(just(user));

        // and we will fail to update the user
        when(userDaoMock.updateUser(any(), anyString(), anyString())).thenReturn(error(new SQLException()));

        // and we make sure that we will not be able to insert a new user to db
        rx.Observable<Integer> obs = just(1).doOnSubscribe(() -> fail("should not be called within this test"));
        when(userDaoMock.insertUser(any())).thenReturn(obs);

        UserResource userResource = new UserResourceImpl(userDaoMock, responseHeaderHolder, openIdValidator, applicationTokenCreator, applicationTokenConfig);

        // and that the user has changed name since last visit
        ImmutableOpenIdToken openId = new ImmutableOpenIdToken("Arnold", user.getEmail(), user.getPicture());
        when(openIdValidator.validate(any())).thenReturn(just(openId));

        // when the user shall update name
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> userResource.generateToken(OPEN_ID_TOKEN).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                // then a internal server error is returned
                assertEquals(INTERNAL_SERVER_ERROR, e.getStatus());
                assertEquals(FAILED_TO_UPDATE_USER_NAME_OR_PICTURE, e.getError());
            });

    }

    @Test
    public void shouldFetchUserByEmail() {
        // given that the user exists in rocket-fuel
        User user = insertUser();

        // when we try to find the user by mail
        User foundUser = userResource.getUserByEmail(user.getEmail(), false).toBlocking().singleOrDefault(null);

        // then we shall get the user
        assertNotNull(foundUser);
        assertEquals(user.getEmail(), foundUser.getEmail());
        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getPicture(), foundUser.getPicture());
    }

    @Test
    public void shouldFetchUserById() {
        // given
        User user = insertUser();

        // when
        User foundUser = userResource.getUserById(user.getId()).toBlocking().singleOrDefault(null);

        // then
        assertNotNull(foundUser);
        assertEquals(user.getEmail(), foundUser.getEmail());
        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getPicture(), foundUser.getPicture());
    }

    @Test
    public void shouldReturnNotFoundIfUserDoesNotExistWhenSearchingById() {
        try {
            userResource.getUserById(1234).toBlocking().singleOrDefault(null);
            fail("expected exception");
        } catch (WebException e) {
            assertEquals(HttpResponseStatus.NOT_FOUND, e.getStatus());
        }
    }

    @Test
    public void shouldReturnNotFoundIfUserDoesNotExistWhenSearchingByEmail() {
        try {
            userResource.getUserByEmail("random@email.com", false).toBlocking().singleOrDefault(null);
            fail("expected exception");
        } catch (WebException e) {
            assertEquals(HttpResponseStatus.NOT_FOUND, e.getStatus());
        }
    }

    @Test
    public void shouldReturnInternalServerErrorIfUserInAuthCannotBeFound() {
        try {
            Auth auth = new Auth();
            auth.setUserId(33);
            userResource.getCurrent(auth).toBlocking().singleOrDefault(null);
            fail("expected exception");
        } catch (WebException e) {
            assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getStatus());
            assertEquals("current.user.not.found", e.getError());
        }

    }

    private User insertUser() {
        return insertUser("Test Subject", "picture");
    }

    private User insertUser(String name, String picture) {
        final String generatedEmail = UUID.randomUUID().toString() + "@fortnox.se";
        User user = new User();
        user.setEmail(generatedEmail);
        user.setName(name);
        user.setPicture(picture);
        userResource.createUser(null, user).toBlocking().single();
        return userResource.getUserByEmail(generatedEmail, false).toBlocking().single();
    }
}
