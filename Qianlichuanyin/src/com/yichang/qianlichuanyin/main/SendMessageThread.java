package com.yichang.qianlichuanyin.main;

import com.yichang.qianlichuanyin.net.Client;
import com.yichang.qianlichuanyin.net.ControlMessage;

public class SendMessageThread  implements Runnable{

	private Client client;
	private ControlMessage message;
	
	public SendMessageThread(Client client,ControlMessage message){
		this.client=client;
		this.message=message;
	}

	@Override
	public void run() {
		client.sendMessage(message);
	}
}
