# Mongo Migrate

[![CircleCI](https://circleci.com/gh/reinkrul/mongo-migrate.svg?style=svg)](https://circleci.com/gh/reinkrul/mongo-migrate)


This is a tool, inspired by [Flyway](https://flywaydb.org/) for updating your MongoDB databases in a safe, reproducible and versioned manner.

# Getting started
1. Add the mongo-migrate dependency to your (Maven) project:
```xml
<dependency>
    <groupId>nl.reinkrul.mongomigrate</groupId>
    <artifactId>mongo-migrate</artifactId>
    <version>(latest)</version>
</dependency>
```  
2. Find out which package you want your migrations to reside in. We'll be using **com.acme.helloworld.migrations**. Create a class which will contain all of our migrations:
```java
package com.acme.helloworld.migrations;

import com.mongodb.client.MongoDatabase;
import nl.reinkrul.mongomigrate.Migration;
import org.bson.Document;

public class Migrations {

    @Migration(1)
    public void step1(MongoDatabase database) {
        database.getCollection("worlds").insertOne(new Document("name", "Hello, World!"));
    }
}
```
3. Run mongo-migrate when your application starts (don't forget to configure the right Mongo URI and database):
```java
package com.acme.helloworld.migrations;

import nl.reinkrul.mongomigrate.MigrationException;
import nl.reinkrul.mongomigrate.MongoMigrate;

public class App {

    public static void main(String... args) throws MigrationException {
        new MongoMigrate("mongodb://localhost:1234").migrate(Migrations.class.getPackageName(), "acme");
        // Do application stuff
    }
}
```

## Spring Boot
```java
package com.acme.helloworld.migrations;

import com.mongodb.MongoClient;

import nl.reinkrul.mongomigrate.MigrationException;
import nl.reinkrul.mongomigrate.MongoMigrate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class HelloWorldApplication {

    public static void main(String... args) {
        SpringApplication.run(HelloWorldApplication.class, args);
    }

    @Autowired
    private MongoClient mongoClient;

    @PostConstruct
    public void migrate() throws MigrationException {
        new MongoMigrate(mongoClient).migrate(getClass().getPackageName(), "helloworld");
    }
}
```


## Maven
