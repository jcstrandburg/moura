package web.api.v1.actions

import adr.JsonAction
import adr.JsonResult
import domain.accounts.IAccountsReadRepository
import io.javalin.Context
import services.AuthenticationService
import web.api.v1.toSummaryDto

class GetOrganizationByToken(
    authenticationService: AuthenticationService,
    private val accountsReadRepository: IAccountsReadRepository
) : JsonAction(authenticationService) {
    override fun doHandle(ctx: Context): JsonResult {

        val token = ctx.pathParam("orgToken")
        val org = accountsReadRepository.getOrganization(token) ?: return NotFound()
        return Ok(org.toSummaryDto())
    }
}
