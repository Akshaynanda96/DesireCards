package com.example.desirecards

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

suspend fun downloadVideoWithProgress(
    context: Context,
    videoUrl: String,
    fileName: String,
    onProgress: (progress: Int) -> Unit
) {
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val request = Request.Builder().url(videoUrl).build()

        client.newCall(request).execute().use { response ->
            val body = response.body ?: return@use

            val contentLength = body.contentLength()

            val videoFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            var downloaded: Long = 0

            FileOutputStream(videoFile).use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        val progress = ((downloaded * 100) / contentLength).toInt()
                        withContext(Dispatchers.Main) {
                            onProgress(progress)
                        }
                    }
                }
            }

            // Scan file
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(videoFile)
            context.sendBroadcast(mediaScanIntent)
        }
    }
}