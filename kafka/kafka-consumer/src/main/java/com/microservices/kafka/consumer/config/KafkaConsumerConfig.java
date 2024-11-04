package com.microservices.kafka.consumer.config;

import com.microservices.config.KafkaConfigData;
import com.microservices.config.KafkaConsumerConfigData;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for Kafka consumer properties, enabling batch and concurrent processing
 * for microservices that consume Kafka messages using Avro serialization.
 * <p>
 * This configuration is annotated with {@link EnableKafka} to enable Kafka listeners in the application.
 * The consumer properties are configured via {@link KafkaConfigData} and {@link KafkaConsumerConfigData} beans.
 *
 * @param <K> Key type of Kafka messages, extending {@link Serializable}.
 * @param <V> Value type of Kafka messages, extending {@link SpecificRecordBase}.
 */
@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig<K extends Serializable, V extends SpecificRecordBase> {

    private final KafkaConfigData kafkaConfigData;
    private final KafkaConsumerConfigData kafkaConsumerConfigData;

    /**
     * Configures the properties for Kafka consumer, specifying settings such as deserializers, group ID,
     * session timeout, heartbeat interval, maximum fetch size, and other consumer properties.
     *
     * @return A map of Kafka consumer configuration properties.
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        //set the bootstrap servers for Kafka, retrieved from configuration data
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigData.getBootstrapServers());
        //configure the key deserializer class for the consumer
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaConsumerConfigData.getKeyDeserializer());
        //configure the value deserializer class for Avro deserialization
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConsumerConfigData.getValueDeserializer());
        //set the consumer group ID for Kafka consumer
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerConfigData.getConsumerGroupId());
        //define the behavior for handling offsets if no offset is present (e.g., "earliest" or "latest")
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerConfigData.getAutoOffsetReset());
        //specify the schema registry URL for Avro deserialization
        props.put(kafkaConfigData.getSchemaRegistryUrlKey(), kafkaConfigData.getSchemaRegistryUrl());
        //enable specific Avro reader for handling Avro messages
        props.put(kafkaConsumerConfigData.getSpecificAvroReaderKey(), kafkaConsumerConfigData.getSpecificAvroReader());
        //set the session timeout, defining how long the consumer can be idle before considered failed
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaConsumerConfigData.getSessionTimeoutMs());
        //define the interval for sending heartbeats to the broker to maintain active consumer status
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaConsumerConfigData.getHeartbeatIntervalMs());
        //configure the maximum interval between polls before the consumer is considered dead
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaConsumerConfigData.getMaxPollIntervalMs());
        //set the maximum data size to fetch from each partition, with a boost factor applied
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
                kafkaConsumerConfigData.getMaxPartitionFetchBytesDefault() *
                        kafkaConsumerConfigData.getMaxPartitionFetchBytesBoostFactor());
        //specify the maximum number of records returned in a single poll
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConsumerConfigData.getMaxPollRecords());
        return props;
    }

    /**
     * Configures and provides a Kafka {@link ConsumerFactory} for generating Kafka consumers with the specified
     * configuration properties.
     *
     * @return The {@link ConsumerFactory} for Kafka consumers.
     */
    @Bean
    public ConsumerFactory<K, V> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * Configures and provides a Kafka listener container factory for managing Kafka listener concurrency,
     * batch processing, and other container properties.
     *
     * @return The {@link KafkaListenerContainerFactory} for handling Kafka listeners.
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<K, V>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(kafkaConsumerConfigData.getBatchListener()); //get data from kafka as batches, not one by one
        factory.setConcurrency(kafkaConsumerConfigData.getConcurrencyLevel()); //spring will create
        factory.setAutoStartup(kafkaConsumerConfigData.getAutoStartup());
        factory.getContainerProperties().setPollTimeout(kafkaConsumerConfigData.getPollTimeoutMs());
        return factory;


    }
}
