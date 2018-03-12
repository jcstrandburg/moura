package data.inmemory

import data.util.AutoIncrementMemoryRepository
import domain.discussion.IDiscussionContextRepository
import domain.projects.IProjectRepository
import domain.projects.Project
import domain.projects.ProjectCreate

class InMemoryProjectRepository(
    private val discussionContextRepository: IDiscussionContextRepository
): IProjectRepository {

    private val projects = AutoIncrementMemoryRepository<Project>()

    override fun getProjectById(projectId: Int): Project? = projects.get(projectId)

    override fun createProject(project: ProjectCreate): Project {
        return projects.insert { id -> Project(
            id,
            project.name,
            project.organizationId,
            project.parentProjectId,
            discussionContextRepository.createContextId())
        }
    }

    override fun getProjectsForOrganization(organizationId: Int, parentProjectId: Int?): List<Project> {
        return projects.entities.filter { org ->
            org.organizationId == organizationId && org.parentProjectId == parentProjectId
        }
    }
}