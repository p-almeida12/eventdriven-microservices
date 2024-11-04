package com.microservices.elastic.query.client.util;

import com.microservices.elastic.model.index.IndexModel;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class ElasticQueryUtil<T extends IndexModel> {

    /**
     * Creates a query to search for a document by its unique identifier.
     *
     * @param id the unique ID of the document to search for.
     * @return a {@link Query} object representing the search query for the document with the specified ID.
     */
    public Query getSearchQueryById(String id) {
        return new NativeSearchQueryBuilder()
                .withIds(Collections.singleton(id))
                .build();
    }

    /**
     * Creates a query to search for documents where a specified field contains the given text.
     * Uses a match query to perform a full-text search on the specified field.
     *
     * @param field the name of the field to search.
     * @param text  the text value to search within the specified field.
     * @return a {@link Query} object representing the search query.
     */
    public Query getSearchQueryByFieldText(String field, String text) {
        return new NativeSearchQueryBuilder()
                .withQuery(new BoolQueryBuilder()
                        .must(QueryBuilders.matchQuery(field, text)))
                .build();
    }

    /**
     * Creates a query to retrieve all documents in the index.
     * This is useful for retrieving a complete list of indexed documents.
     *
     * @return a {@link Query} object representing the query to fetch all documents.
     */
    public Query getSearchQueryForAll() {
        return new NativeSearchQueryBuilder()
                .withQuery(new BoolQueryBuilder()
                        .must(QueryBuilders.matchAllQuery()))
                .build();
    }

}
