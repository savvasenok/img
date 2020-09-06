package xyz.savvamirzoyan.img

import java.io.File
import kotlin.reflect.KFunction1

object PhotoManager {
    fun getAllPhotosPaths(): Sequence<String> {
        return mapFiles(File::getAbsolutePath)
    }

    fun getAllPhotosNames(): Sequence<String> {
        return mapFiles(File::getName)
    }

    fun mapFiles(prop: KFunction1<File, String>): Sequence<String> {
        return File(Config.imagePath).walk().mapNotNull { if (it.isFile) prop.call(it) else null }
    }

    fun getAllPhotosLinks(): Sequence<String> {
        return getAllPhotosNames().map { "${Config.prefix}$it" }
    }

    fun createRandomString(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..10)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun findAndDeleteIdenticalPhoto(newPhoto: File): String {
        val photosPaths = this.getAllPhotosPaths()
        var url = Config.prefix + newPhoto.name

        for (i in photosPaths) {
            val file = File(i)

            if (file.exists()) {
                if ((newPhoto.name != file.name) and
                    (file.readBytes().contentEquals(newPhoto.readBytes()))
                ) {
                    newPhoto.delete()
                    url = Config.prefix + file.name

                    break
                }
            }
        }

        return url
    }
}