package com.example.kafka.Aggregator

import com.example.kafka.usecase.event.DeleteBusinessAccountEvent
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import reactor.core.publisher.Flux
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class EventPublisherImpl(
//    ApplicationEventPublisher는 Spring의 ApplicationContext가 상속하는 인터페이스 중 하나이다.
//    옵저버 패턴의 구현체로 이벤트 프로그래밍에 필요한 기능을 제공한다
//    private val applicationEventPublisher: ApplicationEventPublisher, <- 해당 부분은 그냥 당근 초창기 kafka를 사용하지 않았을때 이벤트 처리를 위해 사용
    private val kafkaSender: KafkaSender<String, String>
) : EventPublisher {

    @Value("\${kafka.topic}")
    private lateinit var topic: String

    private val log = LoggerFactory.getLogger(EventPublisherImpl::class.java)

    companion object {
        private val eventObjectMapper = ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(KotlinModule())
            .registerModule(
                JavaTimeModule().addSerializer(
                    LocalDateTime::class.java,
                    LocalDateTimeSerializer(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                    )
                )
            )
    }

    override suspend fun deleteEventPublisher(deleteBusinessAccountEvent: DeleteBusinessAccountEvent) {
        // applicationEventPublisher.publishEvent(deleteBusinessAccountEvent)

        // 보낼 record 작성
        val message = eventObjectMapper.writeValueAsString(deleteBusinessAccountEvent)
        val outboundFlux = Flux.range(1, 10)
            .map { i: Int ->
                SenderRecord.create(
                    ProducerRecord(topic, i.toString(), message), i
                )
            }

        // Send message to kafka
        kafkaSender.send(outboundFlux).doOnError { e -> log.error(e.message, e) }
            .subscribe { r ->
                val metadata = r.recordMetadata()
                println(
                    "Message ${r.correlationMetadata()} sent successfully, topic-partition=${metadata.topic()}-" +
                        "${metadata.partition()} offset=${metadata.offset()}\n"
                )
            }
    }
}
