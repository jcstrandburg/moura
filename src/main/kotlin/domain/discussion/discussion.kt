package domain.discussion

import java.time.OffsetDateTime

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
    fun addDiscussionMessage(message: DiscussionMessageCreate): DiscussionMessage
    fun getDiscussionMessages(contextId: Int): List<DiscussionMessage>
}

interface IDiscussionContextRepository {
    fun createContextId(): Int
}
