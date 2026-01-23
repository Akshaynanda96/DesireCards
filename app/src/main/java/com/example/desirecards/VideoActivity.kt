package com.example.desirecards

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
import java.io.IOException

class VideoActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var btnBack: Button

    // This client stays internal to your app and won't trigger external app opens
    private val okHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        playerView = findViewById(R.id.player_view)
        progressBar = findViewById(R.id.progress_bar)
        tvProgress = findViewById(R.id.tv_progress)
        btnBack = findViewById(R.id.back_btn)

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        val videoUrlFromIntent = intent.getStringExtra("VIDEO_URL") ?: ""
        btnBack.setOnClickListener { finish() }

        if (videoUrlFromIntent.isNotBlank()) {
            startExtraction(videoUrlFromIntent)
        }
    }

    private fun startExtraction(url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            tvProgress.visibility = View.VISIBLE
            tvProgress.text = "Fetching data (staying in app)..."
            progressBar.visibility = View.VISIBLE

            try {
                val videoUrl = withContext(Dispatchers.IO) { fetchVideoUrlInternal(url) }
                playVideo(videoUrl)
                tvProgress.visibility = View.GONE
                progressBar.visibility = View.GONE
            } catch (e: Exception) {
                tvProgress.text = "Error: ${e.message}"
                progressBar.visibility = View.GONE
                Toast.makeText(this@VideoActivity, "Couldn't bypass Reddit app redirect", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun fetchVideoUrlInternal(redditUrl: String): String = withContext(Dispatchers.IO) {
        // THE FIX: Change www.reddit.com to old.reddit.com to prevent the Reddit App from opening
        var modifiedUrl = redditUrl.replace("www.reddit.com", "old.reddit.com")
            .replace("reddit.com", "old.reddit.com")
            .substringBefore("?")

        // Force the .json extension
        if (!modifiedUrl.endsWith(".json")) {
            modifiedUrl = "${modifiedUrl.trimEnd('/')}.json"
        }

        val request = Request.Builder().url(modifiedUrl).build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Server Error: ${response.code}")

            val body = response.body?.string() ?: throw IOException("Empty Response")

            // Check for HTML (Bot detection)
            if (body.trim().startsWith("<!DOCTYPE")) {
                throw IOException("Reddit blocked the data request.")
            }

            val rootArray = JSONArray(body)
            val postData = rootArray.getJSONObject(0)
                .getJSONObject("data")
                .getJSONArray("children")
                .getJSONObject(0)
                .getJSONObject("data")

            var finalVideoUrl = ""

            // Look for video in standard media object
            if (postData.has("media") && !postData.isNull("media")) {
                finalVideoUrl = postData.getJSONObject("media")
                    .getJSONObject("reddit_video")
                    .getString("fallback_url")
            }
            // Look for video in preview (fallback)
            else if (postData.has("preview") && postData.getJSONObject("preview").has("reddit_video_preview")) {
                finalVideoUrl = postData.getJSONObject("preview")
                    .getJSONObject("reddit_video_preview")
                    .getString("fallback_url")
            }

            if (finalVideoUrl.isEmpty()) throw IOException("No video found.")

            finalVideoUrl
        }
    }

    private fun playVideo(url: String) {
        playerView.visibility = View.VISIBLE
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}