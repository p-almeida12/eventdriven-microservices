package com.microservices.elastic.query.web.client.service;

import com.microservices.elastic.query.web.client.common.model.ElasticQueryWebClientRequestModel;
import com.microservices.elastic.query.web.client.common.model.ElasticQueryWebClientResponseModel;

import java.util.List;

public interface ElasticQueryWebClient {

    List<ElasticQueryWebClientResponseModel> getByText(ElasticQueryWebClientRequestModel requestModel);

}
