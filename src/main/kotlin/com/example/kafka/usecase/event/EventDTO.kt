package com.example.kafka.usecase.event

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.LocalDateTime

@JsonTypeName(EventType.BA_DELETE)
data class DeleteEvent(
    val dto: Dto,
    val actorDto: ActorEventDto,
    val deletedAt: LocalDateTime
) : Event(EventType.BA_DELETE, dto.id.toString())

data class Dto(
    val id: Long,
    val name: String,
    val categoryId: Int? = null,
    val regionId: Long? = null,
    val imageId: String? = null,
    val pictureId: String? = null,
    val individual: Boolean? = null,
    val status: String,
    val editedAt: LocalDateTime? = null,
    val deletedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class ActorEventDto(
    var userType: String,
    var userId: Long? = null,
    var businessUserId: Long? = null,
    var businessAccountId: Long? = null,
    var nickname: String? = null,
    var regionId: Int? = null,
    var displayRegionCheckinsCount: Int? = null,
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
sealed class Event(
    type: String,
    subject: String,
) : BaseEvent(
    type = type,
    subject = subject
)

open class BaseEvent(
    /**
     * type should be <dataset name>.<behavior>
     * ex: post.create, post.update
     */
    val type: String,
    /**
     * subject is id of target.
     * ex: if event is related to post, subject is id of post
     */
    val subject: String
)

object EventType {
    const val BA_DELETE = "ba.delete"
}