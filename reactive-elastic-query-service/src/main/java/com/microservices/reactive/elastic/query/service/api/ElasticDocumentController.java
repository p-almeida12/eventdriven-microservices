package com.microservices.reactive.elastic.query.service.api;

import com.microservices.elastic.query.service.common.model.ElasticQueryServiceRequestModel;
import com.microservices.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import com.microservices.reactive.elastic.query.service.business.ElasticQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/documents")
public class ElasticDocumentController {

    private final ElasticQueryService elasticQueryService;

    @PostMapping(value = "/get-doc-by-text",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE, //make this endpoint an event stream endpoint - return the response by chunks to the client
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ElasticQueryServiceResponseModel> getDocumentByText(@RequestBody @Valid ElasticQueryServiceRequestModel requestModel) {
        Flux<ElasticQueryServiceResponseModel> response =
                elasticQueryService.getDocumentByText(requestModel.getText());
        response = response.log(); //.log() observes all reactive stream signals and trace them using logger support
        log.info("Returning from query reactive service for text {}!", requestModel.getText());
        return response;
    }

}
