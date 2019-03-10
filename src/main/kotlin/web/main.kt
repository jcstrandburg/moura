package web

import data.mysql.MysqlAccountsRepository
import data.mysql.MysqlDiscussionRepository
import data.mysql.MysqlProjectRepository
import domain.accounts.IAccountsReadRepository
import domain.accounts.IAccountsRepository
import domain.discussion.IDiscussionContextRepository
import domain.discussion.IDiscussionRepository
import domain.projects.IProjectRepository
import org.sql2o.Sql2o
import services.AuthenticationService
import vulcan.Container
import vulcan.Lifecycle

fun main(args: Array<String>) {
    val container = Container().apply {
        register { Sql2o("jdbc:mysql://localhost:3306/moura", "root", "jimbolina") }
        register<IAccountsReadRepository, IAccountsRepository>()
        register<IAccountsRepository, MysqlAccountsRepository>()
        register<IProjectRepository, MysqlProjectRepository>()
        register<IDiscussionRepository, MysqlDiscussionRepository>()
        register<IDiscussionContextRepository, MysqlDiscussionRepository>()
        register<AuthenticationService, AuthenticationService>(Lifecycle.PerContainer)
    }

    Moura(7000, container).start()
}
