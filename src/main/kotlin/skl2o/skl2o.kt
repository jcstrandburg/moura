package skl2o

import org.sql2o.Connection
import org.sql2o.Query
import org.sql2o.Sql2o
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

/** Helper annotation to attach MySql primary key to a DbObject */
@Target(AnnotationTarget.CLASS)
annotation class PrimaryKey(val columnName: String)

/** Generates a list of colon prefixed sql prepared query params from a class */
inline fun <reified T: Any> mySqlParams(): String = mySqlParams(T::class)
fun mySqlParams(kclass: KClass<*>) =
    kclass.declaredMemberProperties.map { ":${it.name}" }.sorted().joinToString(",")

/** Generates a full mysql simpleInsert statement for a table with given name and shape */
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
inline fun <reified T: Any> Connection.simpleSelect(tableName: String, condition: String, params: Map<String, Any>) =
    simpleSelect(T::class, tableName, condition, params)

fun Connection.simpleSelect(kclass: KClass<*>, tableName: String, condition: String, params: Map<String, Any>) {
    return createQuery(mySqlSelectStatement(kclass, tableName, condition)).use { query ->
        query.addParametersFromMap(params).executeAndFetchAs(kclass)
    }
}

inline fun <reified T: Any> Connection.simpleInsert(tableName: String, insertMe: T) = simpleInsert(T::class, tableName, insertMe)
inline fun <reified T: Any> Connection.simpleInsert(insertMe: T) = simpleInsert(T::class, getTableName(T::class), insertMe)
fun <T: Any> Connection.simpleInsert(kclass: KClass<T>, tableName: String, insertMe: T) {
    createQuery(mySqlInsertStatement(kclass, tableName) + ";SELECT LAST_INSERT_ID()").use {
        it.addParameters(kclass, insertMe).executeUpdate().getKeyAs<Int>()
    }
}

inline fun <reified T: Any> Connection.simpleDelete(condition: String, params: Map<String, Any>) = simpleDelete(getTableName(T::class), condition, params)
fun Connection.simpleDelete(tableName: String, condition: String, params: Map<String, Any>) {
    createQuery("DELETE FROM `$tableName` WHERE $condition").use { query ->
        query.addParametersFromMap(params).executeUpdate()
    }
}

fun <T: Any> Sql2o.openAndUse(block: (Connection) -> T) = open().use(block)

fun <T: Any> Sql2o.query(sql: String, block: (Query) -> T) = this.openAndUse({ conn -> conn.createQuery(sql).let(block) })