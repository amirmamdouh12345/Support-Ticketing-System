package com.itsupport.configurations;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableMongoAuditing
public class AppConfig {

    @Value("${MONGO_TICKETS_HOST}")
    private String host;

    @Value("${MONGO_TICKETS_PORT}")
    private String port;

    @Value("${MONGO_TICKETS_DB}")
    private String db;

    @Value("${MONGO_TICKETS_USERNAME}")
    private String username;

    @Value("${MONGO_TICKETS_PASSWORD}")
    private String password;

    @Value("${MONGO_TICKETS_AUTH_SOURCE}")
    private String authSource;

    @Bean
    public MongoClient mongoClient() {
        String uri = String.format(
                "mongodb://%s:%s@%s:%s/%s?authSource=%s",
                username, password, host, port, db, authSource
        );
        return MongoClients.create(uri);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


}
