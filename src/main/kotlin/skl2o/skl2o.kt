package skl2o

import org.sql2o.Connection
import org.sql2o.Query
import org.sql2o.Sql2o
import java.sql.Timestamp
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

/**
 * Converts a kotlinesque camelCase field name to a mysql style snake_case column name
 * NOTE: Does not play well with leading capitals (i.e. PascalCase instead of camelCase)
 * NOTE: Does not play will with adjacent capitals (i.e. messageXML instead of messageXml)
 */
fun toMySqlCasing(fieldName: String): String = fieldName.replace(toMySqlCasingRegex) { "_" + it.value.toLowerCase() }

private val toMySqlCasingRegex = "[A-Z]".toRegex()

/** Generates a comma separated list of sql-style fields names from a class */
inline fun <reified T: Any> mySqlFields(): String = mySqlFields(T::class, true, null)
inline fun <reified T: Any> mySqlFields(tableAlias: String): String = mySqlFields(T::class, true, tableAlias)
fun mySqlFields(kclass: KClass<*>, includeColumnAliases: Boolean, tableAlias: String? = null): String {
    val prefix = tableAlias?.let { "`$it`.`"} ?: ""

    return kclass.declaredMemberProperties.map { property ->
        val mysqlColumn = toMySqlCasing(property.name)
        val postfix = if (includeColumnAliases) " as `${property.name}`" else ""
        "$prefix`$mysqlColumn`$postfix"
    }.sorted().joinToString(",")
}

/** Helper annotation to attach MySql table name to a DbObject */
@Target(AnnotationTarget.CLASS)
annotation class TableName(val tableName: String)

/** Gets the table name via the TableName annotation on a clas, or null if no attribute is present */
inline fun <reified T: Any> getTableNameOrNull() = getTableNameOrNull(T::class)
fun getTableNameOrNull(kclass: KClass<*>): String? = kclass.findAnnotation<TableName>()?.tableName

/** Gets the table name via the TableName annotation on a class */
inline fun <reified T: Any> getTableName() = getTableName(T::class)
fun getTableName(kclass: KClass<*>): String =
    getTableNameOrNull(kclass) ?: throw IllegalArgumentException("No TableName annotation found")

/** Helper annotation to attach MySql primary key to a DbObject */
@Target(AnnotationTarget.PROPERTY)
annotation class PrimaryKey

inline fun <reified T: Any> getPrimaryKeyOrNull() = getPrimaryKeyOrNull(T::class)
fun getPrimaryKeyOrNull(kclass: KClass<*>): String? {
    val propertiesWithPrimaryKeyAnnotation = kclass
        .declaredMemberProperties
        .filter { it.findAnnotation<PrimaryKey>() != null }

    if (propertiesWithPrimaryKeyAnnotation.size > 1)
        throw Exception("Multiple properties annotated with PrimaryKey")

    return propertiesWithPrimaryKeyAnnotation.singleOrNull()?.let { toMySqlCasing(it.name) }
}

inline fun <reified T: Any> getPrimaryKey() = getPrimaryKey(T::class)
fun getPrimaryKey(kclass: KClass<*>): String =
    getPrimaryKeyOrNull(kclass) ?: throw IllegalArgumentException("No TableName annotation found")

/** Generates a list of colon prefixed sql prepared query params from a class */
inline fun <reified T: Any> mySqlParams(): String = mySqlParams(T::class)
fun mySqlParams(kclass: KClass<*>) =
    kclass.declaredMemberProperties.map { ":${it.name}" }.sorted().joinToString(",")

/** Generates a full mysql simpleInsert statement for a table with given name and shape */
inline fun <reified T: Any> mySqlInsertStatement(tableName: String): String = mySqlInsertStatement(T::class, tableName)
inline fun <reified T: Any> mySqlInsertStatement(): String = mySqlInsertStatement(T::class, getTableName(T::class))
fun mySqlInsertStatement(kclass: KClass<*>, tableName: String): String =
    "INSERT INTO `$tableName` (${mySqlFields(kclass, false)}) VALUES (${mySqlParams(kclass)})"

/** Generates a full mysql update statement for a table with given name and shape */
inline fun <reified T: Any> mySqlUpdateStatement(tableName: String, key: String) = mySqlUpdateStatement(T::class, tableName, key)
inline fun <reified T: Any> mySqlUpdateStatement(key: String): String = mySqlUpdateStatement(T::class, getTableName(T::class), key)
fun mySqlUpdateStatement(kclass: KClass<*>, tableName: String, key: String): String {
    val assignments = kclass.declaredMemberProperties.filter { it.name != key }.map { "${toMySqlCasing(it.name)}=:${it.name}" }
    return "UPDATE `$tableName` SET ${assignments.sorted().joinToString(",")} WHERE ${toMySqlCasing(key)}=:$key"
}

