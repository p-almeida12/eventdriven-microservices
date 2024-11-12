# Event-Driven Microservices

## CREATE THE DOCKER IMAGES USING SPRING BOOT MAVEN PLUGIN
Use -DskipTests to skip the tests, otherwise the build will fail because the elastic and kafka cluster are not running.
```shell
mvn clean install -DskipTests
```

## RUN THE DOCKER COMPOSE FILE TO START THE KAFKA CLUSTER
Before running this,
change the credentials for the config-serve, also remember
to create a git repository with the configuration files and alter the credentials.
```shell
docker-compose -f common.yml -f kafka_cluster.yml up
```

## SEE TOPICS AND BROKERS IN THE KAFKA CLUSTER
```shell
kcat -L -b localhost:19092
```

## SEE MESSAGES THAT ARE BEING SENT TO KAFKA
```shell
kcat -C -b localhost:19092 -t twitter-topic
```

## SET ENCRYPT KEY AS ENVIRONMENT VARIABLE
```shell
export ENCRYPT_KEY='insert_key_here'
```
Use echo to check if the key is set
```shell
echo $ENCRYPT_KEY    
```
To encrypt and decrypt use the following commands, respectively
```shell
spring encrypt something --key 'insert_key_here'
``` 
```shell
spring decrypt something_encrypted --key 'insert_key_here'
``` 

## SWAGGER URLS FOR ELASTIC QUERY SERVICE
http://localhost:8183/elastic-query-service/api-docs
http://localhost:8183/elastic-query-service/swagger-ui/index.html?configUrl=/elastic-query-service/api-docs/swagger-config

## GET TOKEN FROM KEYCLOAK
```shell
curl -X POST -d 'grant_type=password&username=app_user&password=app_user&client_id=elastic-query-web-client&client_secret=834ec069-a703-4f62-8912-47e17ba1e553' http://localhost:9091/auth/realms/microservices-realm/protocol/openid-connect/token
```

# ABOUT THE REPOSITORY

In this repository we have a project that uses event-driven microservices. As a sum of what is addressed in this project:
- Development of event-driven microservices with Apache Kafka.
- Basics of Kafka brokers, topics, partitions, producers, consumers, admin client, avro serialization, zookeper and schema registry.
- Implement CQRS pattern with Kafka and Elastic Search.
- Client side load balancing with Spring Cloud Load Balancer.
- Basic Authentication and Authorization with Spring Security Oauth 2.0 and OpenID connect protocols with Keycloak using JWT.
- Containerization of microservices using Docker and Docker Compose with the usage of spring boot maven plugin to create the images.
- Basic elastic search , index api and query api usage, with Spring Data Elastic Search.
- Implemented externalized configuration pattern with Spring Cloud Config.
- API versioning.
- Hateoas in REST APIs with Spring HATEOAS.


## KAFKA

In this project kafka is the event store, where it will hold the tweets in form of events. 
The tweets are sent to kafka by the twitter-to-kafka-service (consumer). 
The kafka-to-elastic-service will process the tweets and send the processed tweets to the elastic search service. 
The elastic search service will store the tweets in the elastic search database. 
The elastic query service will query the elastic search database and return the results to the user.

### ADVANTAGES OF USING KAFKA

- Kafka is an open source processing platform, it is designed to handle data feeds with low latency 
(the time it takes to go from one point to another) and high throughput
  (amount of data processed in a given amount of time).
- Holds the data in an immutable append-only structure, called topics (logs or events). This immutability 
ensures that you will have all history of data feeds and can get a picture of the data at any point in time.
- Resilient because it relies on the file system instead of memory, and it keeps all messages on disk and uses a configurable amount of replicas 
to prevent data loss).
- Fast because it relies on disk caching and memory mapped file of the underlying OS, instead of the garbage collector 
eligible JVM memory. Memory mapped files contain the contents of a file in virtual memory. 
The mapping between a file and memory space enables an application to modify the file by reading and writing directly 
to the memory. Accessing memory mapped files is faster than using direct read and write operations as it operates on memory.
In addition to that, in most OS, the memory region assigned to a mapped file actually is in Kernels page cache (physical pages in RAM).
That means no copied will be created in the user memory space and it will directly operate in this cache (faster).
- Scalable thanks to the partitions inside each topic, which is configurable, allowing you to scale it by altering the partition number.
- Is ordering is importante be sure you put all related data which requires ordering inside the same partition because 
ordering is only guaranteed in a single partition. You can achieve to put the related data to the same partition, using a partition strategy.
- Perfect match for event-driven microservices. It is a solid platform that can hold events and provides nice producer and consumer API's to work with events.
- To achieve high availability and fault tolerance, Kafka uses a leader-follower replication model. And each partition has one leader and multiple followers. 
The leader handles all read and write requests for the partition, while the followers replicate the data from the leader. 
It is a good practice to hold those brokers in different machines.
- A broker can have one or more topics, and a topic can have one or more partitions. The kafka producers will feed the topics with messages.
- The consumers will read the messages from the topics. Each partition can have only one consumer, but a consumer can read from multiple partitions.

## KAFKA PRODUCERS

