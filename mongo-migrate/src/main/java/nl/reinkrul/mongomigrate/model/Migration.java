package nl.reinkrul.mongomigrate.model;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import static nl.reinkrul.mongomigrate.Util.checkNotNull;

public class Migration {

    @BsonProperty("id")
    private final int id;

    @BsonProperty("description")
    private final String description;

    @BsonProperty("timeStarted")
    private long timeStarted;

    @BsonProperty("timeFinished")
    private long timeFinished;

    @BsonProperty("executed")
    private boolean executed;

    @BsonCreator
    public Migration(@BsonProperty("id") final int id,
                     @BsonProperty("description") final String description,
                     @BsonProperty("timeStarted") final long timeStarted,
                     @BsonProperty("timeFinished") final long timeFinished,
                     @BsonProperty("executed") final boolean executed) {
        this.id = checkNotNull(id);
        this.description = description;
        this.timeStarted = timeStarted;
        this.timeFinished = timeFinished;
        this.executed = executed;
    }
}
