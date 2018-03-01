package domain.accounts

data class User(
    val id: Int,
    val name: String,
    val password: String,
    val authToken: String?,
    val alias: String,
    val email: String)

data class UserCreate(
    val name: String,
    val password: String,
    val alias: String,
    val email: String)

data class Organization(
    val id: Int,
    val name: String,
    val token: String)

data class OrganizationCreate(
    val name: String,
    val token: String)

interface IAccountsReadRepository {
    fun getOrganization(id: Int): Organization?
    fun getOrganization(token: String): Organization?
    fun getOrganizationsForUser(userId: Int): List<Organization>
    fun getUserByEmail(name: String): User?
    fun getUserById(id: Int): User?
    fun getUsersForOrganization(organizationId: Int): List<User>
    fun isUserInOrganization(userId: Int, organizationId: Int): Boolean
}

interface IAccountsRepository : IAccountsReadRepository {
    fun createOrganization(org: OrganizationCreate): Organization
    fun createUser(user: UserCreate): User
    fun setUserAuthToken(id: Int, token: String): User
    fun updateUser(user: User): User?
    fun addUserToOrganization(userId: Int, organizationId: Int)
    fun removeUserFromOrganization(userId: Int, organizationId: Int)
}