/** Generates a full mysql update statement for a table from a list of field names */
fun mySqlUpdateStatement(changes: Map<String, Any?>, tableName: String, condition: String): String {
    val assignments = changes.keys.map { "${toMySqlCasing(it)}=:$it" }
    return "UPDATE `$tableName` SET ${assignments.sorted().joinToString(",")} WHERE $condition"
}

/** Generates a full mysql select statement for a table with given name, shape, and condition */
inline fun <reified T: Any> mySqlSelectStatement(tableName: String, condition: String) = mySqlSelectStatement(T::class, tableName, condition)
inline fun <reified T: Any> mySqlSelectStatement(condition: String) = mySqlSelectStatement(T::class, getTableName(T::class), condition)
fun mySqlSelectStatement(kclass: KClass<*>, tableName: String, condition: String): String =
    "SELECT ${mySqlFields(kclass, true)} FROM `$tableName` WHERE $condition"

/** Extension method for Sql2o, eases getting primary key of inserted row with ::class.java boilerplate */
inline fun <reified T: Any> Connection.getKeyAs(): T = getKeyAs(T::class)
fun <T: Any> Connection.getKeyAs(kclass: KClass<T>): T = getKey(kclass.java)

/** Extension method for Sql2o, eases calls to executeScalar without ::class.java boilerplate */
inline fun <reified T: Any> Query.executeScalarAs(): T = executeScalarAs(T::class)
fun <T: Any> Query.executeScalarAs(kclass: KClass<T>): T = executeScalar(kclass.java)

/** Extension method that makes it possible to use Sql2o without annoying ::class.java boilerplate */
inline fun <reified T: Any> Query.executeAndFetchAs() = executeAndFetchAs(T::class)
fun <T: Any> Query.executeAndFetchAs(kclass: KClass<T>) = executeAndFetch(kclass.java) as List<T>

/** Extension method that makes it possible to use Sql2o without annoying ::class.java boilerplate */
inline fun <reified T: Any> Query.executeAndFetchFirstAs(): T? = executeAndFetchFirst(T::class.java)

fun Query.addParameters(params: Map<String, Any?>): Query {
    params.forEach {
        addParameter(it.key, it.value)
    }
    return this
}

/** Extension method that allows for adding multiple parameters from an object */
inline fun <reified T: Any> Query.addParameters(params: T) = addParameters(T::class, params)
fun <T: Any> Query.addParameters(kclass: KClass<T>, params: T): Query {
    kclass.declaredMemberProperties.forEach { property ->
        val propValue = property.get(params)
        addParameter(property.name, propValue)
    }
    return this
}

/** Extension method that allows for adding multiple parameters from an Map */
private fun Query.addParametersFromMap(params: Map<String, Any>): Query {
    for ((param, value) in params) {
        addParameter(param, value)
    }
    return this
}

inline fun <reified T: Any> Connection.simpleSelect(condition: String, params: Map<String, Any>) =
    simpleSelect(T::class, getTableName(T::class), condition, params)

fun <T: Any> Connection.simpleSelect(kclass: KClass<T>, tableName: String, condition: String, params: Map<String, Any>): List<T> {
    return createQuery(mySqlSelectStatement(kclass, tableName, condition)).use { query ->
        query.addParametersFromMap(params).executeAndFetchAs(kclass)
    }
}

inline fun <reified T: Any> Connection.simpleSelectByPrimaryKey(id: Int) =
    simpleSelectByPrimaryKey(T::class, getTableName(T::class), getPrimaryKey(T::class), id)

fun <T: Any> Connection.simpleSelectByPrimaryKey(kclass: KClass<T>, tableName: String, primaryKeyName: String, id: Int): T? =
    simpleSelect(kclass, tableName, "$primaryKeyName=:userId", mapOf("userId" to id)).singleOrNull()


inline fun <reified T: Any> Connection.simpleInsert(tableName: String, insertMe: T) = simpleInsert(T::class, tableName, insertMe)
inline fun <reified T: Any> Connection.simpleInsert(insertMe: T) = simpleInsert(T::class, getTableName(T::class), insertMe)
fun <T: Any> Connection.simpleInsert(kclass: KClass<T>, tableName: String, insertMe: T): Int {
    return createQuery(mySqlInsertStatement(kclass, tableName)).use {
        it.addParameters(kclass, insertMe).executeUpdate().getKeyAs<Int>()
    }
}

inline fun <reified T: Any> Connection.simpleDelete(condition: String, params: Map<String, Any>) = simpleDelete(getTableName(T::class), condition, params)
fun Connection.simpleDelete(tableName: String, condition: String, params: Map<String, Any>) {
    createQuery("DELETE FROM `$tableName` WHERE $condition").use { query ->
        query.addParametersFromMap(params).executeUpdate()
    }
}

fun <T: Any?> Sql2o.openAndApply(block: Connection.() -> T) = open().use { it.block() }

fun Timestamp.toUtcZonedDateTime(): ZonedDateTime = ZonedDateTime.ofInstant(this.toInstant(), ZoneOffset.UTC)
fun ZonedDateTime.toTimestamp(): Timestamp = Timestamp.from(this.toInstant())