package morpheus.services.mysql

import org.sql2o.Connection
import org.sql2o.Query
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

/**
 * Converts a kotlinesque camelCase field name to a mysql style snake_case column name
 * NOTE: Does not play well with leading capitals (i.e. PascalCase instead of camelCase)
 * NOTE: Does not play will with adjacent capitals (i.e. messageXML instead of messageXml)
 */
fun toMySqlCasing(fieldName: String): String = fieldName.replace(toMySqlCasingRegex, { "_" + it.value.toLowerCase() })

private val toMySqlCasingRegex = "[A-Z]".toRegex()

/** Generates a comma separated list of sql-style fields names from a class */
inline fun <reified T: Any> mySqlFields(): String = mySqlFields(T::class, null)
inline fun <reified T: Any> mySqlFields(tableAlias: String): String = mySqlFields(T::class, tableAlias)
fun mySqlFields(kclass: KClass<*>, tableAlias: String? = null) =
    kclass.declaredMemberProperties.map { "`${tableAlias?.let { "$it`.`"}}${toMySqlCasing(it.name)}`" }.sorted().joinToString(",")

/** Gets the table name via the TableName annotation on a clas, or null if no attribute is present */
inline fun <reified T: Any> getTableNameOrNull() = getTableNameOrNull(T::class)
fun getTableNameOrNull(kclass: KClass<*>): String? = kclass.findAnnotation<TableName>()?.tableName

/** Gets the table name via the TableName annotation on a class */
inline fun <reified T: Any> getTableName() = getTableName(T::class)
fun getTableName(kclass: KClass<*>): String =
    getTableNameOrNull(kclass) ?: throw IllegalArgumentException("No TableName annotation found")

/** Helper annotation to attach MySql table name to a DbObject */
@Target(AnnotationTarget.CLASS)
annotation class TableName(val tableName: String)

/** Generates a list of colon prefixed sql prepared query params from a class */
inline fun <reified T: Any> mySqlParams(): String = mySqlParams(T::class)
fun mySqlParams(kclass: KClass<*>) =
    kclass.declaredMemberProperties.map { ":${it.name}" }.sorted().joinToString(",")

/** Generates a full mysql insert statement for a table with given name and shape */
inline fun <reified T: Any> mySqlInsertStatement(tableName: String): String = mySqlInsertStatement(T::class, tableName)
inline fun <reified T: Any> mySqlInsertStatement(): String = mySqlInsertStatement(T::class, getTableName(T::class))
fun mySqlInsertStatement(kclass: KClass<*>, tableName: String): String =
    "INSERT INTO `$tableName` (${mySqlFields(kclass, null)}) VALUES (${mySqlParams(kclass)})"

/** Generates a full mysql update statement for a table with given name and shape */
inline fun <reified T: Any> mySqlUpdateStatement(tableName: String, key: String) = mySqlUpdateStatement(T::class, tableName, key)
inline fun <reified T: Any> mySqlUpdateStatement(key: String): String = mySqlUpdateStatement(T::class, getTableName(T::class), key)
fun mySqlUpdateStatement(kclass: KClass<*>, tableName: String, key: String): String {
    val assignments = kclass.declaredMemberProperties.filter { it.name != key }.map { "${toMySqlCasing(it.name)}=:${it.name}" }
    return "UPDATE `$tableName` SET ${assignments.sorted().joinToString(",")} WHERE ${toMySqlCasing(key)}=:$key"
}

/** Generates a full mysql select statement for a table with given name, shape, and condition */
inline fun <reified T: Any> mySqlSelectStatement(tableName: String, condition: String) = mySqlSelectStatement(T::class, tableName, condition)
inline fun <reified T: Any> mySqlSelectStatement(condition: String) = mySqlSelectStatement(T::class, getTableName(T::class), condition)
fun mySqlSelectStatement(kclass: KClass<*>, tableName: String, condition: String): String =
    "SELECT ${mySqlFields(kclass)} FROM `$tableName` WHERE $condition"

/** Extension method for Sql2o, eases getting primary key of inserted row with ::class.java boilerplate */
inline fun <reified T: Any> Connection.getKey(): T = getKey(T::class.java)

/** Extension method that allows for adding multiple parameters from an object */
inline fun <reified T: Any> Query.addParameters(params: T) = addParameters(T::class, params)
fun <T: Any> Query.addParameters(kclass: KClass<T>, params: T): Query {
    kclass.declaredMemberProperties.forEach { property ->
        val propValue = property.get(params)
        addParameter(property.name, propValue)
    }
    return this
}

/** Extension method that makes it possible to use Sql2o without annoying ::class.java boilerplate */
inline fun <reified T: Any> Query.executeAndFetch(): List<T> = executeAndFetch(T::class.java)

/** Extension method that makes it possible to use Sql2o without annoying ::class.java boilerplate */
inline fun <reified T: Any> Query.executeAndFetchFirst(): T? = executeAndFetchFirst(T::class.java)
