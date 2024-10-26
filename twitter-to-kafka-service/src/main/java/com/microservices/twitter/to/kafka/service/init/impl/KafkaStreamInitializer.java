package com.microservices.twitter.to.kafka.service.init.impl;

import com.microservices.config.KafkaConfigData;
import com.microservices.kafka.admin.client.KafkaAdminClient;
import com.microservices.twitter.to.kafka.service.init.StreamInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * KafkaStreamInitializer is responsible for initializing Kafka streams by ensuring that
 * topics are created and the schema registry is available before the application processes data.
 * This class implements the StreamInitializer interface to provide an initialization method.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaStreamInitializer implements StreamInitializer {

    private final KafkaConfigData kafkaConfigData;
    private final KafkaAdminClient kafkaAdminClient;

    /**
     * Initializes the Kafka stream by creating the necessary topics and verifying the schema registry availability.
     * Calls the KafkaAdminClient to create topics and check the schema registry status.
     * Logs a confirmation message indicating that the topics are ready once the initialization completes.
     */
    @Override
    public void init() {
        kafkaAdminClient.createTopics();
        kafkaAdminClient.checkSchemaRegistry();
        log.info("Topics {} ready ...", kafkaConfigData.getTopicNamesToCreate().toArray());
    }

}
