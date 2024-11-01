package com.microservices.elastic.query.service.business;

import com.microservices.elastic.query.service.common.model.ElasticQueryServiceResponseModel;

import java.util.List;

public interface ElasticQueryService {

    ElasticQueryServiceResponseModel getById(String id);

    List<ElasticQueryServiceResponseModel> getByText(String text);

    List<ElasticQueryServiceResponseModel> getAll();

}
