package com.example.kafka.Aggregator

import com.example.kafka.usecase.event.DeleteBusinessAccountEvent

interface EventPublisher {
    suspend fun deleteEventPublisher(deleteBusinessAccountEvent: DeleteBusinessAccountEvent)
}
