package com.acme.helloworld.migrations;

import nl.reinkrul.mongomigrate.MigrationException;
import nl.reinkrul.mongomigrate.MongoMigrate;

public class App {

    public static void main(String... args) throws MigrationException {
        new MongoMigrate("mongodb://localhost:1234").migrate(Migrations.class.getPackageName(), "acme");
        // Do application stuff
    }
}
