package com.example.mytext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	EditText textBox;
	static final int READ_BLOCK_SIZE = 100;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textBox = (EditText) findViewById(R.id.txtText1);

	}

	public void onClickSave(View view) {
		String str = textBox.getText().toString();
		try
		{
			FileOutputStream fOut =
					openFileOutput("textfile.txt",
							MODE_WORLD_READABLE);
                        
			OutputStreamWriter osw = new
					OutputStreamWriter(fOut);

			//---write the string to the file---
			osw.write(str);
			osw.flush(); 
			osw.close();

			//---display file saved message---
			Toast.makeText(getBaseContext(),
					"文件保存成功!",
					Toast.LENGTH_SHORT).show();

			//---clears the EditText---
			textBox.setText("请输入");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	public void onClickLoad(View view) {
		try
		{
			File fileDir = this.getFilesDir();
			File[] files = fileDir.listFiles();
			
			Log.d("mydebug", "files Dir: " + fileDir.toString());
			Log.d("mydebug", "files count: " + files.length);
			Log.d("mydebug", "files path: " + files[0].getPath()+", name:"+files[0].getName());
			Log.d("mydebug", "files can read: " + files[0].canRead());
			Log.d("mydebug", "files can write: " + files[0].canWrite());

			FileInputStream fIn = 
					openFileInput("textfile.txt");
			InputStreamReader isr = new 
					InputStreamReader(fIn);
            
			char[] inputBuffer = new char[READ_BLOCK_SIZE];
			String s = "";

			int charRead;
			while ((charRead = isr.read(inputBuffer))>0)
			{
				//---convert the chars to a String---
				String readString =
						String.copyValueOf(inputBuffer, 0,
								charRead);
				s += readString;

				inputBuffer = new char[READ_BLOCK_SIZE];
			}
			//---set the EditText to the text that has been 
			// read---
			textBox.setText(s);

			Toast.makeText(getBaseContext(),
					"成功读取文件!",
					Toast.LENGTH_SHORT).show();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

}