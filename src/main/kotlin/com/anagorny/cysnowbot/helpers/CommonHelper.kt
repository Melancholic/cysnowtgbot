package com.anagorny.cysnowbot.helpers

fun <T> withErrorLogging(consumer: (String) -> Unit, s: String, function: () -> T): T? {
    return try {
        function.invoke()
    } catch (e: java.lang.Exception) {
        consumer.invoke(s)
        null
    }
}
