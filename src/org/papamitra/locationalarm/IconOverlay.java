package org.papamitra.locationalarm;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Bitmap;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class IconOverlay extends Overlay
{
	Bitmap mIcon;
	int mOffsetX;
	int mOffsetY;

	GeoPoint mPoint;

	IconOverlay(Bitmap icon, GeoPoint initial)
		{
			mIcon = icon;
			mOffsetX = 0 - icon.getWidth() / 2;
			mOffsetY = 0 - icon.getHeight() /2;
			mPoint = initial;
		}
	
	@Override
	public boolean onTap(GeoPoint point, MapView mapView)
		{
			mPoint = point;
			return super.onTap(point, mapView);
		}

	@Override
	public void draw(Canvas canvas, MapView mapView,
					 boolean shadow)
		{
			super.draw(canvas, mapView, shadow);
			if(!shadow){
				Projection projection = mapView.getProjection();
				Point point = new Point();
				projection.toPixels(mPoint, point);
				point.offset(mOffsetX, mOffsetY);
				canvas.drawBitmap(mIcon, point.x, point.y, null);
			}
			
		}
};
