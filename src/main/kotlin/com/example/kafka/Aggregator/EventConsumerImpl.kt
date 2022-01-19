package com.example.kafka.Aggregator

import com.example.kafka.usecase.event.DeleteBusinessAccountEvent
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import reactor.kafka.receiver.KafkaReceiver
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class EventConsumerImpl(
    private val kafkaReceiver: KafkaReceiver<Int, String>
) : EventConsumer {

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

    override suspend fun deleteEventConsumer() {
        // receive message
        val kafkaFlux = kafkaReceiver.receive()
        kafkaFlux.subscribe {
            val ba = eventObjectMapper.readValue(it.value(), DeleteBusinessAccountEvent::class.java)
            println(ba)
            val offset = it.receiverOffset()
            println(
                "Received message: topic-partition=${offset.topicPartition()}, offset=${offset.offset()} " +
                    " key=${it.key()}, value=${it.value()}"
            )
            offset.acknowledge()
        }
    }
}
