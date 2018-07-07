package integrationtests.web

import data.mysql.MysqlAccountsRepository
import data.mysql.MysqlDiscussionRepository
import data.mysql.MysqlProjectRepository
import domain.accounts.Organization
import domain.accounts.OrganizationCreateSet
import domain.accounts.User
import domain.accounts.UserCreateSet
import domain.discussion.DiscussionMessage
import domain.discussion.DiscussionMessageCreate
import domain.projects.Project
import domain.projects.ProjectCreate
import junit.framework.TestCase
import org.sql2o.Sql2o
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals

class DatabaseObjectMother {
    companion object {
        val sql2o by lazy {
            Sql2o(
                "jdbc:mysql://localhost:3306/moura",
                "root",
                "jimbolina")
        }

        val accountsRepository by lazy { MysqlAccountsRepository(sql2o) }

        val discussionRepository by lazy { MysqlDiscussionRepository(sql2o) }

        val projectsRepository by lazy { MysqlProjectRepository(sql2o, discussionRepository) }

        fun createTestUser(): User {
            val uuid = UUID.randomUUID()

            val userCreate = UserCreateSet(
                name = uuid.toString(),
                password = "pathword",
                alias = "bob loblaw",
                email = "$uuid@example.com",
                token = uuid.toString())

            val createdUser = accountsRepository.createUser(userCreate)

            TestCase.assertEquals(userCreate.name, createdUser.name)
            TestCase.assertEquals(userCreate.password, createdUser.password)
            TestCase.assertEquals(userCreate.alias, createdUser.alias)
            TestCase.assertEquals(userCreate.email, createdUser.email)

            return createdUser
        }

        fun createTestOrganization(): Organization {
            val uuid = UUID.randomUUID()

            val organizationCreate = OrganizationCreateSet(name = uuid.toString(), token = uuid.toString())
            val organization = accountsRepository.createOrganization(organizationCreate)

            TestCase.assertEquals(organizationCreate.name, organization.name)
            TestCase.assertEquals(organizationCreate.token, organization.token)

            return organization
        }

        fun createTestProject(organizationId: Int, parentProjectId: Int?): Project {
            val projectCreate = ProjectCreate(
                name = UUID.randomUUID().toString(),
                organizationId = organizationId,
                parentProjectId = parentProjectId)

            val project = projectsRepository.createProject(projectCreate)
            assertEquals(projectCreate.name, project.name)
            assertEquals(projectCreate.organizationId, project.organizationId)
            assertEquals(projectCreate.parentProjectId, project.parentProjectId)

            return project
        }

        fun createTestMessage(contextId: Int, userId: Int, createdTime: ZonedDateTime): DiscussionMessage {
            val messageCreate = DiscussionMessageCreate(contextId, userId, "Message ${UUID.randomUUID()}")

            val message = discussionRepository.createDiscussionMessage(messageCreate, createdTime)
            assertEquals(messageCreate.contextId, message.contextId)
            assertEquals(messageCreate.userId, message.userId)
            assertEquals(messageCreate.content, message.content)
            assertEquals(createdTime.withNano(0), message.createdTime.withNano(0))

            return message
        }
    }
}