package web

import adr.Router
import data.inmemory.InMemoryAccountsRepository
import data.inmemory.InMemoryDiscussionRepository
import data.inmemory.InMemoryProjectRepository
import domain.accounts.IAccountsReadRepository
import domain.accounts.IAccountsRepository
import domain.accounts.UserCreate
import domain.discussion.IDiscussionRepository
import domain.projects.IProjectRepository
import io.javalin.Javalin
import io.javalin.embeddedserver.Location
import org.mindrot.jbcrypt.BCrypt
import vulcan.Container
import web.actions.GetSignInForm
import web.actions.PostSignInForm
import web.actions.ServeApp
import web.actions.SignOut
import web.api.v1.actions.CreateOrganization
import web.api.v1.actions.CreateProject
import web.api.v1.actions.CreateProjectComment
import web.api.v1.actions.CreateUser
import web.api.v1.actions.GetCurrentUser
import web.api.v1.actions.GetOrganizationByToken
import web.api.v1.actions.GetOrganizationsForCurrentUser
import web.api.v1.actions.GetProjectById
import web.api.v1.actions.GetProjectComments
import web.api.v1.actions.GetProjectsForOrganization
import web.api.v1.actions.GetUserById

fun main(args: Array<String>) {

    val container = Container()

    container.register { InMemoryAccountsRepository() }
    container.register<IAccountsReadRepository, InMemoryAccountsRepository>()
    container.register<IAccountsRepository, InMemoryAccountsRepository>()
    container.register<IProjectRepository, InMemoryProjectRepository>()
    container.register<IDiscussionRepository, InMemoryDiscussionRepository>()

    val accountsRepository = container.get<IAccountsRepository>()

    val hash = { password: String -> BCrypt.hashpw(password, BCrypt.gensalt(12))!! }

    accountsRepository.createUser(UserCreate(
        name = "Bob Bobberton",
        password = hash("password"),
        alias = "Bob",
        email = "bob@example.com"))
    accountsRepository.createUser(UserCreate(
        name = "Jim Jameson",
        password = hash("password"),
        alias = "Jimothy",
        email = "jim@example.com"))

    val app = Javalin.create()

    app.enableStaticFiles("""C:\code\moura\src\main\resources\static\""", Location.EXTERNAL)

    val router = Router(doGetAction = { ctx, kclass -> container.getNestedContainer().register(ctx).get(kclass) })
    router.routes(app) {
        path("/signin") {
            get<GetSignInForm>()
            post<PostSignInForm>()
        }
        get<SignOut>("/signout")
        path("/app/") {
            get<ServeApp>()
        }
        path ("/api/v1/") {
            path("users/") {
                post<CreateUser>()
                path("me/") {
                    get<GetCurrentUser>()
                    get<GetOrganizationsForCurrentUser>("organizations")
                }
                get<GetUserById>(":userId")
            }
            path("orgs") {
                post<CreateOrganization>()
                path(":token/") {
                    get<GetOrganizationByToken>()
                    get<GetProjectsForOrganization>("projects")
                }
            }
            path("projects") {
                path(":projectId/") {
                    get<GetProjectById>()
                    path("comments") {
                        get<GetProjectComments>()
                        post<CreateProjectComment>()
                    }
                }
                post<CreateProject>()
            }
        }
    }

    app.port(7000).start()
}
