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

### 3. yml 설정
```yaml
kafka:
  bootstrapServers: localhost:9092
  topic: demo-topic
  groupId: test-group-01
```

### 4. Config

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
- 0: 빠른전송을 의미하지만 일부 메시지 손실 가능성이 존재
- 1: 리더가 메시지를 받았는지 확인하지만 모든 팔로워를 전부 확인하지는X
- all(-1): 팔로워가 메시지를 받았는지 여부를 확인, 다소 느릴 수 있지만 하나의 팔로워가 있는 한 메시지는 손실되지X
```

4. KEY_SERIALIZER_CLASS_CONFIG, VALUE_SERIALIZER_CLASS_CONFIG

```text
메시지의 키와 밸류는 문자열 타입이므로 카프카의 기본 StringSerializer를 지정
```

5. senderOptions 생성
6. 옵션을 통해 KafkaSender 생성

#### 2. Consumer

```kotlin
@Bean
fun kafkaReceiver(): KafkaReceiver<String, String> {
    val props = mapOf<String, Any>( // 1
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBootStrapServers, // 2
        ConsumerConfig.GROUP_ID_CONFIG to groupId, // 3 
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java, // 4
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java, // 4
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest" // 5
    )
    val receiverOptions = ReceiverOptions.create<String, String>(props).subscription(listOf(topic)) // 6
    return KafkaReceiver.create(receiverOptions) // 6
}
```

1. 컨슈머와 동일
2. 컨슈머와 동일
3. GROUP_ID_CONFIG

```text
컨슈머가 속한 컨슈머 그룹을 식별하는 식별자이다. 동일한 그룹내의 컨슈머 정보는 모두 공유된다.
```

4. 컨슈머와 동일
5. AUTO_OFFSET_RESET_CONFIG

```text
카프카에서 초기 오프셋이 없거나 현재 오프셋이 더이상 존재하지 않는 경우에 다음 옵션으로 reset 한다
- earliest: 가장 초기의 오프셋값으로 설정
- latest: 가장 마지막의 오프셋값으로 설정
- none: 이전 오프셋값을 찾지 못하면 에러를 나타낸다.
```

### 5. Sender, Receiver

#### 1. Sender
```kotlin
override suspend fun publish(deleteEvent: DeleteEvent) {
    // 1. 보낼 record 작성
    val message = eventObjectMapper.writeValueAsString(deleteEvent)
    val outboundFlux = Flux.range(1, 10)
        .map { i: Int ->
            SenderRecord.create(
                ProducerRecord(topic, i.toString(), message), i
            )
        }

    // 2 Send message to kafka 
    kafkaSender.send(outboundFlux).doOnError { e -> log.error(e.message, e) }
        .subscribe { r ->
            val metadata = r.recordMetadata()
            println(
                "Message ${r.correlationMetadata()} sent successfully, topic-partition=${metadata.topic()}-" +
                        "${metadata.partition()} offset=${metadata.offset()}\n"
            )
        }
}
```
1. 레코드 생성
```text
- 레코드는 `Topic`, `Partition`, `Key`, `Value`로 구성
- 이 중 `Topic`, `Value`는 필수 값이다.
- 나머지는 옵션
    - partition: 특정 파티션 정하는 값
    - key: 특정 파티션의 레코드들을 정렬하기 위한 값
```
2. send()
```text
- 해당 메소드를 통해 `serializer`와 `partitioner`를 거치게 된다.
- 레코드의 파티션 값이 있다면
    → 지정된 파티션으로 전달되고
- 레코드의 파티션 값이 없다면
    → 라운드로빈 방식으로 동작한다.
- `send()` 동작 이후 레코드들은 카프카에 전달되기 전 `배치` 전송을 하기 위해 파티션 별로 잠시 모아두게된다.
    → 전송 실패시 재시도 동작이 이루어 지고 지정된 횟수 만큼의 재시도가 실패하면 최종실패를 전달하고 전송이 성공하면 `send()`는 메타데이터를 리턴하게 된다.
```

#### 2. Receiver
- 참고 [Reactor onErrorContinue VS onErrorResume](https://devdojo.com/ketonemaniac/reactor-onerrorcontinue-vs-onerrorresume)
- 참고 [reactor kafka](https://java.tutorialink.com/continue-consuming-subsequent-records-in-reactor-kafka-after-deserialization-exception/)
```kotlin
override suspend fun subscribe() {
    kafkaReceiver.receive() // 1
        .doOnNext {  // 2
            println("========================Start==================================")
            val offset = it.receiverOffset() 
            println(
                "Received message: topic-partition=${offset.topicPartition()}, offset=${offset.offset()} " +
                        " key=${it.key()}, value=${it.value()}"
            )
            offset.acknowledge() // 3
        }.onErrorContinue { e, u -> // 4
                log.error(e.message, e)
                log.info("continuing") 
            }.doOnNext { it.receiverOffset().acknowledge() } // 5
            .subscribe() // 6
}
```
1. kafkaReceiver.receive()
```text
kafkaReceiver를 통해 커밋 가능한 receiverOffset가 담긴 ReceiverRecord 배열을 받는다.
추후 receiverOffset.acknowledge()를 통해 반드시 commit을 수행해야 한다.
```
2. doOnNext{} 를 통해 message 출력
3. offset.acknowledge()
```text
message의 담긴 receiverOffset 인스턴스에 대해
acknowledge() 를 수행하여 commit을 완료한다.
```
4. [onErrorContinue](https://akageun.github.io/2019/07/26/spring-webflux-tip-3.html)
```text
onErrorContinue를 통해 doNext에서 error 발생시 'e' 를 출력하고 계속 진행한다.

```
5. doOnNext { it.receiverOffset().acknowledge() } // 5
```
에러 발생 후 acknowledge()가 없는데 이를 처리
```
6. subscribe()
```text
producer의 record를 subscribe() 하도록
```
