package com.microservices.reactive.elastic.query.service.business.impl;

import com.microservices.config.ElasticQueryServiceConfigData;
import com.microservices.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.reactive.elastic.query.service.business.ReactiveElasticQueryClient;
import com.microservices.reactive.elastic.query.service.repository.ElasticQueryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@Service
public class TwitterReactiveElasticQueryClient implements ReactiveElasticQueryClient<TwitterIndexModel> {

    private final ElasticQueryRepository elasticQueryRepository;

    private final ElasticQueryServiceConfigData elasticQueryServiceConfigData;

    public TwitterReactiveElasticQueryClient(ElasticQueryRepository elasticRepository,
                                             ElasticQueryServiceConfigData configData) {
        this.elasticQueryRepository = elasticRepository;
        this.elasticQueryServiceConfigData = configData;
    }


    @Override
    public Flux<TwitterIndexModel> getIndexModelByText(String text) {
        log.info("Getting data from reactive elasticsearch for text {}", text);
        return elasticQueryRepository
                .findByText(text)
                .delayElements(Duration.ofMillis(elasticQueryServiceConfigData.getBackPressureDelayMs()));
    }
}
