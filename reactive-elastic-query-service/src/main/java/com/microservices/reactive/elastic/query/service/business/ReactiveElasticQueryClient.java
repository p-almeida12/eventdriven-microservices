package com.microservices.reactive.elastic.query.service.business;

import com.microservices.elastic.model.index.IndexModel;
import com.microservices.elastic.model.index.impl.TwitterIndexModel;
import reactor.core.publisher.Flux;

public interface ReactiveElasticQueryClient<T extends IndexModel> {

    Flux<TwitterIndexModel> getIndexModelByText(String text);

}
