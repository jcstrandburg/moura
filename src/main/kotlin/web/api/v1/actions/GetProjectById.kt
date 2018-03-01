package web.api.v1.actions

import adr.JsonAction
import adr.JsonResult
import domain.accounts.IAccountsReadRepository
import domain.projects.IProjectRepository
import io.javalin.Context
import services.AuthenticationService
import web.api.v1.ProjectSummaryDto

class GetProjectById(
    authenticationService: AuthenticationService,
    private val projectRepository: IProjectRepository,
    private val accountsReadRepository: IAccountsReadRepository
) : JsonAction(authenticationService) {
    override fun doHandle(ctx: Context): JsonResult {
        val projectId = ctx.param("projectId")?.toIntOrNull() ?: return BadRequest()
        val project = projectRepository.getProject(projectId) ?: return NotFound()

        // dont' expose the existence of projects to users who shouldn't be able to see them
        if (!accountsReadRepository.isUserInOrganization(authenticatedUser.id, project.organizationId))
            return NotFound()

        return Ok(ProjectSummaryDto(
            id = project.id,
            name = project.name,
            organizationId = project.organizationId,
            parentProjectId = project.parentProjectId))
    }
}
