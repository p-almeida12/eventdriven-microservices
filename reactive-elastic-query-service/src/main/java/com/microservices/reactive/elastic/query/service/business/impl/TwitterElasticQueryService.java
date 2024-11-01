package com.microservices.reactive.elastic.query.service.business.impl;

import com.microservices.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import com.microservices.elastic.query.service.common.transformer.ElasticToResponseModelTransformer;
import com.microservices.reactive.elastic.query.service.business.ElasticQueryService;
import com.microservices.reactive.elastic.query.service.business.ReactiveElasticQueryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class TwitterElasticQueryService implements ElasticQueryService {

    private final ReactiveElasticQueryClient<TwitterIndexModel> reactiveElasticQueryClient;
    private final ElasticToResponseModelTransformer elasticToResponseModelTransformer;

    public TwitterElasticQueryService(ReactiveElasticQueryClient<TwitterIndexModel> elasticQueryClient,
                                      ElasticToResponseModelTransformer transformer) {
        this.reactiveElasticQueryClient = elasticQueryClient;
        this.elasticToResponseModelTransformer = transformer;
    }


    @Override
    public Flux<ElasticQueryServiceResponseModel> getDocumentByText(String text) {
        log.info("Querying reactive elasticsearch for text {}", text);
        return reactiveElasticQueryClient
                .getIndexModelByText(text)
                .map(elasticToResponseModelTransformer::transform);
    }

}
