
package org.papamitra.locationalarm

import android.content.{Context, Intent, BroadcastReceiver}
import android.util.Log

class AlarmInitReceiver extends BroadcastReceiver {
  import Define._
  /**
  * Sets alarm on ACTION_BOOT_COMPLETED.  Resets alarm on
  * TIME_SET, TIMEZONE_CHANGED
     */
  override def onReceive(context:Context, intent:Intent) {
    val action = intent.getAction()
    //        if (Log.LOGV) Log.v("AlarmInitReceiver" + action);

    if (context.getContentResolver() == null) {
      Log.e(TAG, "AlarmInitReceiver: FAILURE unable to get content resolver.  Alarms inactive.")
      return
    }
    
    context.startService(new Intent(context, classOf[AlarmService]))
  }
}
