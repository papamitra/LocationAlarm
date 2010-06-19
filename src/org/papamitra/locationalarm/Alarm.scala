
package org.papamitra.locationalarm

import android.provider.BaseColumns
import android.net.Uri
import android.database.Cursor

import org.scalaandroid.AndroidHelper._

object Alarm{

  object Columns extends BaseColumns{
    val CONTENT_URI = Uri.parse("content://org.papamitra.locationalarm/alarm")
    val LABEL = "label"
    val ADDRESS = "address"
    val ENABLED = "enabled"
    val LONGITUDE = "longitude"
    val LATITUDE = "latitude"
    val INITIALIZED = "initialized"

    val SHOUR = "shour"
    val SMINUTE = "sminute"
    val MIN = "min"
    val NEXTMILLIS = "nextmillis"

    val ALARM_QUERY_COLUMNS = Array[String]( BaseColumns._ID, LABEL,
					    ADDRESS, ENABLED,
					    LONGITUDE, LATITUDE, INITIALIZED,
					    SHOUR, SMINUTE, MIN,
					    NEXTMILLIS)
    val ALARM_ID_INDEX = 0
    val ALARM_LABEL_INDEX = 1
    val ALARM_ADDRESS_INDEX = 2
    val ALARM_ENABLED_INDEX = 3
    val ALARM_LONGITUDE_INDEX = 4
    val ALARM_LATITUDE_INDEX = 5
    val ALARM_INITIALIZED_INDEX = 6

    val ALARM_SHOUR_INDEX = 7
    val ALARM_SMINUTE_INDEX = 8
    val ALARM_MIN_INDEX = 9
    val ALARM_NEXTMILLIS_INDEX = 10

  }
}

case class TTL(val shour:Int, val sminute:Int, val min:Int){
  import java.util.Calendar
  import android.util.Log
  import Define._
  
  def isInTimeRange(nowMillis:Long):Boolean = {
    val smin = shour * 60 + sminute

    val c = Calendar.getInstance withAction(_ setTimeInMillis(nowMillis))
    val nowmin = (c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE))

    if(nowmin < smin){
      (nowmin + (24 * 60) - smin) < min
    }else{
      (nowmin - smin) < min
    }
  }
}

class Alarm(c:Cursor){

  import Alarm._
  val id = c.getInt(Columns.ALARM_ID_INDEX)
  val label = c.getString(Columns.ALARM_LABEL_INDEX)
  val address = c.getString(Columns.ALARM_ADDRESS_INDEX)
  val enabled = c.getInt(Columns.ALARM_ENABLED_INDEX) == 1
  val longitude:Double = c.getDouble(Columns.ALARM_LONGITUDE_INDEX)
  val latitude:Double = c.getDouble(Columns.ALARM_LATITUDE_INDEX)
  val initialized = c.getInt(Columns.ALARM_INITIALIZED_INDEX) == 1

  val ttl = TTL(c.getInt(Columns.ALARM_SHOUR_INDEX),
	    c.getInt(Columns.ALARM_SMINUTE_INDEX),
	    c.getInt(Columns.ALARM_MIN_INDEX))

  val nextmillis = c.getLong(Columns.ALARM_NEXTMILLIS_INDEX)

  def isActiveAt(nowMillis:Long):Boolean = enabled match {
    case false => false
    case true if nextmillis > nowMillis => false
    case true => ttl.isInTimeRange(nowMillis)
  }
}
