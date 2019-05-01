package nl.reinkrul.mongomigrate;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.reinkrul.mongomigrate.Util.checkNotNull;

public class MongoMigrate {

    public static final String COLLECTION_NAME = "mongomigrate";
    private static final Logger LOG = LoggerFactory.getLogger(MongoMigrate.class);

    private final MongoClient providedClient;
    private final MongoClientURI uri;

    public MongoMigrate(final MongoClient providedClient) {
        this.providedClient = checkNotNull(providedClient);
        this.uri = null;
    }

    public MongoMigrate(final MongoClientURI uri) {
        this.uri = checkNotNull(uri);
        this.providedClient = null;
    }

    public MongoMigrate(final String uri) {
        this(new MongoClientURI(checkNotNull(uri)));
    }

    public void migrate(final String searchPackage, final String database) throws MigrationException {
        checkNotNull(searchPackage);

        if (database == null && (uri == null || uri.getDatabase() == null)) {
            throw new MigrationException("Database name should either be supplied to migrate() or in Mongo URI.");
        }

        final Set<Method> methods = new Reflections(new ConfigurationBuilder()
                .forPackages(searchPackage)
                // A bug causes classes outside the searchPackage to be included, so we add an additional filter here.
                // See https://github.com/ronmamo/reflections/issues/245
                .filterInputsBy(new FilterBuilder().includePackage(searchPackage))
                .setScanners(new MethodAnnotationsScanner())).getMethodsAnnotatedWith(Migration.class);
        final List<ExecutableMigration> migrations = new ArrayList<>();

        final Map<Class<?>, Object> instances = new HashMap<>();
        for (final Method method : methods) {
            migrations.add(createMigration(instances, method));
        }

        if (migrations.isEmpty()) {
            LOG.error("No migrations found.");
            return;
        }

        if (!areMigrationsUnique(migrations)) {
            throw new MigrationException("Migrations aren't uniquely numbered.");
        }

        Collections.sort(migrations);

        LOG.info("Connecting to Mongo database...");
        if (uri != null) {
            try (final MongoClient client = connect()) {
                doMigrate(database, migrations, client);
            }
        } else {
            doMigrate(database, migrations, providedClient);
        }
    }

    private void doMigrate(final String databaseName, final List<ExecutableMigration> migrations, final MongoClient mongoClient) throws MigrationException {
        final MongoDatabase database = mongoClient.getDatabase(databaseName == null ? uri.getDatabase() : databaseName);

        final Lock lock = new Lock();
        try {
            if (lock.acquire(database, 10000)) {
                try {
                    new MigrationExecutor(database).execute(migrations);
                } finally {
                    lock.release();
                }
            } else {
                throw new MigrationException("Timeout while acquiring lock.");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MigrationException("Interrupted while acquiring lock.", e);
        }
    }

    public void migrate(final String searchPackage) throws MigrationException {
        migrate(searchPackage, null);
    }

    private MongoClient connect() {
        return new MongoClient(uri);
    }

    private ExecutableMigration createMigration(final Map<Class<?>, Object> instances, final Method method) throws MigrationException {
        final Object instance;
        if (Modifier.isStatic(method.getModifiers())) {
            instance = null;
        } else {
            if (instances.containsKey(method.getDeclaringClass())) {
                instance = instances.get(method.getDeclaringClass());
            } else {
                instance = createInstance(method.getDeclaringClass());
                instances.put(method.getDeclaringClass(), instance);
            }
        }

        final Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length != 0 && !(parameters.length == 1 && parameters[0].equals(MongoDatabase.class))) {
            throw new MigrationException("Invalid method signature for migration: " + method);
        }

        return new ExecutableMigration(method, instance, method.getAnnotation(Migration.class));
    }

    private boolean areMigrationsUnique(final List<ExecutableMigration> migrations) {
        return migrations.stream().map(m -> m.getMigration().value()).collect(Collectors.toSet()).size() == migrations.size();
    }

    private Object createInstance(final Class<?> clazz) throws MigrationException {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new MigrationException("Declaring class of method has no default constructor: " + clazz.getName(), e);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new MigrationException("Unable to instantiate migration class: " + clazz.getName(), e);
        }
    }
}
