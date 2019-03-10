package web.api.v1.actions

import adr.JsonAction
import adr.JsonResult
import domain.accounts.IAccountsReadRepository
import domain.discussion.DiscussionMessageCreate
import domain.discussion.IDiscussionRepository
import domain.projects.IProjectRepository
import io.javalin.Context
import services.AuthenticationService
import web.api.v1.DiscussionCommentCreateDto
import web.api.v1.toDto

class CreateProjectComment(
    authenticationService: AuthenticationService,
    private val accountsReadRepository: IAccountsReadRepository,
    private val projectRepository: IProjectRepository,
    private val discussionRepository: IDiscussionRepository
) : JsonAction(authenticationService) {
    override fun doHandle(ctx: Context): JsonResult {
        val projectId = ctx.pathParam("projectId").toIntOrNull() ?: return BadRequest()
        val project = projectRepository.getProjectById(projectId) ?: return NotFound()

        val organization = accountsReadRepository.getOrganization(project.organizationId)
            ?: throw Exception("Unable to locate organization $project.organizationId")

        if (!accountsReadRepository.isUserInOrganization(authenticatedUser.id, organization.id))
            return NotFound()

        val createDto = fromBody<DiscussionCommentCreateDto>(ctx)

        val message = discussionRepository.createDiscussionMessage(
            DiscussionMessageCreate(project.discussionContextId, authenticatedUser.id, createDto.content))

        return Created(message.toDto())
    }
}
