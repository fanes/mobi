package cn.edu.hrbeu.heu_newsclient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;

import cn.edu.hrbeu.heu_newsclient.common.ServerWebRoot;
import cn.edu.hrbeu.heu_newsclient.model.News;
import cn.edu.hrbeu.heu_newsclient.model.ResponseJson;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class AccountCenter extends Activity {
	ListView listView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listView =  new ListView(this);
		setContentView(listView);
		final SharedPreferences sp_user = getSharedPreferences("sp_user", Context.MODE_PRIVATE);
		
		new Thread(new Runnable(){
			public void run(){
				try {
					int userId = sp_user.getInt("UserID", -1);
					if(userId==-1){
						return;
					}
					String requestUrl = ServerWebRoot.getServerWebRoot()+"FavNews?flag=query&userid="+userId;
					URL url = new URL(requestUrl);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					InputStream in = conn.getInputStream();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] buf = new byte[1024];
					while(true){
						int len = in .read(buf);
						if(len==-1){
							break;
						}
						out.write(buf,0,len);
					}
					final String json  = new String (out.toByteArray(),"UTF-8");
					runOnUiThread(new Runnable() {
						public void run(){
							Gson g =new Gson();
							ResponseJson jsonObj = g.fromJson(json, ResponseJson.class);
							if(jsonObj.data==null){
								Toast.makeText(AccountCenter.this, "暂无收藏数据！", Toast.LENGTH_LONG).show();
								Intent in = new Intent();
								in.setClass(AccountCenter.this, MainActivity.class);
								startActivity(in);
								finish();
							} else{
							NewsFavAdapter na = new NewsFavAdapter(AccountCenter.this, jsonObj.data);
							listView.setAdapter(na);
							listView.setOnItemClickListener(new OnItemClickListener(){
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									TextView Title = (TextView) view.findViewById(R.id.news_title);
									TextView Provider = (TextView) view.findViewById(R.id.news_provider);
									TextView Pubtime = (TextView) view.findViewById(R.id.news_pubtime);
									TextView NewsID = (TextView) view.findViewById(R.id.news_id);
									TextView Details = (TextView) view.findViewById(R.id.news_details);
									TextView OrigLink = (TextView) view.findViewById(R.id.news_orig);
									
									Intent in = new Intent();
									in.putExtra("id", NewsID.getText());
									in.putExtra("loc", Details.getText());
									in.putExtra("title", Title.getText());
									in.putExtra("orig", OrigLink.getText());
									in.putExtra("provider", Provider.getText());
									in.putExtra("pubtime", Pubtime.getText());
									in.setClass(AccountCenter.this, NewsDetail.class);
									startActivity(in);
								}
								
							});
							//Toast.makeText(AccountCenter.this,jsonObj.msg, Toast.LENGTH_LONG).show();
							}
						}
					});
					//Log.e("json",json);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}).start();

	}

}

class NewsFavAdapter extends BaseAdapter{
    public Context ctx;
    public List<News> data;
    LayoutInflater inflater;
	
    public NewsFavAdapter(Context ctx, List data){
    	super();
		this.ctx = ctx;
		this.data = data;
		inflater =(LayoutInflater) this.ctx .getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
    }
	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView==null){
			convertView = inflater.inflate(R.layout.listitem, null);
		}
		TextView Title = (TextView) convertView.findViewById(R.id.news_title);
		TextView Provider = (TextView) convertView.findViewById(R.id.news_provider);
		TextView Pubtime = (TextView) convertView.findViewById(R.id.news_pubtime);
		TextView NewsID = (TextView) convertView.findViewById(R.id.news_id);
		TextView Details = (TextView) convertView.findViewById(R.id.news_details);
		TextView OrigLink = (TextView) convertView.findViewById(R.id.news_orig);
		
		Title.setText(data.get(position).getTitle());
		Provider.setText(data.get(position).getProvider());
		Pubtime.setText(data.get(position).getDatetime());
		NewsID.setText(data.get(position).getNewsID());
		Details.setText(data.get(position).getStorageLoc());
		OrigLink.setText(data.get(position).getLink());
		return convertView;
	}
}
