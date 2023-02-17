package com.anagorny.cysnowbot.helpers

import org.slf4j.Logger
import java.io.File

fun removeFile(file: File?, logger: Logger) {
    if (file == null) {
        logger.warn("Can't remove file cause it is null")
    } else try {
        val path = file.absolutePath
        if (file.delete()) {
            logger.info("File $path was deleted")
        } else {
            logger.error("File $path cant be deleted")
        }
    } catch (e: Exception) {
        logger.error("Error while removing file", e)
    }
}
