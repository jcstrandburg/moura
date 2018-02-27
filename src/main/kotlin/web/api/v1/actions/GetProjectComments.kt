package web.api.v1.actions

import adr.JsonAction
import adr.JsonResult
import domain.accounts.IAccountsReadRepository
import domain.discussion.IDiscussionRepository
import domain.projects.IProjectRepository
import io.javalin.Context
import services.AuthenticationService
import web.api.v1.DiscussionCommentCollectionDto
import web.api.v1.DiscussionCommentDto

class GetProjectComments(
    authenticationService: AuthenticationService,
    private val accountsReadRepository: IAccountsReadRepository,
    private val projectRepository: IProjectRepository,
    private val discussionRepository: IDiscussionRepository
) : JsonAction(authenticationService) {
    override fun doHandle(ctx: Context): JsonResult {
        val projectId = ctx.param("projectId")?.toIntOrNull() ?: return BadRequest()
        val project = projectRepository.getProject(projectId) ?: return NotFound()
        val organization = accountsReadRepository.getOrganization(project.organizationId)
            ?: throw Exception("Unable to locate organization $project.organizationId")

        if (!accountsReadRepository.isUserInOrganization(authenticatedUser.id, organization.id))
            return NotFound()

        val messages = discussionRepository.getDiscussionMessages(project.discussionContextId)
        return Ok(DiscussionCommentCollectionDto(
            messages.map {
                DiscussionCommentDto(
                    id = it.id,
                    userId = it.userId,
                    content = it.content,
                    createdTime = it.createdTime)
            }
        ))
    }
}
