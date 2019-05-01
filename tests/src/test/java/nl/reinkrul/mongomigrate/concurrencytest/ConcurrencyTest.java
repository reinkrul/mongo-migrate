package nl.reinkrul.mongomigrate.concurrencytest;

import com.mongodb.client.MongoDatabase;

import nl.reinkrul.mongomigrate.Migration;
import nl.reinkrul.mongomigrate.MigrationException;
import nl.reinkrul.mongomigrate.MongoMigrate;
import nl.reinkrul.mongomigrate.TestBase;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

public class ConcurrencyTest extends TestBase {

    private static boolean isExecuting;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        isExecuting = false;
    }

    @Test
    public void test() throws InterruptedException {
        final int threadCount = 5;
        final Thread[] threads = new Thread[threadCount];

        // Setup
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(this::run);
        }

        // Start threads
        for (final Thread thread : threads) {
            thread.start();
        }

        // Wait for threads to finish
        for (final Thread thread : threads) {
            thread.join();
        }
    }

    private void run() {
        try {
            new MongoMigrate("mongodb://" + mongoHost + ":" + mongoPort).migrate(getClass().getPackageName(), "local");
        } catch (MigrationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Migration(1)
    public void doSomething(final MongoDatabase database) {
        if (isExecuting) {
            fail("Parallel execution of migration detected!");
        }
        isExecuting = true;
        database.getCollection("users").insertOne(new Document("firstName", "John"));
        isExecuting = false;
    }
}
