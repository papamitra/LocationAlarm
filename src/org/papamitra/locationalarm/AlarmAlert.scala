
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
          stopService(new Intent(Alarms.ALARM_ALERT_ACTION))
	  AlarmAlert.this.finish	  
	}
      })
      .show()
  }

  override def onDestroy(){
    super.onDestroy
    unregisterReceiver(mReceiver)
  }

  
}
