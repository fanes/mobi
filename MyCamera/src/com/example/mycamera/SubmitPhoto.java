package com.example.mycamera;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
public class SubmitPhoto extends Activity{
private Button subButton;
private Button returnButton;
private ImageView subPicImgView;
private Bundle preBundle;
private MyThread myThread;
// private Spinner stationSpinner;
private DBHelper helper;
private Cursor cursor;
private List<StationModel> list = new ArrayList<StationModel>();
// private MyThread thread = new MyThread();
@Override
    public void onCreate(Bundle savedInstanceState){
super.onCreate(savedInstanceState);
setContentView(R.layout.photo_submit);
setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //横屏
getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏
setListener();
Bitmap bitmap = getBitMap();
BitmapDrawable bmpDraw=new BitmapDrawable(bitmap);
subPicImgView.setImageDrawable(bmpDraw);
bitmap = null;
}
/**
* 给界面上的按钮及控件添加Listener
*/
public void setListener(){
preBundle = this.getIntent().getExtras();
subPicImgView = (ImageView)findViewById(R.id.subPicImgView);
subButton = (Button)findViewById(R.id.subButton);
returnButton = (Button)findViewById(R.id.returnMain);
// stationSpinner = (Spinner)findViewById(R.id.stationId);
subButton.setOnClickListener(subClick);
returnButton.setOnClickListener(returnClick);
helper = new DBHelper(this);
helper.open(this);
cursor = helper.loadAll();
cursor.moveToFirst();
if(!cursor.isAfterLast()){
int modelId = cursor.getColumnIndex("ID");
int stName = cursor.getColumnIndex("STNAME");
int stid = cursor.getColumnIndex("STID");
int addvcd = cursor.getColumnIndex("FATHER");
StationModel sm = new StationModel();
sm.setId(cursor.getInt(modelId));
sm.setStid(cursor.getInt(stid));
sm.setStname(cursor.getString(stName));
sm.setAddvcd(cursor.getString(addvcd));
list.add(sm);
cursor.moveToNext();
}
cursor.close();
helper.close();
}
/**
* 点击上传按钮触发
*/
private OnClickListener subClick = new OnClickListener(){
public void onClick(View v) {
ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);  
       NetworkInfo networkinfo = manager.getActiveNetworkInfo();  
       if (networkinfo == null || !networkinfo.isAvailable()) {  // 当前网络不可用
        new AlertDialog.Builder(SubmitPhoto.this)
           .setMessage("检查到没有可用的网络连接,请打开网络连接")
           .setPositiveButton("确定",
            new DialogInterface.OnClickListener(){
                   public void onClick(DialogInterface dialoginterface, int i){
                    ComponentName cn = new ComponentName("com.android.settings","com.android.settings.Settings");
Intent intent = new Intent();
intent.setComponent(cn);
intent.setAction("android.intent.action.VIEW");
startActivity(intent);
//                     finish();
                   }
            }
           ).show();
         }else{
         showDialog(0);
         myThread = new MyThread();
         myThread.start();
         }
}
};
/**
* 点击返回按钮时触发的事件
*/
private OnClickListener returnClick = new OnClickListener(){
public void onClick(View v) {
new AlertDialog.Builder(SubmitPhoto.this)
.setTitle("提示").setMessage("确定放弃上传?")
.setPositiveButton("确定",
new DialogInterface.OnClickListener() {
public void onClick(DialogInterface dialog,
int whichButton) {
// if(!myThread.isInterrupted()){
// myThread.interrupt();
// }
Intent intent = new Intent();
intent.setClass(SubmitPhoto.this, MainPage.class);
finish();
startActivity(intent);
}
}).setNegativeButton("取消",
new DialogInterface.OnClickListener() {
public void onClick(DialogInterface dialog,
int whichButton) {
// 取消按钮事件
}
}
).show();
}
};
/**
* 将上个界面传过来的图片地址转换成Bitmap返回
* @return
*/
public Bitmap getBitMap(){
String picPath = preBundle.getString("prePath");
BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap bm = BitmapFactory.decodeFile(picPath, options);
return bm;
}
/**
* 当调用showDialog()时触发此函数
* 控制弹出的内容
*/
@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
       case 0: {
           ProgressDialog dialog = new ProgressDialog(SubmitPhoto.this);
           dialog.setMessage("文件上传中，请稍后...");
           dialog.setIndeterminate(true);
           dialog.setCancelable(true);
//            Log.d("dialog","<<<<<<<<<<<<<<<<-----<<<<<<<<<dialoging");
           return dialog;
       }
            case 1: {
             ProgressDialog mProgressDialog = new ProgressDialog(SubmitPhoto.this);
                mProgressDialog.setIcon(R.drawable.ic_menu_upload_mini);
                mProgressDialog.setTitle("文件上传中请稍后");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMax(100);
                mProgressDialog.setButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
//                     removeDialog(1);
                    }
                });
                return mProgressDialog;
            }
        }
        return null;
    }
