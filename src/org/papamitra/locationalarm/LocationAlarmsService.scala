
package org.papamitra.locationalarm

import android.os.{Bundle, IBinder}
import android.app.Service
import android.location.{Location, LocationManager, LocationListener}
import android.content.{Intent, Context, ContentProvider}
import android.net.Uri
import android.util.Log

class LocationAlarmsService extends Service with LocationListener {
  import org.scalaandroid.AndroidHelper._

  private val TIMER_PERIOD = 60 * 1000
  private val TAG = "LocationAlarmsService"

  private lazy val locationManager = getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]

  override def onCreate(){
    Log.i(TAG, "Service Created")
    super.onCreate()

    // get setting from ContentProvider
    val r = getContentResolver()
    val projection = Array("_id", "latitude", "longitude")
    val cur = r.query(Uri.parse("content://org.papamitra.locationalarm/"),
		      projection,
		      null,
		      null,
		      null)
    
    for( c <- cur){
      Log.i(TAG, "latitude:" + c.getInt(1))
      Log.i(TAG, "longitude:" + c.getInt(2))
    }

    val minTime = 0
    val minDistance = 0
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this)
  }
  
  override def onLocationChanged(location: Location){
    Log.i(TAG, location.toString)

    // get setting from ContentProvider
    val r = getContentResolver()
    val projection = Array("_id", "latitude", "longitude")
    val cur = r.query(Uri.parse("content://org.papamitra.locationalarm/"),
		      projection,
		      null,
		      null,
		      null)
    
    for( c <- cur){
      val latitude = c.getInt(1) / 1e6
      val longitude = c.getInt(2) / 1e6
    }
  }

  override def onProviderDisabled(provider:String){
  }

  override def onProviderEnabled(provider:String){
  }

  override def onStatusChanged(provider:String, status:Int, extras:Bundle){
  }

  override def onDestroy(){
    Log.i(TAG, "call onDestroy")
    super.onDestroy
    locationManager.removeUpdates(this)
  }

  override def onBind(intent:Intent):IBinder = null
}
