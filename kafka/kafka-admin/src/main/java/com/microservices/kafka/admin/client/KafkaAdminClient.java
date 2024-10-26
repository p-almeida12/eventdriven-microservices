package com.microservices.kafka.admin.client;

import com.microservices.config.KafkaConfigData;
import com.microservices.config.RetryConfigData;
import com.microservices.kafka.admin.exception.KafkaClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * KafkaAdminClient is responsible for managing Kafka topics and checking the status of the schema registry.
 * It leverages Spring Retry to handle transient errors and retry Kafka operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAdminClient {

    private final KafkaConfigData kafkaConfigData;
    private final RetryConfigData retryConfigData;
    private final AdminClient adminClient;
    private final RetryTemplate retryTemplate;
    private final WebClient webClient;

    /**
     * Creates Kafka topics defined in the configuration.
     * This method uses a retry template to attempt topic creation in case of transient errors.
     * If the maximum number of retries is reached without success, a KafkaClientException is thrown.
     */
    public void createTopics() {
        CreateTopicsResult createTopicsResult;
        try {
            createTopicsResult = retryTemplate.execute(this::doCreateTopics);
            log.info("Create topic result {}", createTopicsResult.values().values());
        } catch (Throwable t) {
            throw new KafkaClientException("Max number of retries reached for creating topics.", t);
        }
        checkTopicsCreated();
    }

    /**
     * Verifies if the topics defined in the configuration have been successfully created.
     * This method repeatedly checks Kafka for the presence of each topic, applying an exponential backoff
     * delay between attempts and retrying up to a configured maximum limit.
     * Throw KafkaClientException if any topic is not found after exhausting retries.
     */
    public void checkTopicsCreated() {
        Collection<TopicListing> topics = getTopics();
        int retryCount = 1;
        Integer maxRetry = retryConfigData.getMaxAttempts();
        int multiplier = retryConfigData.getMultiplier().intValue();
        Long sleepTimeMs = retryConfigData.getSleepTimeMs();
        for (String topic : kafkaConfigData.getTopicNamesToCreate()) {
            while (!isTopicCreated(topics, topic)) {
                checkMaxRetry(retryCount++, maxRetry);
                sleep(sleepTimeMs);
                sleepTimeMs *= multiplier;
                topics = getTopics();
            }
        }
    }

    /**
     * Checks the status of the schema registry by sending a GET request to its endpoint.
     * The method retries the status check with exponential backoff in case of failures.
     * Throw KafkaClientException if the schema registry is unavailable after max retries.
     */
    public void checkSchemaRegistry() {
        int retryCount = 1;
        Integer maxRetry = retryConfigData.getMaxAttempts();
        int multiplier = retryConfigData.getMultiplier().intValue();
        Long sleepTimeMs = retryConfigData.getSleepTimeMs();
        while (!getSchemaRegistryStatus().is2xxSuccessful()) {
            checkMaxRetry(retryCount++, maxRetry);
            sleep(sleepTimeMs);
            sleepTimeMs *= multiplier;
        }
    }

    /**
     * Retrieves the HTTP status code from the schema registry endpoint.
     * This method sends an asynchronous GET request to the schema registry URL and maps
     * the response to its HTTP status code.
     *
     * @return HttpStatus representing the response status of the schema registry.
     */
    private HttpStatus getSchemaRegistryStatus() {
        try {
            return webClient
                    .method(HttpMethod.GET)
                    .uri(kafkaConfigData.getSchemaRegistryUrl())
                    .exchange()
                    .map(ClientResponse::statusCode)
                    .block();
        } catch (Exception e) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
    }

    /**
     * Sleeps for a specified amount of time in milliseconds.
     * Throws KafkaClientException if interrupted while sleeping.
     *
     * @param sleepTimeMs the time in milliseconds for which the thread should sleep.
     */
    private void sleep(Long sleepTimeMs) {
        try {
            Thread.sleep(sleepTimeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaClientException("Error while sleeping for waiting new created topics!!");
        }
    }

    /**
     * Checks if the retry count has exceeded the maximum allowed retries.
     * Throws KafkaClientException if the retry count exceeds the configured limit.
     *
     * @param retry    the current retry count.
     * @param maxRetry the maximum allowed retry count.
     */
    private void checkMaxRetry(int retry, Integer maxRetry) {
        if (retry > maxRetry) {
            throw new KafkaClientException("Reached max number of retry for reading kafka topic(s)!");
        }
    }

    /**
     * Checks if a specific topic exists within a collection of TopicListings.
     *
     * @param topics    the collection of existing topics.
     * @param topicName the name of the topic to check.
     * @return true if the topic is found; false otherwise.
     */
    private boolean isTopicCreated(Collection<TopicListing> topics, String topicName) {
        if (topics == null) {
            return false;
        }
        return topics.stream().anyMatch(topic -> topic.name().equals(topicName));
    }

    /**
     * Executes the Kafka topic creation logic.
     * Uses the RetryContext to log the retry attempt number and create topics
     * with configurations defined in KafkaConfigData.
     *
     * @param retryContext the context for the current retry operation.
     * @return CreateTopicsResult representing the result of the topic creation operation.
     */
    private CreateTopicsResult doCreateTopics(RetryContext retryContext) {
        List<String> topicNames = kafkaConfigData.getTopicNamesToCreate();
        log.info("Creating {} topics, attempt {}", topicNames.size(), retryContext.getRetryCount());
        List<NewTopic> kafkaTopics = topicNames.stream().map(topic -> new NewTopic(
                topic.trim(),
                kafkaConfigData.getNumOfPartitions(),
                kafkaConfigData.getReplicationFactor()
        )).collect(Collectors.toList());
        return adminClient.createTopics(kafkaTopics);
    }

    /**
     * Retrieves the current list of Kafka topics, using a retry template to handle
     * transient errors. Throws KafkaClientException if the maximum retry limit is reached.
     *
     * @return Collection of TopicListing objects representing the existing Kafka topics.
     */
    private Collection<TopicListing> getTopics() {
        Collection<TopicListing> topics;
        try {
            topics = retryTemplate.execute(this::doGetTopics);
        } catch (Throwable t) {
            throw new KafkaClientException("Reached max number of retry for reading kafka topics.", t);
        }
        return topics;
    }

    /**
     * Executes the logic to retrieve Kafka topic listings.
     * Uses the RetryContext to log the retry attempt number.
     *
     * @param retryContext the context for the current retry operation.
     * @return Collection of TopicListing objects representing the Kafka topics.
     * @throws ExecutionException   if the topic listing fails to execute.
     * @throws InterruptedException if the operation is interrupted.
     */
    private Collection<TopicListing> doGetTopics(RetryContext retryContext)
            throws ExecutionException, InterruptedException {
        log.info("Reading kafka topic {}, attempt {}",
                kafkaConfigData.getTopicNamesToCreate().toArray(), retryContext.getRetryCount());
        Collection<TopicListing> topics = adminClient.listTopics().listings().get();
        if (topics != null) {
            topics.forEach(topic -> log.debug("Topic with name {}", topic.name()));
        }
        return topics;
    }


}
