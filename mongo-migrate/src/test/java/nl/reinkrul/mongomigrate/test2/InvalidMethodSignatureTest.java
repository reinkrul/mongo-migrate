package nl.reinkrul.mongomigrate.test2;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import nl.reinkrul.mongomigrate.Migration;
import nl.reinkrul.mongomigrate.MigrationException;
import nl.reinkrul.mongomigrate.MongoMigrate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InvalidMethodSignatureTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void migrate() throws MigrationException {
        exception.expect(MigrationException.class);
        exception.expectMessage("Invalid method signature for migration");

        final MongoDatabase database = mock(MongoDatabase.class);

        final MongoClient client = mock(MongoClient.class);
        when(client.getDatabase("test")).thenReturn(database);

        final MongoMigrate migrate = new MongoMigrate(client);
        migrate.migrate(getClass().getPackageName(), "test");
    }

    public static class TestContainer {

        @Migration(1)
        public void step(final String randomParameter) {

        }
    }
}