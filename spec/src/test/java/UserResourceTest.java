import api.User;
import api.UserResource;
import api.auth.Auth;
import auth.application.ApplicationTokenConfig;
import auth.openid.ImmutableOpenIdToken;
import auth.openid.OpenIdValidator;
import dao.UserDao;
import dates.DateProvider;
import dates.DateProviderImpl;
import impl.ApplicationTokenCreator;
import impl.ResponseHeaderHolder;
import impl.UserResourceImpl;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.testcontainers.containers.PostgreSQLContainer;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
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
        ImmutableOpenIdToken openIdObject = new ImmutableOpenIdToken(user.getName(), user.getEmail(), "pictureUrl");
        when(openIdValidator.validate(any())).thenReturn(just(openIdObject));

        // when
        User returnedUser = userResource.generateToken(OPEN_ID_TOKEN).toBlocking().singleOrDefault(null);

        // then
        assertNotNull(returnedUser);
        User insertedUser = userResource.getUserByEmail(user.getEmail(), false).toBlocking().singleOrDefault(null);
        assertEquals(user.getName(), insertedUser.getName());
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
    public void shouldFetchUserByEmail() {
        // given
        User user = insertUser();

        // when
        User foundUser = userResource.getUserByEmail(user.getEmail(), false).toBlocking().singleOrDefault(null);

        // then
        assertNotNull(foundUser);
        assertEquals(user.getEmail(), foundUser.getEmail());
        assertEquals(user.getId(), foundUser.getId());
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
    }

    @Test
    public void shouldReturnNotFoundIfUserDoesNotExistWhenSearchingById() {

        // when
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
            assertEquals("current user not found", e.getError());
        }

    }

    private User insertUser() {
        final String generatedEmail = UUID.randomUUID().toString() + "@fortnox.se";
        User user = new User();
        user.setEmail(generatedEmail);
        user.setName("Test Subject");
        userResource.createUser(null, user).toBlocking().single();
        return userResource.getUserByEmail(generatedEmail, false).toBlocking().single();
    }
}
