
package org.papamitra.locationalarm

import android.content.{Context, Intent, BroadcastReceiver}

class AlarmReceiver extends BroadcastReceiver{
  override def onReceive(context:Context, intent:Intent){
    intent.getAction match {
      case Alarms.ALARM_ACTIVE =>
	context.startService(new Intent(context, classOf[AlarmService]))
    }
  }
}
