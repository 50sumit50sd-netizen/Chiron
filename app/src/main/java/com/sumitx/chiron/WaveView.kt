package com.sumitx.chiron
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.view.View
import kotlin.math.abs
import kotlin.math.sin
class WaveView(context:Context):View(context) {
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth=4f; strokeCap=Paint.Cap.ROUND }
 private var phase=0.0
 override fun onDraw(canvas:Canvas) {
  super.onDraw(canvas); val bars=30; val gap=width.toFloat()/(bars+1)
  for(i in 0 until bars) {
   val amplitude=abs(sin(phase+i*.48))*.75+.15; val h=(height*amplitude).toFloat()
   paint.color=Color.HSVToColor(floatArrayOf((i*360f/bars+phase.toFloat()*18f)%360f,.85f,1f))
   val x=gap*(i+1); canvas.drawLine(x,height/2f-h/2,x,height/2f+h/2,paint)
  }
  phase+=.16; postInvalidateDelayed(70)
 }
}