package postgres;

import com.fortnox.reactivewizard.db.migrate.LiquibaseConfig;
import com.fortnox.reactivewizard.db.migrate.LiquibaseMigrate;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import liquibase.exception.LiquibaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import se.fortnox.reactivewizard.config.ConfigFactory;
import se.fortnox.reactivewizard.db.config.DatabaseConfig;
import se.fortnox.reactivewizard.server.ServerConfig;

import java.io.IOException;

public class PostgresFixture {
	private static Logger LOG = LoggerFactory.getLogger(PostgresFixture.class);
	private static PostgreSQLContainer postgreSQLContainer = null;
	private static AbstractModule staticOverrides;

	PostgresFixture() {
		if (postgreSQLContainer == null) {
			postgreSQLContainer = new PostgreSQLContainer();
		}
	}

	public static void startPostgres() {
		if (!postgreSQLContainer.isRunning()) {
			postgreSQLContainer.start();
			Runtime.getRuntime().addShutdownHook(new Thread(postgreSQLContainer::stop));
		}
	}

	protected void setupStaticOverridesAndMigratePostgresDatabase(String configFile) {
		if (staticOverrides == null) {
			ConfigFactory configFactory = new ConfigFactory(configFile);

			// Need to disable normal webserver
			ServerConfig serverConfig = configFactory.get(ServerConfig.class);
			serverConfig.setEnabled(false);
			staticOverrides = new AbstractModule() {
				@Override
				protected void configure() {
					bind(ConfigFactory.class).toInstance(configFactory);
					bind(String[].class).annotatedWith(Names.named("args")).toInstance(new String[]{configFile});
					bind(ServerConfig.class).toInstance(serverConfig);
				}
			};

			LiquibaseConfig databaseConfig = new LiquibaseConfig();

			databaseConfig.setUrl(postgreSQLContainer.getJdbcUrl());
			databaseConfig.setUser(postgreSQLContainer.getUsername());
			databaseConfig.setPassword(postgreSQLContainer.getPassword());
			databaseConfig.setPoolSize(1);
			LOG.info("Database url: {}, user: {}, password: {}", postgreSQLContainer.getJdbcUrl(), postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword());

			setupDatabaseModule(databaseConfig);

			migrateDatabase(databaseConfig);
		}
	}

	private void setupDatabaseModule(DatabaseConfig databaseConfig) {
		if (databaseConfig.getUrl() != null) {
			databaseConfig.setPoolSize(1);
		}
	}

	public void migrateDatabase(LiquibaseConfig liquibaseConfig) {
		try {

			if (liquibaseConfig.getUrl() != null) {
				LiquibaseMigrate migrator = new LiquibaseMigrate(liquibaseConfig);
				migrator.forceDrop();
				migrator.run();
			}

		} catch (LiquibaseException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}

