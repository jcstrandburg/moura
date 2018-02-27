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
import java.time.OffsetDateTime

class MysqlDiscussionContextService(private val sql2o: Sql2o) : IDiscussionContextRepository, IDiscussionRepository {

    override fun addDiscussionMessage(message: DiscussionMessageCreate): DiscussionMessage {

        val dbDiscussionMessage = DbDiscussionMessageCreate(message.contextId, message.userId, message.content, OffsetDateTime.now())

        val dbMessage =sql2o.openAndUse { conn ->
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
            DiscussionMessage(discussionMessageId, discussionContextId, userId, content, createdTime)
    }

    @TableName("discussion_message")
    data class DbDiscussionMessage(
        @PrimaryKey
        val discussionMessageId: Int,
        val discussionContextId: Int,
        val userId: Int,
        val content: String,
        val createdTime: OffsetDateTime)

    @TableName("discussion_message")
    data class DbDiscussionMessageCreate(
        val discussionContextId: Int,
        val userId: Int,
        val content: String,
        val createdTime: OffsetDateTime)
}