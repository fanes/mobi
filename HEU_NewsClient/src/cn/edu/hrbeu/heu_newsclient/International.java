package cn.edu.hrbeu.heu_newsclient;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import cn.edu.hrbeu.heu_newsclient.common.ServerWebRoot;
import cn.edu.hrbeu.heu_newsclient.model.News;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class International extends Activity {

	ListView mNews;
	Handler mMainHandler;
	SimpleAdapter mNewsList;
	ArrayList<HashMap<String,Object>> mlist;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_international);
		
		mNews = (ListView)findViewById(R.id.newsList_international);
        mlist = new ArrayList<HashMap<String, Object>>();
        mNewsList = new SimpleAdapter(this,
        		mlist,
        		R.layout.listitem,
        		new String[]{"title", "provider", "pubtime", "id", "loc", "orig"},
        		new int[]{R.id.news_title, R.id.news_provider, R.id.news_pubtime, R.id.news_id, R.id.news_details, R.id.news_orig}
        		);
        
        mNews.setAdapter(mNewsList);
        httpGet();
		mMainHandler = new Handler() {
        	public  void handleMessage(Message msg) {
        		if(msg.obj != null) {
        			News news = (News)msg.obj;
        			HashMap<String, Object> map = new HashMap<String, Object>();
        			map.put("title", news.getTitle());
        			map.put("provider", news.getProvider());
        			map.put("pubtime", news.getDatetime());
        			map.put("id", news.getNewsID());
        			map.put("loc", news.getStorageLoc());
        			map.put("orig", news.getLink());
        			mlist.add(map);
        			mNewsList.notifyDataSetChanged();
        		}
        	}
        };
        
        mNews.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TextView curid =(TextView) view.findViewById(R.id.news_id);
				TextView curtitle = (TextView) view.findViewById(R.id.news_title);
				TextView curloc = (TextView) view.findViewById(R.id.news_details);
				TextView curoriglink = (TextView) view.findViewById(R.id.news_orig);
				TextView curprovider = (TextView) view.findViewById(R.id.news_provider);
				TextView curpubtime = (TextView) view.findViewById(R.id.news_pubtime);
				
				Intent in = new Intent();
				in.putExtra("id", curid.getText());
				in.putExtra("loc", curloc.getText());
				in.putExtra("title", curtitle.getText());
				in.putExtra("orig", curoriglink.getText());
				in.putExtra("provider", curprovider.getText());
				in.putExtra("pubtime", curpubtime.getText());
				in.setClass(International.this, NewsDetail.class);
				startActivity(in);
			}
        	
        });
	}
	
	void httpGet() {
    	GetHttpTask task = new GetHttpTask();
    	task.execute(ServerWebRoot.getServerWebRoot()+"InternationalNews.xml");
    }
    
    public class GetHttpTask extends AsyncTask<String, Integer, String> {
    	protected String doInBackground(String... params) {
        	HttpGet httpRequest = new HttpGet(params[0]);
        	HttpClient httpclient = new DefaultHttpClient();
        	try {
        		HttpResponse httpResponse = httpclient.execute(httpRequest);
        		if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
        			HttpEntity entitiy = httpResponse.getEntity();
        			InputStream in = entitiy.getContent();
        			InputSource source = new InputSource(in);
        			SAXParserFactory sax = SAXParserFactory.newInstance();
        			XMLReader xmlReader = sax.newSAXParser().getXMLReader();
        			xmlReader.setContentHandler(new NewsHandler());
        			xmlReader.parse(source);
        		}
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        	return null;
    	}
    }
    
    
    class NewsHandler extends DefaultHandler {
    	private News curNews;
    	private String content;
    	
    	public void startElement(String uri, String localName, String name, org.xml.sax.Attributes attributes) throws SAXException {
    		if(localName.equals("news")) {
    			curNews = new News();
    		} /*else if(localName.equals("id")) {
    			//String curid = attributes.getValue("id");
    			//curNews.setNewsID(Integer.parseInt(curid));
    		} else if(localName.equals("title")) {
    			curNews.setTitle(attributes.getValue("title"));
    		} else if(localName.equals("category")) {
    			curNews.setCategory(attributes.getValue("category"));
    		} else if(localName.equals("abstract")) {
    			curNews.setAbstract(attributes.getValue("abstract"));
    		} else if(localName.equals("provider")) {
    			curNews.setProvider(attributes.getValue("provider"));
    		} else if(localName.equals("pubtime")) {
    			curNews.setDatetime(attributes.getValue("pubtime"));
    		} else if(localName.equals("link")) {
    			curNews.setStorageLoc(attributes.getValue("link"));
    		} else if(localName.equals("origlink")) {
    			curNews.setLink(attributes.getValue("origlink"));
    		}*/
    		super.startElement(uri, localName, name, attributes);
    	}
    	
    	public void endElement(String uri, String localName, String name) throws SAXException{
    		if(localName.equals("news")) {
    			Message msg = mMainHandler.obtainMessage();
    			msg.obj = curNews;
    			mMainHandler.sendMessage(msg);
    		}else if(localName.equals("id")) {
    			curNews.setNewsID(content);
    		} else if(localName.equals("title")) {
    			curNews.setTitle(content);
    		} else if(localName.equals("category")) {
    			curNews.setCategory(content);
    		} else if(localName.equals("abstract")) {
    			curNews.setAbstract(content);
    		} else if(localName.equals("provider")) {
    			curNews.setProvider(content);
    		} else if(localName.equals("pubtime")) {
    			curNews.setDatetime(content);
    		} else if(localName.equals("link")) {
    			curNews.setStorageLoc(content);
    		} else if(localName.equals("origlink")) {
    			curNews.setLink(content);
    		}
    		super.endElement(uri, localName, name);
    	}
    	
    	public void characters (char[] ch, int start, int length)  throws SAXException{
    		content = new String(ch, start, length);
    		super.characters(ch, start, length);
    	}
    }
}