
package org.papamitra.locationalarm

import android.content.{Context, ContentValues, ContentUris, ContentResolver, Intent}
import android.app.{PendingIntent, AlarmManager}
import android.util.Log

import java.text.SimpleDateFormat
import java.util.Calendar

object Alarms{
  import Define._
  import org.scalaandroid.AndroidHelper._

  val ALERT_START = "alert_start"

  val ALARM_ACTIVE = "org.papamitra.locationalarm.ALARM_ACTIVE"

  val ALARM_ALERT_ACTION = "org.papamitra.locationalarm.ALARM_ALERT"

  val ALARM_INTENT_INITIALIZED = "org.papamitra.locationalarm.intent.extra.initialized"
  val ALARM_INTENT_LATITUDE = "org.papamitra.locationalarm.intent.extra.latitude"
  val ALARM_INTENT_LONGITUDE = "org.papamitra.locationalarm.intent.extra.longitude"

  val ALARM_KILLED = "alarm_killed"

  def updateAlarm(context:Context, alarm:Alarm, nextMillis:Long=0) =
    setAlarm(context,
	     id = alarm.id,
	     enabled = alarm.enabled,
	     label = alarm.label,
	     address = alarm.address,
	     latitude = alarm.latitude,
	     longitude = alarm.longitude,
	     ttl=alarm.ttl,
	     nextmillis=nextMillis)

  def setAlarm(context:Context, id:Int, enabled:Boolean, label:String, address:String,
	       latitude:Double, longitude:Double,
	       ttl:TTL, nextmillis:Long=0){
    
    val values = new ContentValues(10) withActions(
      _ put(Alarm.Columns.LABEL, label),
      _ put(Alarm.Columns.ADDRESS, address),
      _ put(Alarm.Columns.ENABLED, enabled),
      _ put(Alarm.Columns.LONGITUDE, longitude),
      _ put(Alarm.Columns.LATITUDE, latitude),
      _ put(Alarm.Columns.INITIALIZED, false),
      _ put(Alarm.Columns.SHOUR, ttl.shour.asInstanceOf[java.lang.Integer]),
      _ put(Alarm.Columns.SMINUTE, ttl.sminute.asInstanceOf[java.lang.Integer]),
      _ put(Alarm.Columns.MIN, ttl.min.asInstanceOf[java.lang.Integer]),
      _ put(Alarm.Columns.NEXTMILLIS, nextmillis.asInstanceOf[java.lang.Long]))

    context.getContentResolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, id),
				      values, null, null)

  }

  def enabledAlarm(context:Context, id:Int, enabled:Boolean):Unit =
    getAlarm(context.getContentResolver, id) match {
      case Some(alarm) => enabledAlarm(context, alarm, enabled)
      case _ =>
	Log.w(TAG,"Faild to get Alarm")
    }

  def enabledAlarm(context:Context, alarm:Alarm, enabled:Boolean):Unit =
    setAlarm(context, alarm.id, enabled, alarm.label, alarm.address,
	     alarm.latitude, alarm.longitude,
	     alarm.ttl, 0)


  def getAlarm(resolver:ContentResolver, id:Int):Option[Alarm] = 
    using(resolver.query(
      ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, id),
      Alarm.Columns.ALARM_QUERY_COLUMNS,
      null, null, null)) { c => c match {
      case null => None
      case c if c.moveToFirst => Some(new Alarm(c))
      case _ => None
    }}

  def getAlarmsCursor(resolver:ContentResolver) = 
    resolver.query(
		Alarm.Columns.CONTENT_URI,
		Alarm.Columns.ALARM_QUERY_COLUMNS,
		null, null, null)

  def getAllAlarm(resolver:ContentResolver) = 
    using(resolver.query(
		Alarm.Columns.CONTENT_URI,
		Alarm.Columns.ALARM_QUERY_COLUMNS,
		null, null, null)){ c=> c.map(new Alarm(_)).toArray}

  def calculateNextMillis(alarm:Alarm):Long = calculateAlarm(alarm.ttl.shour, alarm.ttl.sminute).getTimeInMillis

  def calculateAlarm(hour:Int, minute:Int):Calendar = {
    val c = Calendar.getInstance withAction(
      _ setTimeInMillis(System.currentTimeMillis))
    
    val nowHour = c.get(Calendar.HOUR_OF_DAY)
    val nowMinute = c.get(Calendar.MINUTE)
    
    if((hour < nowHour) || (hour == nowHour && minute < nowMinute)){
      c.add(Calendar.DAY_OF_YEAR, 1)
    }
    
    c withActions(
      _ set(Calendar.HOUR_OF_DAY, hour),
      _ set(Calendar.MINUTE, minute),
      _ set(Calendar.SECOND, 0),
      _ set(Calendar.MILLISECOND, 0))
  }

  def getCalendar(hour:Int, minute:Int, nowMillis:Long):Calendar = 
    Calendar.getInstance withActions(
      _ setTimeInMillis(nowMillis),
      _ set(Calendar.HOUR_OF_DAY, hour),
      _ set(Calendar.MINUTE, minute),
      _ set(Calendar.SECOND, 0),
      _ set(Calendar.MILLISECOND, 0))


  def enableAlert(context:Context, atTimeInMillis:Long){
    Log.i(TAG, "set alert:" + formatDate(Calendar.getInstance.withAction(_ setTimeInMillis(atTimeInMillis))))

    val am = context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
    val intent = new Intent(ALARM_ACTIVE)
    val sender:PendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

    am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender)
  }

  def disableAlert(context:Context){
    val am = context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
    val sender = PendingIntent.getBroadcast(
      context, 0, new Intent(ALARM_ACTIVE),
      PendingIntent.FLAG_CANCEL_CURRENT)

    am.cancel(sender);
    // TODO:setStatusBarIcon(context, false);
  }

  private def formatDate(cal:Calendar) = 
    (new SimpleDateFormat("yyyy/MM/dd/ HH:mm")).format(cal.getTime())

}
