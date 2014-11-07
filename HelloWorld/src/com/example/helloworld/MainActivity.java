package com.example.helloworld;

import com.example.helloworld.HelloAndroid.ClickListener;

import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends ActionBarActivity {
	Button btnShow;
	Button btnClear;
	EditText edtInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d("1111111", "savedInstanceState: "+ savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btnShow=(Button)findViewById(R.id.btnShow);//控件与代码绑定
        btnClear=(Button)findViewById(R.id.btnClear);//控件与代码绑定
        edtInput=(EditText)findViewById(R.id.edtInput);//控件与代码绑定
        btnShow.setOnClickListener(new ClickListener());//使用点击事件
        btnClear.setOnClickListener(new ClickListener());//使用点击事件
    }

    class  ClickListener implements OnClickListener
    {
    	public void onClick(View v)
    	{
    		Log.d("1111111", "v: " + v);
    		if(v==btnShow)
    		{
    			Log.d("1111111", "btnShow: " + v);
    			new AlertDialog.Builder(MainActivity.this)
    			.setIcon(android.R.drawable.ic_dialog_alert)
    			.setTitle("Information")
    			.setMessage(edtInput.getText())
    			.show();		
    		}
    		else if(v==btnClear)
    		{
    			Log.d("1111111", "btnClear: " + v);
    			edtInput.setText("HelloAndroid");
    		}
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
