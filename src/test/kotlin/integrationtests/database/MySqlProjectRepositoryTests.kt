package integrationtests.database

import domain.projects.IProjectRepository
import domain.projects.Project
import junit.framework.TestCase

class MySqlProjectRepositoryTests : TestCase() {

    private val projectsRepository: IProjectRepository = DatabaseObjectMother.projectsRepository

    fun `test create and get project`() {
        val organization = DatabaseObjectMother.createTestOrganization()
        val project = DatabaseObjectMother.createTestProject(organization.id, null)
        val fetchedProduct = projectsRepository.getProjectById(project.id)
        assertEquals(project, fetchedProduct)
    }

    fun `test get projects by organization and parent project`() {
        val organization = DatabaseObjectMother.createTestOrganization()

        val project1 = DatabaseObjectMother.createTestProject(organization.id, null)
        val project2 = DatabaseObjectMother.createTestProject(organization.id, project1.id)
        val project3 = DatabaseObjectMother.createTestProject(organization.id, project2.id)
        val project4 = DatabaseObjectMother.createTestProject(organization.id, project2.id)

        fun getProjectsAndAssert(organizationId: Int, parentProjectId: Int?, expectedProjects: List<Project>) {
            val projects = projectsRepository.getProjectsForOrganization(organizationId, parentProjectId)
            assertEquals(expectedProjects.sortedBy { it.id }, projects.sortedBy { it.id })
        }

        getProjectsAndAssert(organization.id, null, listOf(project1))
        getProjectsAndAssert(organization.id, project1.id, listOf(project2))
        getProjectsAndAssert(organization.id, project2.id, listOf(project3, project4))
        getProjectsAndAssert(organization.id, project3.id, emptyList())
    }
}
