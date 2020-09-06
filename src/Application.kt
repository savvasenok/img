package xyz.savvamirzoyan.img

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.io.InputStream
import java.io.OutputStream

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {

    install(DefaultHeaders)
    install(CallLogging)
    install(ConditionalHeaders)
    install(StatusPages) {
        exception<NoSuchFileException> {
            call.respond(HttpStatusCode.NotFound)
        }
    }
    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/{photo_id}") {
            val file = File(Config.imagePathTemplate.format(call.parameters["photo_id"]))
            call.respondFile(file)
        }

        get("/info") {
            call.respond(
                mapOf(
                    "getAllPhotosPaths" to PhotoManager.getAllPhotosPaths(),
                    "getAllPhotosNames" to PhotoManager.getAllPhotosNames(),
                    "getAllPhotosLinks" to PhotoManager.getAllPhotosLinks()
                )
            )
        }

        get("/test") {
            call.respond(
                mapOf(
                    "OK" to true,
                    "links" to PhotoManager.getAllPhotosLinks()
                )
            )
        }

        post("/upload") {
            val multipart = call.receiveMultipart()
            val urls = arrayListOf<String>()
            val urlMap = mutableMapOf<String, Any>(
                "OK" to false,
                "links" to arrayListOf<String>()
            )

            val parts = multipart.readAllParts()

            parts.forEach { part ->
                if (part.contentType?.contentType == Config.imageContentType) {
                    when (part) {
                        is PartData.FileItem -> {
                            val ext = File(part.originalFileName!!).extension
                            val title = "${System.currentTimeMillis()}-${PhotoManager.createRandomString()}.$ext"
                            val file = File(Config.imagePathTemplate.format(title))

                            part.streamProvider().use { input ->
                                file.outputStream().buffered().use { output ->
                                    input.copyToSuspend(output)
                                }
                            }

                            urls.add(PhotoManager.findAndDeleteIdenticalPhoto(file))
                        }
                        else -> {}
                    }
                    part.dispose()
                }
            }

            urlMap["OK"] = true
            urlMap["links"] = urls

            call.respond(urlMap)
        }
    }
}

suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        return@withContext bytesCopied
    }
}
