package domain.discussion

import java.time.ZoneOffset
import java.time.ZonedDateTime

data class DiscussionMessage(
    val id: Int,
    val contextId: Int,
    val userId: Int,
    val content: String,
    val createdTime: ZonedDateTime)

data class DiscussionMessageCreate(
    val contextId: Int,
    val userId: Int,
    val content: String)

interface IDiscussionRepository {
    fun createDiscussionMessage(message: DiscussionMessageCreate, createdTime: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)): DiscussionMessage
    fun getDiscussionMessages(contextId: Int): List<DiscussionMessage>
}

interface IDiscussionContextRepository {
    fun createContextId(): Int
}
