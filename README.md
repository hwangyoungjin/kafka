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
        val props = mapOf<String, Any>( // 1
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBootStrapServers, // 2
            ProducerConfig.ACKS_CONFIG to "all", // 3
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java, // 4
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java // 4
        )
        val senderOptions = SenderOptions.create<String, String>(props) // 5
        return KafkaSender.create(senderOptions) // 6
    }
```
1. props = mapOf<>()
```text
Properties 객체 생성
```
2. BOOTSTRAP_SERVERS_CONFIG to kafkaBootStrapServers
```text
카프카 클러스터는 클러스터 마스터라는 개념이 없으므로 클러스터 내 모든 서버가 클라리언트의 요청을 받을 수 있다. 
클라이언트가 카프카 클러스터에 처음 연결하기 위한 호스트와 포트정보를 나타낸다.
```
3. ACK_CONFIG = all
```text
프로듀서가 카프카 토픽의 리더 측에 메시지를 전송한 후 요청을 완료하기를 결정하는 옵션
0: 빠른전송을 의미하지만 일부 메시지 손실 가능성이 존재
1: 리더가 메시지를 받았는지 확인하지만 모든 팔로워를 전부 확인하지는X
all(-1): 팔로워가 메시지를 받았는지 여부를 확인, 다소 느릴 수 있지만 하나의 팔로워가 있는 한 메시지는 손실되지X
```
4. KEY_SERIALIZER_CLASS_CONFIG, VALUE_SERIALIZER_CLASS_CONFIG
```text
메시지의 키와 밸류는 문자열 타입이므로 카프카의 기본 StringSerializer를 지정
```
5. senderOptions 생성
6. 옵션을 통해 KafkaSender 생성