package com.microservices.twitter.to.kafka.service.transformer;

import com.microservices.kafka.avro.model.TwitterAvroModel;
import org.springframework.stereotype.Component;
import twitter4j.Status;

/**
 * TwitterStatusToAvroTransformer is responsible for transforming a Twitter Status object
 * into a TwitterAvroModel, which is used for Kafka messaging in Avro format.
 */
@Component
public class TwitterStatusToAvroTransformer {

    /**
     * Transforms a Twitter4J Status object into a TwitterAvroModel.
     * Maps fields such as the status ID, user ID, text, and creation timestamp.
     *
     * @param status the Twitter4J Status object containing Twitter status information.
     * @return a TwitterAvroModel populated with the corresponding fields from the Status object.
     */
    public TwitterAvroModel getTwitterAvroModelFromStatus(Status status) {
        return TwitterAvroModel
                .newBuilder()
                .setId(status.getId())
                .setUserId(status.getUser().getId())
                .setText(status.getText())
                .setCreatedAt(status.getCreatedAt().getTime())
                .build();
    }

}
