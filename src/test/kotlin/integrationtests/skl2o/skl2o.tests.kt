package integrationtests.skl2o

import integrationtests.database.DatabaseObjectMother
import junit.framework.TestCase
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import skl2o.getKeyAs
import skl2o.openAndUse
import skl2o.simpleSelectByPrimaryKey
import skl2o.toTimestamp
import skl2o.toUtcOffsetDateTime
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@RunWith(Parameterized::class)
class TestTimeConverters(val offsetDateTime: OffsetDateTime) : TestCase() {

    @Test
    fun testTimeConverters() {
        sql2o.openAndUse { conn ->
            val timestamp = toTimestamp(offsetDateTime)
            val id = conn
                .createQuery("INSERT INTO `$tableName` (`time`) VALUES (:time)")
                .addParameter("time", timestamp)
                .executeUpdate()
                .getKeyAs<Int>()

            val row = conn.simpleSelectByPrimaryKey(TestTable::class, tableName, "id", id)!!
            val roundTripOffsetDateTime = toUtcOffsetDateTime(row.time)

            assertEquals(timestamp, row.time)
            assertEquals(offsetDateTime.toInstant(), row.time.toInstant())
            assertEquals(offsetDateTime.toInstant(), roundTripOffsetDateTime.toInstant())
        }
    }

    companion object {
        val sql2o = DatabaseObjectMother.sql2o
        val tableName = UUID.randomUUID().toString()

        @BeforeClass
        @JvmStatic
        fun setup() {
            sql2o.openAndUse { conn ->
                conn.createQuery("""
CREATE TABLE `$tableName` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `time` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
""").executeUpdate()
            }
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            sql2o.openAndUse { conn ->
                conn.createQuery("DROP TABLE `$tableName`").executeUpdate()
            }
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun testData(): Collection<Array<OffsetDateTime>> {
            // test data deliberately has no nanoseconds values to avoid issues with milliseconds not surviving round trip to the db
            return listOf(
                arrayOf(OffsetDateTime.of(2015, 10, 23, 12, 44, 43, 0, ZoneOffset.UTC)),
                arrayOf(OffsetDateTime.of(2016, 5, 10, 23, 1, 55, 0, ZoneOffset.ofHours(-3))),
                arrayOf(OffsetDateTime.of(2017, 7, 15, 15, 3, 12, 0, ZoneOffset.ofHours(6)))
            )
        }
    }

    data class TestTable(val id: Int, val time: Timestamp)
}
