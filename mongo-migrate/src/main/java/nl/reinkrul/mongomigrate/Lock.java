package nl.reinkrul.mongomigrate;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReturnDocument;

import nl.reinkrul.mongomigrate.model.LockDao;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static nl.reinkrul.mongomigrate.Util.checkNotNull;

public class Lock {

    private static final int LOCK_ACQUIRE_ATTEMPT_INTERVAL = 500;
    private static final int MONGO_ERROR_DUPLICATE_KEY = 11000;
    private static final Logger LOG = LoggerFactory.getLogger(Lock.class);

    private MongoCollection<Document> collection;
    private Document lockDocument;

    public boolean acquire(final MongoDatabase database, int timeout) throws InterruptedException {
        checkNotNull(database);

        collection = database.getCollection(MongoMigrate.COLLECTION_NAME);
        initializeLock();

        LOG.info("Acquiring lock");
        final long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout) {
            lockDocument = tryAttemptAcquire();
            if (lockDocument != null) {
                LOG.info("Lock acquired.");
                return true;
            }
            Thread.sleep(LOCK_ACQUIRE_ATTEMPT_INTERVAL);
        }
        return false;
    }

    public void release() {
        LOG.info("Releasing lock");
        final Document result = collection.findOneAndReplace(lockDocument, LockDao.unlocked().doc());
        if (result == null) {
            LOG.error("Could not release lock: possible race condition triggered?");
        }
    }

    private void initializeLock() {
        final long locks = collection.countDocuments(new Document("key", "lock"));
        if (locks == 0) {
            LOG.info("First use of Mongo Migrate on this database, initializing...");
            collection.createIndex(Indexes.ascending("key"), new IndexOptions().unique(true));
            try {
                collection.insertOne(new LockDao().doc());
            } catch (final MongoWriteException e) {
                if (e.getError().getCode() == MONGO_ERROR_DUPLICATE_KEY) {
                    LOG.debug("Got Mongo error 11000 (duplicate key), this means a concurrent process tried to initialize the lock at the same time. This is not an issue.");
                } else {
                    throw e;
                }
            }
        }
    }

    private Document tryAttemptAcquire() {
        final String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
        return collection.findOneAndReplace(LockDao.unlocked().doc(), LockDao.locked(System.currentTimeMillis(), hostName).doc(),
                new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER));
    }
}
