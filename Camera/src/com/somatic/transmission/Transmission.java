package com.somatic.transmission;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import com.somatic.common.Message;
import com.somatic.common.MessageType;


public class Transmission {

	public static Socket socket = null;;
	public static boolean isConnected = false;
	
	public Transmission(String ip)throws Exception{ 
		socket = new Socket(ip, 9999);

	}

	
	public void send(Object o) throws Exception{
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		oos.writeObject(o);
	}
	
	
 
}
