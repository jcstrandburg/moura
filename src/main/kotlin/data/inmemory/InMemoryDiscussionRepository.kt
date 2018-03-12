package data.inmemory

import data.util.AutoIncrementMemoryRepository
import domain.discussion.DiscussionMessage
import domain.discussion.DiscussionMessageCreate
import domain.discussion.IDiscussionContextRepository
import domain.discussion.IDiscussionRepository
import java.time.OffsetDateTime

class InMemoryDiscussionRepository: IDiscussionRepository, IDiscussionContextRepository {

    private var nextContextId = 1000
    private val messages = AutoIncrementMemoryRepository<DiscussionMessage>()

    override fun createContextId() = nextContextId++

    override fun createDiscussionMessage(message: DiscussionMessageCreate, createdTime: OffsetDateTime): DiscussionMessage {
        return messages.insert { id ->
            DiscussionMessage(
                id,
                message.contextId,
                message.userId,
                message.content,
                createdTime)
        }
    }

    override fun getDiscussionMessages(contextId: Int): List<DiscussionMessage> {
        return messages.entities.filter { message -> message.contextId == contextId }
    }
}