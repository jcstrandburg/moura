package web.api.v1.actions

import adr.JsonAction
import adr.JsonResult
import domain.accounts.IAccountsRepository
import domain.accounts.UserCreate
import io.javalin.Context
import services.AuthenticationService
import web.api.v1.UserCreateDto
import web.api.v1.UserDto

class CreateUser(
    authenticationService: AuthenticationService,
    private val accountsRepository: IAccountsRepository
) : JsonAction(authenticationService) {

    override fun before(): JsonResult? {
        return if (authenticatedUser != null)
            BadRequest()
        else
            null
    }

    override fun doHandle(ctx: Context): JsonResult {
        val body = fromBody<UserCreateDto>(ctx) ?: return BadRequest()

        val userCreate = UserCreate(
            name = body.name,
            password = body.password,
            alias = body.alias,
            email = body.email)

        val createdUser = accountsRepository.createUser(userCreate)
        val authenticatedUser = authenticationService.logInUser(body.email, body.password)
            ?: throw Exception("Failed to authenticate user ${createdUser.id} after creating them")

        return Ok(UserDto(id = authenticatedUser.id, name = authenticatedUser.name, alias = authenticatedUser.alias))
    }
}