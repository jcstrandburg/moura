package logging

import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

fun getLogger(kclass: KClass<*>) = LoggerFactory.getLogger(kclass.java)!!
