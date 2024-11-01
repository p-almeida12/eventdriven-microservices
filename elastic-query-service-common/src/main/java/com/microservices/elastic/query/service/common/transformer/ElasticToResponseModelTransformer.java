package com.microservices.elastic.query.service.common.transformer;

import com.microservices.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ElasticToResponseModelTransformer {

    public ElasticQueryServiceResponseModel transform(TwitterIndexModel twitterIndexModel) {
        return ElasticQueryServiceResponseModel.builder()
                .id(twitterIndexModel.getId())
                .userId(twitterIndexModel.getUserId())
                .text(twitterIndexModel.getText())
                .createdAt(twitterIndexModel.getCreatedAt())
                .build();
    }

    public List<ElasticQueryServiceResponseModel> transform(List<TwitterIndexModel> twitterIndexModels) {
        return twitterIndexModels.stream().map(this::transform).collect(Collectors.toList());
    }

}
