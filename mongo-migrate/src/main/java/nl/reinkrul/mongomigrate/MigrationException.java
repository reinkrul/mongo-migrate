package nl.reinkrul.mongomigrate;

public class MigrationException extends Exception {

    public MigrationException(final String message) {
        super(message);
    }

    public MigrationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
