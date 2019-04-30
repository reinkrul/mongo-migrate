package nl.reinkrul.mongomigrate;

import com.mongodb.client.MongoDatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static nl.reinkrul.mongomigrate.Util.checkNotNull;

public class MigrationExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(MigrationExecutor.class);

    private final MongoDatabase database;

    public MigrationExecutor(final MongoDatabase database) {
        this.database = checkNotNull(database);
    }

    public void execute(final List<ExecutableMigration> migrations) throws MigrationException {
        checkNotNull(migrations);
        for (final ExecutableMigration migration : migrations) {
            execute(migration);
        }
    }

    private void execute(final ExecutableMigration migration) throws MigrationException {
        LOG.info("Migrating {}", migration);
        try {
            if (migration.getMethod().getParameterCount() == 0) {
                migration.getMethod().invoke(migration.getInstance());
            } else {
                migration.getMethod().invoke(migration.getInstance(), database);
            }
        } catch (final IllegalAccessException e) {
            throw new MigrationException("Migration failed: " + migration, e);
        } catch (final InvocationTargetException e) {
            throw new MigrationException("Migration failed: " + migration, e.getTargetException());
        }
    }
}
