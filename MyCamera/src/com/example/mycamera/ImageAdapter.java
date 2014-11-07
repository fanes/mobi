package com.example.mycamera;


import java.util.List;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
public class ImageAdapter extends BaseAdapter {
    int mGalleryItemBackground;
    private Context mContext;
    private List<String> lis;
    public ImageAdapter(Context c,List<String> li) 
    {
      mContext = c;
      lis=li;
      TypedArray a = mContext.obtainStyledAttributes(R.styleable.Gallery);
      mGalleryItemBackground = a.getResourceId(
          R.styleable.Gallery_android_galleryItemBackground, 0);
      a.recycle();
    }
    public int getCount() 
    {
      return lis.size();
    }
    public Object getItem(int position) 
    {
      return position;
    }
    public long getItemId(int position) 
    {
      return position;
    }
    public View getView(int position, View convertView, 
                          ViewGroup parent) 
    {
      ImageView i = new ImageView(mContext);
      Bitmap bm = BitmapFactory.decodeFile(lis.get(position).toString());
      i.setImageBitmap(bm);
      i.setScaleType(ImageView.ScaleType.CENTER_CROP);
      i.setLayoutParams(new GridView.LayoutParams(88, 88));
      i.setBackgroundResource(mGalleryItemBackground);
      i.setPadding(3,3,3,3);
      return i;
    } 
}