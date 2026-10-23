package com.medsentinel.config;

public class DatabaseBootstrap {

    public void run() {
        MongoDBManager.getInstance().bootstrapIfNeeded();
    }
}
