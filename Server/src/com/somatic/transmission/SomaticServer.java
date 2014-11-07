package com.somatic.transmission;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.somatic.common.Message;
import com.somatic.common.MessageType;

public class SomaticServer extends JFrame implements ActionListener{
	Socket s = null;
	public static boolean isConnected = false;
	private JButton openBtn;
	private JButton takePicBtn;
	
	public static void main(String[] args) throws Exception{

		SomaticServer ss = new SomaticServer();
		ss.launchFrame();
		//ss.acceptData();
		
	}
	
	
	public void launchFrame(){
		openBtn = new JButton("开启服务器");
		takePicBtn = new JButton("拍照");
		JPanel jp = new JPanel();
		jp.setLayout(null);
		
		openBtn.setBounds(150, 10, 100, 25);
		takePicBtn.setBounds(150, 40, 100, 25);
		takePicBtn.setEnabled(false);
		
		openBtn.addActionListener(this);
		openBtn.setActionCommand("openBtn");
		takePicBtn.addActionListener(this);
		takePicBtn.setActionCommand("takePicBtn");
		
		jp.add(openBtn);
		jp.add(takePicBtn);
		
		this.add(jp);
		
		this.setSize(400, 300);		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	class MyThread extends Thread{
		SomaticServer ss;
		
		public MyThread(SomaticServer ss){
			this.ss = ss;
		}
		
		@Override
		public void run() {
			try {
				ss.acceptData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	public  void acceptData()throws Exception{			
		//在9999监听
		System.out.println("我是服务器，在11111监听");
		ServerSocket ss = new ServerSocket(9999);
		
		while(true) {
			s = ss.accept();
				
			System.out.println("一个Client连接上");
			
			ServerThread thread = new ServerThread(s);
			thread.start();
			
			
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("openBtn")){
			new MyThread(this).start();
			openBtn.setEnabled(false);
		} else {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				Message m = new Message();
				m.setId(MessageType.TAKE_PIC);
				oos.writeObject(m);
				System.out.println("已发送可以怕照");
				//takePicBtn.setEnabled(false);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	class ServerThread extends Thread{

		private Socket socket;
		
		public ServerThread(Socket socket){
			isConnected = true;
			this.socket = socket;
		}
		
		@Override
		public void run() {
			while(isConnected){
				try {
					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					Message msg = (Message)ois.readObject();
					
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					
					switch(msg.getId()){
					case MessageType.LINK:
						//返回一个成功登陆的信息报
						Message m = new Message();
						m.setId(MessageType.RECEIVED);
						oos.writeObject(m);
						System.out.println("连接上");
						break;
					case MessageType.TAKE_ACCESS:
						System.out.println("可以拍照");
						takePicBtn.setEnabled(true);
						break;
					}
				
				} catch (ClassNotFoundException e) {
					isConnected = false;
					e.printStackTrace();
				} catch (SocketException e){
					isConnected = false;
					e.printStackTrace();
				} catch (EOFException e){
					isConnected = false;
					//e.printStackTrace();
					
				} catch (IOException e) {
					isConnected = false;
					e.printStackTrace();
				}
			}
		}

		

	}
 

}
