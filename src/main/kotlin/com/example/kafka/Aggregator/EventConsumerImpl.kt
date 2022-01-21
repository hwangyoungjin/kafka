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

    override suspend fun deleteEventConsumer() {
        // receive message
        val kafkaFlux = kafkaReceiver.receive()
        kafkaFlux.subscribe {
            println("========================Start==================================")
            if (it.value().contains(BusinessAccountEventType.BA_UPDATE) || it.value()
                .contains(BusinessAccountEventType.BA_DELETE) || it.value()
                    .contains(BusinessAccountEventType.BA_UNDELETE)
            ) {
                println("========================Check finished==================================")
                val strIndex = it.value().indexOfFirst { c -> c == '{' }
                val json = it.value().substring(strIndex)
                val ba = eventObjectMapper.readValue(json, BaseEvent::class.java)
                var account: Any
                when (ba.type) {
                    BusinessAccountEventType.BA_UPDATE -> {

                        account = eventObjectMapper.readValue(
                            json,
                            UpdateBusinessAccountEvent::class.java
                        )
                        println("BA Update!!")
                        if (account.afterUpdate.status == "ACTIVE") {
                            println("store: inActive -> Active \n Product ALL Inactive")
                        } else if (account.afterUpdate.status == "INACTIVE") {
                            println("store: Active -> inActive \n Product ALL Inactive")
                        } else {
                            println("None!")
                        }
                    }
                    BusinessAccountEventType.BA_DELETE -> {

                        account = eventObjectMapper.readValue(
                            json,
                            DeleteBusinessAccountEvent::class.java
                        )
                        println("BA Delete!!")
                        println("Store, Product Delete !!")
                    }
                    BusinessAccountEventType.BA_UNDELETE -> {
                        account = eventObjectMapper.readValue(
                            json,
                            UndeleteBusinessAccountEvent::class.java
                        )
                        println("BA UnDelete!!")
                        if (account.businessAccountDto.status == "ACTIVE") {
                            println("store: Active because Active Status of BA")
                        } else if (account.businessAccountDto.status == "INACTIVE") {
                            println("store 변화 없음! because InActive Status of BA")
                        }
                    }
                    else -> RuntimeException("type error")
                }
            }
            val offset = it.receiverOffset()
            println(
                "Received message: topic-partition=${offset.topicPartition()}, offset=${offset.offset()} " +
                    " key=${it.key()}, value=${it.value()}"
            )
            offset.acknowledge()
        }
    }
}