A kafka producer holds a buffer of unsent records per partition and sends the records to the cluster using an internal batch.size property. In most
of the cases the default size could be good enough, but you may want to play around with this value to get a better throughput by increasing the batching.
But keep in mind that if the batch size is too big the memory will be wasted since the part of the memory that is allocated for batching will not be used.
This happens because the data will be sent before the batch size limit hits. Using a larger batch size makes compression more efficient.

Under heavy loads, batching will probably be in place, however, in light loads data may not be batch until the batch size limit hits, in that case increasing the linger.ms
can increase the throughput by increasing batching with fewer requests and with an increased latency on producer send. By default linger.ms is 0, which means no delay.
As mentioned previously in Kafka there are multiple broker nodes that holds the same data to enable resilience and availability. 
By default the producer will wait all replica nodes to return the result because the default value for acknowledgement is all, 
by setting ack to 1 only the broker that sent the request will send confirmation instead of waiting all in-sync replicas.

Producers can also compress the data before sending it to the broker, this can be done by setting the compression.type property.
The compression is done by batch and improves with larger batch sizes. By using compressed data, you can ssend more data 
at once through the network and increase the throughput. End to end compression is also possible, this way compression only happens once 
and is reused by the producer and consumer (better for performance).

## KAFKA CONSUMERS

Kafka relies on logs which are known as partitions, the producers write to the end of a specific partition and the 
consumers read the logs starting from the beginning while holding an offset to remember where to left. When it is done with the data, 
the consumer sends a commit request to the broker so that there will be an offset per consumer per partition,
to remember the latest record that was read. This way consumer will continue reading new data instead of reading the same data again.

Each consumer belongs to a consumer group and Kafka distributes partitions to consumers based on these consumer groups. 
Each partition is strictly assigned to a single consumer in a consumer group, therefore it is not possible to assign the same aprtition to more than one consumer.
This is to prevent conflicts with the offset values and make consuming easier. Different consumer groups with different ids 
can read the same data form the same partition, so kafka allows you to read the same data more than once, but for that you 
need to create a new consumer group and read the data again. For maximum concurrency, it is wise to set the consumer number as equal to the partition number.
If you create more consumers than partition numbers, after assigning each partition to a consumer, some consumers will be idle and will not read any data.

There is a poll time out in Kafka consumer and setting it to a too high value will make the waiting take longer and causes delays in 
the application. If the poll time out is too low, the consumer will poll more frequently and this will cause more network traffic and can lead to CPU stall.
If you have a single consumer with multiple partitions, there will be no concurrent work on multiple partitions by deafult (multithreading), 
but we can still use multiple threads while consuming partitions. This should be implemented on the client side.

When it comes to delivery semantics, Kafka provides three options: at most once, at least once, and exactly once. This behavior 
relies on the consumer strategy and the ack property on the client side. For example, if you commit after processing the data, 
the commit operation might fail, but you already processed the data, so in the next poll you will read the same data and it will imply at least once behavior.
On the other hand, if you commit before processing the data, you might lose the data if the consumer crashes, so it will imply at most once behavior.
Exactly once requires to coordinate between producer and consumer, using transactions starting from the producer part.
There is a transaction Kafka API which will start the transaction from producer to use the same transaction in the consumer part.
Normally auto commit is set to true on the consumer client, so it does a commit after a configured timer and then reset it.
But keep in mind that in some scenarios you might still encounter an error while auto commit is set to true, because a failure can always happen.
With this being said, you might need to implement some rollback mechanism or use strict exactly once semantic depending on your needs.

## ELASTIC QUERY API

This was the created index:

```json
{
  "mappings": {
    "properties": {
      "userId": {
        "type": "long"
      },
      "id": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "createdAt": {
        "type": "date",
        "format": "yyyy-MM-dd'T'HH:mm:ssZZ"
      },
      "text": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      }
    }
  }
}
```

Basic query samples that uses HTTP GET method:
- twitter-index/_search - returns all documents in the index (max 10000 records, use scroll API to get more)
- twitter-index/_search?size=100 - returns 100 documents in the index
- twitter-index/_search/q=id:1 - returns the document with id 1
- twitter-index/_search?q=text:test - returns the documents with text=test

Using POST method we can also supply a query in the request body as a JSON and write more complex queries using 
elastic search DSL (Domain Specific Language). For example, POST twitter-index/_search:

### TERM QUERY TYPE

```json
{
  "query": {
    "term": {
      "text": "test"
    }
  }
}
```

This example uses term query, which uses text field and searches for the term test. So all records that include test in the text 
field will be retrieved. Term query uses the exact term and does not analyse the input, so if you provide a sentence with multiple words 
it will look for the whole sentence in the inverted index. If we have a document with the text "test multiple words" and we run this query:

```json
{
  "query": {
    "term": {
      "text": "test multiple words"
    }
  }
}
```
we will not get any results because the term query will look for the exact term "test multiple words" in the inverted index, but 
since the inverted index indexes the 3 words separately, it will not find any results.

### MATCH QUERY TYPE

We can also use match query:

