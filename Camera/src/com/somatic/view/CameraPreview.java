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
    // �̳�surfaceView���Զ���view ���ڴ�������ͼƬ 
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
					takeReady.setText("��������");
					takeReady.setEnabled(false);
				}
				
				super.handleMessage(msg);
			}
        	
        };
    } 
    
    // �ص��õ�picture��ʵ����ߵ�onPictureTaken����������byte[]���鼴Ϊ������ȡ����ͼƬ��Ϣ 
    private Camera.PictureCallback picture = new Camera.PictureCallback() { 
 
        @Override 
        public void onPictureTaken(byte[] data, Camera camera) { 
            // ��Ҫ���ǽ�ͼƬת����drawable������Ϊ�̶�����ı�����չʾͼƬ������ȻҲ����ֱ���ڲ����ļ����һ��surfaceView��ʹ�á� 
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
 
    //��Ҫ��surfaceView������չʾԤ��ͼƬ��camera�Ŀ��� 
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
                    //����ע�͵���������Ԥ��ʱ��ͼ���Լ����յ�һЩ���� 
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
                        //����cameraԤ���ĽǶȣ���ΪĬ��ͼƬ����б90�ȵ� 
                        camera.setDisplayOrientation(90); 
                        //����holder��Ҫ������surfaceView��ͼƬ��ʵʱԤ�����Լ���ȡͼƬ�ȹ��ܣ��������Ϊ����camera�Ĳ���.. 
                        camera.setPreviewDisplay(holder); 
                    } catch (IOException e) { 
                        camera.release(); 
                        camera = null; 
                        e.printStackTrace(); 
                    } 
 
                } 
 
                @Override 
                public void surfaceDestroyed(SurfaceHolder holder) { 
                    //����˼����Կ��� 
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
    			new AlertDialog.Builder(CameraPreview.this).setTitle("��ʾ��")
    				.setMessage("�������Ӿ�������").setPositiveButton("ȷ��", 
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
				
				System.out.println("�����һ���߳�");
				
				boolean isOk = ck.receiveTake();
				
				if(isOk){
					camera.takePicture(null, null, picture);
					
				}
				
				System.out.println("�����ڶ����߳�");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
} 