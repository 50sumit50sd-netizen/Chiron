package com.sumitx.chiron
object LyricsParser {
 private val stamp=Regex("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?]")
 fun parse(raw:String):String {
  val lines=raw.lineSequence().flatMap { line ->
   val matches=stamp.findAll(line).toList()
   if(matches.isEmpty()) emptySequence() else {
    val lyric=line.substring(matches.last().range.last+1).trim()
    matches.asSequence().map { m ->
     val mins=m.groupValues[1].toLong(); val secs=m.groupValues[2].toLong()
     val ms=m.groupValues[3].padEnd(3,'0').ifEmpty { "000" }.toLong()
     Pair((mins*60+secs)*1000+ms,lyric)
    }
   }
  }.sortedBy { it.first }.toList()
  if(lines.isEmpty()) return "No timestamped LRC lines found."
  return lines.joinToString("\n") { "[%02d:%02d] %s".format(it.first/60000,(it.first/1000)%60,it.second) }
 }
 fun currentLine(raw:String,positionMs:Long):String {
  val parsed=parse(raw); if(parsed.startsWith("No timestamped")) return ""
  var current=""
  for(line in parsed.lineSequence()) {
   val m=Regex("^\\[(\\d{2}):(\\d{2})]").find(line) ?: continue
   val time=(m.groupValues[1].toLong()*60+m.groupValues[2].toLong())*1000
   if(time<=positionMs) current=line.substring(m.range.last+1).trim() else break
  }
  return current
 }
}