/**
* 显示Dialog的method
* @param mess
*/
private void showToast(String mess) {
// Log.d("showToast", "======================showToast");
new AlertDialog.Builder(SubmitPhoto.this).setTitle("Message")
.setMessage(Html.fromHtml(mess)).setNegativeButton("确定",
new DialogInterface.OnClickListener() {
public void onClick(DialogInterface dialog, int which) {
// if(!myThread.isInterrupted()){
// myThread.interrupt();
// }
Intent intent = new Intent();
    intent.setClass(SubmitPhoto.this, MainPage.class);
    finish();
    startActivity(intent);
}
}).show();
}
/*
* 接收子线程传递过来的结果
* 负责给主线程发送消息
*/
Handler subHandler = new Handler() {  
        public void handleMessage(Message msg) {   
             switch (msg.what) {   
             case 0:   
             showDialog(0);  
             break;   
             case 1:
             removeDialog(0);
             Bundle bundle = msg.getData();
//             Log.d("upload result",">>>>>>>>>>>>>>>>>>upload result:"+bundle.getString("uploadMsg"));
             showToast(bundle.getString("uploadMsg"));
             break;
             case 2:
             removeDialog(0);
             showToast("您请求的网络连接失败,请稍后重试");
             break;
             case 3:
             removeDialog(0);
             showToast("<p>对不起,您的手机卡没有被许可使用本程序进行上传功能,如有问题请<a href="mailto:qingsong.yang@bjwmt.com" mce_href="mailto:c.com">联系我们</a></p>");
             break;
             }   
             super.handleMessage(msg);   
        }   
};  
/**
* 图片上传线程
* 将上传结果通过Handler传递给主线程
* @author peter
*/
class MyThread extends Thread implements Runnable { 
private String upMsg = "",file="",fileName="";
/**
* 判断网络是否可用
*/ 
public int getCon(String url) {
int con1 = 0;
URL objURL = null;
try {
objURL = new URL(url);
con1 = 1;
} catch (MalformedURLException e2) {
con1 = 0;
}
if (objURL != null) {
URLConnection conn = null;
try {
conn = objURL.openConnection();
conn.setConnectTimeout(1000);
conn.setReadTimeout(1000);
con1 = 1;
} catch (IOException e) {
con1 = 0;
}
if (conn != null) {
try {
conn.connect();
con1 = 1;
} catch (IOException e) {
con1 = 0;
}
}
}
return con1;
}
/**
* 取得请求返回的数据字符串
* @param url
* @return
*/
public String getText(String url){
String text = "";
try {
URL objURL = new URL(url);
URLConnection conn = objURL.openConnection();
conn.setConnectTimeout(6 * 1000);
conn.connect();
BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
String inputLine;
if (br == null) {
return null;
}
while ((inputLine = br.readLine()) != null) {
text += inputLine;
}
} catch (MalformedURLException e) {
e.printStackTrace();
} catch (IOException e) {
e.printStackTrace();
}
return text;
}
/**
* 图片上传
*/
public void uploadFile(){
TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
FTPClient ftp = new FTPClient();
String server = "xx.xx.xx.xx";
String username = tm.getDeviceId(); 
String password = "micromulti"; 
try {
ftp.setDefaultTimeout(30000);
ftp.setDefaultPort(21);
ftp.connect(server);
int reply = ftp.getReplyCode();
if (!FTPReply.isPositiveCompletion(reply)) {
ftp.disconnect();
upMsg = "FTP 连接失败";
} else {
if (ftp.login(username, password)) {
ftp.enterLocalPassiveMode();
ftp.setFileType(FTP.BINARY_FILE_TYPE);
ftp.setRemoteVerificationEnabled(false); 
FileInputStream fis = new FileInputStream(new File(file));
ftp.storeFile(fileName, fis);
fis.close();
    }
    ftp.logout();
    upMsg = "文件上传成功";
     }
     } catch (Exception e) {
     upMsg = ""+e;
     } finally {
     if (ftp.isConnected()) {
     try {
     ftp.disconnect();
     } catch (Exception ioe) {
     upMsg = ""+ioe;
     }
     }
     }
}
        public void run() { 
         String file = preBundle.getString("prePath");
         String arrs[] = file.split("/");
     String fileName = arrs[arrs.length-1];
     String fileStrArr[] = fileName.split("-");
     String phoneIMSI = fileStrArr[0];
     String lat = fileStrArr[1];
         String lng = fileStrArr[2];
     String shootTime = fileStrArr[3];
//         Log.d("lat&lng","*******************lat is:"+lat+"  lng is:"+lng+"  fileName is:"+fileName);
         EditText picInfo = (EditText)findViewById(R.id.photoInfo);
         String freshUrl ="http://xx.xx.xx.xx:8080/DataAcquisition/photoUploadAction.do?photoWebModel.actiontype=upload";
     String postUrl = freshUrl+"&photoWebModel.phoneIMSI="+phoneIMSI
     +"&photoWebModel.lat="+lat
     +"&photoWebModel.lng="+lng
     +"&photoWebModel.shootTime="+shootTime
     +"&photoWebModel.picInfo="+picInfo.getText()
     +"&photoWebModel.filename="+fileName;
     Message message = new Message();
     String result = "";
     if(this.getCon(postUrl)==1){
     result = getText(postUrl);
     Log.d("uploadConfirmResult", ">>>>>>>>>>>>>>>>>>"+result.trim());
            }else{
                message.what = 2;   
            }
     if(result.trim().equals("true")){
     String thumbnailFilePath = "sdcard/fatalityUpload/Thumbnail_fatality/"+fileName;
     uploadFile(); //执行图片上传
     Log.d("uploadFile",">>>>>>>>>>>>>>>>>>>>>>>>>complate!stop Upload...");
File file2 = new File(file);
file2.delete();
file2 = null;
File file3 = new File(thumbnailFilePath);
file3.delete();
file3 = null;
             Bundle bundle = new Bundle();
             bundle.putString("uploadMsg", upMsg);
             message.setData(bundle);
                message.what = 1;   
}else if (result.trim().equals("false1")){
                message.what = 3;   
}else{}
     Log.d("mess.what", "<<<<<<<<<<<<<<<<<<message.what is :"+message.what);
     subHandler.sendMessage(message);
        }   
}
}