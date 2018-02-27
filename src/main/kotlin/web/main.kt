package web

import adr.Router
import data.inmemory.InMemoryAccountsRepository
import data.inmemory.InMemoryDiscussionRepository
import data.inmemory.InMemoryProjectRepository
import domain.accounts.IAccountsRepository
import domain.discussion.IDiscussionRepository
import domain.projects.IProjectRepository
import io.javalin.Javalin
import vulcan.Container
import web.actions.GetSignInForm
import web.actions.PostSignInForm
import web.actions.ServeApp
import web.actions.SignOut
import web.api.v1.actions.CreateOrganization
import web.api.v1.actions.CreateProject
import web.api.v1.actions.CreateProjectComment
import web.api.v1.actions.GetCurrentUser
import web.api.v1.actions.GetOrganizationByToken
import web.api.v1.actions.GetOrganizationsForCurrentUser
import web.api.v1.actions.GetProjectById
import web.api.v1.actions.GetProjectComments
import web.api.v1.actions.GetProjectsForOrganization
import web.api.v1.actions.GetUserById

fun main(args: Array<String>) {

    val container = Container()

    container.register<IAccountsRepository, InMemoryAccountsRepository>()
    container.register<IProjectRepository, InMemoryProjectRepository>()
    container.register<IDiscussionRepository, InMemoryDiscussionRepository>()

    val app = Javalin.create()
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
