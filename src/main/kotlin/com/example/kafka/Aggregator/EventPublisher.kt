package com.example.kafka.Aggregator

import com.example.kafka.usecase.event.DeleteEvent

interface EventPublisher {
    suspend fun publish(deleteEvent: DeleteEvent)
}
