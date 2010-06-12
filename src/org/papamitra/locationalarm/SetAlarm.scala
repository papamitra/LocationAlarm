package org.papamitra.locationalarm

import android.content.Intent
import android.os.Bundle
import android.preference.{Preference, PreferenceActivity, EditTextPreference, CheckBoxPreference, PreferenceScreen}
import android.preference.Preference.OnPreferenceClickListener
import android.widget.Toast
import android.util.Log

import android.view.{View, LayoutInflater}
import android.view.ViewGroup.LayoutParams
import android.widget.{FrameLayout,LinearLayout,Button}

import org.scalaandroid.AndroidHelper.ActivityResultTrait
import android.app.TimePickerDialog
import android.widget.TimePicker

class SetAlarm extends PreferenceActivity with ActivityResultTrait{

  import org.scalaandroid.AndroidHelper._
  import LocationPicker._
  import android.app.Activity._
  import Define._

  private var mLatitude:Double = _
  private var mLongitude:Double = _
  private var mAddress:String = _
  private var mInitialized:Boolean = _
  
  private var mStartHour:Int = _
  private var mStartMinute:Int = _
  private var mEndHour:Int = _
  private var mEndMinute:Int = _

  private lazy val mLabel = findPreference("label").asInstanceOf[EditTextPreference]
  private lazy val mStartTime = findPreference("start_time")
  private lazy val mEndTime = findPreference("end_time")
  private lazy val mLocation = findPreference("location")
  private lazy val mTTL = findPreference("ttl").asInstanceOf[CheckBoxPreference]

  reactions += {
    case (REQUEST_LOCATION,RESULT_OK,data) =>
      setLocationPref(
	latitude = data.getIntExtra(LATITUDE, 0) / 1e6,
	longitude = data.getIntExtra(LONGITUDE, 0) / 1e6,
	address = data.getStringExtra(ADDRESS))
  }

  def setLocationPref(alarm:Alarm):Unit =
    setLocationPref(
      latitude = alarm.latitude,
      longitude = alarm.longitude,
      address = alarm.address)

  def setLocationPref(address:String, latitude:Double, longitude:Double){
    mLatitude = latitude
    mLongitude = longitude
    mAddress = address
    mLocation.setSummary(mAddress)
  }

