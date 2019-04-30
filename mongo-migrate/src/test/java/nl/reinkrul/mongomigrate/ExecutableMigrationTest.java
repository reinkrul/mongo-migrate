package nl.reinkrul.mongomigrate;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExecutableMigrationTest {

    @Test
    public void compareTo() {
        final Migration migrationAnnotation1 = mock(Migration.class);
        when(migrationAnnotation1.value()).thenReturn(100);
        final ExecutableMigration migration1 = new ExecutableMigration(getClass().getMethods()[0], null, migrationAnnotation1);

        final Migration migrationAnnotation2 = mock(Migration.class);
        when(migrationAnnotation2.value()).thenReturn(10);
        final ExecutableMigration migration2 = new ExecutableMigration(getClass().getMethods()[0], null, migrationAnnotation2);

        assertEquals(1, migration1.compareTo(migration2));
    }
}