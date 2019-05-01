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
