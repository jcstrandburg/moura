package integrationtests.web

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import data.inmemory.InMemoryAccountsRepository
import data.inmemory.InMemoryDiscussionRepository
import data.inmemory.InMemoryProjectRepository
import domain.Change
import domain.accounts.IAccountsReadRepository
import domain.accounts.IAccountsRepository
import domain.accounts.UserChangeSet
import domain.accounts.UserCreateSet
import domain.discussion.IDiscussionRepository
import domain.projects.IProjectRepository
import junit.framework.TestCase
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import services.AuthenticationService
import vulcan.Container
import vulcan.Lifecycle
import web.Moura
import web.api.v1.CurrentUserDto
import java.io.StringReader
import kotlin.reflect.KClass

@RunWith(JUnit4::class)
class AuthTests : TestCase() {

    @Test
    fun `test with no auth`() {
        val response = khttp.get(url = baseUri + "api/v1/users/me")
        assertEquals(401, response.statusCode)
    }

    @Test
    fun `test cookie auth`() {
        val cookies = mapOf("authToken" to authenticatedUser.authToken!!)

        val response = khttp.get(baseUri + "api/v1/users/me", cookies = cookies)

        assertEquals(200, response.statusCode)
        val currentUserDto = response.text.deserialize(CurrentUserDto::class)
        assertEquals(authenticatedUser.id, currentUserDto.user.id)
    }

    @Test
    fun `test header auth`() {
        val headers = mapOf("X-Authorization" to "TOKEN ${authenticatedUser.authToken}")

        val response = khttp.get(baseUri + "api/v1/users/me", headers = headers)

        assertEquals(200, response.statusCode)
        val currentUserDto = response.text.deserialize(CurrentUserDto::class)
        assertEquals(authenticatedUser.id, currentUserDto.user.id)
    }

    companion object {

        private val port = 8000
        private val baseUri = "http://localhost:$port/"
        private val klaxon = Klaxon()
        private lateinit var app: Moura

        @BeforeClass
        @JvmStatic
        fun setup() {
            app = Moura(port, container)
            app.start()
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            app.stop()
        }

        private fun <T : Any> String.deserialize(kclass: KClass<T>): T {
            try {
                return klaxon.fromJsonObject(
                    klaxon.parser(kclass).parse(StringReader(this)) as JsonObject,
                    kclass.java,
                    kclass) as T
            } catch (e: KlaxonException) {
                val exception = Exception("Failed to deserialize to type ${kclass.simpleName}: `$this`")
                exception.addSuppressed(e)
                throw exception
            }
        }

        private val container by lazy {
            Container().apply {
                register<IAccountsReadRepository, IAccountsRepository>()
                register<IAccountsRepository, InMemoryAccountsRepository>()
                register<IProjectRepository, InMemoryProjectRepository>()
                register<IDiscussionRepository, InMemoryDiscussionRepository>()
                register<AuthenticationService, AuthenticationService>(Lifecycle.PerContainer)
            }
        }

        private val accountsRepository by lazy { container.get<IAccountsRepository>() }

        private val authenticatedUser by lazy {
            val user = accountsRepository.createUser(UserCreateSet("name", "password", "alias", "email@example.com", "token"))
            accountsRepository.updateUser(user.id, UserChangeSet(authToken = Change("auth-token")))!!
        }
    }
}