package com.example.kafka.usecase

import com.example.kafka.Aggregator.EventConsumer
import com.example.kafka.Aggregator.EventPublisher
import com.example.kafka.usecase.event.ActorEventDto
import com.example.kafka.usecase.event.BusinessAccountDto
import com.example.kafka.usecase.event.DeleteBusinessAccountEvent
import java.time.LocalDateTime
import javax.inject.Named

@Named
class DeleteBusinessAccountUsecase(
    private val eventPublisher: EventPublisher,
    private val eventConsumer: EventConsumer,
) {
    suspend fun deleteEventProduce() {
        eventPublisher.deleteEventPublisher(
            DeleteBusinessAccountEvent(
                businessAccountDto = BusinessAccountDto(
                    id = 1234, // baId of StoreRecode
                    name = "하비랜드",
                    status = "INACTIVE",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                actorDto = ActorEventDto(
                    userType = "DEFAULT"
                ),
                deletedAt = LocalDateTime.now()
            )
        )
    }

    suspend fun deleteEventConsume() {
        eventConsumer.deleteEventConsumer()
    }
}
