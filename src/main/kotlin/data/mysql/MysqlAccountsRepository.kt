package data.mysql

import domain.accounts.IAccountsRepository
import domain.accounts.Organization
import domain.accounts.OrganizationCreate
import domain.accounts.User
import domain.accounts.UserCreate
import org.sql2o.Sql2o
import skl2o.PrimaryKey
import skl2o.TableName
import skl2o.executeAndFetchAs
import skl2o.executeScalarAs
import skl2o.mySqlFields
import skl2o.openAndUse
import skl2o.simpleDelete
import skl2o.simpleInsert
import skl2o.simpleSelect
import skl2o.simpleSelectByPrimaryKey

class MysqlAccountsRepository(private val sql2o: Sql2o) : IAccountsRepository {

    override fun getOrganization(id: Int): Organization? =
        sql2o.openAndUse { conn -> conn.simpleSelectByPrimaryKey<DbOrganization>(id) }?.toDomain()

    override fun getOrganization(token: String): Organization? {
        return sql2o.openAndUse { conn ->
            conn.simpleSelect<DbOrganization>("token=:token", mapOf("token" to token))
                .singleOrNull()
                ?.toDomain()
        }
    }

    override fun createOrganization(org: OrganizationCreate): Organization {
        val id = sql2o.openAndUse { conn ->
            conn.simpleInsert(DbOrganizationCreate(org.name, org.token))
        }

        return getOrganization(id)!!
    }

    override fun getOrganizationsForUser(userId: Int): List<Organization> {
        val dbOrganizations = sql2o.openAndUse { conn ->
            val organizationIds = conn
                .simpleSelect<DbOrganizationRelationship>(
                    "user_id=:userId",
                    mapOf("userId" to userId))
                .map { it.organizationId }

            conn.simpleSelect<DbOrganization>("organization_id IN (organizationIds)", mapOf("organizationIds" to organizationIds))
        }

        return dbOrganizations.map { it.toDomain() }
    }

    override fun createUser(user: UserCreate): User {
        val userId = sql2o.openAndUse { conn ->
            conn.simpleInsert(DbUserCreate(user.name, user.password, user.alias, user.email))
        }
        return getUserById(userId)!!
    }

    override fun getUserByEmail(email: String): User? {
        val dbUser = sql2o.openAndUse { conn ->
            conn.simpleSelect<DbUser>("email=:email", mapOf("email" to email)).singleOrNull()
        }
        return dbUser?.toDomain()
    }

    override fun getUserById(id: Int): User? {
        val dbUser = sql2o.openAndUse { conn ->
            conn.simpleSelectByPrimaryKey<DbUser>(id)
        }
        return dbUser?.toDomain()
    }

    override fun setUserAuthToken(id: Int, token: String): User {
        val sql = "UPDATE users SET auth_token=:authToken WHERE user_id=:userId"

        sql2o.openAndUse { conn ->
            conn.createQuery(sql)
                .addParameter("userId", id)
                .addParameter("authToken", token)
                .executeUpdate()
        }

        return getUserById(id) ?: throw NoSuchElementException()
    }

    override fun updateUser(user: User): User? {
        val sql = """
UPDATE users
SET
    username=:username,
    password=:password,
    auth_token=:authToken,
    email=:email
WHERE
    user_id=:userId
"""

        sql2o.open().createQuery(sql).use { query ->
            query.addParameter("username", user.name)
                .addParameter("password", user.password)
                .addParameter("authToken", user.authToken)
                .addParameter("email", user.email)
                .addParameter("userId", user.id)
                .executeUpdate()
        }

        return getUserById(user.id)
    }

    override fun addUserToOrganization(userId: Int, organizationId: Int) {
        sql2o.openAndUse {
            it.simpleInsert(DbOrganizationRelationshipCreate(userId, organizationId))
        }
    }

    override fun removeUserFromOrganization(userId: Int, organizationId: Int) {
        sql2o.openAndUse {
            it.simpleDelete<DbOrganizationRelationship>(
                "user_id=:userId AND organization_id=:organizationId",
                mapOf("userId" to userId, "organizationId" to organizationId))
        }
    }

    override fun isUserInOrganization(userId: Int, organizationId: Int): Boolean {
        val sql = """
SELECT COUNT(user_id) FROM `organization_relationships` WHERE user_id=:userId AND organization_id=:organizationId
"""

        val count = sql2o.openAndUse {
            it.createQuery(sql)
                .addParameter("userId", userId)
                .addParameter("organizationId", organizationId)
                .executeScalarAs<Long>()
        }

        return count > 0
    }

    override fun getUsersForOrganization(organizationId: Int): List<User> {
        val sql = """
SELECT ${mySqlFields<DbUser>("u")}
FROM users u
JOIN organization_relationships r
ON u.user_id = r.user_id
WHERE r.organization_id=:organizationId
"""
        val dbUsers = sql2o.open().createQuery(sql).use { query ->
            query.addParameter("organizationId", organizationId)
                .executeAndFetchAs<DbUser>()
        }

        return dbUsers.map { it.toDomain() }
    }

    companion object {
        private fun DbUser.toDomain() =
            User(this.userId, this.username, this.password, this.authToken ?: "", this.alias, this.email)

        private fun DbOrganization.toDomain() =
            Organization(this.organizationId, this.name, this.token)
    }

    @TableName("organization_relationships")
    data class DbOrganizationRelationship(
        @PrimaryKey
        val organizationRelationshipId: Int,
        val userId: Int,
        val organizationId: Int)

    @TableName("organization_relationships")
    data class DbOrganizationRelationshipCreate(
        val userId: Int,
        val organizationId: Int)

    @TableName("users")
    data class DbUser (
        @PrimaryKey
        val userId: Int,
        val username: String,
        val password: String,
        val alias: String,
        val authToken:String?,
        val email: String)

    @TableName("users")
    data class DbUserCreate (
        val username: String,
        val password: String,
        val alias: String,
        val email: String)

    @TableName("organizations")
    data class DbOrganization(
        @PrimaryKey
        val organizationId: Int,
        val name: String,
        val token: String)

    @TableName("organizations")
    data class DbOrganizationCreate(
        val name: String,
        val token: String)
}
