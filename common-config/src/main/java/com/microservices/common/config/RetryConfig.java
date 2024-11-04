package com.microservices.common.config;

import com.microservices.config.RetryConfigData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Configuration class for setting up a retry mechanism using Spring's {@link RetryTemplate}.
 * This configuration uses an exponential back-off policy and a simple retry policy, both
 * of which are customizable based on properties provided in {@link RetryConfigData}.
 */
@Configuration
public class RetryConfig {

    private final RetryConfigData retryConfigData;

    /**
     * Constructs the {@code RetryConfig} with the necessary configuration data.
     *
     * @param configData the configuration data for retry properties such as initial interval,
     *                   max interval, multiplier, and max attempts.
     */
    public RetryConfig(RetryConfigData configData) {
        this.retryConfigData = configData;
    }

    /**
     * Creates and configures a {@link RetryTemplate} bean for retrying operations
     * with customizable policies for back-off and retry limits.
     *
     * @return a configured {@link RetryTemplate} instance.
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        //configure exponential back-off policy
        ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setInitialInterval(retryConfigData.getInitialIntervalMs());
        exponentialBackOffPolicy.setMaxInterval(retryConfigData.getMaxIntervalMs());
        exponentialBackOffPolicy.setMultiplier(retryConfigData.getMultiplier());

        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);

        //configure simple retry policy
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(retryConfigData.getMaxAttempts());

        retryTemplate.setRetryPolicy(simpleRetryPolicy);

        return retryTemplate;
    }

}




