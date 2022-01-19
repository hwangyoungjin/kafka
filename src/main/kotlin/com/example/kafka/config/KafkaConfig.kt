package com.example.kafka.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaConfig {
    @Value("\${kafka.bootstrapServers}")
    private lateinit var kafkaBootStrapServers: String

    @Value("\${kafka.topic}")
    private lateinit var topic: String

    @Bean
    fun kafkaSender(): KafkaSender<String, String> {
        val props = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBootStrapServers,
            ProducerConfig.CLIENT_ID_CONFIG to "commerce-producer",
            ProducerConfig.ACKS_CONFIG to "all",
//            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java, //mercury
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )
        val senderOptions = SenderOptions.create<String, String>(props)
        return KafkaSender.create(senderOptions)
    }

    @Bean
    fun kafkaReceiver(): KafkaReceiver<Int, String> {
        val props = mapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBootStrapServers,
            ConsumerConfig.CLIENT_ID_CONFIG to "commerce-consumer",
            ConsumerConfig.GROUP_ID_CONFIG to "commerce-group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )
        val receiverOptions = ReceiverOptions.create<Int, String>(props).subscription(listOf(topic))
        return KafkaReceiver.create(receiverOptions)
    }
}
