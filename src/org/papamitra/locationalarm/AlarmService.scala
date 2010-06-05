
package org.papamitra.locationalarm

import android.app.Service
import android.content.{Context, Intent, IntentFilter, BroadcastReceiver}
import android.os.{Handler, IBinder, Bundle}
import android.location.{Location,LocationListener, LocationManager}
import android.util.Log

import Define._

class AlarmService extends Service with LocationListener{

  var mAlarms:Array[Alarm] = _

  val MIN_TIME:Int = 60 * 1000 // 1分
  val MIN_DISTANCE:Int = 0

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
    val locationManager = getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this)

    registerReceiver(alertReceiver, new IntentFilter(Alarms.ALERT_START))

    Log.i(TAG, "AlarmService.onCreate")
  }

  override def onStartCommand(intent:Intent, flags:Int, startId:Int):Int = {
    Log.i(TAG, "AlarmService.onStartCommand id = " + startId)

    mAlarms = Alarms.getAllAlarm(getContentResolver).toArray

    Alarms.disableAlert(this)

    if(mAlarms.filter(_.enabled).isEmpty){
      Log.i(TAG, "No Alarm Enabled")
      stopSelf()
      return Service.START_NOT_STICKY
    }

    if(mAlarms.filter(_.isActiveAt(System.currentTimeMillis)).isEmpty){
      Log.i(TAG, "No Alarm Enabled")
      val minNextMillis = mAlarms.map(Alarms.calculateNextMillis(_)).reduceLeft(Math.min(_,_))
      if (minNextMillis - System.currentTimeMillis < MIN_TIME * 2){
	return Service.START_STICKY
      }else{
	Alarms.enableAlert(this,minNextMillis)
	stopSelf()
	return Service.START_NOT_STICKY
      }
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

  def checkAlert(location:Location){
    for(alarm <- Alarms.getAllAlarm(getContentResolver)
			.filter(_.isActiveAt(System.currentTimeMillis))){
      val result = Array[Float](1)
      Location.distanceBetween(location.getLatitude, location.getLongitude,
			       alarm.latitude, alarm.longitude, result)
      Log.i(TAG,"Distance:" + result(0))
      if(result(0) < 500){
	Log.i(TAG, "!!!!! Location Alert !!!!!")
	// アラート発動
	sendBroadcast(new Intent(Alarms.ALERT_START))

	disabledAlarm(alarm)

      }
    }
  }
    
  def disabledAlarm(alarm:Alarm) {
    alarm.ttlenabled match {
      case true =>
	Alarms.updateAlarm(this, alarm, Alarms.calculateNextMillis(alarm))
      case false =>
	Alarms.enabledAlarm(this, alarm, false)
    }
  }

  override def onLocationChanged(location:Location){
    Log.i(TAG, format("onLocationChanged (%f, %f)", location.getLatitude, location.getLongitude))
    checkAlert(location)
    startService(new Intent(this, classOf[AlarmService]))
  }

  // TODO
  override def onProviderDisabled(provider:String){}
  override def onProviderEnabled(provider:String){}
  override def onStatusChanged(provider:String,status:Int,extras:Bundle){}
  
}
