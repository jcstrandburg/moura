package web

import adr.Action
import adr.Router
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import data.mysql.MysqlAccountsRepository
import data.mysql.MysqlDiscussionRepository
import data.mysql.MysqlProjectRepository
import domain.accounts.IAccountsReadRepository
import domain.accounts.IAccountsRepository
import domain.discussion.IDiscussionRepository
import domain.projects.IProjectRepository
import io.javalin.Javalin
import io.javalin.embeddedserver.Location
import io.javalin.translator.json.JavalinJsonPlugin
import io.javalin.translator.json.JsonToObjectMapper
import io.javalin.translator.json.ObjectToJsonMapper
import org.sql2o.Sql2o
import services.AuthenticationService
import vulcan.Container
import vulcan.Lifecycle
import web.actions.GetSignInForm
import web.actions.PostSignInForm
import web.actions.ServeApp
import web.actions.ServeMock
import web.actions.SignOut
import web.api.v1.actions.CreateOrganization
import web.api.v1.actions.CreateProject
import web.api.v1.actions.CreateProjectComment
import web.api.v1.actions.CreateUser
import web.api.v1.actions.GetCurrentUser
import web.api.v1.actions.GetOrganizationByToken
import web.api.v1.actions.GetProjectById
import web.api.v1.actions.GetProjectComments
import web.api.v1.actions.GetProjectsForOrganization
import web.api.v1.actions.GetUserByToken
import java.io.StringReader

class Moura(val port: Int, val container: Container) {

    private lateinit var app : Javalin

    fun start() {
        app = Javalin.create()

        app.enableStaticFiles("""C:\code\moura\src\main\resources\static\""", Location.EXTERNAL)

        val klaxon = Klaxon()

        JavalinJsonPlugin.jsonToObjectMapper = object: JsonToObjectMapper {
            override fun <T> map(json: String, targetClass: Class<T>): T {
                val kclass = targetClass::class

                return klaxon.fromJsonObject(
                    klaxon.parser(kclass).parse(StringReader(json)) as JsonObject,
                    targetClass,
                    kclass) as T
            }
        }
        JavalinJsonPlugin.objectToJsonMapper = object: ObjectToJsonMapper {
            override fun map(obj: Any): String = klaxon.toJsonString(obj)
        }

        Router { ctx, kclass -> container.getNestedContainer().register(ctx).get(kclass) as Action }
            .routes(app) {
                path("signin/") {
                    get<GetSignInForm>()
                    post<PostSignInForm>()
                }
                get<SignOut>("signout/")
                path("app/") {
                    get<ServeApp>()
                }
                path("mock/") {
                    get<ServeMock>()
                }
                path ("api/v1/") {
                    path("users/") {
                        post<CreateUser>()
                        path("me/") {
                            get<GetCurrentUser>()
                        }
                        get<GetUserByToken>(":token/")
                    }
                    path("organizations/") {
                        post<CreateOrganization>()
                        path(":token/") {
                            get<GetOrganizationByToken>()
                            get<GetProjectsForOrganization>("projects/")
                        }
                    }
                    path("projects/") {
                        post<CreateProject>()
                        path(":projectId/") {
                            get<GetProjectById>()
                            path("comments/") {
                                get<GetProjectComments>()
                                post<CreateProjectComment>()
                            }
                        }
                    }
                }
            }

        app.port(port).start()
    }

    fun stop() {
        app.stop()
    }
}

fun main(args: Array<String>) {
    val container = Container().apply {
        register { Sql2o("jdbc:mysql://localhost:3306/moura", "root", "jimbolina") }
        register<IAccountsReadRepository, IAccountsRepository>()
        register<IAccountsRepository, MysqlAccountsRepository>()
        register<IProjectRepository, MysqlProjectRepository>()
        register<IDiscussionRepository, MysqlDiscussionRepository>()
        register<AuthenticationService, AuthenticationService>(Lifecycle.PerContainer)
    }

    Moura(7000, container).start()
}
