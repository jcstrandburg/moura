package data.inmemory

import data.util.AutoIncrementMemoryRepository
import domain.accounts.IAccountsRepository
import domain.accounts.Organization
import domain.accounts.OrganizationCreate
import domain.accounts.User
import domain.accounts.UserCreate

class InMemoryAccountsRepository : IAccountsRepository {

    private val users = AutoIncrementMemoryRepository<User>()
    private val organizations = AutoIncrementMemoryRepository<Organization>()
    private val userIdsByOrgId = HashMap<Int, HashSet<Int>>()

    override fun createUser(user: UserCreate): User =
        users.insert { id -> User(id, user.name, user.password, null, user.alias, user.email) }

    override fun getUserByEmail(email: String): User? =
        users.entities.singleOrNull { it.email == email }

    override fun getUserById(id: Int): User? =
        users.get(id)

    override fun setUserAuthToken(id: Int, token: String): User {
        val user = users.get(id)!!.copy(authToken = token)
        users.replace(id, user)
        return user
    }

    override fun updateUser(user: User): User? {
        if (!users.containsId(user.id))
            return null

        users.replace(user.id, user)
        return user
    }

    override fun getUsersForOrganization(organizationId: Int): List<User> =
        userIdsByOrgId[organizationId].orEmpty().let { ids -> users.get(ids) }

    override fun addUserToOrganization(userId: Int, organizationId: Int) {
        val userIdSet = userIdsByOrgId[organizationId]

        if (userIdSet != null) {
            userIdSet.add(userId)
        } else {
            throw Exception()
        }
    }

    override fun removeUserFromOrganization(userId: Int, organizationId: Int) {
        val userIdSet = userIdsByOrgId[organizationId]

        if (userIdSet != null) {
            userIdSet.remove(userId)
        } else {
            throw Exception()
        }
    }

    override fun isUserInOrganization(userId: Int, organizationId: Int): Boolean =
        userIdsByOrgId[organizationId]?.contains(userId) ?: false

    override fun getOrganization(id: Int): Organization? =
        organizations.get(id)

    override fun getOrganization(token: String): Organization? =
        organizations.entities.singleOrNull { it.token == token }

    override fun createOrganization(org: OrganizationCreate): Organization =
        organizations.insert { id -> Organization(id, org.name, org.token) }

    override fun getOrganizationsForUser(userId: Int): List<Organization> {
        return userIdsByOrgId.keys
            .filter { orgId -> userIdsByOrgId[orgId]?.contains(userId) ?: false }
            .let { ids -> organizations.get(ids) }
    }
}