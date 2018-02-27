package web.api.v1

import java.time.OffsetDateTime

data class UserDto(val id: Int, val name: String, val alias: String)
data class CurrentUserDto(val user: UserDto, val organizations: Collection<OrganizationSummaryDto>)

data class OrganizationSummaryDto(val id: Int, val name: String, val token: String)
data class OrganizationCreateDto(val organization: OrganizationSummaryDto)
data class OrganizationCollectionDto(val organizations: List<OrganizationSummaryDto>)

data class ProjectSummaryDto(val id: Int, val name: String, val organizationId: Int, val parentProjectId: Int?)
data class ProjectCreateDto(val project: ProjectSummaryDto)
data class ProjectCollectionDto(val projects: List<ProjectSummaryDto>)

data class DiscussionCommentCreateDto(val content: String)
data class DiscussionCommentDto(val id: Int, val userId: Int, val content: String, val createdTime: OffsetDateTime)
data class DiscussionCommentCollectionDto(val comments: List<DiscussionCommentDto>)