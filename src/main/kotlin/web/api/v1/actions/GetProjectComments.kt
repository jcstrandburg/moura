package web.api.v1.actions

import adr.JsonAction
import adr.JsonResult
import domain.accounts.IAccountsReadRepository
import domain.discussion.IDiscussionRepository
import domain.projects.IProjectRepository
import io.javalin.Context
import services.AuthenticationService
import web.api.v1.DiscussionCommentCollectionDto
import web.api.v1.toDto

class GetProjectComments(
    authenticationService: AuthenticationService,
    private val accountsReadRepository: IAccountsReadRepository,
    private val projectRepository: IProjectRepository,
    private val discussionRepository: IDiscussionRepository
) : JsonAction(authenticationService) {
    override fun doHandle(ctx: Context): JsonResult {
        val projectId = ctx.param("projectId")?.toIntOrNull() ?: return BadRequest()
        val project = projectRepository.getProjectById(projectId) ?: return NotFound()

        // don't expose the existence of projects to users who shouldn't be able to see them
        if (!accountsReadRepository.isUserInOrganization(authenticatedUser.id, project.organizationId))
            return NotFound()

        val messages = discussionRepository.getDiscussionMessages(project.discussionContextId)
        return Ok(DiscussionCommentCollectionDto(
            messages.map { it.toDto() },
            isLastPage = true
        ))
    }
}
