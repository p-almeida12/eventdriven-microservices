package com.microservices.twitter.to.kafka.service;

import com.microservices.config.TwitterToKafkaServiceConfigData;
import com.microservices.twitter.to.kafka.service.runner.StreamRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import twitter4j.TwitterException;

import java.util.Arrays;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
@ComponentScan(basePackages = "com.microservices")
public class TwitterToKafkaServiceApplication implements CommandLineRunner {

    private final StreamRunner streamRunner;
    private final TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData;

    public static void main(String[] args) {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws TwitterException {
        log.info("TwitterToKafkaServiceApplication started...");
        log.info(Arrays.toString(twitterToKafkaServiceConfigData.getTwitterKeywords().toArray(new String[] {})));
        log.info(twitterToKafkaServiceConfigData.getWelcomeMessage());
        streamRunner.start();
    }

}
