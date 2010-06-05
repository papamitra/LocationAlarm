
package org.papamitra.locationalarm

import android.content.{ContentProvider, ContentUris, ContentValues, Context, UriMatcher}
import android.database.{Cursor, SQLException}
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper, SQLiteQueryBuilder}
import android.net.Uri
import android.text.TextUtils
import android.util.Log

class LocationAlarmProvider extends ContentProvider{
  import Define._

  private val DATABASE_NAME = "alarms.db"
  private val DATABASE_VERSION = 5  

  private lazy val mOpenHelper = new DBHelper(getContext())

  class DBHelper(context:Context) extends SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    override def onCreate(db:SQLiteDatabase){
      Log.i(TAG, "DBHelper#onCreate")
      db.execSQL("CREATE TABLE alarms (" +
		 "_id INTEGER PRIMARY KEY," +
		 "label TEXT, " +
		 "address TEXT, " +
		 "enabled INTERGER, " +
		 "longitude REAL, " +
		 "latitude REAL, " +
		 "valid INTEGER, " +
	         "ttl INTEGER, " +
		 "shour INTEGER, " +
		 "sminute INTEGER, " +
		 "ehour INTEGER, " +
		 "eminute INTEGER, " +
		 "nextmillis INTEGER);" )

      val insertMe = "INSERT INTO alarms " +
      "(label, address, enabled, latitude, longitude, valid, ttl, shour, sminute, ehour, eminute, nextmillis) " +
      "VALUES "

      db.execSQL(insertMe + "('test1','test2',1, 35.0, 136.0, 1, 0, 0,0,0,0,0);")
    }

    override def onUpgrade(db:SQLiteDatabase, oldVersion:Int, currentVersion:Int){
      Log.i(TAG, "onUpgrade")
      db.execSQL("DROP TABLE IF EXISTS alarms")
      onCreate(db)
    }
  }

  override def onCreate() = true

  override def query(url:Uri, projectionIn:Array[String], selection:String, selectionArgs:Array[String], sort:String):Cursor ={
    val qb = new SQLiteQueryBuilder()
    qb.setTables("alarms")

    val db = mOpenHelper.getReadableDatabase

    val ret = qb.query(db, projectionIn, selection, selectionArgs, null, null, sort)
    
    ret match{
      case null =>
	Log.v(TAG, "Alarms.query: failed")
      case _ =>
	ret.setNotificationUri(getContext.getContentResolver, url)
    }

    return ret
  }

  override def getType(url:Uri):String = "vnd.android.cursor.dir/alarms"

  override def update(url:Uri, values:ContentValues, where:String, whereArgs:Array[String]):Int = {
    val db = mOpenHelper.getWritableDatabase

    val count = db.update("alarms", values, "_id= 1", null)

    Log.i(TAG, "update")
    getContext.getContentResolver.notifyChange(url,null)
    return count
  }

  // TODO
  override def insert(url:Uri, initialValues:ContentValues):Uri = url
  override def delete(url:Uri, where:String, whereArgs:Array[String]):Int = 0

}
