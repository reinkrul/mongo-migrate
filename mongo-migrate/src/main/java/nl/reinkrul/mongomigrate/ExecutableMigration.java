package nl.reinkrul.mongomigrate;

import java.lang.reflect.Method;

import static nl.reinkrul.mongomigrate.Util.checkNotNull;

public class ExecutableMigration implements Comparable<ExecutableMigration> {

    private final Method method;
    private final Object instance;
    private final Migration migration;

    public ExecutableMigration(final Method method, final Object instance, final Migration migration) {
        this.method = checkNotNull(method);
        this.instance = instance;
        this.migration = checkNotNull(migration);
    }

    public Method getMethod() {
        return method;
    }

    public Object getInstance() {
        return instance;
    }

    public Migration getMigration() {
        return migration;
    }

    @Override
    public int compareTo(final ExecutableMigration o) {
        checkNotNull(o);
        return Integer.compare(migration.value(), o.migration.value());
    }

    @Override
    public String toString() {
        return "[" + migration.value() + "] " + method.getDeclaringClass().getName() + "." + method.getName();
    }
}
