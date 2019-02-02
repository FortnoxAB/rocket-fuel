import api.QuestionResource;
import api.User;
import api.UserResource;
import api.auth.ApplicationToken;
import auth.application.ApplicationTokenConfig;
import auth.openid.ImmutableOpenIdToken;
import auth.openid.OpenIdValidator;
import dao.UserDao;
import dates.DateProvider;
import dates.DateProviderImpl;
import impl.ApplicationTokenCreator;
import impl.ResponseHeaderHolder;
import impl.ResponseHeadersTransformerFactory;
import impl.UserResourceImpl;
import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserResourceTest {
    private static final String OPEN_ID_TOKEN = "eyJhbGciOiJIUzI1NiIsImtpZCI6ImIxNWEyYjhmN2E2YjNmNmJjMDhiYzFjNTZhODg0MTBlMTQ2ZDAxZmQiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiamVwcDMiLCJwaWN0dXJlIjoidXJsdG9waWN0dXJlIiwiZW1haWwiOiJqZXNwZXIubGFoZGV2aXJ0YUBnbWFpbC5jb20iLCJpYXQiOjE1NDg0MTc2NDcsImV4cCI6MTY0ODQyMTI0N30.WH70YBPaMFg1QtTaZddikcslsN2C5sxm4oQSOVIt_PU";
    private static QuestionResource questionResource;
    private static UserResource userResource;
    private static OpenIdValidator openIdValidator;


    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup testSetup;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        UserDao userDao = testSetup.getInjector().getInstance(UserDao.class);
        ResponseHeaderHolder responseHeaderHolder = new ResponseHeadersTransformerFactory();
        openIdValidator = mock(OpenIdValidator.class);
        ApplicationTokenConfig applicationTokenConfig = new ApplicationTokenConfig();
        applicationTokenConfig.setSecret("my-test-secret-that-is-valid");
        DateProvider dateProvider = new DateProviderImpl();
        ApplicationTokenCreator applicationTokenCreator = new ApplicationTokenCreator(applicationTokenConfig, dateProvider);
        userResource = new UserResourceImpl(userDao, responseHeaderHolder, openIdValidator, applicationTokenCreator);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
    }

    @After
    public void afterEach() throws Exception {
        testSetup.clearDatabase();
    }

    @Before
    public void beforeEach() throws Exception {
        testSetup.setupDatabase();
    }


    @Test
    public void shouldCreateUserInDatabaseIfFirstTimeLogin() {
        ImmutableOpenIdToken openIdObject = new ImmutableOpenIdToken("jeppe", "jeppe@email.com", "pictureUrl");
        when(openIdValidator.validate(any())).thenReturn(openIdObject);
        ApplicationToken applicationToken = userResource.generateToken(OPEN_ID_TOKEN).toBlocking().singleOrDefault(null);

        assertNotNull(applicationToken);

        User insertedUser = userResource.getUserByEmail("jeppe@email.com").toBlocking().singleOrDefault(null);
        assertEquals("jeppe", insertedUser.getName());
    }

    @Test
    public void shouldFetchExistingUserInDatabaseIfReturningUser() {

        User user = insertUser();
        ImmutableOpenIdToken openIdObject = new ImmutableOpenIdToken(user.getName(), user.getEmail(), "pictureUrl");
        when(openIdValidator.validate(any())).thenReturn(openIdObject);
        ApplicationToken applicationToken = userResource.generateToken(OPEN_ID_TOKEN).toBlocking().singleOrDefault(null);

        assertNotNull(applicationToken);

        User insertedUser = userResource.getUserByEmail(user.getEmail()).toBlocking().singleOrDefault(null);
        assertEquals(user.getName(), insertedUser.getName());
    }

    private User insertUser() {
        final String generatedEmail = UUID.randomUUID().toString() + "@fortnox.se";
        User user = new User();
        user.setEmail(generatedEmail);
        user.setName("Test Subject");
        userResource.createUser(null, user).toBlocking().single();
        return userResource.getUserByEmail(generatedEmail).toBlocking().single();
    }
}
