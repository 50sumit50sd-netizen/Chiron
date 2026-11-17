package com.sumitx.chiron
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class IslandOverlayService:Service() {
 private lateinit var wm:WindowManager
 private lateinit var panel:LinearLayout
 private lateinit var title:TextView
 private lateinit var artist:TextView
 private lateinit var lyric:TextView
 private lateinit var controls:LinearLayout
 private var expanded=false
 private var controller:MediaController?=null
 private val handler=Handler(Looper.getMainLooper())
 private val prefs by lazy { getSharedPreferences("sumitx_settings",MODE_PRIVATE) }
 private val updater=object:Runnable { override fun run(){ updateTrack(); handler.postDelayed(this,1000) } }
 override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
  if(intent?.action==ACTION_STOP){ stopSelf(); return START_NOT_STICKY }
  if(intent?.action==ACTION_START) startIsland()
  return START_STICKY
 }
 private fun startIsland() {
  if(!Settings.canDrawOverlays(this)){ stopSelf(); return }
  createChannel()
  val n=Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("SUMITX music island is active").setContentText("Floating music controls are running because you started them.").setOngoing(true).build()
  if(Build.VERSION.SDK_INT>=34) startForeground(NOTIFICATION_ID,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) else startForeground(NOTIFICATION_ID,n)
  wm=getSystemService(WINDOW_SERVICE) as WindowManager
  buildPanel()
  val type=if(Build.VERSION.SDK_INT>=26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
  val params=WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT)
  params.gravity=Gravity.TOP or Gravity.CENTER_HORIZONTAL; params.y=dp(12)
  try { wm.addView(panel,params) } catch(_:Exception){ stopSelf(); return }
  handler.removeCallbacks(updater); handler.post(updater)
 }
 private fun buildPanel() {
  val palette=intArrayOf(Color.rgb(0,229,255),Color.rgb(170,90,255),Color.rgb(255,70,160),Color.rgb(100,255,150))
  val accent=palette[prefs.getInt("accent_index",0).coerceIn(0,3)]
  panel=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(18),dp(10),dp(18),dp(10)); background=GradientDrawable().apply { setColor(Color.rgb(8,11,22)); cornerRadius=dp(28).toFloat(); setStroke(dp(1),accent) } }
  val header=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
  header.addView(TextView(this).apply { text="●"; textSize=12f; setTextColor(accent); setPadding(0,0,dp(8),0) })
  val column=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; layoutParams=LinearLayout.LayoutParams(0,-2,1f) }
  title=TextView(this).apply { text="SUMITX • Waiting for music"; textSize=13f; typeface=Typeface.DEFAULT_BOLD; setTextColor(Color.WHITE); maxLines=1 }
  artist=TextView(this).apply { text="Start playback in your music app"; textSize=10f; setTextColor(Color.LTGRAY); maxLines=1 }
  column.addView(title); column.addView(artist); header.addView(column)
  header.addView(TextView(this).apply { text="⌄"; textSize=22f; setTextColor(accent); setPadding(dp(12),0,dp(4),0); setOnClickListener { expanded=!expanded; updateExpanded() } })
  panel.addView(header)
  panel.addView(WaveView(this).apply { visibility=View.GONE; layoutParams=LinearLayout.LayoutParams(-1,dp(34)) })
  lyric=TextView(this).apply { textSize=12f; setTextColor(accent); gravity=Gravity.CENTER; visibility=View.GONE; setPadding(0,dp(6),0,dp(6)) }
  panel.addView(lyric)
  controls=LinearLayout(this).apply { gravity=Gravity.CENTER; visibility=View.GONE }
  addControl("PREV"){ controller?.transportControls?.skipToPrevious() }
  addControl("PLAY / PAUSE"){ if(controller?.playbackState?.state==android.media.session.PlaybackState.STATE_PLAYING) controller?.transportControls?.pause() else controller?.transportControls?.play() }
  addControl("NEXT"){ controller?.transportControls?.skipToNext() }
  panel.addView(controls)
 }
 private fun addControl(value:String,action:()->Unit){ controls.addView(TextView(this).apply { text=value; textSize=10f; setTextColor(Color.WHITE); gravity=Gravity.CENTER; setPadding(dp(10),dp(8),dp(10),dp(8)); setOnClickListener { action() } }) }
 private fun updateExpanded(){
  if(!::panel.isInitialized)return
  panel.getChildAt(1).visibility=if(expanded)View.VISIBLE else View.GONE
  lyric.visibility=if(expanded)View.VISIBLE else View.GONE
  controls.visibility=if(expanded)View.VISIBLE else View.GONE
  if(expanded)handler.postDelayed({ if(expanded){ expanded=false; updateExpanded() } },prefs.getInt("collapse_seconds",5)*1000L)
 }
 private fun updateTrack(){
  try {
   val manager=getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
   val sessions=manager.getActiveSessions(ComponentName(this,MediaNotificationListener::class.java))
   controller=sessions.firstOrNull()
   val metadata=controller?.metadata
   val song=metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
   val performer=metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
   if(!song.isNullOrBlank()){
    title.text=song; artist.text=performer.orEmpty()
    lyric.text=LyricsParser.currentLine(prefs.getString("lyrics_raw","").orEmpty(),controller?.playbackState?.position ?: 0L)
   } else {
    val saved=getSharedPreferences("sumitx_media",MODE_PRIVATE)
    title.text=saved.getString("title","SUMITX • Waiting for music")
    artist.text=saved.getString("artist","Start playback in your music app")
   }
  } catch(_:SecurityException){ title.text="Media access not ready"; artist.text="Check notification access in SUMITX" } catch(_:Exception){}
 }
 private fun createChannel(){
  val manager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
  if(Build.VERSION.SDK_INT>=26)manager.createNotificationChannel(NotificationChannel(CHANNEL,"SUMITX Music Island",NotificationManager.IMPORTANCE_LOW))
 }
 private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
 override fun onDestroy(){
  handler.removeCallbacks(updater)
  if(::panel.isInitialized)try{wm.removeView(panel)}catch(_:Exception){}
  stopForeground(STOP_FOREGROUND_REMOVE); super.onDestroy()
 }
 override fun onBind(intent:Intent?):IBinder?=null
 companion object {
  const val ACTION_START="com.sumitx.chiron.START_ISLAND"
  const val ACTION_STOP="com.sumitx.chiron.STOP_ISLAND"
  private const val CHANNEL="sumitx_island"
  private const val NOTIFICATION_ID=6201
 }
}