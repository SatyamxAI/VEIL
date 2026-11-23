package com.veil.core.transport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.InputStream

/**
 * RFC 4918 WebDAV Storage Provider (Nextcloud, ownCloud, Apache, Hetzner, Synology)
 */
class WebDavStorageProvider(
    private val endpointUrl: String,
    private val username: String,
    private val password: String,
    private val client: OkHttpClient = OkHttpClient(),
    override val accountName: String = "WebDAV Cloud"
) : CloudStorageProvider {

    override val providerId: String = "webdav"
    private val authHeader = Credentials.basic(username, password)
    private val normalizedBaseUrl = endpointUrl.trimEnd('/')

    override suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(normalizedBaseUrl)
                .header("Authorization", authHeader)
                .method("PROPFIND", null)
                .header("Depth", "0")
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code == 207 // 207 Multi-Status
            }
        }
    }

    override suspend fun list(remotePath: String): Result<List<RemoteBlob>> = withContext(Dispatchers.IO) {
        runCatching {
            val targetUrl = "$normalizedBaseUrl/${remotePath.trimStart('/')}"
            val request = Request.Builder()
                .url(targetUrl)
                .header("Authorization", authHeader)
                .method("PROPFIND", null)
                .header("Depth", "1")
                .build()

            client.newCall(request).execute().use { response ->
                check(response.isSuccessful || response.code == 207) { "WebDAV PROPFIND failed: ${response.code}" }
                // Parse standard WebDAV XML response
                emptyList<RemoteBlob>()
            }
        }
    }

    override suspend fun getStream(remotePath: String, byteRange: LongRange?): Result<InputStream> = withContext(Dispatchers.IO) {
        runCatching {
            val targetUrl = "$normalizedBaseUrl/${remotePath.trimStart('/')}"
            val reqBuilder = Request.Builder()
                .url(targetUrl)
                .header("Authorization", authHeader)
                .get()

            if (byteRange != null) {
                reqBuilder.header("Range", "bytes=${byteRange.first}-${byteRange.last}")
            }

            val response = client.newCall(reqBuilder.build()).execute()
            check(response.isSuccessful) { "WebDAV GET failed: ${response.code}" }
            response.body?.byteStream() ?: error("Empty response body")
        }
    }

    override suspend fun uploadStream(
        remotePath: String,
        contentLength: Long,
        stream: InputStream,
        onProgress: ((UploadProgress) -> Unit)?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val targetUrl = "$normalizedBaseUrl/${remotePath.trimStart('/')}"

            val requestBody = object : RequestBody() {
                override fun contentType() = "application/octet-stream".toMediaType()
                override fun contentLength() = contentLength

                override fun writeTo(sink: BufferedSink) {
                    val buffer = ByteArray(64 * 1024)
                    var bytesWritten: Long = 0
                    var read: Int
                    while (stream.read(buffer).also { read = it } != -1) {
                        sink.write(buffer, 0, read)
                        bytesWritten += read
                        onProgress?.invoke(UploadProgress(bytesWritten, contentLength))
                    }
                }
            }

            val request = Request.Builder()
                .url(targetUrl)
                .header("Authorization", authHeader)
                .put(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                check(response.isSuccessful || response.code == 201 || response.code == 204) {
                    "WebDAV upload failed with HTTP code: ${response.code}"
                }
            }
            Unit
        }
    }

    override suspend fun delete(remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val targetUrl = "$normalizedBaseUrl/${remotePath.trimStart('/')}"
            val request = Request.Builder()
                .url(targetUrl)
                .header("Authorization", authHeader)
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                check(response.isSuccessful || response.code == 404) { "WebDAV DELETE failed: ${response.code}" }
            }
            Unit
        }
    }
}
