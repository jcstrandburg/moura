package unittests.domain

import domain.Change
import domain.ChangeSet
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ChangeSetTestFixture(
    private val testCaseName: String,
    private val changeSet: TestChangeSet,
    private val expectedEntity: TestEntity,
    private val expectedChanges: Map<String, Any?>
): TestCase() {

    data class TestEntity(val id: Int, val name: String?, val age: Int)
    data class TestChangeSet(val name: Change<String?>? = null, val age: Int? = null): ChangeSet<TestEntity>()

    companion object {

        private val entity = TestEntity(10, "Whatever", 99)

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun testData(): Collection<Array<*>> {
            return listOf(
                arrayOf(
                    "No changes",
                    TestChangeSet(),
                    entity.copy(),
                    emptyMap<String, Any?>()
                ),
                arrayOf(
                    "Fields changed to their current value",
                    TestChangeSet(name = Change(entity.name), age = entity.age),
                    entity.copy(),
                    mapOf("name" to entity.name, "age" to entity.age)
                ),
                arrayOf(
                    "Change a single nullable field to null",
                    TestChangeSet(name = Change(null)),
                    entity.copy(name = null),
                    mapOf("name" to null)
                ),
                arrayOf(
                    "Change a single non-nullable field",
                    TestChangeSet(age = 101),
                    entity.copy(age = 101),
                    mapOf("age" to 101)
                ),
                arrayOf(
                    "Change all fields",
                    TestChangeSet(name = Change("New name"), age = 2),
                    entity.copy(name = "New name", age = 2),
                    mapOf("name" to "New name", "age" to 2)
                )
            )
        }
    }

    @Test
    fun `test get changes`() {
        assertEquals(expectedChanges, changeSet.getChanges())
    }

    @Test
    fun `test apply changes`() {
        assertEquals(expectedEntity, changeSet.applyTo(entity))
    }
}