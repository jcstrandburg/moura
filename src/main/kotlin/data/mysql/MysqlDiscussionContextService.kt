package data.mysql

import domain.discussion.DiscussionMessage
import domain.discussion.DiscussionMessageCreate
import domain.discussion.IDiscussionContextRepository
import domain.discussion.IDiscussionRepository
import org.sql2o.Sql2o
import skl2o.PrimaryKey
import skl2o.TableName
import skl2o.getKeyAs
import skl2o.openAndUse
import skl2o.simpleInsert
import skl2o.simpleSelect
import skl2o.simpleSelectByPrimaryKey
import skl2o.toTimestamp
import skl2o.toUtcOffsetDateTime
import java.sql.Timestamp
import java.time.OffsetDateTime

class MysqlDiscussionRepository(private val sql2o: Sql2o) : IDiscussionContextRepository, IDiscussionRepository {

    override fun createDiscussionMessage(message: DiscussionMessageCreate, createdTime: OffsetDateTime): DiscussionMessage {

        val dbDiscussionMessage = DbDiscussionMessageCreate(
            message.contextId,
            message.userId,
            message.content,
            toTimestamp(createdTime))

        val dbMessage = sql2o.openAndUse { conn ->
            val id = conn.simpleInsert(dbDiscussionMessage)
            conn.simpleSelectByPrimaryKey<DbDiscussionMessage>(id)!!
        }

        return dbMessage.toDomain()
    }

    override fun getDiscussionMessages(contextId: Int): List<DiscussionMessage> {

        val dbMessages = sql2o.openAndUse { conn ->
            conn.simpleSelect<DbDiscussionMessage>("discussion_context_id=:contextId", mapOf("contextId" to contextId))
        }

        return dbMessages.map { it.toDomain() }
    }

    override fun createContextId(): Int {
        val sql = "INSERT INTO discussion_contexts VALUES ()"

        return sql2o.openAndUse { conn ->
            conn.createQuery(sql).use { query ->
                query.executeUpdate().getKeyAs<Int>()
            }
        }
    }

    companion object {
        fun DbDiscussionMessage.toDomain() =
            DiscussionMessage(discussionMessageId, discussionContextId, userId, content, toUtcOffsetDateTime(createdTime))
    }

    @TableName("discussion_messages")
    data class DbDiscussionMessage(
        @PrimaryKey
        val discussionMessageId: Int,
        val discussionContextId: Int,
        val userId: Int,
        val content: String,
        val createdTime: Timestamp)

    @TableName("discussion_messages")
    data class DbDiscussionMessageCreate(
        val discussionContextId: Int,
        val userId: Int,
        val content: String,
        val createdTime: Timestamp)
}