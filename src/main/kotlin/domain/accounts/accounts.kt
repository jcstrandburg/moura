package domain.accounts

import domain.Change
import domain.ChangeSet

data class User(
    val id: Int,
    val name: String,
    val password: String,
    val authToken: String?,
    val alias: String,
    val email: String,
    val token: String)

class UserChangeSet(
    val name: String? = null,
    val password: String? = null,
    val alias: String? = null,
    val authToken: Change<String?>? = null)
    : ChangeSet<User>()

data class UserCreateSet(
    val name: String,
    val password: String,
    val alias: String,
    val email: String,
    val token: String)

data class Organization(
    val id: Int,
    val name: String,
    val token: String)

data class OrganizationCreateSet(
    val name: String,
    val token: String)

interface IAccountsReadRepository {
    fun getOrganization(id: Int): Organization?
    fun getOrganization(token: String): Organization?
    fun getOrganizationsForUser(userId: Int): List<Organization>
    fun getUserByEmail(email: String): User?
    fun getUserById(id: Int): User?
    fun getUserByAuthToken(authToken: String): User?
    fun getUserByToken(token: String): User?
    fun getUsersForOrganization(organizationId: Int): List<User>
    fun isUserInOrganization(userId: Int, organizationId: Int): Boolean
}

interface IAccountsRepository : IAccountsReadRepository {
    fun createOrganization(org: OrganizationCreateSet): Organization
    fun createUser(user: UserCreateSet): User
    fun updateUser(userId: Int, changeSet: UserChangeSet): User?
    fun addUserToOrganization(userId: Int, organizationId: Int)
    fun removeUserFromOrganization(userId: Int, organizationId: Int)
}
