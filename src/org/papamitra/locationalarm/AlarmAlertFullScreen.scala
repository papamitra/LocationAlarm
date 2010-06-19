
package org.papamitra.locationalarm

import android.os.Bundle

class AlarmAlertFullScreen extends AlarmAlert{
  override def onCreate(icicle:Bundle){
    super.onCreate(icicle)
  }

  override def onBackPressed(){
    return
  }
}
