package com.anagorny.cysnowbot.helpers

fun <T> withErrorLogging(consumer: (String, java.lang.Exception) -> Unit, s: String, function: () -> T): T? {
    return try {
        function.invoke()
    } catch (e: java.lang.Exception) {
        consumer.invoke(s, e)
        null
    }
}
