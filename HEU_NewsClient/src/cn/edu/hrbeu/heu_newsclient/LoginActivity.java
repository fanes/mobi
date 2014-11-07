package cn.edu.hrbeu.heu_newsclient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.edu.hrbeu.heu_newsclient.common.ServerWebRoot;
import cn.edu.hrbeu.heu_newsclient.model.ResponseJson;

import com.google.gson.Gson;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		final EditText name_view = (EditText) findViewById(R.id.login_uname);
		final EditText pwd_view = (EditText) findViewById(R.id.login_upwd);
		Button login = (Button) findViewById(R.id.login_login);
		Button register = (Button) findViewById(R.id.login_reg);
				
		login.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				final String username = name_view.getText().toString();
				final String password = pwd_view.getText().toString();
				new Thread(new Runnable(){
					public void run() {
						try{
							String requestUrl = ServerWebRoot.getServerWebRoot()+"UserLogin?username="+username+"&password="+password;
							URL url = new URL(requestUrl);
							HttpURLConnection conn = (HttpURLConnection) url.openConnection();
							conn.setRequestMethod("POST");
							InputStream in = conn.getInputStream();
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							byte[] buf = new byte[1024];
							while(true){
								int len = in .read(buf);
								if(len==-1) break;
								out.write(buf,0,len);
							}
							
							final String json  = new String (out.toByteArray(),"UTF-8");
							runOnUiThread(new Runnable() {
								public void run(){
									Gson g =new Gson();
									ResponseJson jsonObj = g.fromJson(json, ResponseJson.class);
									if(jsonObj.status==200){
										SharedPreferences sp_user = getSharedPreferences("sp_user", Context.MODE_PRIVATE);
										SharedPreferences.Editor sp_editor = sp_user.edit(); 
										sp_editor.putInt("UserID", jsonObj.userid);
										sp_editor.putString("UserName", username);
										sp_editor.commit(); 
										
										Intent in = new Intent(LoginActivity.this,MainActivity.class);
										startActivity(in);
										finish();
									}else{
										Toast.makeText(LoginActivity.this, jsonObj.msg, Toast.LENGTH_SHORT).show();
									}
								}
							});
							
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}).start();
			}
			
		});

		register.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Intent in = new Intent();
				in.setClass(LoginActivity.this, Register.class);
				startActivity(in);
				finish();
			}

		});
	}


}
