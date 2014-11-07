package com.somatic.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.test.R;
import com.somatic.transmission.CheckConnection;

public class Setting extends Activity {

	private EditText ed_ip = null;
	private Button btn_conn;
	private Thread thread = null;
	private Handler handler;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);

		ed_ip = (EditText) findViewById(R.id.ed_ip);
		btn_conn = (Button) findViewById(R.id.btn_conn);

		btn_conn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if(!ConnectionState.isConn){
					if(thread == null){
						thread = new MyThread();
						thread.start();
					}
					
				} else {
					if(ConnectionState.isConn){
						new AlertDialog.Builder(Setting.this).setTitle("ȷ�Ͽ�")
						.setMessage("�ѳɹ����ӣ�").setPositiveButton("ȷ��", 
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if(ConnectionState.isConn){
										ConnectionState.isConn = true;
										Setting.this.finish();
										overridePendingTransition(R.anim.slide_left_2, R.anim.slide_right_2);
									}
								}
						})
						.show();
					}
				}
			}

		});
		
		handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 0x11){
					if(msg.arg1 == 2){
						thread.interrupt();
						thread = null;
					}
					
					String message = (String)msg.obj;
					new AlertDialog.Builder(Setting.this).setTitle("ȷ�Ͽ�")
						.setMessage(message).setPositiveButton("ȷ��", 
								new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if(ConnectionState.isConn){
										ConnectionState.isConn = true;
										Setting.this.finish();
										overridePendingTransition(R.anim.slide_left_2, R.anim.slide_right_2);
									}
								}
						})
						.show();
				}
			}
		};
	}
	
	class MyThread extends Thread {

		@Override
		public void run() {
			//while()
			String ip = ed_ip.getText().toString().trim();
			
			// ����ip
			SharedPreferences sp = getSharedPreferences("save_ip", MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putString("ip", ip);
			editor.commit();
			
			ConnectionState.ip = ip;
			// �����������Ӷ���
			CheckConnection checker;
			String message = null;			
			try {
				checker = new CheckConnection(ip);
				
				if (checker.isConnectionOK()) {
					message = "��ϲ�������ӳɹ�!"; // �������ӳɹ�
					ConnectionState.isConn = true;
				} else {
					message = "�Բ�������ʧ��!"; // ����ʧ��
					ConnectionState.isConn = false;
				}
				
			} catch (Exception e) {
				message = "�Բ�������ʧ��!"; // ����ʧ��
				ConnectionState.isConn = false;
			} finally {
				Message msg = handler.obtainMessage();
				msg.what = 0x11;
				msg.arg1 = ConnectionState.isConn==true?1:2;
				msg.obj = new String(message);
				handler.sendMessage(msg);
			}
		}
		
	}

	public void onClick(View view) {
		Intent intent = new Intent(Setting.this, CameraPreview.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_right, R.anim.slide_left);
		
		Setting.this.finish();
	}

	protected void onResume() {
		super.onResume();
		// ��ȡ������Ϣ����IP�����Ե�
		SharedPreferences sp = getSharedPreferences("save_ip", MODE_PRIVATE);
		String ipStr = sp.getString("ip", "");
		// ��ʼ��EditText��ֵ
		ed_ip.setText(ipStr);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Setting.this.finish();
		overridePendingTransition(R.anim.slide_left_2, R.anim.slide_right_2);
	}
}