
package org.papamitra.locationalarm

import android.content.{Context, ContentValues, ContentUris, ContentResolver, Intent}
import android.app.{PendingIntent, AlarmManager}
import android.util.Log

import java.util.Calendar

object Alarms{
  import Define._
  import org.scalaandroid.AndroidHelper._

  val ALERT_START = "location_alarm_alert_start"

  val ALARM_ACTIVE = "org.papamitra.locationalarm.ALARM_ACTIVE"

  val ALARM_INTENT_LATITUDE = "org.papamitra.locationalarm.intent.extra.latitude"
  val ALARM_INTENT_LONGITUDE = "org.papamitra.locationalarm.intent.extra.longitude"

  def updateAlarm(context:Context, alarm:Alarm, nextMillis:Long=0) =
    setAlarm(context,
	     id = alarm.id,
	     enabled = alarm.enabled,
	     label = alarm.label,
	     address = alarm.address,
	     latitude = alarm.latitude,
	     longitude = alarm.longitude,
	     ttlenabled = alarm.ttlenabled,
	     ttl=alarm.ttl,
	     nextmillis=nextMillis)

  def setAlarm(context:Context, id:Int, enabled:Boolean, label:String, address:String,
	       latitude:Double, longitude:Double,
	       ttlenabled:Boolean, ttl:TTL,
	       nextmillis:Long=0){
    
    val values = new ContentValues(12)
    val resolver = context.getContentResolver
    
    values.put(Alarm.Columns.LABEL, label)
    values.put(Alarm.Columns.ADDRESS, address)
    values.put(Alarm.Columns.ENABLED, enabled)
    values.put(Alarm.Columns.LONGITUDE, longitude)
    values.put(Alarm.Columns.LATITUDE, latitude)
    values.put(Alarm.Columns.VALID, true)
    
    values.put(Alarm.Columns.TTL, ttlenabled)
    values.put(Alarm.Columns.SHOUR, ttl.shour.asInstanceOf[java.lang.Integer])
    values.put(Alarm.Columns.SMINUTE, ttl.sminute.asInstanceOf[java.lang.Integer])
    values.put(Alarm.Columns.EHOUR, ttl.ehour.asInstanceOf[java.lang.Integer])
    values.put(Alarm.Columns.EMINUTE, ttl.eminute.asInstanceOf[java.lang.Integer])

    values.put(Alarm.Columns.NEXTMILLIS, nextmillis.asInstanceOf[java.lang.Long])

    resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, id),
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
	     alarm.ttlenabled, alarm.ttl, 0)


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
    val c = Calendar.getInstance
    c.setTimeInMillis(System.currentTimeMillis)
    
    val nowHour = c.get(Calendar.HOUR_OF_DAY)
    val nowMinute = c.get(Calendar.MINUTE)
    
    if(hour < nowHour || (hour == nowHour && minute < nowMinute)){
      c.add(Calendar.DAY_OF_YEAR, 1)
    }
    
    c.set(Calendar.HOUR_OF_DAY, hour)
    c.set(Calendar.MINUTE, minute)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)

    return c
  }

  def getCalendar(hour:Int, minute:Int, nowMillis:Long):Calendar = {
    val c = Calendar.getInstance
    c.setTimeInMillis(nowMillis)
    
    c.set(Calendar.HOUR_OF_DAY, hour);
    c.set(Calendar.MINUTE, minute);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);

    return c
  }

  def enableAlert(context:Context, atTimeInMillis:Long){
    val am = context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
    val intent = new Intent(ALARM_ACTIVE)
    val sender:PendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

    am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender)

    //TODO setStatusBarIcon(context, true);
  }

  def disableAlert(context:Context){
    val am = context.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
    val sender = PendingIntent.getBroadcast(
      context, 0, new Intent(ALARM_ACTIVE),
      PendingIntent.FLAG_CANCEL_CURRENT)

    am.cancel(sender);
    // TODO:setStatusBarIcon(context, false);
  }

}
