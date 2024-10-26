# Event-Driven Microservices

## Run the docker compose file to start the kafka cluster

```shell
docker-compose -f common.yml -f kafka_cluster.yml up
```

## See topics and brokers in the kafka cluster
```shell
kcat -L -b localhost:19092
```

## See messages that are being sent to Kafka
```shell
kcat -C -b localhost:19092 -t twitter-topic
```