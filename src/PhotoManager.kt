package xyz.savvamirzoyan.img

import java.io.File

object PhotoManager {
    fun getAllPhotosPaths(): ArrayList<String> {
        val photos = arrayListOf<String>()
        File(Config.imagePath).walk().forEach { if (it.isFile) photos.add(it.absolutePath) }
        return photos
    }

    fun getAllPhotosNames(): ArrayList<String> {
        val photos = arrayListOf<String>()
        File(Config.imagePath).walk().forEach { if (it.isFile) photos.add(it.name) }
        return photos
    }

    fun getAllPhotosLinks(): ArrayList<String> {
        return ArrayList(getAllPhotosNames().map { "${Config.prefix}$it" })
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

        mainLoop@ for (i in photosPaths) {
            val file = File(i)

            if (file.exists()) {
                if ((newPhoto.name != file.name) and
                    (file.readBytes().contentEquals(newPhoto.readBytes()))
                ) {
                    newPhoto.delete()
                    url = Config.prefix + file.name

                    break@mainLoop
                }
            }
        }

        return url
    }
}