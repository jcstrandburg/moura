package web.api.v1.actions

import adr.JsonAction
import adr.JsonResult
import domain.accounts.IAccountsReadRepository
import io.javalin.Context
import services.AuthenticationService
import web.api.v1.UserDto

class GetUserById(
    authenticationService: AuthenticationService,
    private val accountsRepository: IAccountsReadRepository
) : JsonAction(authenticationService) {

    override fun doHandle(ctx: Context): JsonResult {
        val userId: Int = ctx.param("userId")?.toIntOrNull() ?: return BadRequest()

        val user = accountsRepository.getUserById(userId) ?: return NotFound()
        return Ok(UserDto(
            id = user.id,
            name = user.name,
            alias = user.alias))
    }
}
