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
### 3. Config
#### 1. Producer
```kotlin
@Bean
    fun kafkaSender(): KafkaSender<String, String> {
        val props = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBootStrapServers,
            ProducerConfig.CLIENT_ID_CONFIG to "group-producer",
            ProducerConfig.ACKS_CONFIG to "all", 
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java, //mercury
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )
        val senderOptions = SenderOptions.create<String, String>(props)
        return KafkaSender.create(senderOptions)
    }
```
- ACK_CONFIG = all
```text
메시지의 내구성을 강화 
 - 카프카로 전송되는 모든 메시지는 안전한 저장소인 카프카의 로컬 디스크에 저장된다.
 - 컨슈머가 메시지를 가져가더라고 삭제X
 - 메시지는 브로커 한대에만 저장되는것이 아닌 여러대에 저장되어 한 브로커가 down되더라도 메시지 복구 가능
```