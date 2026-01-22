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

suspend fun downloadVideo(
    context: Context,
    videoUrl: String,
    fileName: String = "downloaded_video.mp4"
) {
    withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(videoUrl)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to download video: ${response.code}")

            val body = response.body ?: throw IOException("No response body")

            // For Android 10+ - save to Downloads
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val videoFile = File(downloadsDir, fileName)

            FileOutputStream(videoFile).use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                }
            }

            // Notify MediaScanner so it appears in gallery
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(videoFile)
            context.sendBroadcast(mediaScanIntent)

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Video saved to Downloads!", Toast.LENGTH_LONG).show()
            }
        }
    }
}