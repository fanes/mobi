package com.example.mycamera;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery.LayoutParams;
/**
 * ͼƬ����ʽ��ѡ����� ʵ��
 * ��ʾ��Ƭ(AdapterView)���ȴ���ȡ��Ƭ��������(OnItemSelectedListener)��������Ƭ�л�����(ViewSwitcher)�����𱣴���Ƭ(ViewFactory)�ȹ���
 */
public class PhotoView extends Activity implements
AdapterView.OnItemSelectedListener, ViewSwitcher.ViewFactory {
private static ImageSwitcher mSwitcher;
private static Button returnButton;
private static Button saveButton;
private List<String> phoList;
// private List<String> or_phoList;
private String newFilePath = "";
@SuppressWarnings("unchecked")
private static List<String> readFiles(String path) {
List picFiles = new ArrayList();
// �����ļ�����
File dir = new File(path);
// �õ����ļ����������ļ�
File[] files = dir.listFiles();
if (files != null) {
for (int i = 0; i < files.length; i++) {
String fileName = files[i].getName();
// �������к�׺Ϊ.jpg���ļ�
if (fileName.lastIndexOf(".") > 0
&& fileName.substring(fileName.lastIndexOf(".") + 1,
fileName.length()).equals("jpg")||fileName.substring(fileName.lastIndexOf(".") + 1,
fileName.length()).equals(".gif")) {
picFiles.add(files[i].getPath());
}
}
}
return picFiles;
}
// Thread myT = new Thread(new Runnable() {
// public void run() {
// // TODO �ø��߳�ʵ�ֵĹ���
// try {
// or_phoList = readFiles("sdcard/fatalityUpload/");
// } catch (Exception e) {
// e.printStackTrace();
// }
// }
// });
public static void setInSampleSize(BitmapFactory.Options options,int targetSize) {
options.outWidth >>= 1;
options.outHeight >>= 1;
while (options.outWidth > targetSize || options.outHeight > targetSize) {
options.inSampleSize <<= 1;
options.outWidth >>= 1;
options.outHeight >>= 1;
}
}
@Override
public void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.photoview);
setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // ����
getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
WindowManager.LayoutParams.FLAG_FULLSCREEN);// ȫ��
this.setListener();
mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
mSwitcher.setFactory(this);
// mSwitcher.setOnClickListener(clickImageView);
// ������Ƭ�л�ʱ�ĵ��뵭��ģʽ
mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
android.R.anim.fade_in));
mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
android.R.anim.fade_out));
Gallery g = (Gallery) findViewById(R.id.gallery);
g.setAdapter(new ImageAdapter(this,phoList));
g.setOnItemSelectedListener(this);
g.setOnItemClickListener(new OnItemClickListener() 
   {
     public void onItemClick(AdapterView<?> parent, 
                      View v, int position, long id) 
     { 
       //�����ݱ������ʱ������¼�
     }
   });
}
private void setListener() {
phoList = readFiles("sdcard/fatalityUpload/Thumbnail_fatality/");
returnButton = (Button) findViewById(R.id.backMain);
saveButton = (Button) findViewById(R.id.existUpload);
returnButton.setOnClickListener(returnClick);
saveButton.setOnClickListener(saveClick);
}
private OnClickListener saveClick = new OnClickListener() {
public void onClick(View v) {
Intent intent = new Intent();
Bundle bundle = new Bundle();
// bundle.putByteArray("picPre", newData);
bundle.putString("picPath",newFilePath);
// intent.putExtra("picPre", data);
intent.putExtras(bundle);
intent.setClass(PhotoView.this, SubmitPhoto.class);
finish();
startActivity(intent);
}
};
private OnClickListener returnClick = new OnClickListener() {
public void onClick(View v) {
Intent intent = new Intent();
intent.setClass(PhotoView.this, MainActivity.class);
finish();
startActivity(intent);
}
};
/**
* ���û������Ƭ�����е���Ƭʱ�����ú���
*/
@SuppressWarnings("unchecked")
public void onItemSelected(AdapterView parent, View v, int position, long id) {
String fileArr[] = phoList.get(position).toString().split("/");
newFilePath = "sdcard/fatalityUpload/fatality/"+fileArr[fileArr.length-1];
// Log.d("onItemSelected LOG",">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+phoList.get(position).toString());
// Log.d("onItemSelected LOG",">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+newFilePath);
Bitmap bm = BitmapFactory.decodeFile(phoList.get(position).toString());
BitmapDrawable bmpDraw=new BitmapDrawable(bm);
mSwitcher.setImageDrawable(bmpDraw);
}
public void onNothingSelected(AdapterView<?> arg0) {
}
/**
* ��ʾ��Ƭ����
*/
public View makeView(){
ImageView imageView = new ImageView(this);
imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
android.widget.FrameLayout.LayoutParams imageSwitcher = new ImageSwitcher.LayoutParams(
LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
imageView.setLayoutParams(imageSwitcher);
return imageView;
}
/**
* �����ͼƬʱ�л���ͼƬ�༭�ϴ�����
*/
// private OnClickListener clickImageView = new OnClickListener() {
// public void onClick(View v) {
// Intent intent = new Intent();
// intent.setClass(PhotoView.this, SubmitPhoto.class);
// finish();
// startActivity(intent);
// }
// };
}