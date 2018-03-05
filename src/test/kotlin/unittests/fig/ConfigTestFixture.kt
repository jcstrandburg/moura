package unittests.fig

import fig.Config
import fig.ConfigReader
import fig.ConfigurationError
import fig.getConfig
import junit.framework.TestCase
import java.io.ByteArrayInputStream
import java.net.URI
import kotlin.test.assertFailsWith

class ConfigTestFixture : TestCase() {

    fun `test basic config works`() {
        val input = """
thing1=23
thing2=whatever
thing3=3.0
"""

        val config = getTestConfig<BasicConfig>(input)
        assertEquals(23, config.thing1)
        assertEquals("whatever", config.thing2)
        assertEquals(3.0, config.thing3)
    }

    fun `test string constructor logic works`() {
        val input = """
authorization=abcdefg
uri=http://example.com
"""

        val config = getTestConfig<ConfigWithStringType>(input)
        assertEquals("abcdefg", config.authorization)
    }

    fun `test nested config works`() {
        val input = """
database.username=usor
database.password=pathword
database.timeout=300
services.endpoint1=http://example.com
services.endpoint2=http://whatever.com
"""

        val config = getTestConfig<NestedConfig>(input)

        assertEquals("usor", config.database.username)
        assertEquals("pathword", config.database.password)
        assertEquals(300, config.database.timeout)
        assertEquals(URI("http://example.com"), config.services.endpoint1)
        assertEquals(URI("http://whatever.com"), config.services.endpoint2)
    }

    fun `test invalid types throws`() {
        val input = """
thing1=notaninteger
thing2=whatever
thing3=3.0
"""

        assertFailsWith<ConfigurationError> {
            getTestConfig<NestedConfig>(input)
        }
    }

    fun `test missing properties throws`() {
        val input = """
thing1=23
thing2=whatever
"""

        assertFailsWith<ConfigurationError> {
            getTestConfig<NestedConfig>(input)
        }
    }

    fun `test config with custom type registration`() {
        val input = """
customValue=abc123
"""

        var configReader = ConfigReader()
        configReader.registerType { propValue ->
            CustomConfig.CustomType(propValue.substring(0, 3), propValue.substring(3, 6).toInt())
        }

        val config = configReader.getConfig<CustomConfig>(ByteArrayInputStream(input.toByteArray()))
        assertEquals("abc", config.customValue.prefix)
        assertEquals(123, config.customValue.quantity)
    }

    private inline fun <reified T: Config> getTestConfig(input: String) =
        getConfig<T>(ByteArrayInputStream(input.toByteArray()))

    class BasicConfig(val thing1: Int, val thing2: String, val thing3: Double) : Config()
    class ConfigWithStringType(val authorization: String, val uri: URI): Config()
    class CustomConfig(val customValue: CustomType): Config() {
        class CustomType(val prefix: String, val quantity: Int)
    }

    class NestedConfig(
        val database: Database,
        val services: Services
    ) : Config() {
        class Database(
            val username: String,
            val password: String,
            val timeout: Int)
        class Services(
            val endpoint1: URI,
            val endpoint2: URI)
    }
}