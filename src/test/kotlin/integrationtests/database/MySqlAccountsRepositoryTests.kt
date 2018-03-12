package integrationtests.database

import domain.accounts.IAccountsRepository
import junit.framework.TestCase
import java.util.*

class MySqlAccountsRepositoryTests : TestCase() {
    private val accountsRepository: IAccountsRepository = DatabaseObjectMother.accountsRepository

    fun `test create and get user`() {
        val createdUser = DatabaseObjectMother.createTestUser()

        val fetchedUser = accountsRepository.getUserById(createdUser.id)!!
        assertEquals(createdUser, fetchedUser)
    }

    fun `test update user`() {
        val user = DatabaseObjectMother.createTestUser()

        val userUpdate = user.copy(
            name = user.name+"!",
            email = "test"+user.email,
            password = user.password+"!")
        val updatedUser = accountsRepository.updateUser(userUpdate)
        assertNotNull(updatedUser)
        assertEquals(userUpdate, updatedUser)

        val fetchedUser = accountsRepository.getUserById(user.id)
        assertEquals(userUpdate, fetchedUser)
    }

    fun `test set auth token`() {
        val user = DatabaseObjectMother.createTestUser()
        val token = UUID.randomUUID().toString()

        val updatedUser = accountsRepository.setUserAuthToken(user.id, token)
        assertNotNull(updatedUser)
        assertEquals(token, updatedUser.authToken)

        val fetchedUser = accountsRepository.getUserById(user.id)!!
        assertEquals(token, fetchedUser.authToken)
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
    }
}