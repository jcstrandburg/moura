package domain.projects

data class Project(
    val id: Int,
    val name: String,
    val organizationId: Int,
    val parentProjectId: Int?,
    val discussionContextId: Int)

data class ProjectCreate(
    val name: String,
    val organizationId: Int,
    val parentProjectId: Int?)

interface IProjectRepository {
    fun getProject(projectId: Int): Project?
    fun createProject(project: ProjectCreate): Project
    fun getProjectsForOrganization(organizationId: Int, parentProjectId: Int?): List<Project>
}
