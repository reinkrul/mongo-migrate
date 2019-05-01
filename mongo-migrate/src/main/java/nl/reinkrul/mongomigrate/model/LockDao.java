package nl.reinkrul.mongomigrate.model;

import org.bson.Document;

import static nl.reinkrul.mongomigrate.Util.checkNotNull;

public class LockDao {

    public static LockDao unlocked() {
        return new LockDao();
    }

    public static LockDao locked(final long lockDate, final String hostname) {
        return new LockDao(lockDate, hostname);
    }

    private final String key = "lock";
    private Long lockDate;
    private String hostname;

    public LockDao() {
        this.lockDate = null;
        this.hostname = null;
    }

    public LockDao(final Long lockDate, final String hostname) {
        this.lockDate = checkNotNull(lockDate);
        this.hostname = checkNotNull(hostname);
    }

    public Document doc() {
        final Document document = new Document();
        document.put("key", key);
        document.put("lockDate", lockDate);
        document.put("hostname", hostname);
        return document;
    }
}
