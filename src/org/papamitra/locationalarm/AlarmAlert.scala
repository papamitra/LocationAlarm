
package org.papamitra.locationalarm

import android.content.{Context, Intent, IntentFilter, BroadcastReceiver}
import android.app.Activity
import android.app.AlertDialog.Builder
import android.os.Bundle

import android.util.Log

// for AlertDialog
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener

import android.widget.Button
import android.view.{WindowManager,KeyEvent}

class AlarmAlert extends Activity{
  import org.scalaandroid.AndroidHelper._

  val TAG = "AlarmAlert"

  private val mReceiver = new BroadcastReceiver(){
    override def onReceive(context:Context, intent:Intent){
      AlarmAlert.this.finish
    }
  }

  override protected def onCreate(icicle:Bundle) {
    super.onCreate(icicle);

    requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
			 | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
			 | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
			 | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

    setContentView(R.layout.alarm_alert)

    this.%[Button](R.id.dismiss).setOnClickListener(() => dismiss)

    registerReceiver(mReceiver, new IntentFilter(Alarms.ALARM_KILLED))
  }

  override def onStop(){
    super.onStop
    dismiss // TODO
    finish
  }

  override def onDestroy(){
    super.onDestroy
    unregisterReceiver(mReceiver)
  }

  override def dispatchKeyEvent(event:KeyEvent):Boolean = {
    val up = (event.getAction == KeyEvent.ACTION_UP)
    if(Array(KeyEvent.KEYCODE_VOLUME_UP,
	     KeyEvent.KEYCODE_VOLUME_DOWN,
	     KeyEvent.KEYCODE_CAMERA,
	     KeyEvent.KEYCODE_FOCUS).exists(_ == event.getKeyCode)){
      if(up)dismiss
    }

    super.dispatchKeyEvent(event)
  }

  def dismiss(){
    stopService(new Intent(Alarms.ALARM_ALERT_ACTION))
    finish
  }
    
}
