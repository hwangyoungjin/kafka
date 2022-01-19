package com.example.kafka.Aggregator

interface EventConsumer {
    suspend fun deleteEventConsumer()
}