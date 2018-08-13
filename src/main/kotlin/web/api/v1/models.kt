package web.api.v1

import domain.accounts.Organization
import domain.accounts.User
import domain.discussion.DiscussionMessage
import domain.projects.Project
import java.time.ZonedDateTime

data class UserDto(val id: Int, val name: String, val alias: String, val token: String)
fun User.toDto() = UserDto(id=this.id, name=this.name, alias=this.alias, token=this.token)

data class UserCreateDto(val name: String, val password: String, val alias: String, val email: String)
data class CurrentUserDto(val user: UserDto, val organizations: Collection<OrganizationSummaryDto>)

data class OrganizationSummaryDto(val id: Int, val name: String, val token: String)
fun Organization.toSummaryDto() = OrganizationSummaryDto(id = id, name = name, token = token)

data class OrganizationCreateDto(val organization: OrganizationSummaryDto)
data class OrganizationCollectionDto(val organizations: List<OrganizationSummaryDto>, val isLastPage: Boolean)

data class ProjectSummaryDto(val id: Int, val name: String, val organizationId: Int, val parentProjectId: Int?)
fun Project.toSummaryDto() = ProjectSummaryDto(id = id, name = name, organizationId = id, parentProjectId = parentProjectId)

data class ProjectCreateDto(val project: ProjectSummaryDto)
data class ProjectCollectionDto(val projects: List<ProjectSummaryDto>, val isLastPage: Boolean)

data class DiscussionCommentDto(val id: Int, val userId: Int, val content: String, val createdTime: ZonedDateTime)
fun DiscussionMessage.toDto() = DiscussionCommentDto(id = id, userId = userId, content = content, createdTime = createdTime)

data class DiscussionCommentCreateDto(val content: String)
data class DiscussionCommentCollectionDto(val comments: List<DiscussionCommentDto>, val isLastPage: Boolean)

