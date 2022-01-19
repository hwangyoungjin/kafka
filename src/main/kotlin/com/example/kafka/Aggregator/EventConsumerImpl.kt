package com.example.kafka.Aggregator

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import reactor.kafka.receiver.KafkaReceiver
import javax.inject.Named

@Named
class EventConsumerImpl(
    private val kafkaReceiver: KafkaReceiver<Int, String>
) : EventConsumer {

    @Value("\${kafka.topic}")
    private lateinit var topic: String
    private val log = LoggerFactory.getLogger(EventPublisherImpl::class.java)

    override suspend fun deleteEventConsumer() {
        // receive message
        val kafkaFlux = kafkaReceiver.receive()
        kafkaFlux.subscribe {
            val offset = it.receiverOffset()
            println(
                "Received message: topic-partition=${offset.topicPartition()}, offset=${offset.offset()} " +
                    " key=${it.key()}, value=${it.value()}"
            )
            offset.acknowledge()
        }
    }
}
