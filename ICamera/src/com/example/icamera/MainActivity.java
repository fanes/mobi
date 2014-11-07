package com.example.icamera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {  
 
    public static final int NONE = 0;  
    public static final int PHOTOHRAPH = 1;// 拍照  
    public static final int PHOTOZOOM = 2; // 缩放  
    public static final int PHOTORESOULT = 3;// 结果  
 
    public static final String IMAGE_UNSPECIFIED = "image/*";  
    ImageView imageView = null;  
    Button btn_list = null;  
    Button btn_take = null;
    
    File fileDir;

    @Override  
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ///////////
        int myProcessID = Process.myPid();
        fileDir = this.getFilesDir();
        //this.getCacheDir();
        /*****
         * 
        String parentDir = this.getFilesDir().getParent() + File.separator +".dir";
        File parentFile = new File(parentDir);
        parentFile.mkdirs();
        
        String str = "chmod " + parentDir + "  " + " 777 "+
        " && busybox chmod "+ parentDir + "  " + " 777 ";
        
        try{
        	Runtime.getRuntime().exec(str);
        }
        catch(IOException ex){
//        	ex.printStackTrace();
        	Log.d("mydebug", ex.getLocalizedMessage());
        }
        fileDir = parentFile;
        */
        /*****
        String yygypathstr = fileDir.toString();
        File file = new File(fileDir, "yygytest"); 
        try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        yygypathstr = yygypathstr + " pid is " + myProcessID;
        
        TextView  tv = new TextView(this);
        tv.setText(yygypathstr);
        setContentView(tv);
        */
        
         
        setContentView(R.layout.activity_main);  
        imageView = (ImageView) findViewById(R.id.imageID);  
        btn_list = (Button) findViewById(R.id.btn_list);
        btn_take = (Button) findViewById(R.id.btn_take);
 /******
        btn_list.setOnClickListener(new OnClickListener() {
            @Override  
            public void onClick(View v) {  
                Intent intent = new Intent(Intent.ACTION_PICK, null);  
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);  
                startActivityForResult(intent, PHOTOZOOM);  
            } 
        });  
 
        btn_take.setOnClickListener(new OnClickListener() {
 
            @Override  
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //Log.d("mydebug", "getExternalStorageDirectory: "+Environment.getExternalStorageDirectory());
                Log.d("mydebug", "getDataDirectory: "+Environment.getDataDirectory());
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp.jpg")));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File("temp.jpg")));
                startActivityForResult(intent, PHOTOHRAPH);
            }
        });
        ******/
    }
 
    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == NONE)  
            return;  
        // 拍照  
        if (requestCode == PHOTOHRAPH) {
            //设置文件保存路径这里放在跟目录下  
        	File picture = new File("/storage/sdcard0/DCIM/Camera/temp.jpg");
//            File picture = new File(Environment.getExternalStorageDirectory() + "/temp.jpg");  
            startPhotoZoom(Uri.fromFile(picture));  
        }  
          
        if (data == null)  
            return;  
          
        // 读取相册缩放图片  
        if (requestCode == PHOTOZOOM) {
            startPhotoZoom(data.getData());  
        }  
        // 处理结果  
        if (requestCode == PHOTORESOULT) {
            Bundle extras = data.getExtras();  
            if (extras != null) {  
                Bitmap photo = extras.getParcelable("data");  
                ByteArrayOutputStream stream = new ByteArrayOutputStream();  
                photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);// (0 - 100)压缩文件  
                imageView.setImageBitmap(photo);  
            }  
 
        }  
 
        super.onActivityResult(requestCode, resultCode, data);  
    }  
 
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP"); 
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例  
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高  
        intent.putExtra("outputX", 64);
        intent.putExtra("outputY", 64);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTORESOULT);
    }
    
    public void onClickList(View v){
    	File myfile = new File("/storage/sdcard0/DCIM/Camera/temp.jpg");
    	Uri uri = Uri.fromFile(myfile);
    	
    	Intent intent = new Intent(Intent.ACTION_PICK, null);
//        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
        startActivityForResult(intent, PHOTOZOOM);  
    }
    
    public void onClickTake(View v){
		
		 
    	String storagePath = SdCardHelper.getStorageDirectory(this);
    	File myfile = new File("/storage/sdcard0/DCIM/Camera/temp.jpg");
    	
    	if(!myfile.exists()){
    		File vDirPath = myfile.getParentFile(); //new File(vFile.getParent());
    		vDirPath.mkdirs();
    	}
    		
		
		 /***
			 * 
		 try
		{
			FileOutputStream fOut =
					openFileOutput(storagePath + "/temp.jpg",
							MODE_WORLD_READABLE);
                        
			OutputStreamWriter osw = new
					OutputStreamWriter(fOut);

			//---write the string to the file---
			osw.write("");
			osw.flush(); 
			osw.close();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		* 
		 */
		////////////
    	/*
		File fileDir = this.getFilesDir();
		File[] files = fileDir.listFiles();
		*/
		Log.d("mydebug", "files Dir: " + myfile.toString());
		Log.d("mydebug", "fileDir can read: " + myfile.canRead());
		Log.d("mydebug", "fileDir can write: " + myfile.canWrite());
		/*
		if(files != null && files.length>0){
			Log.d("mydebug", "files count: " + files.length);
			Log.d("mydebug", "files path: " + files[0].getPath()+", name:"+files[0].getName());
			Log.d("mydebug", "files can read: " + files[0].canRead());
			Log.d("mydebug", "files can write: " + files[0].canWrite());
		}
		*/
		/////////
		
    	
        //Log.d("mydebug", "getExternalStorageDirectory: "+Environment.getExternalStorageDirectory());
//        Log.d("mydebug", "getDataDirectory: "+Environment.getDataDirectory());
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp.jpg")));

    	
    	//Uri uri = Uri.fromFile(new File(files[0].getPath()));
    	Uri uri = Uri.fromFile(myfile);
    	
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.d("mydebug", "uri: "+uri.getEncodedPath());
        
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, PHOTOHRAPH);
    }
} 