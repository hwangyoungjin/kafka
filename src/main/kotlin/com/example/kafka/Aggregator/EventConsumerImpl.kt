package com.example.kafka.Aggregator

import com.example.kafka.usecase.event.*
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.util.StringUtils
import reactor.core.publisher.Sinks
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
        //        private val eventObjectMapper = ObjectMapper()
//            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
//            .registerModule(KotlinModule())
//            .registerModule(
//                JavaTimeModule().addSerializer(
//                    LocalDateTime::class.java,
//                    LocalDateTimeSerializer(
//                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
//                    )
//                )
//            )
        private val eventObjectMapper = ObjectMapper()
            .apply {
                this.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                this.configure(SerializationFeature.INDENT_OUTPUT, false)

                this.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
                this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

                this.registerModule(KotlinModule())
                this.registerModule(JavaTimeModule().apply {
                    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    addDeserializer(
                        LocalDateTime::class.java,
                        LocalDateTimeDeserializer(dateFormat)
                    )
                    addSerializer(
                        LocalDateTime::class.java,
                        LocalDateTimeSerializer(dateFormat)
                    )
                })
            }
    }


    override suspend fun deleteEventConsumer() {
        // receive message
        val kafkaFlux = kafkaReceiver.receive()
        kafkaFlux.subscribe {
            println()
            println()
            println()
            println("========================Start==================================")
            val strIndex = it.value().indexOfFirst { c -> c == '{' }
            val json = it.value().substring(strIndex)
            val ba = eventObjectMapper.readValue(json, BaseEvent::class.java)
            val account = when (ba.type) {
                BusinessAccountEventType.BA_CREATE -> eventObjectMapper.readValue(
                    json,
                    CreateBusinessAccountEvent::class.java
                )
                BusinessAccountEventType.BA_UPDATE -> eventObjectMapper.readValue(
                    json,
                    UpdateBusinessAccountEvent::class.java
                )
                BusinessAccountEventType.BA_DELETE -> eventObjectMapper.readValue(
                    json,
                    DeleteBusinessAccountEvent::class.java
                )
                BusinessAccountEventType.BA_UNDELETE -> eventObjectMapper.readValue(
                    json,
                    UndeleteBusinessAccountEvent::class.java
                )
                else -> RuntimeException("type error")
            }
            println(account::class.java)
            val offset = it.receiverOffset()
            println(
                "Received message: topic-partition=${offset.topicPartition()}, offset=${offset.offset()} " +
                        " key=${it.key()}, value=${it.value()}"
            )
            offset.acknowledge()
        }
    }
}
