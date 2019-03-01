import api.Question;
import api.User;
import api.UserResource;
import auth.JwkResource;
import auth.application.ApplicationTokenConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import se.fortnox.reactivewizard.binding.AutoBindModules;
import se.fortnox.reactivewizard.config.ConfigFactory;
import se.fortnox.reactivewizard.db.config.DatabaseConfig;
import se.fortnox.reactivewizard.dbmigrate.LiquibaseConfig;
import se.fortnox.reactivewizard.dbmigrate.LiquibaseMigrate;
import se.fortnox.reactivewizard.logging.LoggingFactory;
import se.fortnox.reactivewizard.server.ServerConfig;
import slack.SlackRTMClient;

import java.util.UUID;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TestSetup {

    private static Logger LOG = LoggerFactory.getLogger(TestSetup.class);
    private static final String FAKE_CONFIG = "config.yml";
    private final LiquibaseMigrate migrator;
    private final Injector injector;

    public TestSetup(PostgreSQLContainer postgreSQLContainer, Module mocks) {

        LOG.info("Postgres connection info: url: {}, user: {}, password: {}", postgreSQLContainer.getJdbcUrl(), postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword());

        LiquibaseConfig databaseConfig = TestSetup.setupDatabaseConfig(postgreSQLContainer);

        this.migrator = TestSetup.getMigrator(databaseConfig);

        this.injector = Guice.createInjector(new AutoBindModules(Modules.override(TestSetup.createGuiceModuleForReactiveWizard(TestSetup.createConfigFactory(databaseConfig)))
            .with(mocks, new AbstractModule() {
                @Override
                protected void configure() {
                    SlackRTMClient slackRTMClient = mock(SlackRTMClient.class);
                    binder().bind(SlackRTMClient.class).toInstance(slackRTMClient);
                    binder().bind(JwkResource.class).toProvider(() -> mock(JwkResource.class));
                }
            })));
    }

    public TestSetup(PostgreSQLContainer postgreSQLContainer) {

        this(postgreSQLContainer, new AbstractModule() {
            @Override
            protected void configure() {
            }
        });
    }

    public Injector getInjector() {
        return injector;
    }

    @NotNull
    private static Module createGuiceModuleForReactiveWizard(ConfigFactory configFactory) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                String[] args = new String[]{FAKE_CONFIG};

                bind(String[].class).annotatedWith(Names.named("args"))
                        .toInstance(args);
                ApplicationTokenConfig applicationTokenConfig = new ApplicationTokenConfig();
                applicationTokenConfig.setSecret("the-test-secret-used-during-spec");
                bind(ApplicationTokenConfig.class).toInstance(applicationTokenConfig);
                bind(ConfigFactory.class).toInstance(configFactory);
                ServerConfig serverConfig = configFactory.get(ServerConfig.class);
                serverConfig.setEnabled(false);
                bind(ServerConfig.class).toInstance(serverConfig);
            }
        };
    }

    @NotNull
    private static LiquibaseMigrate getMigrator(LiquibaseConfig databaseConfig) {
        try {
            return new LiquibaseMigrate(databaseConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static ConfigFactory createConfigFactory(LiquibaseConfig databaseConfig) {
        ConfigFactory configFactory = spy(new ConfigFactory((String) null));
        when(configFactory.get(DatabaseConfig.class)).thenReturn(databaseConfig);
        when(configFactory.get(LiquibaseConfig.class)).thenReturn(databaseConfig);
        configFactory.get(LoggingFactory.class).init();
        return configFactory;
    }

    @NotNull
    private static LiquibaseConfig setupDatabaseConfig(PostgreSQLContainer postgreSQLContainer) {
        LiquibaseConfig databaseConfig = new LiquibaseConfig();

        databaseConfig.setUrl(postgreSQLContainer.getJdbcUrl());
        databaseConfig.setUser(postgreSQLContainer.getUsername());
        databaseConfig.setPassword(postgreSQLContainer.getPassword());
        databaseConfig.setPoolSize(1);
        return databaseConfig;
    }


    public void setupDatabase() throws Exception {
        this.migrator.run();
    }

    public void clearDatabase() throws Exception {
        this.migrator.forceDrop();
    }

    @NotNull
    public static final Question getQuestion(String title, String question) {
        Question questionObject = new Question();
        questionObject.setAnswerAccepted(false);
        questionObject.setBounty(300);
        questionObject.setTitle(title);
        questionObject.setVotes(3);
        questionObject.setQuestion(question);
        return questionObject;
    }

    public static User insertUser(UserResource userResource) {
        final String generatedEmail = UUID.randomUUID().toString()+"@fortnox.se";
        User user = new User();
        user.setEmail(generatedEmail);
        user.setName("Test Subject");
        userResource.createUser(null, user).toBlocking().single();
        return userResource.getUserByEmail(generatedEmail, false).toBlocking().single();
    }
}
