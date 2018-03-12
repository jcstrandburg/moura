package data.mysql

import domain.discussion.IDiscussionContextRepository
import domain.projects.IProjectRepository
import domain.projects.Project
import domain.projects.ProjectCreate
import org.sql2o.Sql2o
import skl2o.PrimaryKey
import skl2o.TableName
import skl2o.executeAndFetchAs
import skl2o.mySqlSelectStatement
import skl2o.openAndUse
import skl2o.simpleInsert
import skl2o.simpleSelectByPrimaryKey

class MysqlProjectRepository(
    private val sql2o: Sql2o,
    private val discussionContextService: IDiscussionContextRepository
): IProjectRepository {

    override fun getProjectById(projectId: Int): Project? =
        sql2o.openAndUse { it.simpleSelectByPrimaryKey<DbProject>(projectId) }?.toDomain()

    override fun createProject(project: ProjectCreate): Project {
        val discussionContextId = discussionContextService.createContextId()
        val id = sql2o.openAndUse {
            it.simpleInsert(DbProjectCreate(
                project.name,
                project.organizationId,
                discussionContextId,
                project.parentProjectId
            ))
        }

        return getProjectById(id)!!
    }

    override fun getProjectsForOrganization(organizationId: Int, parentProjectId: Int?): List<Project> {
        val conditional = """
organization_id=:organizationId
AND ${if (parentProjectId == null) "parent_project_id IS NULL" else "parent_project_id=:parentProjectId"}"""

        val sql = mySqlSelectStatement<DbProject>(conditional)

        val dbProjects = sql2o.open().createQuery(sql).use { query ->
            if (parentProjectId == null) {
                query
                    .addParameter("organizationId", organizationId)
            }
            else  {
                query
                    .addParameter("organizationId", organizationId)
                    .addParameter("parentProjectId", parentProjectId)
            }
            .executeAndFetchAs<DbProject>()
        }

        return dbProjects.map { it -> it.toDomain() }
    }

    companion object {
        private fun DbProject.toDomain(): Project {
            return Project(this.projectId, this.name, this.organizationId, this.parentProjectId, this.discussionContextId)
        }
    }

    @TableName("projects")
    data class DbProject(
        @PrimaryKey
        val projectId: Int,
        val name: String,
        val organizationId: Int,
        val discussionContextId: Int,
        val parentProjectId: Int?)

    @TableName("projects")
    data class DbProjectCreate(
        val name: String,
        val organizationId: Int,
        val discussionContextId: Int,
        val parentProjectId: Int?)
}
