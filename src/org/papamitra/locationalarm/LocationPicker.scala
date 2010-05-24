package org.papamitra.locationalarm

import android.app.Activity
import android.content.Intent
import android.graphics.{Bitmap, BitmapFactory, Canvas, Point}
import android.os.Bundle
import android.view.{Gravity, ViewGroup}
import android.widget.{ZoomControls, LinearLayout}

import com.google.android.maps.{GeoPoint, MapActivity, MapController, MapView, Overlay, Projection}

import android.graphics.drawable.Drawable
import android.location.{Location, Geocoder}
import android.widget.{Toast,Button}

import android.util.Log
import com.google.android.maps.OverlayItem

import java.util.ArrayList
import java.util.Locale

import org.maidroid.scalamap.SItemizedOverlay
import SItemizedOverlay.BOUND_CENTER_BOTTOM

object LocationPicker{
  val LATITUDE = "LATITUDE"
  val LONGITUDE = "LONGITUDE"
  val ADDRESS = "ADDRESS"

  val REQUEST_LOCATION = 0
}

class LocationPicker extends MapActivity{
  import org.scalaandroid.AndroidHelper._
  import org.maidroid.scalamap._
  import android.app.Activity._
  import LocationPicker._

  val TAG = "LocationPicker"
  val INITIAL_ZOOM_LEVEL = 15
  val INITIAL_LATITUDE = 35455281
  val INITIAL_LONGITUDE = 139629711

  private lazy val smapView = new SMapView(findViewById(R.id.mapview))
  private lazy val smapCtrl = smapView.getSMapController
  private lazy val smapOverlays = smapView.getOverlays
  private lazy val drawable    = getResources.
				getDrawable (R.drawable.androidmarker)
  private lazy val pickerOverlay = new PickerOverlay (drawable)
  private lazy val geocoder = new Geocoder(this, Locale.JAPAN)

  private var mPoint:GeoPoint = _
  private var mAddressName = ""

  override def onCreate(saveInstanceState: Bundle){
    super.onCreate(saveInstanceState)

    val intent = getIntent
    mPoint = new GeoPoint( (intent.getDoubleExtra(Alarms.ALARM_INTENT_LATITUDE, INITIAL_LATITUDE) * 1e6).toInt,
			   (intent.getDoubleExtra(Alarms.ALARM_INTENT_LONGITUDE, INITIAL_LONGITUDE) * 1e6).toInt )

    Log.i(TAG, "LocationPicker.onCreate:" + mPoint.toString)

    setContentView(R.layout.map)

    smapView.setBuiltInZoomControls (true)

    smapCtrl.animateTo(mPoint)
    smapCtrl.setZoom(INITIAL_ZOOM_LEVEL)

    setOverlay(mPoint)
    smapOverlays.add(new TapOverlay(){
      @Override def onTapImpl(point: GeoPoint){
	mAddressName = point.toString
	mPoint = point
	setOverlay(mPoint)
	smapCtrl.animateTo(mPoint)
	try{
	  val list = geocoder.getFromLocation(
            LocationHelper.getGeocoderDouble(point.getLatitudeE6()),
            LocationHelper.getGeocoderDouble(point.getLongitudeE6()),
            1)
          val address = list.get(0)
          mAddressName = LocationHelper.convertAddressName(address)
	  Toast.makeText(getApplicationContext(), mAddressName, Toast.LENGTH_LONG).show()
	}catch{
	  case e =>
	    Log.i(TAG, "getFronLocation Failed:" + e.toString)
	    mAddressName = point.toString
	}
      }
    })

    this.$[Button](R.id.map_ok).setOnClickListener( () => {
      setResult(RESULT_OK, (new Intent())
		.putExtra(LATITUDE, mPoint.getLatitudeE6)
		.putExtra(LONGITUDE, mPoint.getLongitudeE6)
		.putExtra(ADDRESS, mAddressName))
      finish
    })

    this.$[Button](R.id.map_cancel).setOnClickListener( () => {
      setResult(RESULT_CANCELED)
      finish
    })
  }

  protected override def isRouteDisplayed(): Boolean = false

  private def setOverlay(point: GeoPoint){    
    val overlayitem1 = new OverlayItem (point, "", "")
    pickerOverlay.addOverlay (overlayitem1)
    smapOverlays.add (pickerOverlay)
  }
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
