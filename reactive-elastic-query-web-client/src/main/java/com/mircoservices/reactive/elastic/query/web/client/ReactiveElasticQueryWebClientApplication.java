package com.mircoservices.reactive.elastic.query.web.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.microservices"})
public class ReactiveElasticQueryWebClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReactiveElasticQueryWebClientApplication.class, args);
    }
}
