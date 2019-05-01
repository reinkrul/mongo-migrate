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
