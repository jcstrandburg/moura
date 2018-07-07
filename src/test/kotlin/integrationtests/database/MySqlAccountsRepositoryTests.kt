package integrationtests.database

import domain.Change
import domain.accounts.IAccountsRepository
import domain.accounts.UserChangeSet
import junit.framework.TestCase
import java.util.*

class MySqlAccountsRepositoryTests : TestCase() {
    private val accountsRepository: IAccountsRepository = DatabaseObjectMother.accountsRepository

    fun `test create and get user`() {
        val createdUser = DatabaseObjectMother.createTestUser()

        assertEquals(createdUser, accountsRepository.getUserById(createdUser.id)!!)
        assertEquals(createdUser, accountsRepository.getUserByToken(createdUser.token)!!)
    }

    fun `test update user`() {
        val user = DatabaseObjectMother.createTestUser()

        val userUpdate = UserChangeSet(
            name = user.name+"!",
            alias = "test"+user.alias,
            password = user.password+"!",
            authToken = Change(UUID.randomUUID().toString()))
        val updatedUser = accountsRepository.updateUser(user.id, userUpdate)!!

        assertEquals(userUpdate.name, updatedUser.name)
        assertEquals(userUpdate.alias, updatedUser.alias)
        assertEquals(userUpdate.password, updatedUser.password)
        assertEquals(userUpdate.authToken?.value, updatedUser.authToken)

        val fetchedUser = accountsRepository.getUserById(user.id)
        assertEquals(updatedUser, fetchedUser)
    }

    fun `test create and get organization`() {
        val createdOrganization = DatabaseObjectMother.createTestOrganization()

        val organization = accountsRepository.getOrganization(createdOrganization.id)
        assertEquals(createdOrganization, organization)
    }

    fun `test organization membership`() {
        val organizationId = DatabaseObjectMother.createTestOrganization().id
        val users = 0.rangeTo(1).map { DatabaseObjectMother.createTestUser() }

        assertFalse(accountsRepository.isUserInOrganization(users[0].id, organizationId))
        assertFalse(accountsRepository.isUserInOrganization(users[1].id, organizationId))

        accountsRepository.addUserToOrganization(users[0].id, organizationId)

        assertTrue(accountsRepository.isUserInOrganization(users[0].id, organizationId))
        assertFalse(accountsRepository.isUserInOrganization(users[1].id, organizationId))

        val orgs = accountsRepository.getOrganizationsForUser(users[0].id)
        assertTrue(orgs.map { it.id }.contains(organizationId))
    }
}