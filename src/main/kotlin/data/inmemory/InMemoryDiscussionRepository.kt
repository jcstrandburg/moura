package data.inmemory

import domain.discussion.DiscussionMessage
import domain.discussion.DiscussionMessageCreate
import domain.discussion.IDiscussionContextRepository
import domain.discussion.IDiscussionRepository
import data.util.AutoIncrementMemoryRepository
import java.time.OffsetDateTime

class InMemoryDiscussionRepository: IDiscussionRepository, IDiscussionContextRepository {

    private var nextContextId = 1000
    private val messages = AutoIncrementMemoryRepository<DiscussionMessage>()

    override fun createContextId() = nextContextId++

    override fun addDiscussionMessage(message: DiscussionMessageCreate): DiscussionMessage {
        return messages.insert { id ->
            DiscussionMessage(
                id,
                message.contextId,
                message.userId,
                message.content,
                OffsetDateTime.now())
        }
    }

    override fun getDiscussionMessages(contextId: Int): List<DiscussionMessage> {
        return messages.entities.filter { message -> message.contextId == contextId }
    }
}