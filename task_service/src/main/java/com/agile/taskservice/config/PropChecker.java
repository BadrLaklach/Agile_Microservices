package com.agile.taskservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class PropChecker {

    @Value("${spring.data.mongodb.uri:NOT_FOUND}")
    private String mongoUri;

    @Value("${spring.data.mongodb.host:NOT_FOUND}")
    private String mongoHost;

    @PostConstruct
    public void printProps() {
        System.out.println("======================================");
        System.out.println("MONGO URI: " + mongoUri);
        System.out.println("MONGO HOST: " + mongoHost);
        System.out.println("======================================");
    }
}
