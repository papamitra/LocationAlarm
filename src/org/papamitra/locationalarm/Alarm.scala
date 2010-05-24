
package org.papamitra.locationalarm

import android.provider.BaseColumns
import android.net.Uri
import android.database.Cursor

object Alarm{

  object Columns extends BaseColumns{
    val CONTENT_URI = Uri.parse("content://org.papamitra.locationalarm/alarm")
    val LABEL = "label"
    val ADDRESS = "address"
    val ENABLED = "enabled"
    val LONGITUDE = "longitude"
    val LATITUDE = "latitude"
    val VALID = "valid"

    val ALARM_QUERY_COLUMNS = Array[String]( BaseColumns._ID, LABEL,
					    ADDRESS, ENABLED,
					    LONGITUDE, LATITUDE, VALID)
    val ALARM_ID_INDEX = 0
    val ALARM_LABEL_INDEX = 1
    val ALARM_ADDRESS_INDEX = 2
    val ALARM_ENABLED_INDEX = 3
    val ALARM_LONGITUDE_INDEX = 4
    val ALARM_LATITUDE_INDEX = 5
    val ALARM_VALID_INDEX = 6
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
  val valid = c.getInt(Columns.ALARM_VALID_INDEX) == 1
}
