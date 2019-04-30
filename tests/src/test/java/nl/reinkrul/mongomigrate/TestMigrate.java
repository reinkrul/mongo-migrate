package nl.reinkrul.mongomigrate;

import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

public class TestMigrate extends TestBase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void test() throws MigrationException {
        new MongoMigrate("mongodb://" + mongoHost + ":" + mongoPort).migrate(getClass().getPackageName(), "local");
    }

    @Migration(1)
    public void step1(final MongoDatabase database) {
        database.getCollection("users").insertOne(new Document("firstName", "John"));
    }

    @Migration(2)
    public void step2(final MongoDatabase database) {
        database.getCollection("users").updateMany(new Document(), new Document("$set", new Document("hasName", true)));
    }
}
