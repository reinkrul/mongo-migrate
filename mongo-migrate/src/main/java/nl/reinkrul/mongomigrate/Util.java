package nl.reinkrul.mongomigrate;

public final class Util {

    private Util() {
        // Util class
    }

    public static <T> T checkNotNull(final T obj) {
        if (obj == null) {
            throw new IllegalArgumentException("argument is null");
        }
        return obj;
    }
}
