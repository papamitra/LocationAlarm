
package org.papamitra.locationalarm

import android.app.Service
import android.content.{Context, Intent, IntentFilter, BroadcastReceiver}
import android.os.{Handler, IBinder, Bundle}
import android.location.{Location,LocationListener, LocationManager}
import android.util.Log

import Define._

class AlarmService extends Service with LocationListener{

  private lazy val alertReceiver = new BroadcastReceiver(){
    override def onReceive(context:Context, intent:Intent){
      val alarmAlert = new Intent(context, classOf[AlarmAlert])
      alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
			  | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
      context.startActivity(alarmAlert)
    }
  }

  override def onBind(intent:Intent):IBinder = null
  
  override def onCreate(){
    super.onCreate()
    val minTime = 0
    val minDistance = 0
    val locationManager = getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this)

    registerReceiver(alertReceiver, new IntentFilter(Alarms.ALERT_START))

    Log.i(TAG, "AlarmService.onCreate")
  }

  override def onStartCommand(intent:Intent, flags:Int, startId:Int):Int = {
    Log.i(TAG, "AlarmService.onStartCommand id = " + startId)

    if (Alarms.getAllAlarm(getContentResolver).filter(_.enabled).isEmpty){
      stopSelf()
      return Service.START_NOT_STICKY
    }
      
    return Service.START_STICKY
  }

  override def onDestroy(){
    Log.i(TAG, "Service Stop")

    val locationManager = getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]
    locationManager.removeUpdates(this)

    unregisterReceiver(alertReceiver)

    super.onDestroy()
  }

  override def onLocationChanged(location:Location){
    for(alarm <- Alarms.getAllAlarm(getContentResolver).filter(_.enabled)){
      val result = Array[Float](1)
      Location.distanceBetween(location.getLatitude, location.getLongitude,
			       alarm.latitude, alarm.longitude, result)
      Log.i(TAG,"Distance:" + result(0))
      if(result(0) < 500){
	Log.i(TAG, "!!!!! Location Alert !!!!!")
	// アラート発動
	sendBroadcast(new Intent(Alarms.ALERT_START))

	// TODO:アラーム無効
	Alarms.enabledAlarm(this, alarm, false)
      }
    }

    if (Alarms.getAllAlarm(getContentResolver).filter(_.enabled).isEmpty){
      stopSelf()
    }
  }

  // TODO
  override def onProviderDisabled(provider:String){}
  override def onProviderEnabled(provider:String){}
  override def onStatusChanged(provider:String,status:Int,extras:Bundle){}
  
}

