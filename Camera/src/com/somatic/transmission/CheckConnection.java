package com.somatic.transmission;

import java.io.ObjectInputStream;

import com.somatic.common.Message;
import com.somatic.common.MessageType;

public class CheckConnection extends Transmission {
	
	private ObjectInputStream ois;
	
	public CheckConnection(String ip) throws Exception {
		super(ip);
	}
	
	/** 发送: "link" */
	public void sendCheck() throws Exception{
		Message m = new Message();
		m.setId(MessageType.LINK);
		m.setMsg("HelloWorld");
		send(m);
	}
	
	public void sendAccess() throws Exception{
		Message m = new Message();
		m.setId(MessageType.TAKE_ACCESS);
		send(m);
		ois = new ObjectInputStream(socket.getInputStream());
	}
	
	/** 等待接收："receiped"，成功返回true，否则反回false */
	public boolean receiveCheck() throws Exception{
		boolean isOk = false;
		Message m;
		ois = new ObjectInputStream(socket.getInputStream());
		socket.setSoTimeout(2000);
		m = (Message)ois.readObject();
		isOk = (m.getId() == MessageType.RECEIVED);
	
		return isOk;
	}
	
	public boolean receiveTake() throws Exception{
		boolean isOk = false;
		Message m;
		ois = new ObjectInputStream(socket.getInputStream());
		m = (Message)ois.readObject();
		isOk = (m.getId() == MessageType.TAKE_PIC);
	
		return isOk;
	}
	
	public boolean isConnectionOK() throws Exception{
		boolean result = false;
		sendCheck();
		result = receiveCheck();
		return result;
	}
	
	public boolean isTakePicOK() throws Exception{
		boolean result = false;
		sendAccess();
		result = receiveTake();
		return result;
	}
}