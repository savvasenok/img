package xyz.savvamirzoyan.img

import io.ktor.server.netty.*
import java.io.File

object Config {
    val root: String =
        File(EngineMain.javaClass.protectionDomain.codeSource.location.toURI()).parent//Paths.get("").toAbsolutePath().toString()//System.getProperties().getProperty("user.dir")
    const val prefix = "http://127.0.0.1:8080/"
    val imagePath = "$root/img/"
    val imagePathTemplate = "$root/img/%s"
    const val imageContentType = "image"
}
