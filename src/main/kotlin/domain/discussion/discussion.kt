package domain.discussion

import java.time.OffsetDateTime
import java.time.ZoneOffset

data class DiscussionMessage(
    val id: Int,
    val contextId: Int,
    val userId: Int,
    val content: String,
    val createdTime: OffsetDateTime)

data class DiscussionMessageCreate(
    val contextId: Int,
    val userId: Int,
    val content: String)

interface IDiscussionRepository {
    fun createDiscussionMessage(message: DiscussionMessageCreate, createdTime: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)): DiscussionMessage
    fun getDiscussionMessages(contextId: Int): List<DiscussionMessage>
}

interface IDiscussionContextRepository {
    fun createContextId(): Int
}
