
package org.papamitra.locationalarm

import android.app.Activity
import android.app.AlertDialog.Builder
import android.os.Bundle
import android.util.Log
import android.view.{Window,WindowManager,View}
import android.content.{Context, Intent}
import android.util.Log

import android.content.res.{Resources,AssetFileDescriptor}

// for AlertDialog
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnErrorListener
import android.media.RingtoneManager

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager

import android.widget.Button

object AlarmAlert{
  val IN_CALL_VOLUME = 0.125f
}

class AlarmAlert extends Activity{
  import AlarmAlert._
  import org.scalaandroid.AndroidHelper._

  val TAG = "AlarmAlert"
  private var mMediaPlayer = new MediaPlayer()

  private val mPhoneStateListener = new PhoneStateListener() {
    override def onCallStateChanged(state:Int, ignored:String) {
      // The user might already be in a call when the alarm fires. When
      // we register onCallStateChanged, we get the initial in-call state
      // which kills the alarm. Check against the initial call state so
      // we don't kill the alarm during a call.
      if (state != TelephonyManager.CALL_STATE_IDLE
          && state != mInitialCallState) {
       // sendKillBroadcast(mCurrentAlarm);
       //  stopSelf();
	stop()
	finish() // TODO
      }
    }
  }

  private lazy val mTelephonyManager = getSystemService(Context.TELEPHONY_SERVICE).asInstanceOf[TelephonyManager]

  private var mInitialCallState:Int = _

  override protected def onCreate(icicle:Bundle) {
    super.onCreate(icicle);

    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    mInitialCallState = mTelephonyManager.getCallState()

//    mAlarm = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA)

    val self = this
    new AlertDialog.Builder(this)
      .setTitle("Location Alert")
      .setMessage("")
      .setPositiveButton("OK", new DialogInterface.OnClickListener(){
	override def onClick(dialog: DialogInterface, whichButton:Int){
	  self.stop
	  self.finish
	}
      })
      .show()

    play()
  }

  def play(){
    Log.i(TAG, "play alert")

    val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    mMediaPlayer.setOnErrorListener(new OnErrorListener() {
      override def onError(mp:MediaPlayer, what:Int,extra:Int):Boolean = {
        Log.e(TAG, "Error occurred while playing audio.")
        mp.stop()
        mp.release()
        mMediaPlayer = null
        return true
      }
    })

    try {
      // Check if we are in a call. If we are, use the in-call alarm
      // resource at a low volume to not disrupt the call.
/*
      if (mTelephonyManager.getCallState()
          != TelephonyManager.CALL_STATE_IDLE) {
//        Log.v("Using the in-call alarm");
        mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
        setDataSourceFromResource(getResources(), mMediaPlayer,
				  R.raw.in_call_alarm);
      } else {
        mMediaPlayer.setDataSource(this, alert);
      }
*/
      mMediaPlayer.setDataSource(this, alert)
      startAlarm(mMediaPlayer)
    } catch {
      case ex:Exception =>
	Log.i(TAG,"alarm error")
//	Log.v("Using the fallback ringtone");
	// The alert may be on the sd card which could be busy right
	// now. Use the fallback ringtone.

	try {
          // Must reset the media player to clear the error state.
          mMediaPlayer.reset();
          setDataSourceFromResource(getResources(), mMediaPlayer, R.raw.fallbackring)
          startAlarm(mMediaPlayer)
	} catch {
	  case ex2:Exception =>
            // At this point we just don't play anything.
            Log.e("Failed to play fallback ringtone", ex2.toString)
	}

    }
  }

  def stop() {
    Log.v(TAG, "AlarmAlert stop");

    // Stop audio playing
    if (mMediaPlayer != null) {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    // Stop vibrator
    //mVibrator.cancel();
  
    //disableKiller();
  }

  private def setDataSourceFromResource(resources:Resources, player:MediaPlayer, res:Int) {
    val afd:AssetFileDescriptor = resources.openRawResourceFd(res)
    if (afd != null) {
      player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
			   afd.getLength());
      afd.close()
    }
  }

  private def startAlarm(player:MediaPlayer){
    val audioManager = getSystemService(Context.AUDIO_SERVICE).asInstanceOf[AudioManager]
    // do not play alarms if stream volume is 0
    // (typically because ringer mode is silent).
    if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
      player.setAudioStreamType(AudioManager.STREAM_ALARM)
      player.setLooping(true)
      player.prepare()
      player.start()
    }
  }

}
