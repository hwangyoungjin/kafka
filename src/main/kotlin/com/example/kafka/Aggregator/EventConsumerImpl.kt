package com.example.kafka.Aggregator

import com.example.kafka.usecase.event.* // ktlint-disable no-wildcard-imports
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import reactor.kafka.receiver.KafkaReceiver
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Named

@Named
class EventConsumerImpl(
    private val kafkaReceiver: KafkaReceiver<String, String>
) : EventConsumer {

    private val log = LoggerFactory.getLogger(EventPublisherImpl::class.java)

    companion object {
        private val eventObjectMapper = ObjectMapper()
            .apply {
                this.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                this.configure(SerializationFeature.INDENT_OUTPUT, false)

                this.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
                this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

                this.registerModule(KotlinModule())
                this.registerModule(
                    JavaTimeModule().apply {
                        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        addDeserializer(
                            LocalDateTime::class.java,
                            LocalDateTimeDeserializer(dateFormat)
                        )
                        addSerializer(
                            LocalDateTime::class.java,
                            LocalDateTimeSerializer(dateFormat)
                        )
                    }
                )
            }
    }

    override suspend fun subscribe() {
        // receive message
        val kafkaFlux = kafkaReceiver.receive()
        kafkaFlux.subscribe {
            println("========================Start==================================")
            val offset = it.receiverOffset()
            println(
                "Received message: topic-partition=${offset.topicPartition()}, offset=${offset.offset()} " +
                    " key=${it.key()}, value=${it.value()}"
            )
            offset.acknowledge()
        }
    }
}
