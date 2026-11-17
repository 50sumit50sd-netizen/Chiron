package com.sumitx.chiron

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private val neon = Color.rgb(0, 230, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(5, 8, 18)
        window.navigationBarColor = Color.rgb(5, 8, 18)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 36)
            setBackgroundColor(Color.rgb(5, 8, 18))
        }
        val title = TextView(this).apply {
            text = "SUMITX"
            textSize = 34f
            letterSpacing = 0.18f
            setTextColor(neon)
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "CHIRON  •  MUSIC COMPANION"
            textSize = 12f
            letterSpacing = 0.12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        val status = TextView(this).apply {
            text = "Your music island starts here."
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 42, 0, 28)
        }
        val permissionButton = Button(this).apply {
            text = "GRANT NOTIFICATION ACCESS"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }
        val refreshButton = Button(this).apply {
            text = "CHECK DETECTED TRACK"
            setOnClickListener {
                val prefs = getSharedPreferences("sumitx_media", MODE_PRIVATE)
                val song = prefs.getString("title", null)
                val artist = prefs.getString("artist", null)
                status.text = if (song.isNullOrBlank()) {
                    "No media notification detected yet. Start music and check again."
                } else {
                    "NOW PLAYING\n$song\n${artist.orEmpty()}"
                }
            }
        }
        root.addView(title, matchWrap())
        root.addView(subtitle, matchWrap())
        root.addView(status, matchWrap())
        root.addView(permissionButton, matchWrap())
        root.addView(refreshButton, matchWrap())
        setContentView(root)
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = 12 }
}
