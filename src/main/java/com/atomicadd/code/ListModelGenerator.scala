package com.atomicadd.code

import com.atomica.templ.parse.Striper
import com.atomica.templ.parse.FreeState
import com.atomicadd.templ.Context
import com.atomicadd.templ.Template
import com.atomicadd.templ.ValueString
import com.atomicadd.templ.ValueList
import com.atomicadd.templ.ValueList
import com.atomicadd.templ.ValueString
import java.util.Date
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object ListModelGenerator {

  val TEMPLATE_ADAPTER = """package $(package).$(adapterPackage);

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import $(package).R;
import $(package).model.$(itemClass);

import java.util.List;

/**
 * ListAdapter of $(itemClass) 
 * Created by liuwei on $(date).
 */
public class $(itemClass)ListAdapter extends ArrayAdapter<$(itemClass)> {

    private static class ViewHodler {
        $(field <- fields)
        TextView $(field);$(end)        
    }

    public $(itemClass)ListAdapter(Context context, List<$(itemClass)> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.$(itemLayout), null);
            
            ViewHodler vh = new ViewHodler();
            
            $(field <- fields)
            vh.$(field) = (TextView) v.findViewById(R.id.$(field));$(end)

            v.setTag(vh);
        }

        ViewHodler vh = (ViewHodler) v.getTag();

        $(itemClass) item = getItem(position);

        $(field <- fields)
        vh.$(field).setText(item.$(field));$(end)

        return v;
    }
}"""

  val TEMPLATE_MODEL = """package $(package).$(modelPackage);

/**
 * Created by liuwei on $(date)
 */
public class $(itemClass) {

    $(field <- fields)
    public String $(field);$(end)
    
}"""

  val TEMPLATE_LAYOUT = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:orientation="vertical"
    android:layout_height="match_parent">

    $(field <- fields)
    <TextView
        android:id="@+id/$(field)"
        android:layout_margin="10dp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/placeholder" />
    $(end)
</LinearLayout>"""

  def generateListModel(packageName: String,
                        modelPackage: String,
                        adapterPackage: String,
                        itemClass: String,
                        itemLayout: String,
                        fields: Seq[String],
                        src: File): Unit = {
    val context = new Context()
    context("package") = ValueString(packageName)
    context("modelPackage") = ValueString(modelPackage)
    context("adapterPackage") = ValueString(adapterPackage)
    context("itemClass") = ValueString(itemClass)
    context("itemLayout") = ValueString(itemLayout)
    context("fields") = ValueList(fields.map(ValueString(_)))

    // put a timestamp
    context("date") = ValueString(new Date().toString())

    def buildSourceDir(p: String, s: String, klass: String) = new File(src, "java/" + (p + "." + s).replace(".", "/") + "/" + klass + ".java")

    val pairs = List(
      (TEMPLATE_ADAPTER, buildSourceDir(packageName, adapterPackage, itemClass + "ListAdapter")),
      (TEMPLATE_MODEL, buildSourceDir(packageName, modelPackage, itemClass)),
      (TEMPLATE_LAYOUT, new File(src, "res/layout/" + itemLayout + ".xml")))

    for (p <- pairs) {
      p match {
        case (t, f) => {
          val template = Striper.strip(t)
          val content = template.build(context)

          val folder = f.getParentFile()
          if (!folder.exists()) folder.mkdirs()

          // write
          Files.write(Paths.get(f.getAbsolutePath), content.getBytes("utf8"))
        }
      }
    }
  }

}