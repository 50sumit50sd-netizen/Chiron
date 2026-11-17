package com.sumitx.chiron

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
 private val prefs by lazy { getSharedPreferences("sumitx_settings", MODE_PRIVATE) }
 private val colors = intArrayOf(Color.rgb(0,229,255),Color.rgb(170,90,255),Color.rgb(255,70,160),Color.rgb(100,255,150))
 private val accent: Int get() = colors[prefs.getInt("accent_index",0).coerceIn(0,3)]
 private lateinit var status: TextView
 private lateinit var lyricsPreview: TextView
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); render() }
 override fun onResume() { super.onResume(); if (::status.isInitialized) refreshStatus() }

 private fun render() {
  window.statusBarColor = Color.rgb(5,8,18); window.navigationBarColor = Color.rgb(5,8,18)
  val content = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(22,28,22,24); setBackgroundColor(Color.rgb(5,8,18)) }
  val scroll = ScrollView(this).apply { addView(content) }
  content.addView(TextView(this).apply { text="SUMITX"; textSize=32f; letterSpacing=.18f; typeface=Typeface.create("sans-serif-black",Typeface.BOLD); setTextColor(accent); gravity=Gravity.CENTER }, matchWrap())
  content.addView(label("CHIRON  •  MUSIC COMPANION"),matchWrap())
  status=label("Checking permissions…").apply { textSize=13f; setPadding(0,24,0,16) }; content.addView(status,matchWrap())
  content.addView(action("GRANT NOTIFICATION ACCESS") { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },matchWrap())
  content.addView(action("ALLOW DISPLAY OVER OTHER APPS") {
   if (!Settings.canDrawOverlays(this)) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.fromParts("package",packageName,null))) else toast("Overlay permission is enabled")
  },matchWrap())
  content.addView(action("START MUSIC ISLAND") {
   when {
    !Settings.canDrawOverlays(this) -> { toast("Allow overlay permission first"); startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.fromParts("package",packageName,null))) }
    !hasNotificationAccess() -> { toast("Grant notification access first"); startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
    else -> { startForegroundService(Intent(this,IslandOverlayService::class.java).setAction(IslandOverlayService.ACTION_START)); toast("SUMITX island started") }
   }
  },matchWrap())
  content.addView(action("STOP MUSIC ISLAND") { startService(Intent(this,IslandOverlayService::class.java).setAction(IslandOverlayService.ACTION_STOP)); toast("Island stopped") },matchWrap())
  content.addView(section("APPEARANCE"),matchWrap())
  content.addView(action("CHANGE NEON ACCENT") { prefs.edit().putInt("accent_index",(prefs.getInt("accent_index",0)+1)%colors.size).apply(); render() },matchWrap())
  content.addView(action("AUTO-COLLAPSE: "+prefs.getInt("collapse_seconds",5)+" SECONDS") {
   val next=when(prefs.getInt("collapse_seconds",5)){3->5;5->8;else->3}; prefs.edit().putInt("collapse_seconds",next).apply(); render()
  },matchWrap())
  content.addView(section("LYRICS"),matchWrap())
  content.addView(action("IMPORT LRC LYRICS FILE") {
   val pick=Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type="text/*" }; startActivityForResult(pick,41)
  },matchWrap())
  lyricsPreview=label(prefs.getString("lyrics_preview","No LRC file imported yet.").orEmpty()).apply { textSize=12f; setPadding(12,12,12,12); setBackgroundColor(Color.rgb(15,22,38)) }
  content.addView(lyricsPreview,matchWrap())
  content.addView(section("PRIVACY"),matchWrap())
  content.addView(label("SUMITX reads media notifications only after you grant access. The floating island starts only when you choose it and allow overlay permission. Imported LRC lyrics remain on this device. No account or analytics SDK is included."),matchWrap())
  content.addView(label("YouTube Music support depends on its media session being available on your device. Lyrics are imported from your own LRC file; automatic lyric-site scraping is not included."),matchWrap())
  setContentView(scroll); refreshStatus()
 }
 private fun refreshStatus() {
  if (!::status.isInitialized) return
  val notification=if(hasNotificationAccess()) "Granted" else "Not granted"
  val overlay=if(Settings.canDrawOverlays(this)) "Granted" else "Not granted"
  val track=getSharedPreferences("sumitx_media",MODE_PRIVATE).getString("title",null) ?: "No track detected"
  status.text="NOTIFICATION ACCESS: "+notification+"\nOVERLAY ACCESS: "+overlay+"\nLAST TRACK: "+track
 }
 private fun hasNotificationAccess(): Boolean {
  val enabled=Settings.Secure.getString(contentResolver,"enabled_notification_listeners").orEmpty()
  return enabled.split(":").any { ComponentName.unflattenFromString(it)==ComponentName(this,MediaNotificationListener::class.java) }
 }
 @Deprecated("Kept for broad Android device compatibility")
 override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
  super.onActivityResult(requestCode,resultCode,data)
  if(requestCode==41 && resultCode==RESULT_OK) {
   val uri=data?.data ?: return
   try {
    val raw=contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText().take(250000) } ?: return
    val preview=LyricsParser.parse(raw).take(1000)
    prefs.edit().putString("lyrics_raw",raw).putString("lyrics_preview",preview).apply()
    if(::lyricsPreview.isInitialized) lyricsPreview.text=preview
    toast("LRC lyrics imported")
   } catch (_:Exception) { toast("Could not read this lyrics file") }
  }
 }
 private fun action(value:String,click:()->Unit)=Button(this).apply { text=value; isAllCaps=false; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(20,31,52)); setOnClickListener { click() } }
 private fun label(value:String)=TextView(this).apply { text=value; setTextColor(Color.LTGRAY); textSize=12f; gravity=Gravity.CENTER }
 private fun section(value:String)=TextView(this).apply { text=value; setTextColor(accent); textSize=13f; typeface=Typeface.DEFAULT_BOLD; setPadding(0,22,0,8) }
 private fun matchWrap()=LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin=9 }
 private fun toast(value:String)=Toast.makeText(this,value,Toast.LENGTH_SHORT).show()
}