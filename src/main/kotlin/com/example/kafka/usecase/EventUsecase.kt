package com.example.kafka.usecase

import com.example.kafka.Aggregator.EventConsumer
import com.example.kafka.Aggregator.EventPublisher
import com.example.kafka.usecase.event.ActorEventDto
import com.example.kafka.usecase.event.BusinessAccountDto
import com.example.kafka.usecase.event.DeleteBusinessAccountEvent
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import java.time.LocalDateTime
import javax.inject.Named

@Named
class EventUsecase(
    private val eventPublisher: EventPublisher,
    private val eventConsumer: EventConsumer,
) : CommandLineRunner {
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

    override fun run(vararg args: String?) {
        runBlocking {
            eventConsumer.deleteEventConsumer()
        }
    }
}
