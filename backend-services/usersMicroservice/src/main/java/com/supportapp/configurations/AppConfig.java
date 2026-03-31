package com.supportapp.configurations;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig  {

    @Value("${MONGO_USERS_HOST}")
    private String host;

    @Value("${MONGO_USERS_PORT}")
    private String port;

    @Value("${MONGO_USERS_DB}")
    private String db;

    @Value("${MONGO_USERS_USERNAME}")
    private String username;

    @Value("${MONGO_USERS_PASSWORD}")
    private String password;

    @Value("${MONGO_USERS_AUTH_SOURCE}")
    private String authSource;

    @Bean
    public MongoClient mongoClient() {
        String uri = String.format(
                "mongodb://%s:%s@%s:%s/%s?authSource=%s",
                username, password, host, port, db, authSource
        );
        return MongoClients.create(uri);
    }





}
