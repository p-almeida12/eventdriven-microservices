package com.microservices.elastic.query.service.business.impl;

import com.microservices.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.elastic.query.client.service.ElasticQueryClient;
import com.microservices.elastic.query.service.business.ElasticQueryService;
import com.microservices.elastic.query.service.model.ElasticQueryServiceResponseModel;
import com.microservices.elastic.query.service.transformer.ElasticToResponseModelTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterElasticQueryService implements ElasticQueryService {

    private final ElasticToResponseModelTransformer transformer;
    private final ElasticQueryClient<TwitterIndexModel> elasticQueryClient;

    @Override
    public ElasticQueryServiceResponseModel getById(String id) {
        log.info("Getting document by id: {}", id);
        return transformer.transform(elasticQueryClient.getIndexModelById(id));
    }

    @Override
    public List<ElasticQueryServiceResponseModel> getByText(String text) {
        log.info("Getting documents by text: {}", text);
        return transformer.transform(elasticQueryClient.getIndexModelByText(text));
    }

    @Override
    public List<ElasticQueryServiceResponseModel> getAll() {
        log.info("Getting all documents in elasticsearch.");
        return transformer.transform(elasticQueryClient.getAllIndexModels());
    }

}
