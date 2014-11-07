package com.example.helloworld;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
public class HelloAndroid extends Activity {
    /** Called when the activity is first created. */
	Button btnShow;
	Button btnClear;
	EditText edtInput;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("1111111", "savedInstanceState: "+ savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btnShow=(Button)findViewById(R.id.btnShow);//�ؼ�������
        btnClear=(Button)findViewById(R.id.btnClear);//�ؼ�������
        edtInput=(EditText)findViewById(R.id.edtInput);//�ؼ�������
        btnShow.setOnClickListener(new ClickListener());//ʹ�õ���¼�
        btnClear.setOnClickListener(new ClickListener());//ʹ�õ���¼�
    }
    
    
    class  ClickListener implements OnClickListener
    {
    	public void onClick(View v)
    	{
    		Log.d("1111111", "v: " + v);
    		if(v==btnShow)
    		{
    			Log.d("1111111", "btnShow: " + v);
    			new AlertDialog.Builder(HelloAndroid.this)
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
}