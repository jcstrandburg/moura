package adr

import io.javalin.HaltException

enum class Status(val httpCode: Int) {
    OK(200),
    CREATED(201),
    Accepted(202),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),
    INTERNAL_SERVER_ERROR(500)
}

fun HaltException(status: Status): HaltException = HaltException(status.httpCode)
