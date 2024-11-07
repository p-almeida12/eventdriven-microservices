# Event-Driven Microservices

## CREATE THE DOCKER IMAGES USING SPRING BOOT MAVEN PLUGIN
Use -DskipTests to skip the tests, otherwise the build will fail because the elastic and kafka cluster are not running.
```shell
mvn clean install -DskipTests
```

## Run the docker compose file to start the kafka cluster
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






















