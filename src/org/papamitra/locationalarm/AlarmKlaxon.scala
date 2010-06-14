
package org.papamitra.locationalarm

import android.app.Service
import android.content.{Context, Intent, BroadcastReceiver}
import android.content.res.{Resources,AssetFileDescriptor}
import android.os.{Message, Handler, Bundle, IBinder}
import android.view.{Window,WindowManager,View}

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnErrorListener
import android.media.RingtoneManager
import android.os.Vibrator

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager

import android.util.Log


object AlarmKlaxon{
  private val IN_CALL_VOLUME = 0.125f

  private val sVibratePattern = Array[Long](500, 500)

  private val ALARM_TIMEOUT_SECONDS = 1 * 60 // 1min
  private val KILLER = 1000
}

class AlarmKlaxon extends Service{
  import AlarmKlaxon._
  import Define._

  private var mMediaPlayer:MediaPlayer = _

  private var mPlaying = false

  private lazy val mPhoneStateListener = new PhoneStateListener() {
    override def onCallStateChanged(state:Int, ignored:String) {
      // The user might already be in a call when the alarm fires. When
      // we register onCallStateChanged, we get the initial in-call state
      // which kills the alarm. Check against the initial call state so
      // we don't kill the alarm during a call.
      if (state != TelephonyManager.CALL_STATE_IDLE
          && state != mInitialCallState) {
	sendKillBroadcast
	stopSelf
      }
    }
  }

  private lazy val mTelephonyManager = getSystemService(Context.TELEPHONY_SERVICE).asInstanceOf[TelephonyManager]

  private var mInitialCallState:Int = _

  private val mHandler = new Handler(){
    override def handleMessage(msg:Message) = msg.what match{
      case KILLER =>
	Log.v(TAG, "*********** Alarm killer triggered ***********")
	sendKillBroadcast
	stopSelf
      case _ =>
    }
  }

  override def onCreate(){
    mPlaying = false
    mMediaPlayer = new MediaPlayer()

    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    mInitialCallState = mTelephonyManager.getCallState()

    AlarmAlertWakeLock.acquireCpuWakeLock(this)
  }

  override def onDestroy(){
    stop
    mTelephonyManager.listen(mPhoneStateListener, 0)
    AlarmAlertWakeLock.releaseCpuLock()
  }

  override def onBind(intent:Intent):IBinder = null

  override def onStartCommand(intent:Intent, flags:Int, startId:Int):Int ={
    play
    return Service.START_STICKY
  }

  def play(){
    Log.i(TAG, "play alert")
    stop
/*
//    val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
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
      // Must reset the media player to clear the error state.
      mMediaPlayer.reset();
      setDataSourceFromResource(getResources(), mMediaPlayer, R.raw.fallbackring)
      startAlarm(mMediaPlayer)
    } catch {
      case ex2:Exception =>
        // At this point we just don't play anything.
        Log.e("Failed to play fallback ringtone", ex2.toString)
    }
*/
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE).asInstanceOf[Vibrator]
    vibrator.vibrate(sVibratePattern,0)

    enableKiller
    mPlaying = true
  }

  def stop() {
    Log.v(TAG, "AlarmAlert stop");

    // Stop audio playing
    if(mPlaying){
      mPlaying = false
/*
      if (mMediaPlayer != null) {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
      }
*/
      // Stop vibrator
      val vibrator = getSystemService(Context.VIBRATOR_SERVICE).asInstanceOf[Vibrator]
      vibrator.cancel();
    }
  
    disableKiller();
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

  private def sendKillBroadcast(){
    sendBroadcast(new Intent(Alarms.ALARM_KILLED))
  }
    
  private def enableKiller(){
    mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER, null),
				1000 * ALARM_TIMEOUT_SECONDS)
  }

  private def disableKiller(){
    mHandler.removeMessages(KILLER)
  }
}
