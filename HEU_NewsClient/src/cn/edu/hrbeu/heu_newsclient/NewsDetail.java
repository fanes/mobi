package cn.edu.hrbeu.heu_newsclient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.edu.hrbeu.heu_newsclient.common.ServerWebRoot;
import cn.edu.hrbeu.heu_newsclient.model.ResponseJson;

import com.google.gson.Gson;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NewsDetail extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_detail);
		
		Intent intent = getIntent();
		final TextView title_text = (TextView) findViewById(R.id.detail_title);
		TextView provider_text = (TextView) findViewById(R.id.detail_provider);
		TextView pubtime_text = (TextView) findViewById(R.id.detail_pubtime);
		WebView wv = (WebView) findViewById(R.id.detail_web);
		Button orig_btn = (Button) findViewById(R.id.detail_relink);
		Button fav = (Button) findViewById(R.id.fav);
		Button share = (Button) findViewById(R.id.detail_share);
		SharedPreferences sp_user = getSharedPreferences("sp_user", Context.MODE_PRIVATE);
		final int newsid = Integer.parseInt(intent.getStringExtra("id"));
		final int userid = sp_user.getInt("UserID", -1);
		final String orig = intent.getStringExtra("orig");

		title_text.setText(intent.getStringExtra("title"));
		provider_text.setText(intent.getStringExtra("provider"));
		pubtime_text.setText(intent.getStringExtra("pubtime"));
		wv.setWebViewClient(new WebViewClient(){     
			public boolean shouldOverrideUrlLoading(WebView  view, String url) {     
				//view.loadUrl(url);
				return true;     
			}     
		});
		wv.loadUrl(intent.getStringExtra("loc"));
		
		orig_btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent in = new Intent();
				in.setAction("android.intent.action.VIEW"); 
				Uri content_url = Uri.parse(orig);   
			    in.setData(content_url);  
			    startActivity(in);
			}
			
		});
	
		fav.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				new Thread(new Runnable(){
					public void run() {
						try{
							String requestUrl = ServerWebRoot.getServerWebRoot()+"FavNews?flag=newfav&userid="+userid+"&newsid="+newsid;
							URL url = new URL(requestUrl);
							HttpURLConnection conn = (HttpURLConnection) url.openConnection();
							conn.setRequestMethod("GET");
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
									Toast.makeText(NewsDetail.this, jsonObj.msg, Toast.LENGTH_SHORT).show();
								}
							});
							
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}).start();
				}
			
		});
		
		share.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				 Intent intent=new Intent(Intent.ACTION_SEND);
			      
			     intent.setType("text/plain");
			     intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
			     intent.putExtra(Intent.EXTRA_TEXT, "我正在使用HEU新闻客户端阅读最新新闻："+title_text.getText().toString()+"\n\r"+"推荐你也使用HEU新闻客户端哦~http://dwz.cn/heu-news-client");
			     startActivity(Intent.createChooser(intent, getTitle()));		
			}
			
		});
	}
}
