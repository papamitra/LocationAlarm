package org.scalaandroid

import android.util.Log

object AndroidHelper {
  import android.view.View
  import android.app.Activity
  val TAG1 = "AndroidHelper"

  // findViewById(id).asInstanceOf[T] -> this.%[T](id)
  class PercentAssocActivity[A <: Activity](x: A){
    def %[B <: View](id:Int): B = x.findViewById(id).asInstanceOf[B]
  }

  implicit def anyPercentAssocActivity[T <: Activity](x: T)= new PercentAssocActivity(x)

  class PercentAssocView[A <: View](x: A){
    def %[B <: View](id:Int): B = x.findViewById(id).asInstanceOf[B]
  }

  implicit def any2PercentAssocView[T <: View](x: T)= new PercentAssocView(x)


  // OnClickListener helper
  import android.view.View.OnClickListener
  implicit def funcToClicker(f:View => Unit):OnClickListener = 
    new OnClickListener(){ override def onClick(v:View):Unit=f.apply(v)}
  implicit def funcToClicker0(f:() => Unit):OnClickListener = 
    new OnClickListener() { override def onClick(v:View):Unit=f.apply}

  import android.preference.Preference
  import android.preference.Preference.OnPreferenceChangeListener
  implicit def funcToPrefChanger(f:(Preference, Any) => Boolean):OnPreferenceChangeListener =
    new OnPreferenceChangeListener(){ override def onPreferenceChange(p:Preference, newValue:Any):Boolean=f.apply(p,newValue)}

  // Cursor helper
  import android.database.Cursor

  private object NilCurIter extends Iterator[Cursor]{
    def hasNext = false
    def next:Cursor = throw new java.util.NoSuchElementException()
  }

  private class CursorIter(cur: Cursor) extends Iterator[Cursor]{
    def hasNext = !cur.isLast()
    def next:Cursor =
      if (cur.moveToNext) cur
      else
	throw new java.util.NoSuchElementException()
  }
    
  implicit def cursor2Iterable(cur: Cursor): Iterator[Cursor] = 
    if(!cur.moveToFirst) NilCurIter
    else{
      cur.moveToPrevious
      new CursorIter(cur)
    }

  def using[A <: {def close(): Unit}, B](param:A)(f: A=>B):B=
    try{
      f(param)
    }finally{
      param.close()
    }

  // onActivityResult Helper
  trait ActivityResultTrait extends android.app.Activity {
    import android.content.Intent
    import scala.collection.mutable.HashSet

    protected val reactions = new HashSet[PartialFunction[(Int,Int,Intent), Unit]]()
    override def onActivityResult(requestCode:Int, retCode:Int, data:Intent){
      for(r <- reactions) if(r isDefinedAt (requestCode,retCode,data)) r(requestCode, retCode, data)
    }
  }

  // CascadingActions from http://gist.github.com/434620
  implicit def tToActioneerT[T](t: T) = Actioneer(t)
    
  case class Actioneer[T](tee: T) {
    def withAction(action: (T => Unit)): T = 
      withActions(action)
    def withActions(actions: (T => Unit)*): T = {
      actions foreach (_ (tee))
      tee
    }
  }

  implicit def funcToRunnable(f:() => Unit):Runnable = 
    new Runnable { override def run():Unit=f.apply}
}
