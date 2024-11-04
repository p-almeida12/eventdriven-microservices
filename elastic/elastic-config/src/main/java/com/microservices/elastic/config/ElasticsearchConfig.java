package com.microservices.elastic.config;

import com.microservices.config.ElasticConfigData;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

/**
 * Configuration class for setting up the connection to an Elasticsearch cluster.
 * Extends {@link AbstractElasticsearchConfiguration} to customize the default Elasticsearch client.
 * Provides configuration for {@link RestHighLevelClient} and {@link ElasticsearchOperations}.
 */
@Configuration
@RequiredArgsConstructor
@EnableElasticsearchRepositories(basePackages = "com.microservices.elastic")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    private final ElasticConfigData elasticConfigData;

    /**
     * Creates and configures the {@link RestHighLevelClient} bean used for Elasticsearch communication.
     * <p>
     * This client connects to the Elasticsearch instance specified in {@link ElasticConfigData#getConnectionUrl()},
     * with customizable connection and socket timeouts.
     * </p>
     *
     * @return a configured {@link RestHighLevelClient} instance.
     */
    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        UriComponents serverUri = UriComponentsBuilder.fromHttpUrl(elasticConfigData.getConnectionUrl()).build();
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(
                        Objects.requireNonNull(serverUri.getHost()),
                        serverUri.getPort(),
                        serverUri.getScheme()
                )).setRequestConfigCallback(
                        requestConfigBuilder ->
                                requestConfigBuilder
                                        .setConnectTimeout(elasticConfigData.getConnectionTimeoutMs())
                                        .setSocketTimeout(elasticConfigData.getSocketTimeoutMs())

                )
        );
    }

    /**
     * Configures and provides the {@link ElasticsearchOperations} bean, which acts as a simplified API
     * for interacting with Elasticsearch, leveraging {@link ElasticsearchRestTemplate}.
     * <p>
     * This bean depends on {@link #elasticsearchClient()} to establish the connection.
     * </p>
     *
     * @return a configured {@link ElasticsearchOperations} instance.
     */
    @Bean
    public ElasticsearchOperations elasticsearchOperations() {
        return new ElasticsearchRestTemplate(elasticsearchClient());
    }

}
