package web
import adr.Action
import adr.Router
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import io.javalin.Javalin
import io.javalin.json.FromJsonMapper
import io.javalin.json.JavalinJson
import io.javalin.json.ToJsonMapper
import io.javalin.staticfiles.Location
import logging.getLogger
import vulcan.Container
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
import java.io.File
import java.io.StringReader

private val logger = getLogger(Moura::class)

class Moura(val port: Int, val container: Container) {

    private lateinit var app : Javalin

    fun start() {
        app = Javalin.create()

        val localDevelopmentResourcesDirectory = """C:\code\moura\src\main\resources\static\"""
        val file = File(localDevelopmentResourcesDirectory)
        if (false && file.exists() && file.isDirectory) {
            logger.info("Using local development files for static resources")
            app.enableStaticFiles(localDevelopmentResourcesDirectory, Location.EXTERNAL)
        }

        val klaxon = Klaxon()

        JavalinJson.fromJsonMapper = object: FromJsonMapper {
            override fun <T> map(json: String, targetClass: Class<T>): T {
                val kclass = targetClass::class

                return klaxon.fromJsonObject(
                    klaxon.parser(kclass).parse(StringReader(json)) as JsonObject,
                    targetClass,
                    kclass) as T
            }
        }
        JavalinJson.toJsonMapper = object: ToJsonMapper {
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
                        path(":projectid/") {
                            get<GetProjectById>()
                            path("comments/") {
                                get<GetProjectComments>()
                                post<CreateProjectComment>()
                            }
                        }
                    }
                }
            }

        app
            .port(port)
            .start()
    }

    fun stop() {
        app.stop()
    }
}
