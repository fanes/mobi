package cn.edu.hrbeu.heu_newsclient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.edu.hrbeu.heu_newsclient.common.ServerWebRoot;
import cn.edu.hrbeu.heu_newsclient.model.ResponseJson;

import com.google.gson.Gson;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Register extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		Button reg = (Button) findViewById(R.id.reg_goreg);
		final EditText name_view = (EditText) findViewById(R.id.reg_username);
		final EditText pwd_view = (EditText) findViewById(R.id.reg_userpwd);
		final EditText pwd_con_view = (EditText) findViewById(R.id.reg_userpwd_confirm);
		
		reg.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				final String username = name_view.getText().toString();
				final String password = pwd_view.getText().toString();
				String password_confirm = pwd_con_view.getText().toString();
				
				if(username.equals("")){
					Toast.makeText(Register.this, "请正确填写用户名！", Toast.LENGTH_SHORT).show();
				} else{
					
					if(password.equals(password_confirm)){
						
						new Thread(new Runnable(){
							public void run() {
								try{
									String requestUrl = ServerWebRoot.getServerWebRoot()+"UserReg?username="+username+"&password="+password;
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
												Toast.makeText(Register.this, jsonObj.msg, Toast.LENGTH_SHORT).show();
												Intent in = new Intent(Register.this, LoginActivity.class);
												startActivity(in);
												finish();
											}else{
												Toast.makeText(Register.this, jsonObj.msg, Toast.LENGTH_SHORT).show();
											}
										}
									});
									
								}catch(Exception e){
									e.printStackTrace();
								}
							}
						}).start();
						
					} else{
						Toast.makeText(Register.this, "两次密码输入不一致，请重试！", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		
		
		
	}

}
