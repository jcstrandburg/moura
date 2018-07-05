package data.mysql

import domain.discussion.DiscussionMessage
import domain.discussion.DiscussionMessageCreate
import domain.discussion.IDiscussionContextRepository
import domain.discussion.IDiscussionRepository
import org.sql2o.Sql2o
import skl2o.PrimaryKey
import skl2o.TableName
import skl2o.openAndApply
import skl2o.simpleInsert
import skl2o.simpleSelect
import skl2o.simpleSelectByPrimaryKey
import skl2o.toTimestamp
import skl2o.toUtcZonedDateTime
import java.sql.Timestamp
import java.time.ZonedDateTime

class MysqlDiscussionRepository(private val sql2o: Sql2o) : IDiscussionContextRepository, IDiscussionRepository {

    override fun createDiscussionMessage(message: DiscussionMessageCreate, createdTime: ZonedDateTime): DiscussionMessage = sql2o.openAndApply {
        val id = simpleInsert(DbDiscussionMessageCreate(message.contextId, message.userId, message.content, createdTime.toTimestamp()))
        simpleSelectByPrimaryKey<DbDiscussionMessage>(id)!!
    }.toDomain()


    override fun getDiscussionMessages(contextId: Int): List<DiscussionMessage> = sql2o.openAndApply {
        simpleSelect<DbDiscussionMessage>("discussion_context_id=:contextId", mapOf("contextId" to contextId))
    }.map { it.toDomain() }

    override fun createContextId(): Int = sql2o.openAndApply {
        simpleInsert(DbDiscussionContextCreate())
    }

    companion object {
        fun DbDiscussionMessage.toDomain() =
            DiscussionMessage(discussionMessageId, discussionContextId, userId, content, createdTime.toUtcZonedDateTime())
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

    @TableName("discussion_contexts")
    class DbDiscussionContextCreate
}