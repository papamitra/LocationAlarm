package org.papamitra.locationalarm

import android.app.Activity
import android.content.{Intent,Context}
import android.graphics.{Bitmap, BitmapFactory, Canvas, Point}
import android.os.Bundle
import android.view.{View, Gravity, ViewGroup}
import android.widget.{ZoomControls, LinearLayout}

import com.google.android.maps.{GeoPoint, MapActivity, MapController, MapView, Overlay, Projection}

import android.graphics.drawable.Drawable
import android.location.{Location,LocationManager}
import android.widget.{Toast,Button}

import android.util.Log
import com.google.android.maps.OverlayItem

import java.util.ArrayList

import org.maidroid.scalamap.SItemizedOverlay
import SItemizedOverlay.BOUND_CENTER_BOTTOM

object LocationPicker{
  val LATITUDE = "LATITUDE"
  val LONGITUDE = "LONGITUDE"
  val ADDRESS = "ADDRESS"

  val REQUEST_LOCATION = 0
}

class LocationPicker extends MapActivity{
  import org.maidroid.scalamap._
  import android.app.Activity._
  import LocationPicker._
  import org.scalaandroid.AndroidHelper._

  val TAG = "LocationPicker"
  val INITIAL_ZOOM_LEVEL = 15
  val INITIAL_LATITUDE = 35455281
  val INITIAL_LONGITUDE = 139629711

  private lazy val drawable    = getResources.
				getDrawable (R.drawable.androidmarker)
  private lazy val pickerOverlay = new PickerOverlay (drawable)

  private var mPoint:GeoPoint = new GeoPoint((INITIAL_LATITUDE * 1e6).toInt, (INITIAL_LONGITUDE * 1e6).toInt)

  def getPoint(intent:Intent) = 
    if(intent.getBooleanExtra(Alarms.ALARM_INTENT_INITIALIZED, true)){
      try{
	val locationManager = getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]
	val loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
	new GeoPoint((loc.getLatitude * 1e6).toInt, (loc.getLongitude * 1e6).toInt)
      }catch{
	case e =>
	  Log.w(TAG, "Failed to get Last Location:" + e.toString)
	  mPoint
      }
    }else{
      new GeoPoint( (intent.getDoubleExtra(Alarms.ALARM_INTENT_LATITUDE, INITIAL_LATITUDE) * 1e6).toInt,
		   (intent.getDoubleExtra(Alarms.ALARM_INTENT_LONGITUDE, INITIAL_LONGITUDE) * 1e6).toInt )
  }

  override def onCreate(saveInstanceState: Bundle){
    super.onCreate(saveInstanceState)

    mPoint = getPoint(getIntent)

    setContentView(R.layout.map)

    val smapView = new SMapView(findViewById(R.id.mapview)) withAction( 
      _ setBuiltInZoomControls true)
    val smapCtrl = smapView.getSMapController withActions(
      _ animateTo mPoint,
      _ setZoom INITIAL_ZOOM_LEVEL)

    val smapOverlays = smapView.getOverlays

    def setOverlay(point: GeoPoint){    
      val overlayitem1 = new OverlayItem (point, "", "")
      pickerOverlay.addOverlay (overlayitem1)
      smapOverlays.add (pickerOverlay)
    }

    setOverlay(mPoint)

    smapOverlays.add(new TapOverlay(){
      @Override def onTapImpl(point: GeoPoint){
	mPoint = point
	setOverlay(mPoint)
	smapCtrl.animateTo(mPoint)
      }
    })

    this.%[Button](R.id.map_ok).setOnClickListener( () => {
      setResult(RESULT_OK, (new Intent())
		.putExtra(LATITUDE, mPoint.getLatitudeE6)
		.putExtra(LONGITUDE, mPoint.getLongitudeE6))
      finish
    })

    this.%[Button](R.id.map_cancel).setOnClickListener(() => {
      setResult(RESULT_CANCELED)
      finish
    })

  }

  protected override def isRouteDisplayed(): Boolean = false

}

class PickerOverlay (drawable: Drawable) extends 
      SItemizedOverlay[OverlayItem] (drawable, BOUND_CENTER_BOTTOM)
{
    private lazy val mOverlays = new ArrayList[OverlayItem]

    override def createItem (i: Int) = mOverlays.get(i)
    override def size () = mOverlays.size() 

    def addOverlay (overlay: OverlayItem){
      mOverlays.clear
      mOverlays.add (overlay)
      populate ()
    }
}
