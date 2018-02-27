package web.api.v1.actions

import adr.JsonAction
import adr.JsonResult
import domain.accounts.IAccountsReadRepository
import io.javalin.Context
import services.AuthenticationService
import web.api.v1.CurrentUserDto
import web.api.v1.OrganizationSummaryDto
import web.api.v1.UserDto

class GetCurrentUser(
    authenticationService: AuthenticationService,
    private val organizationRepository: IAccountsReadRepository
) : JsonAction(authenticationService) {

    override fun doHandle(ctx: Context): JsonResult {
        val user = authenticationService.getLoggedInUser() ?: return Unauthorized()

        val organizations = organizationRepository.getOrganizationsForUser(user.id)

        return Ok(CurrentUserDto(
            UserDto(id = user.id, name = user.name, alias = user.alias),
            organizations.map { OrganizationSummaryDto(id = it.id, name = it.name, token = it.token) }
        ))
    }
}
