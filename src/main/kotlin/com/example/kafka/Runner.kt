package com.example.kafka

import com.example.kafka.usecase.EventUsecase
import kotlinx.coroutines.runBlocking
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class Runner(
    private val deleteBusinessAccountUsecase: EventUsecase
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        runBlocking {
//            val producer = async { deleteBusinessAccountUsecase.deleteEventProduce() }
//            deleteBusinessAccountUsecase.deleteEventConsume()
//            producer.await()
//            consumer.await()
        }
    }
}
