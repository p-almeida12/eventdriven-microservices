package com.microservices.elastic.query.web.client.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElasticQueryWebClientResponseModel {

    private String id;
    private Long userId;
    private String text;
    private ZonedDateTime createdAt;

}
