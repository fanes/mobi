package com.example.test;

import android.app.Activity;
import android.os.Bundle;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		System.out.println("开始连接websocket///");
		final WebSocketConnection wsc = new WebSocketConnection();
		try {
			
			wsc.connect("ws://192.168.0.109:8080/EmailService/chat.ws?username=ws-android-client", new WebSocketConnectionHandler(){

				@Override
				public void onBinaryMessage(byte[] payload) {
					System.out.println("onBinaryMessage size="+payload.length);
				}

				@Override
				public void onClose(int code, String reason) {
					System.out.println("onClose reason="+reason);
				}

				@Override
				public void onOpen() {
					System.out.println("onOpen");
					wsc.sendTextMessage("Hello!");
					wsc.disconnect();
				}

				@Override
				public void onRawTextMessage(byte[] payload) {
					System.out.println("onRawTextMessage size="+payload.length);
				}

				@Override
				public void onTextMessage(String payload) {
					System.out.println("onTextMessage"+payload);
				}
			});
			
			wsc.sendTextMessage("mypayload");
		} catch (WebSocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
