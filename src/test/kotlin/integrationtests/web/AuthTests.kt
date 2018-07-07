package integrationtests.web

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import domain.Change
import domain.accounts.User
import domain.accounts.UserChangeSet
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import web.Moura
import web.api.v1.CurrentUserDto
import java.io.StringReader
import java.util.*
import kotlin.reflect.KClass

@RunWith(JUnit4::class)
class AuthTests : TestCase() {

    val port = 8000
    private val url = "http://127.0.0.1:$port/"

    @Before
    override public fun setUp() {
        app = Moura(port)
        app.start()
    }

    @After
    override public fun tearDown() {
        app.stop()
    }

    @Test
    fun `test with no auth`() {
        val response = khttp.get(url = url + "api/v1/users/me")
        assertEquals(401, response.statusCode)
    }

    @Test
    fun `test cookie auth`() {
        val cookies = mapOf("authToken" to user.authToken!!)

        val response = khttp.get(url = url + "api/v1/users/me", cookies = cookies)

        assertEquals(200, response.statusCode)
        val currentUserDto = response.text.deserialize(CurrentUserDto::class)
        assertEquals(user.id, currentUserDto.user.id)
    }

    @Test
    fun `test header auth`() {
        val headers = mapOf("X-Authorization" to "TOKEN ${user.authToken}")

        val response = khttp.get(url = url + "api/v1/users/me", headers = headers)

        assertEquals(200, response.statusCode)
        val currentUserDto = response.text.deserialize(CurrentUserDto::class)
        assertEquals(user.id, currentUserDto.user.id)
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            val createdUser = DatabaseObjectMother.createTestUser()

            user = DatabaseObjectMother.accountsRepository.updateUser(
                createdUser.id,
                UserChangeSet(authToken = Change(UUID.randomUUID().toString())))!!
        }

        @JvmStatic
        private val klaxon = Klaxon()

        @JvmStatic
        private lateinit var user: User
        @JvmStatic
        private lateinit var app: Moura

        private fun <T : Any> String.deserialize(kclass: KClass<T>): T {
            return klaxon.fromJsonObject(
                klaxon.parser(kclass).parse(StringReader(this)) as JsonObject,
                kclass.java,
                kclass) as T
        }
    }
}