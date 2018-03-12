package integrationtests.database

import junit.framework.TestCase
import java.time.OffsetDateTime
import java.time.ZoneOffset

class MySqlDiscussionRepositoryTests : TestCase() {

    private val repository = DatabaseObjectMother.discussionRepository
    private val users = 0.rangeTo(1).map { DatabaseObjectMother.createTestUser() }

    fun `test create context and messages then retrieve them`() {
        val contextId = repository.createContextId()

        var messages = listOf(
            DatabaseObjectMother.createTestMessage(contextId, users[0].id, OffsetDateTime.now(ZoneOffset.UTC).minusHours(2)),
            DatabaseObjectMother.createTestMessage(contextId, users[1].id, OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5)))

        var fetchedMessages = repository.getDiscussionMessages(contextId)
        assertEquals(messages, fetchedMessages)
    }
}