package web.api.v1.actions

import adr.JsonAction
import adr.JsonResult
import domain.accounts.IAccountsRepository
import io.javalin.Context
import services.AuthenticationService
import web.api.v1.OrganizationCollectionDto
import web.api.v1.OrganizationSummaryDto

class GetOrganizationsForCurrentUser(
    authenticationService: AuthenticationService,
    private val organizationRepository: IAccountsRepository
) : JsonAction(authenticationService) {
    override fun doHandle(ctx: Context): JsonResult {

        val orgs = organizationRepository.getOrganizationsForUser(authenticatedUser.id)
        return Ok(OrganizationCollectionDto(orgs.map { org -> OrganizationSummaryDto(org.id, org.name, org.token) }))
    }
}
