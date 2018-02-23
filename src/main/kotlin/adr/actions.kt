package adr

import io.javalin.Context

interface IGetAction { fun get(ctx: Context) }
interface IPutAction { fun put(ctx: Context) }
interface IDeleteAction { fun delete(ctx: Context) }
interface IPostAction { fun post(ctx: Context) }
interface IPatchAction { fun patch(ctx: Context) }
