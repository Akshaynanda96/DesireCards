package com.example.desirecards

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class VideoActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var btnBack: Button

    private var extractedVideoUrl: String? = null
    private var downloadedFile: File? = null

    private val okHttpClient = OkHttpClient()

    // Example Reddit URL (you can pass it via Intent if needed)
    private val redditUrl = "https://www.reddit.com/r/Whatcouldgowrong/comments/1qjwb9a/wcgw_trying_to_put_a_fire_out_by_putting_it/?utm_source=share&utm_medium=web3x&utm_name=web3xcss&utm_term=1&utm_content=share_button"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        playerView = findViewById(R.id.player_view)
        progressBar = findViewById(R.id.progress_bar)
        tvProgress = findViewById(R.id.tv_progress)
        btnBack = findViewById(R.id.back_btn)

        // Hide player initially
        playerView.visibility = View.GONE
        progressBar.visibility = View.GONE
        tvProgress.visibility = View.GONE

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // Start extracting video URL
        extractRedditVideoUrl()

        btnBack.setOnClickListener { finish() }
    }

    private fun extractRedditVideoUrl() {
        CoroutineScope(Dispatchers.Main).launch {
            tvProgress.visibility = View.VISIBLE
            tvProgress.text = "Extracting video URL..."
            progressBar.visibility = View.VISIBLE

            try {
                val videoUrl = withContext(Dispatchers.IO) { fetchRedditVideoUrl() }
                extractedVideoUrl = videoUrl

                tvProgress.text = "Video ready!"
                progressBar.visibility = View.GONE

                // Auto-play the video directly (streaming)
                playVideoDirectly(videoUrl)

                Toast.makeText(this@VideoActivity, "Video loaded! You can download it too.", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                tvProgress.text = "Failed to load video: ${e.message}"
                Toast.makeText(this@VideoActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun fetchRedditVideoUrl(): String = withContext(Dispatchers.IO) {
        val jsonUrl = redditUrl.replace(Regex("\\?.*"), "") + ".json"

        val request = Request.Builder()
            .url(jsonUrl)
            .header("User-Agent", "Mozilla/5.0 (compatible; Android App; Reddit Video Downloader)")
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to fetch JSON: ${response.code}")

            val jsonString = response.body?.string() ?: throw Exception("Empty response")
            val rootArray = JSONArray(jsonString)

            if (rootArray.length() == 0) throw Exception("Empty JSON response")

            val listing = rootArray.getJSONObject(0)
            val children = listing.getJSONObject("data").getJSONArray("children")

            if (children.length() == 0) throw Exception("No children found")

            val postData = children.getJSONObject(0).getJSONObject("data")

            var videoUrl = ""
            if (postData.has("preview") && postData.getJSONObject("preview").has("reddit_video_preview")) {
                videoUrl = postData.getJSONObject("preview")
                    .getJSONObject("reddit_video_preview")
                    .getString("fallback_url")
            } else if (postData.has("media") && postData.getJSONObject("media").has("reddit_video")) {
                videoUrl = postData.getJSONObject("media")
                    .getJSONObject("reddit_video")
                    .getString("fallback_url")
            }

            if (videoUrl.isEmpty()) throw Exception("No video URL found in post")

            // Clean URL (remove query params)
            videoUrl.split("?")[0]
        }
    }

    private fun playVideoDirectly(url: String) {
        playerView.visibility = View.VISIBLE
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    private suspend fun downloadVideo(url: String): File = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (compatible; Android App; Reddit Video Downloader)")
            .header("Referer", "https://www.reddit.com/")
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Download failed: ${response.code} - ${response.message}")
            }

            val body = response.body ?: throw Exception("Empty response body")

            val file = File(cacheDir, "reddit_video_${System.currentTimeMillis()}.mp4")

            FileOutputStream(file).use { output ->
                val buffer = ByteArray(8 * 1024)
                var totalBytesRead = 0L
                val contentLength = body.contentLength()

                body.byteStream().use { input ->
                    while (true) {
                        val bytesRead = input.read(buffer)
                        if (bytesRead == -1) break
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        withContext(Dispatchers.Main) {
                            if (contentLength > 0) {
                                val progress = ((totalBytesRead * 100) / contentLength).toInt()
                                progressBar.progress = progress
                                tvProgress.text = "Downloading... $progress%"
                            } else {
                                tvProgress.text = "Downloading... ${totalBytesRead / 1024} KB"
                            }
                        }
                    }
                }
            }
            file
        }
    }

    private fun playLocalVideo(file: File) {
        playerView.visibility = View.VISIBLE
        val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onStop() {
        super.onStop()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        // Optional: clean up downloaded file
        // downloadedFile?.delete()
    }
}