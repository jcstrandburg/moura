package fig

import java.io.InputStream
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class ConfigurationError(message: String) : RuntimeException(message)

open class Config

class ConfigReader {

    private val typeReaders = HashMap<KClass<*>, (String) -> Any>()

    init {
        registerType { it }
        registerType { it.toInt() }
        registerType { it.toFloat() }
        registerType { it.toDouble() }
        registerType { it.toBoolean() }
        registerType { it.toLong() }
        registerType { it.toShort() }
    }

    inline fun <reified T: Config> getConfig(stream: InputStream) = getConfig(stream, T::class)
    fun <T: Config> getConfig(stream: InputStream, configClass: KClass<T>): T {
        val props = Properties()
        props.load(stream)

        val constructor = configClass.primaryConstructor
            ?: throw ConfigurationError("Primary constructor required")

        return constructor.callBy(constructor.parameters.associate { it to getConfigValue(props, it) })
    }

    private fun getConfigValue(props: Properties, param: KParameter, prefix: String = ""): Any? {

        val paramClass = param.type.classifier as KClass<*>
        val prefixedPropertyName = prefix + param.name

        if (param.isOptional)
            throw ConfigurationError("Optional params not supported")

        if (props.containsKey(prefixedPropertyName)) {
            val propertyValue = props.getProperty(prefixedPropertyName)!!
            val reader = typeReaders[paramClass]

            return if (reader != null) {
                reader(propertyValue)
            } else {
                val stringConstructor = paramClass
                    .constructors
                    .singleOrNull { it.parameters.size == 1 && it.parameters[0].type.classifier == String::class }
                    ?: throw ConfigurationError("Unable to convert from String to type ${paramClass.qualifiedName}")

                stringConstructor.call(propertyValue)
            }
        }
        else if (props.keys.any { (it as String).startsWith(prefixedPropertyName) }) {
            val nestedPrefix = prefixedPropertyName + "."
            val primaryConstructor = paramClass.primaryConstructor
                ?: throw ConfigurationError("No primary constructor found for type ${paramClass.qualifiedName}")

            val constructorParams = primaryConstructor
                .parameters
                .associate { it to getConfigValue(props, it, nestedPrefix )}

            return primaryConstructor.callBy(constructorParams)
        } else {
            throw ConfigurationError("No data found for $prefixedPropertyName")
        }
    }

    inline fun <reified T: Any> registerType(noinline reader: (String) -> T) = registerType(T::class, reader)
    fun <T: Any> registerType(kclass: KClass<T>, reader: (String) -> T): ConfigReader {
        if (typeReaders.containsKey(kclass))
            throw Exception()

        typeReaders[kclass] = reader
        return this
    }
}

inline fun <reified T: Config> getConfig(stream: InputStream) = getConfig(stream, T::class)
fun <T: Config> getConfig(stream: InputStream, configClass: KClass<T>): T =
    ConfigReader().getConfig(stream, configClass)

