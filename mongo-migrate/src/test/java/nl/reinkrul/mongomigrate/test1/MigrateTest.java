package nl.reinkrul.mongomigrate.test1;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import nl.reinkrul.mongomigrate.Migration;
import nl.reinkrul.mongomigrate.MigrationException;
import nl.reinkrul.mongomigrate.MongoMigrate;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MigrateTest {

    @Before
    public void setUp() throws Exception {
        TestContainer.reset();
    }

    @Test
    public void migrate() throws MigrationException {
        final MongoDatabase database = mock(MongoDatabase.class);

        final MongoClient client = mock(MongoClient.class);
        when(client.getDatabase("test")).thenReturn(database);

        final MongoMigrate migrate = new MongoMigrate(client);
        migrate.migrate(getClass().getPackageName(), "test");

        assertEquals(1, TestContainer.instances);
        assertEquals(4, TestContainer.calls.size());
        assertEquals("step1", TestContainer.calls.get(0));
        assertEquals("step2", TestContainer.calls.get(1));
        assertEquals("step3", TestContainer.calls.get(2));
        assertEquals("step4", TestContainer.calls.get(3));
    }

    public static class TestContainer {

        public static int instances;
        public static List<String> calls = new ArrayList<>();

        public static void reset() {
            instances = 0;
            calls.clear();
        }

        public TestContainer() {
            instances++;
        }

        @Migration(1)
        public void step1NoParameters() {
            calls.add("step1");
        }

        @Migration(2)
        public void step2DatabaseAsParameter(final MongoDatabase database) {
            assertNotNull(database);
            calls.add("step2");
        }

        @Migration(3)
        public static void step3_WhichIsStatic() {
            calls.add("step3");
        }

        @Migration(value = 4, description = "This is step 4")
        public static void step4WithDescription() {
            calls.add("step4");
        }
    }
}