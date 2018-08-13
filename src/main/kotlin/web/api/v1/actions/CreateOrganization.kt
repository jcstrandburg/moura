package web.api.v1.actions

import adr.JsonAction
import adr.JsonResult
import domain.accounts.IAccountsRepository
import domain.accounts.OrganizationCreateSet
import io.javalin.Context
import services.AuthenticationService
import web.api.v1.OrganizationCreateDto
import web.api.v1.toSummaryDto

class CreateOrganization(
    authenticationService: AuthenticationService,
    private val accountsRepository: IAccountsRepository
) : JsonAction(authenticationService) {
    override fun doHandle(ctx: Context): JsonResult {

        val organizationCreate = fromBody<OrganizationCreateDto>(ctx)
        val organization = OrganizationCreateSet(
            name = organizationCreate.organization.name,
            token = organizationCreate.organization.token)

        val existingOrganization = accountsRepository.getOrganization(organization.token)
        if (existingOrganization != null)
            return Conflict()

        val org = accountsRepository.createOrganization(organization)

        accountsRepository.addUserToOrganization(authenticatedUser.id, org.id)

        return Created(org.toSummaryDto())
    }
}
