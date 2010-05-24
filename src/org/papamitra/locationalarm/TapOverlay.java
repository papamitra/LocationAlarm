package org.papamitra.locationalarm;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public abstract class TapOverlay extends Overlay
{
	TapOverlay(){}
	
	@Override
	public boolean onTap(GeoPoint point, MapView mapView)
		{
			onTapImpl(point);
			return super.onTap(point, mapView);
		}
	
	abstract public void onTapImpl(GeoPoint point);

};
