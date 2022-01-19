package com.example.kafka.usecase.event

import java.time.LocalDateTime

data class DeleteBusinessAccountEvent(
    val businessAccountDto: BusinessAccountDto,
    val actorDto: ActorEventDto,
    val deletedAt: LocalDateTime
) : BusinessAccountEvent(BusinessAccountEventType.BA_DELETE, businessAccountDto.id.toString())

data class BusinessAccountDto(
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

enum class UserType {
    USER, BA, ADMIN
}

data class ActorEventDto(
    var userType: String,
    var userId: Long? = null,
    var businessUserId: Long? = null,
    var businessAccountId: Long? = null,
    var nickname: String? = null,
    var regionId: Int? = null,
    var displayRegionCheckinsCount: Int? = null,
)


sealed class BusinessAccountEvent(
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

object BusinessAccountEventType {
    const val BA_CREATE = "ba.create"
    const val BA_UPDATE = "ba.update"
    const val BA_DELETE = "ba.delete"
    const val BA_UNDELETE = "ba.undelete"
    const val BA_FOLLOW = "ba.follow"
    const val BA_UNFOLLOW = "ba.unfollow"
    const val BA_NOTIFICATION_ALLOW = "ba.notification.allow"
    const val BA_NOTIFICATION_DISALLOW = "ba.notification.disallow"
    const val BU_CREATE = "bu.create"
    const val BA_EXTRA_CREATE = "ba.extra.create"
    const val BA_EXTRA_UPDATE = "ba.extra.update"
    const val BAU_CREATE = "bau.create"
    const val BAU_DELETE = "bau.delete"

}