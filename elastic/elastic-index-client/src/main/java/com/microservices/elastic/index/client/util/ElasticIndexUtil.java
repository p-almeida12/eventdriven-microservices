package com.microservices.elastic.index.client.util;

import com.microservices.elastic.model.index.IndexModel;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ElasticIndexUtil<T extends IndexModel> {

    /**
     * Converts a list of documents into a list of {@link IndexQuery} objects,
     * each of which represents an index operation for Elasticsearch.
     *
     * @param documents the list of documents to convert, where each document must extend {@link IndexModel}.
     * @return a list of {@link IndexQuery} objects ready to be indexed in Elasticsearch.
     */
    public List<IndexQuery> getIndexQueries(List<T> documents) {
        return documents.stream()
                .map(document -> new IndexQueryBuilder()
                        .withId(document.getId())
                        .withObject(document)
                        .build()
                ).collect(Collectors.toList());
    }

}
