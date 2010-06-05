package org.scalaandroid

object AndroidHelper {
  import android.view.View

  // findViewById(id).asInstanceOf[T] -> this.$[T](id)
  class DollarAssoc[A <: {def findViewById(id:Int):View}](x: A){
    def $[B <: View](id:Int): B = x.findViewById(id).asInstanceOf[B]
  }
  implicit def any2DollarAssoc[T <: {def findViewById(id:Int):View}](x: T): DollarAssoc[T] = new DollarAssoc(x)

  // OnClickListener helper
  import android.view.View.OnClickListener
  implicit def funcToClicker(f:View => Unit):OnClickListener = 
    new OnClickListener(){ def onClick(v:View)=f.apply(v)}
  implicit def funcToClicker0(f:() => Unit):OnClickListener = 
    new OnClickListener() { def onClick(v:View)=f.apply}

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

  // onActivityResult Helper
  trait ActivityResultTrait extends android.app.Activity {
    import android.content.Intent
    import scala.collection.mutable.HashSet

    protected val reactions = new HashSet[PartialFunction[(Int,Int,Intent), Unit]]()
    override def onActivityResult(requestCode:Int, retCode:Int, data:Intent){
      for(r <- reactions) if(r isDefinedAt (requestCode,retCode,data)) r(requestCode, retCode, data)
    }
  }

}
