## Reactor Kafka through spring boot Webflux
- [kafak 공식문서](https://projectreactor.io/docs/kafka/milestone/api/overview-tree.html)
---
### Environment
```yaml
1. jdk17
2. spring-boot-starter-webflux
```

### dependency
```grdlew
// https://mvnrepository.com/artifact/io.projectreactor.kafka/reactor-kafka
    implementation ("io.projectreactor.kafka:reactor-kafka:1.0.0.M1")

// inject (@Named 사용을 위함)
    implementation("javax.inject:javax.inject:1")

```

### 1. start kafka on local
```yaml
version: '3.7'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
  kafka:
    image: wurstmeister/kafka
    ports:
      - "9092:9092"
    hostname: kafka
    environment:
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://127.0.0.1:9092
      KAFKA_ADVERTISED_HOST_NAME: kafka
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
```

### 2. create topics
#### 1. Run this command to create a topic named "account"
```shell
% docker exec -it kafka-kafka-1 /bin/bash
bash-5.1# /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --replication-factor 1 --partitions 1 --topic account
# output
Created topic account. 
```
#### 2. check topic
```shell
% docker exec -it kafka-kafka-1 /bin/bash
bash-5.1# /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe
# output
Topic: account  TopicId: imn0FQ_FRo2O5LTjM27PGw PartitionCount: 2       ReplicationFactor: 1    Configs: segment.bytes=1073741824
        Topic: account  Partition: 0    Leader: 1001    Replicas: 1001  Isr: 1001
        Topic: account  Partition: 1    Leader: 1001    Replicas: 1001  Isr: 1001

```