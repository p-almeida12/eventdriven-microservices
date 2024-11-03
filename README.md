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

## SET AS ENVIRONMENT VARIABLE
```shell
export ENCRYPT_KEY='insert_key_here'
```

```shell
echo $ENCRYPT_KEY    
```

```shell
spring encrypt something --key 'insert_key_here'
``` 
```shell
spring decrypt something_encrypted --key 'insert_key_here'
``` 

http://localhost:8183/elastic-query-service/api-docs
http://localhost:8183/elastic-query-service/swagger-ui/index.html?configUrl=/elastic-query-service/api-docs/swagger-config

## RUN ELASTIC CLUSTER
```shell
docker-compose -f common.yml -f elastic_cluster.yml up
```

## START KEYCLOAK AUTHORIZATION SERVER
```shell
docker-compose -f keycloak_authorization_server.yml up
```

## GET TOKEN FROM KEYCLOAK
```shell
curl -X POST -d 'grant_type=password&username=app_user&password=app_user&client_id=elastic-query-web-client&client_secret=834ec069-a703-4f62-8912-47e17ba1e553' http://localhost:9091/auth/realms/microservices-realm/protocol/openid-connect/token
```