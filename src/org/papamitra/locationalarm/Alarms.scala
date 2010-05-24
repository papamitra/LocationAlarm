
package org.papamitra.locationalarm

import android.content.{Context, ContentValues, ContentUris, ContentResolver}

object Alarms{
  import org.scalaandroid.AndroidHelper._

  val ALERT_START = "location_alarm_alert_start"

  val ALARM_INTENT_LATITUDE = "org.papamitra.locationalarm.intent.extra.latitude"
  val ALARM_INTENT_LONGITUDE = "org.papamitra.locationalarm.intent.extra.longitude"

  def updateAlarm(context:Context, alarm:Alarm) =
    setAlarm(context,
	     id = alarm.id,
	     enabled = alarm.enabled,
	     label = alarm.label,
	     address = alarm.address,
	     latitude = alarm.latitude,
	     longitude = alarm.longitude)

  def setAlarm(context:Context, id:Int, enabled:Boolean, label:String, address:String,
	       latitude:Double, longitude:Double){
    
    val values = new ContentValues(6)
    val resolver = context.getContentResolver
    
    values.put(Alarm.Columns.LABEL, label)
    values.put(Alarm.Columns.ADDRESS, address)
    values.put(Alarm.Columns.ENABLED, enabled)
    values.put(Alarm.Columns.LONGITUDE, longitude)
    values.put(Alarm.Columns.LATITUDE, latitude)
    values.put(Alarm.Columns.VALID, true)
    
    resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, id),
                    values, null, null)

  }

  def enabledAlarm(context:Context, id:Int, enabled:Boolean):Unit =
    getAlarm(context.getContentResolver, id) match {
      case Some(alarm) => enabledAlarm(context, alarm, enabled)
      case _ =>
    }

  def enabledAlarm(context:Context, alarm:Alarm, enabled:Boolean):Unit =
    setAlarm(context, alarm.id, enabled, alarm.label, alarm.address,
	     alarm.latitude, alarm.longitude)


  def getAlarm(resolver:ContentResolver, id:Int):Option[Alarm] = 
    resolver.query(
      ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, id),
      Alarm.Columns.ALARM_QUERY_COLUMNS,
      null, null, null) match {
      case null => None
      case c if c.moveToFirst => Some(new Alarm(c))
      case _ => None
    }

  def getAlarmsCursor(resolver:ContentResolver) = 
    resolver.query(
		Alarm.Columns.CONTENT_URI,
		Alarm.Columns.ALARM_QUERY_COLUMNS,
		null, null, null)

  def getAllAlarm(resolver:ContentResolver) = 
    resolver.query(
		Alarm.Columns.CONTENT_URI,
		Alarm.Columns.ALARM_QUERY_COLUMNS,
		null, null, null).map(new Alarm(_))

}
