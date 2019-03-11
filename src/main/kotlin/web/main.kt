package web

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import data.mysql.MysqlAccountsRepository
import data.mysql.MysqlDiscussionRepository
import data.mysql.MysqlProjectRepository
import domain.accounts.IAccountsReadRepository
import domain.accounts.IAccountsRepository
import domain.discussion.IDiscussionContextRepository
import domain.discussion.IDiscussionRepository
import domain.projects.IProjectRepository
import logging.getLogger
import org.sql2o.Sql2o
import services.AuthenticationService
import skl2o.executeAndFetchAs
import skl2o.openAndApply
import vulcan.Container
import vulcan.Lifecycle

private val logger = getLogger(Application::class)

class Application: CliktCommand() {
    private val healthCheck by option("--healthcheck").flag(default = false)

    private val container = Container().apply {
        register { Sql2o("jdbc:mysql://localhost:3306/moura", "root", "jimbolina") }
        register<IAccountsReadRepository, IAccountsRepository>()
        register<IAccountsRepository, MysqlAccountsRepository>()
        register<IProjectRepository, MysqlProjectRepository>()
        register<IDiscussionRepository, MysqlDiscussionRepository>()
        register<IDiscussionContextRepository, MysqlDiscussionRepository>()
        register<AuthenticationService, AuthenticationService>(Lifecycle.PerContainer)
    }

    override fun run() {
        if (healthCheck) {
            runHealthCheck(container)
        } else {
            Moura(7000, container).start()
        }
    }

    private fun runHealthCheck(container: Container) {
        val db = container.get<Sql2o>()
        db.openAndApply {
            val version= createQuery("SELECT @@Version").executeAndFetchAs<String>().single()
            logger.info("Mysql version: $version")
        }
    }
}

fun main(args: Array<String>) = Application().main(args)