```json
{
  "query": {
    "match": {
      "text": "test multiple words"
    }
  }
}
```

Match query uses analyzed words to match any documents. It uses every word in the input query, analyzes them and gets combined 
results for each word. So this query will get the records that either include test, multiple or words in the text field.

### KEYWORD QUERY TYPE

In the index mapping we have 2 types: text and keyword. In this query:

```json
{
  "query": {
    "term": {
      "text.keyword": "test multiple words"
    }
  }
}
```

we use text.keyword for the query, so it will search for the whole sentence because the keyword type is saved as a whole text instead 
of analyzing the separate words inside it. Note that with keyword all query types run without analyzing the search text. 
This query will also work with match query and return the documents associated with "test multiple words". However it is better 
to use term query with keyword like queries where we search for the exact term, because term query is a bit faster than match as 
there is no analyse step.

### WILDCARD QUERY TYPE

Wildcard query is used to search for a term that matches a pattern. For example:

```json
{
  "query": {
    "wildcard": {
      "text": "test*"
    }
  }
}
```

It can be slow as it requires a scan in most cases. This query will return all documents that include a word that starts with test.
In this case, * means zero or more characters and ? means exactly one character. 

### QUERY STRING QUERY TYPE

Query string analyzes the input, unlike wildcard query. However, you can still use wildcards in the query DSL. 

```json
{
  "query": {
    "query_string": {
      "fields": ["text", "id"],
      "query": "test*"
    }
  }
}
```

You can give multiple fields in the query_string property to search inside all fields. It is similar to wildcard search 
but has more flexible syntax. The above query will return all documents that include a word that starts with test in the text or id fields.

### COMPLEX QUERIES

You can also write more complex queries by just combining the different query types and use the keywords, like should for 
combining with OR or must to combine with AND. For example:

```json
{
  "from": 0,
  "size": 20,
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "text": "test"
          }
        },
        {
          "match": {
            "text": "words"
          }
        }
      ]
    }
  }
}
``` 

In this query, text must have both test and words in the text field. You can also use should to combine with OR.

## API VERSIONING

APIs can evolve and changes are inevitable and that's ok, but what kind of changes should we allow and how do we do the changes?
First of all, it is a good practice to have a formal versioning in place. 
Semantic versioning uses the Major.Minor.Patch format. Major version is incremented when there are incompatible changes (breaking changes),
minor version is incremented when there are backward compatible changes (non breaking changes) and patch version is incremented 
when there are backward compatible bug fixes. 
When it comes to REST APIs, we should try to keep it simple for both server and client, and prevent big changes on both sides.
So, for non breaking changes, we can just update the server and including minor version change in the documentation would be enough.
This way the client will only see a change in the documentation and doesn't need to update the client code or the way of sending request data or parsing the response.
When it comes to breaking changes, which requires major version change, we need to take into consideration two important concepts:
- Backward compatibility: if you have an updated server and a new updated client, which is actually updated to be able to work 
with the new updated server, then the new client will also work with the old server code.
- Forward compatibility: the client will be able to work with an updated server without doing any change on the client side.

With this being said, when do we need to create a new version? First we need to create a new version if we want backward 
compatibility, because it implies to have two versions. Second, if we cannot have forward compatibility, that implies that 
you have a breaking change and you need to create a new version. In that case, you need any version as you must keep the old 
version live until the client is ready to be updated for the new breaking change in the system.

So we can say that if we can avoid breaking changes and let the client work with the new server changes, for example 
by using extensible schemas, which is easier to implement with JSON, that way we can avoid new versions. If you do not have 
a breaking change, just do not update the major version, but update the software and only include the minor changes 
in the documentation so that the version number in the URL and accept header will remain the same and nothing will change 
for client's library or request data.

Note that avoiding a major version update should be preferable as it will lead to less work for both the server and client side.
So when a breaking change is inevitable, we have four options:
- URI versioning (e.g. /api/v1/resource): this leads to a large URI footprint and difficult to maintain, use and also less 
flexible. This type of versioning is against the hypermedia driven rest APIs, which states that the initial URI shouldn't 
be changed and be the only information given to the client along with the media types, where REST will be resolved by 
hypertext returned to the client. On the other hand, it is easier to use by the client, as using other options require a more 
programmatic approach. It is also cache friendly, since caching with URI is very easy, in contrast to using a header for that 
purpose requires more work.
- Media type versioning (e.g. application/vnd.resource.v1+json): this is also called content negotiation, and uses the accept 
header or a custom vendor media type for versioning. This is a good approach, but requires more work to implement, especially at 
the client side and more difficult to use with caching. On the other hand, it is more flexible to version part of the API 
and works well with Hateoas and level three rest APIs according to Richardson Maturity model. Here are some examples of media type versioning:
  - application/vnd.resource.v1+json
  - application/vnd.resource.v2+json
  - application/vnd.resource.v3+json
- Header versioning (e.g. X-API-Version: 1): versioning with custom headers is less standard and requires more alignment with 
client. Similar to accept header, it is more flexible and can align with level three rest APIs.
- Query parameter versioning (e.g. /api/resource?version=1): difficult to use with routing but is easier to use.































