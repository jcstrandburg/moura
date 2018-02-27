package web.api.v1.actions

import adr.JsonAction
import adr.JsonResult
import domain.accounts.IAccountsReadRepository
import io.javalin.Context
import services.AuthenticationService
import web.api.v1.OrganizationSummaryDto

class GetOrganizationByToken(
    authenticationService: AuthenticationService,
    private val accountsReadRepository: IAccountsReadRepository
) : JsonAction(authenticationService) {
    override fun doHandle(ctx: Context): JsonResult {

        val token = ctx.param("orgToken") ?: return BadRequest()
        val org = accountsReadRepository.getOrganization(token) ?: return NotFound()
        return Ok(OrganizationSummaryDto(org.id, org.name, org.token))
    }
}
