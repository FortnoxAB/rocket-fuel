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

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.just;

public class UserResourceTest {
    private static final String OPEN_ID_TOKEN = "eyJhbGciOiJIUzI1NiIsImtpZCI6ImIxNWEyYjhmN2E2YjNmNmJjMDhiYzFjNTZhODg0MTBlMTQ2ZDAxZmQiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiamVwcDMiLCJwaWN0dXJlIjoidXJsdG9waWN0dXJlIiwiZW1haWwiOiJqZXNwZXIubGFoZGV2aXJ0YUBnbWFpbC5jb20iLCJpYXQiOjE1NDg0MTc2NDcsImV4cCI6MTY0ODQyMTI0N30.WH70YBPaMFg1QtTaZddikcslsN2C5sxm4oQSOVIt_PU";
    private static UserResource userResource;
    private static OpenIdValidator openIdValidator;
    private static ResponseHeaderHolder responseHeaderHolder;
    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup testSetup;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        UserDao userDao = testSetup.getInjector().getInstance(UserDao.class);
        responseHeaderHolder = mock(ResponseHeaderHolder.class);
        openIdValidator = mock(OpenIdValidator.class);
        ApplicationTokenConfig applicationTokenConfig = new ApplicationTokenConfig();
        applicationTokenConfig.setSecret("my-test-secret-that-is-valid");
        applicationTokenConfig.setDomain(".rocket-fuel");
        DateProvider dateProvider = new DateProviderImpl();
        ApplicationTokenCreator applicationTokenCreator = new ApplicationTokenCreator(applicationTokenConfig, dateProvider);

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
        User returnedUser = userResource.signIn(OPEN_ID_TOKEN).toBlocking().singleOrDefault(null);

        // then
        assertNotNull(returnedUser);
        User insertedUser = userResource.getUserByEmail("jeppe@email.com", false).toBlocking().singleOrDefault(null);
        assertEquals("jeppe", insertedUser.getName());
    }

    @Test
    public void shouldInvalidateCookieOnUserSignOut() {
        // given a valid auth
        Auth auth = new Auth();
        auth.setUserId(1234);

        // when the user sign out from rocket fuel
        long userId = userResource.signOut(auth).toBlocking().singleOrDefault(null);

        // then we shall invalidate the cookie by setting the cookie to an invalid value and set the cookie as expired
        ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(responseHeaderHolder, times(1)).addHeaders(any(), mapArgumentCaptor.capture());
        String setCookieHeader = (String) mapArgumentCaptor.getValue().get("Set-Cookie");
        String expectedCookie = "application=deleted; Path=/; Max-Age=0; Domain=.rocket-fuel; SameSite=Strict; HttpOnly; Secure;";
        assertThat(setCookieHeader).isEqualTo(expectedCookie);
        // and assert that user id is returned in the response to the client
        assertThat(userId).isEqualTo(1234);
    }

    @Test
    public void shouldFetchExistingUserInDatabaseIfReturningUser() {
        // given
        User user = insertUser();
        ImmutableOpenIdToken openIdObject = new ImmutableOpenIdToken(user.getName(), user.getEmail(), user.getPicture());
        when(openIdValidator.validate(any())).thenReturn(just(openIdObject));

        // when
        User returnedUser = userResource.signIn(OPEN_ID_TOKEN).toBlocking().singleOrDefault(null);

        // then
        assertNotNull(returnedUser);
        User insertedUser = userResource.getUserByEmail(user.getEmail(), false).toBlocking().singleOrDefault(null);
        assertEquals(user.getName(), insertedUser.getName());
        assertEquals(user.getPicture(), insertedUser.getPicture());
    }

    @Test
    public void shouldAddCookieToHeader() {
        // given a openId user
        User user = insertUser();
        ImmutableOpenIdToken openIdObject = new ImmutableOpenIdToken(user.getName(), user.getEmail(), "pictureUrl");
        when(openIdValidator.validate(any())).thenReturn(just(openIdObject));

        // when the user signs in.
        User returnedUser = userResource.signIn(OPEN_ID_TOKEN).toBlocking().singleOrDefault(null);

        // then a secure cookie should be created
        ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        assertNotNull(returnedUser);
        verify(responseHeaderHolder, times(1)).addHeaders(any(), mapArgumentCaptor.capture());
        String setCookieHeader = (String) mapArgumentCaptor.getValue().get("Set-Cookie");
        assertThat(setCookieHeader).contains("application=ey");
        assertThat(setCookieHeader).contains("Path=/;");
        assertThat(setCookieHeader).contains("Domain=.rocket-fuel;");
        assertThat(setCookieHeader).contains("Max-Age=3600;");
        assertThat(setCookieHeader).contains("HttpOnly;");
        assertThat(setCookieHeader).contains("Secure");
        assertThat(setCookieHeader).contains("SameSite=Strict;");
    }

    @Test
    public void shouldFetchUserByEmail() {
        // given
        User user = insertUser();

        // when
        User foundUser = userResource.getUserByEmail(user.getEmail(), false).toBlocking().singleOrDefault(null);

        // then
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
        final String generatedEmail = UUID.randomUUID().toString() + "@fortnox.se";
        User user = new User();
        user.setEmail(generatedEmail);
        user.setName("Test Subject");
        user.setPicture("picture.jpg");
        userResource.createUser(null, user).toBlocking().single();
        return userResource.getUserByEmail(generatedEmail, false).toBlocking().single();
    }
}