  override def onCreate(saveInstanceState: Bundle){
    super.onCreate(saveInstanceState)

    Log.i(TAG, "SetAlarm.onCreate")

    addPreferencesFromResource(R.xml.preference)

    val id:Int = getIntent().getLongExtra(LocationAlarms.ALARM_ID,-1).asInstanceOf[Int]
    Log.i(TAG, "ALARM_ID:" + id.toString)

    // OK/Cancelボタンの配置
    layoutOkCancel(id)

    mLabel.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
      def onPreferenceChange(p:Preference, newValue:Any):Boolean={
	mLabel.setSummary(newValue.asInstanceOf[String])
	return true
      }
    })

    Alarms.getAlarm(getContentResolver, id) match {
      case Some(alarm) => 
	setLocationPref(alarm)
      
	mLabel withActions(
	  _ setText alarm.label,
	  _ setSummary alarm.label)

	mTTL.setChecked(alarm.ttlenabled)
	mStartHour = alarm.ttl.shour
	mStartMinute = alarm.ttl.sminute
	mStartTime.setSummary(format("%d:%02d", mStartHour, mStartMinute))

	mEndHour = alarm.ttl.ehour
	mEndMinute = alarm.ttl.eminute
	mEndTime.setSummary(format("%d:%02d", mEndHour, mEndMinute))

	mInitialized = alarm.initialized
      case _ => 
	Log.e(TAG, "Failed to get Alarm id:" + id.toString)
	finish
    }

    // PreferenceのLocationをクリックした時にMapを起動
    mLocation.setOnPreferenceClickListener(
      new OnPreferenceClickListener(){
	override def onPreferenceClick(pref: Preference):Boolean={
	  val mapIntent = new Intent(SetAlarm.this, classOf[LocationPicker]) withActions(
	    _ putExtra(Alarms.ALARM_INTENT_INITIALIZED, mInitialized),
	    _ putExtra(Alarms.ALARM_INTENT_LATITUDE, mLatitude),
	    _ putExtra(Alarms.ALARM_INTENT_LONGITUDE, mLongitude))

	  startActivityForResult(mapIntent, REQUEST_LOCATION)
	  return true // todo
	}
      })
  }
  
  private val DIALOG_START_TIME = 0
  private val DIALOG_END_TIME = 1

  class TimeSetListener(val id:Int) extends TimePickerDialog.OnTimeSetListener{
    override def onTimeSet(view:TimePicker,hourOfDay:Int,minute:Int) {
      SetAlarm.this.setTime(id, hourOfDay, minute)
    }
  }

  override def onCreateDialog(id:Int) = id match{
    case DIALOG_START_TIME =>
      new TimePickerDialog(SetAlarm.this,
			   new TimeSetListener(id),
			   mStartHour, mStartMinute,true)
    case DIALOG_END_TIME =>
      new TimePickerDialog(SetAlarm.this,
			   new TimeSetListener(id),
			   mEndHour, mEndMinute,true)
  }

  def setTime(id:Int, hourOfDay:Int, minute:Int){
    id match {
      case DIALOG_START_TIME =>
	mStartHour = hourOfDay
	mStartMinute = minute
	mStartTime.setSummary(format("%d:%02d", hourOfDay,minute))
      case DIALOG_END_TIME =>
	mEndHour = hourOfDay
	mEndMinute = minute
	mEndTime.setSummary(format("%d:%02d", hourOfDay,minute))
    }
  }

  override def onPreferenceTreeClick(preferenceScreen:PreferenceScreen, preference:Preference):Boolean = preference match {
    case pref if pref == mStartTime =>
      showDialog(DIALOG_START_TIME)
      return true
    case pref if pref == mEndTime =>
      showDialog(DIALOG_END_TIME)
      return true
    case _ =>
      Log.i(TAG, "unknown pref")
      return true
    }

  def layoutOkCancel(id:Int){
    // We have to do this to get the save/cancel buttons to highlight on
    // their own.
    getListView().setItemsCanFocus(true)

    // Grab the content view so we can modify it.
    val content = getWindow().getDecorView().%[FrameLayout](android.R.id.content)

    // Get the main ListView and remove it from the content view.
    val lv = getListView()
    content.removeView(lv)

    // Create the new LinearLayout that will become the content view and
    // make it vertical.
    val ll = new LinearLayout(this)
    ll.setOrientation(LinearLayout.VERTICAL)

    // Have the ListView expand to fill the screen minus the save/cancel
    // buttons.
    val lp = new LinearLayout.LayoutParams(
      LayoutParams.FILL_PARENT,
      LayoutParams.WRAP_CONTENT)
    lp.weight = 1
    ll.addView(lv, lp)

    // Inflate the buttons onto the LinearLayout.
    val v = LayoutInflater.from(this).inflate(R.layout.save_cancel_alarm, ll)

    v.%[Button](R.id.alarm_save).setOnClickListener(() => {
      saveAlarm(id)
      startService(new Intent(SetAlarm.this, classOf[AlarmService]))
      finish
    })

    v.%[Button](R.id.alarm_cancel).setOnClickListener(() => {
      finish
    })

    // Replace the old content view with our new one.
    setContentView(ll)

  }

  def saveAlarm(id:Int){
    // TODO: AlarmID
    Alarms.setAlarm(this, id,
		    enabled = true,
		    label = mLabel.getText(),
		    address = mAddress,
		    latitude = mLatitude,
		    longitude = mLongitude,
		    ttlenabled = mTTL.isChecked,
		    ttl = TTL(mStartHour,mStartMinute,mEndHour,mEndMinute),
		    nextmillis=0)
  }

}
