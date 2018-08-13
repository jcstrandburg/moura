package web.api.v1.actions

import adr.JsonAction
import adr.JsonResult
import domain.accounts.IAccountsReadRepository
import domain.projects.IProjectRepository
import io.javalin.Context
import services.AuthenticationService
import web.api.v1.ProjectCollectionDto
import web.api.v1.toSummaryDto

class GetProjectsForOrganization(
    authenticationService: AuthenticationService,
    private val accountsReadRepository: IAccountsReadRepository,
    private val projectRepository: IProjectRepository
) : JsonAction(authenticationService) {
    override fun doHandle(ctx: Context): JsonResult {
        val orgToken = ctx.param("orgToken") ?: return BadRequest()
        val parentProjectId = ctx.queryParam("parent")?.toIntOrNull()

        val organization = accountsReadRepository.getOrganization(orgToken)?: return NotFound()
        val projects = projectRepository.getProjectsForOrganization(organization.id, parentProjectId)

        return Ok(ProjectCollectionDto(
            projects.map { it.toSummaryDto() },
            isLastPage = true))
    }
}
