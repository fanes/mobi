package com.somatic.view; 
 
import java.io.ByteArrayInputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.test.R;
import com.somatic.transmission.CheckConnection;
 
public class CameraPreview extends Activity implements OnClickListener { 
 
    private Camera camera = null; 
    // 继承surfaceView的自定义view 用于存放照相的图片 
    private CameraView cv = null; 
    private Button takeReady = null; 
    private LinearLayout ll = null; 
    
    private Handler handler;
    
    private CheckConnection ck;
    private static boolean isConnected = false;
 
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_main); 
 
        ll = (LinearLayout) findViewById(R.id.cameraView); 
        takeReady = (Button) findViewById(R.id.take_ready); 
        takeReady.setOnClickListener(this); 
        
        ll.removeAllViews(); 
        cv = new CameraView(CameraPreview.this); 
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( 
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.MATCH_PARENT); 
        ll.addView(cv, params); 
        
        
        handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 0x11){
					takeReady.setText("可以拍照");
					takeReady.setEnabled(false);
				}
				
				super.handleMessage(msg);
			}
        	
        };
    } 
    
    // 回调用的picture，实现里边的onPictureTaken方法，其中byte[]数组即为照相后获取到的图片信息 
    private Camera.PictureCallback picture = new Camera.PictureCallback() { 
 
        @Override 
        public void onPictureTaken(byte[] data, Camera camera) { 
            // 主要就是将图片转化成drawable，设置为固定区域的背景（展示图片），当然也可以直接在布局文件里放一个surfaceView供使用。 
            ByteArrayInputStream bais = new ByteArrayInputStream(data); 
            Drawable d = BitmapDrawable.createFromStream(bais, Environment 
                    .getExternalStorageDirectory().getAbsolutePath() 
                    + "/img.jpeg"); 
            ll.setBackgroundDrawable(d); 
            try { 
            } catch (Exception e) { 
                e.printStackTrace(); 
            } 
        } 
 
    }; 
 
    //主要的surfaceView，负责展示预览图片，camera的开关 
    class CameraView extends SurfaceView { 
 
        private SurfaceHolder holder = null; 
 
        public CameraView(Context context) { 
            super(context); 
            holder = this.getHolder(); 
 
            holder.addCallback(new SurfaceHolder.Callback() { 
 
                @Override 
                public void surfaceChanged(SurfaceHolder holder, int format, 
                        int width, int height) { 
                    Camera.Parameters parameters = camera.getParameters(); 
                    //以下注释掉的是设置预览时的图像以及拍照的一些参数 
                    // parameters.setPictureFormat(PixelFormat.JPEG); 
                    // parameters.setPreviewSize(parameters.getPictureSize().width, 
                    // parameters.getPictureSize().height); 
                    // parameters.setFocusMode("auto"); 
                    // parameters.setPictureSize(width, height); 
                    camera.setParameters(parameters); 
                    camera.startPreview(); 
                } 
 
                @Override 
                public void surfaceCreated(SurfaceHolder holder) { 
                    camera = Camera.open(); 
 
                    try { 
                        //设置camera预览的角度，因为默认图片是倾斜90度的 
                        camera.setDisplayOrientation(90); 
                        //设置holder主要是用于surfaceView的图片的实时预览，以及获取图片等功能，可以理解为控制camera的操作.. 
                        camera.setPreviewDisplay(holder); 
                    } catch (IOException e) { 
                        camera.release(); 
                        camera = null; 
                        e.printStackTrace(); 
                    } 
 
                } 
 
                @Override 
                public void surfaceDestroyed(SurfaceHolder holder) { 
                    //顾名思义可以看懂 
                    camera.stopPreview(); 
                    camera.release(); 
                    camera = null; 
                } 
            }); 
//          holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
        } 
    } 
 
    @Override 
    public void onClick(View v) { 
    	switch(v.getId()){
    	case R.id.take_ready:
    		if(!ConnectionState.isConn){
    			new AlertDialog.Builder(CameraPreview.this).setTitle("提示框")
    				.setMessage("请先连接局域网！").setPositiveButton("确定", 
    						new DialogInterface.OnClickListener() {
    						@Override
    						public void onClick(DialogInterface dialog, int which) {
    							
    						}
    				})
    				.show();
    		} else {
	            new MyThread().start();
    		}
    		
            break;
    	case R.id.btn_set:
    		Intent intent = new Intent(CameraPreview.this, Setting.class);
    		startActivity(intent);
    		overridePendingTransition(R.anim.slide_right, R.anim.slide_left);
    		break;
    	}
    	
    } 
    
   
    
    
    class MyThread extends Thread{

		@Override
		public void run() {
			try {
				
				handler.sendEmptyMessage(0x11);
				ck = new CheckConnection(ConnectionState.ip);
				ck.sendAccess();
				
				System.out.println("进入第一个线程");
				
				boolean isOk = ck.receiveTake();
				
				if(isOk){
					camera.takePicture(null, null, picture);
					
				}
				
				System.out.println("开启第二个线程");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
} 