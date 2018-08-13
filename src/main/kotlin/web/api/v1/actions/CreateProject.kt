package web.api.v1.actions

import adr.JsonAction
import adr.JsonResult
import domain.accounts.IAccountsReadRepository
import domain.projects.IProjectRepository
import domain.projects.ProjectCreate
import io.javalin.Context
import services.AuthenticationService
import web.api.v1.ProjectCreateDto
import web.api.v1.toSummaryDto

class CreateProject(
    authenticationService: AuthenticationService,
    private val accountsReadRepository: IAccountsReadRepository,
    private val projectRepository: IProjectRepository
) : JsonAction(authenticationService) {

    override fun doHandle(ctx: Context): JsonResult {

        val createDto = fromBody<ProjectCreateDto>(ctx)

        val project = ProjectCreate(
            name = createDto.project.name,
            organizationId = createDto.project.organizationId,
            parentProjectId = createDto.project.parentProjectId)

        if (!accountsReadRepository.isUserInOrganization(authenticatedUser.id, project.organizationId))
            return NotFound()

        val createdProject = projectRepository.createProject(project)

        return Created(createdProject.toSummaryDto())
    }
}
