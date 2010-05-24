package org.papamitra.locationalarm;
import java.text.DecimalFormat;

import android.location.Location;
import android.location.Address;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class LocationHelper {
   public static final double MILLION = 1e6;
   public static final int ZOOM_MIN = 1;
   public static final int ZOOM_INIT = 16;
   public static final int ZOOM_MAX = 21;
   public static final int LOCATIONUPDATE_MINTIME = 60000; //更新する間隔 60秒
   public static final int LOCATIONUPDATE_MINDISTANCE = 1000; //更新する移動距離 1000m

   public static final String LOGTAG_CLASS = "LocationHelper";

   private static final DecimalFormat DEC_FMT = new DecimalFormat("###.##");

   private LocationHelper() {
   }

   public static final GeoPoint getGeoPoint(final Location location) {
      int iLatitude = (int) (location.getLatitude() * MILLION);
      int iLongitude = (int) (location.getLongitude() * MILLION);
      return new GeoPoint(iLatitude, iLongitude);
   }

   public static final GeoPoint getGeoPointLatLong(
                              final double latitude, final double longitude) {
      int iLatitude = (int) (latitude * MILLION);
      int iLongitude = (int) (longitude * MILLION);
      return new GeoPoint(iLatitude, iLongitude);
   }

   public static String parsePoint(final double point, final boolean siLat) {
      String result = DEC_FMT.format(point);
      if (result.indexOf("-") != -1) {
         result = result.substring(1, result.length());
      }

      return result;
   }

   public static double getGeocoderDouble(int latLog) {
      return latLog / MILLION;
   }

   //アドレスに対応する住所情報を取得
   public static String convertAddressName(Address address) {
      if (address == null) {
         return null;
      }

      StringBuilder sb = new StringBuilder();
      // getMaxAddressLineIndex()
      //  Returns the largest index currently in use to specify an address line.
      //   If no address lines are specified, -1 is returned
      // ※i=1の理由
      for (int i = 1; i < address.getMaxAddressLineIndex() + 1; i++) {
         String item = address.getAddressLine(i);
         if (item == null) {
            break;
         }
         Log.i(LOGTAG_CLASS, item);
         sb.append(item);
      }
      return sb.toString();
   }
}
