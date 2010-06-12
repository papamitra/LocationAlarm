
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
import android.view.KeyEvent

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

    registerReceiver(mReceiver, new IntentFilter(Alarms.ALARM_KILLED))

    new AlertDialog.Builder(this)
      .setTitle("Location Alert")
      .setMessage("")
      .setPositiveButton("OK", new DialogInterface.OnClickListener(){
	override def onClick(dialog: DialogInterface, whichButton:Int){
	  dismiss
	}
      })
      .show()
  }

  override def onStop(){
    super.onStop
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
