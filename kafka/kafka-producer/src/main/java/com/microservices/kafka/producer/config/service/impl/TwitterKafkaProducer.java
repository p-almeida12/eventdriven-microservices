package com.microservices.kafka.producer.config.service.impl;

import com.microservices.kafka.avro.model.TwitterAvroModel;
import com.microservices.kafka.producer.config.service.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PreDestroy;

/**
 * TwitterKafkaProducer is a Kafka producer service that sends TwitterAvroModel messages
 * to a specified Kafka topic. It leverages Spring's KafkaTemplate for producing messages
 * and registers callback methods to handle success and failure cases.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterKafkaProducer implements KafkaProducer<Long, TwitterAvroModel> {

    private final KafkaTemplate<Long, TwitterAvroModel> kafkaTemplate;

    /**
     * Sends a TwitterAvroModel message to the specified Kafka topic.
     * Logs the sending process and attaches callbacks for success and failure handling.
     *
     * @param topicName the name of the Kafka topic to send the message to.
     * @param key       the key for the Kafka message, typically used for partitioning.
     * @param message   the TwitterAvroModel message to be sent.
     */
    @Override
    public void send(String topicName, Long key, TwitterAvroModel message) {
        log.info("Sending message='{}' to topic='{}'", message, topicName);

        //register callback methods for handling events when the response return
        ListenableFuture<SendResult<Long, TwitterAvroModel>> kafkaResultFuture =
                kafkaTemplate.send(topicName, key, message);
        addCallback(topicName, message, kafkaResultFuture);
    }

    /**
     * Cleans up resources and closes the Kafka producer upon bean destruction.
     * Checks if KafkaTemplate is non-null and then logs the closing action before destroying.
     */
    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            log.info("Closing kafka producer...");
            kafkaTemplate.destroy();
        }
    }

    /**
     * Registers callbacks for handling the success and failure scenarios for the Kafka message send operation.
     * In case of failure, logs an error with the details of the throwable.
     * On success, logs detailed metadata including the topic, partition, offset, timestamp, and the nanosecond timestamp.
     *
     * @param topicName         the name of the Kafka topic.
     * @param message           the TwitterAvroModel message being sent.
     * @param kafkaResultFuture the future result of the send operation.
     */
    private void addCallback(String topicName, TwitterAvroModel message,
                             ListenableFuture<SendResult<Long, TwitterAvroModel>> kafkaResultFuture) {
        kafkaResultFuture.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.error("Error while sending message {} to topic {}", message.toString(), topicName, throwable);
            }

            @Override
            public void onSuccess(SendResult<Long, TwitterAvroModel> result) {
                RecordMetadata metadata = result.getRecordMetadata();
                log.debug("Received new metadata. Topic: {}; Partition {}; Offset {}; Timestamp {}, at time {}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp(),
                        System.nanoTime());
            }
        });
    }

}
