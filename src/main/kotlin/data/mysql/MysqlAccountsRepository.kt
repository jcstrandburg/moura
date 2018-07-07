package data.mysql

import domain.Change
import domain.ChangeSet
import domain.accounts.IAccountsRepository
import domain.accounts.Organization
import domain.accounts.OrganizationCreateSet
import domain.accounts.User
import domain.accounts.UserChangeSet
import domain.accounts.UserCreateSet
import org.sql2o.Sql2o
import skl2o.PrimaryKey
import skl2o.TableName
import skl2o.addParameters
import skl2o.executeAndFetchAs
import skl2o.executeScalarAs
import skl2o.mySqlFields
import skl2o.mySqlUpdateStatement
import skl2o.openAndApply
import skl2o.simpleDelete
import skl2o.simpleInsert
import skl2o.simpleSelect
import skl2o.simpleSelectByPrimaryKey

class MysqlAccountsRepository(private val sql2o: Sql2o) : IAccountsRepository {

    override fun getOrganization(id: Int): Organization? = sql2o.openAndApply {
        simpleSelectByPrimaryKey<DbOrganization>(id)?.toDomain()
    }

    override fun getOrganization(token: String): Organization? = sql2o.openAndApply {
        simpleSelect<DbOrganization>("token=:token", mapOf("token" to token)).singleOrNull()
    }?.toDomain()

    override fun createOrganization(org: OrganizationCreateSet): Organization {
        val id = sql2o.openAndApply { simpleInsert(DbOrganizationCreate(org.name, org.token)) }
        return getOrganization(id)!!
    }

    override fun getOrganizationsForUser(userId: Int): List<Organization> = sql2o.openAndApply {
        val organizationIds = simpleSelect<DbOrganizationRelationship>(
                "user_id=:userId",
                mapOf("userId" to userId))
            .map { it.organizationId }

        simpleSelect<DbOrganization>(
            "organization_id IN (:organizationIds)",
            mapOf("organizationIds" to organizationIds))
    }.map { it.toDomain() }

    override fun createUser(user: UserCreateSet): User {
        val userId = sql2o.openAndApply { simpleInsert(DbUserCreate(user.name, user.password, user.alias, user.email, user.token)) }
        return getUserById(userId)!!
    }

    override fun getUserByEmail(email: String): User? = sql2o.openAndApply {
        simpleSelect<DbUser>("email=:email", mapOf("email" to email)).singleOrNull()
    }?.toDomain()

    override fun getUserById(id: Int): User? = sql2o.openAndApply { simpleSelectByPrimaryKey<DbUser>(id) }?.toDomain()

    override fun getUserByAuthToken(authToken: String): User? = sql2o.openAndApply {
        simpleSelect<DbUser>("auth_token=:authToken", mapOf("authToken" to authToken)).singleOrNull()
    }?.toDomain()

    override fun getUserByToken(token: String): User? = sql2o.openAndApply {
        simpleSelect<DbUser>("token=:token", mapOf("token" to token)).singleOrNull()
    }?.toDomain()

    override fun updateUser(userId: Int, changeSet: UserChangeSet): User? {
        val changes = DbUserChangeSet.from(changeSet).getChanges()
        if (changes.any()) {
            sql2o.openAndApply {
                val sql = mySqlUpdateStatement(changes, "users", "user_id=:userId")
                createQuery(sql)
                    .addParameters(changes)
                    .addParameter("userId", userId)
                    .executeUpdate()
            }
        }

        return getUserById(userId)
    }

    override fun addUserToOrganization(userId: Int, organizationId: Int): Unit = sql2o.openAndApply {
        simpleInsert(DbOrganizationRelationshipCreate(userId, organizationId))
    }

    override fun removeUserFromOrganization(userId: Int, organizationId: Int) = sql2o.openAndApply {
        simpleDelete<DbOrganizationRelationship>(
            "user_id=:userId AND organization_id=:organizationId",
            mapOf("userId" to userId, "organizationId" to organizationId))
    }

    override fun isUserInOrganization(userId: Int, organizationId: Int): Boolean = sql2o.openAndApply {
        createQuery("SELECT COUNT(user_id) FROM `organization_relationships` WHERE user_id=:userId AND organization_id=:organizationId")
            .addParameter("userId", userId)
            .addParameter("organizationId", organizationId)
            .executeScalarAs<Long>()
    } > 0

    override fun getUsersForOrganization(organizationId: Int): List<User> {
        val sql = """
SELECT ${mySqlFields<DbUser>("u")}
FROM users u
JOIN organization_relationships r
ON u.user_id = r.user_id
WHERE r.organization_id=:organizationId
"""
        return sql2o.openAndApply { createQuery(sql)
            .addParameter("organizationId", organizationId)
            .executeAndFetchAs<DbUser>()
        }.map { it.toDomain() }
    }

    companion object {
        private fun DbUser.toDomain() =
            User(this.userId, this.username, this.password, this.authToken ?: "", this.alias, this.email, this.token)

        private fun DbOrganization.toDomain() = Organization(this.organizationId, this.name, this.token)
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
        val email: String,
        val token: String)

    class DbUserChangeSet(
        val username: String? = null,
        val password: String? = null,
        val alias: String? = null,
        val authToken: Change<String?>? = null)
        : ChangeSet<DbUser>() {

        companion object {
            fun from(src: UserChangeSet): DbUserChangeSet =
                DbUserChangeSet(src.name, src.password, src.alias, src.authToken)
        }
    }

    @TableName("users")
    data class DbUserCreate (
        val username: String,
        val password: String,
        val alias: String,
        val email: String,
        val token: String)

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
