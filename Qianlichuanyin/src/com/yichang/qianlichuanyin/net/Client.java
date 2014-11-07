package com.yichang.qianlichuanyin.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yichang.qianlichuanyin.main.ActivityHandler;
import com.yichang.qianlichuanyin.main.Const;
import com.yichang.qianlichuanyin.main.FirstActivity;
import com.yichang.qianlichuanyin.main.MainActivity;
import com.yichang.qianlichuanyin.main.R;
import com.yichang.qianlichuanyin.main.YuyinActivity;

/*
 *服务器端
 */
public class Client implements Runnable {

	private boolean ifConnecting;
	private Gson gson = new Gson();
	private ActivityHandler activityHandler;

	String socketType = Const.useTCP; // 使用的通讯类型，UDP还是TCP
	Socket so;
	ObjectInputStream in;
	ObjectOutputStream out;
	private ArrayList<UserInfo> userInfoList = new ArrayList<UserInfo>();

	private UserInfo myself;
	
	/*保存即将要播放声音包的lingkedList，线程播放时播放这里的全部声音包
	 * 这个linkList保存的声音包数量由Const.soundPakNum设定
	 */
	private LinkedList<byte[]> trackingSoundList;  
	//保存接收到但尚未准备播放的声音包的lingkedList
	private LinkedList<byte[]> buffSoundList;  
	private boolean ifTracking =false;  //用来跟AudioTrackThread线程沟通，为true说明正在播放

	public Client() {
		ifConnecting = false; // 表明未与服务器处于连接状态
		activityHandler = new ActivityHandler();
		
		trackingSoundList=new LinkedList<byte[]>();
		buffSoundList=new LinkedList<byte[]>();
	}

	public void connect() {

		// 创建客户端Socket并创建数据流,把UDP端口信息发给服务器
		ControlMessage mesg;
		UserInfo user;
		try {
			InetAddress addr = InetAddress.getByName(Const.ServerIP);
			so = new Socket(addr, Const.ServerPort);
			in = new ObjectInputStream(new BufferedInputStream(
					so.getInputStream()));
			out = new ObjectOutputStream(so.getOutputStream());
			Log.v("test", "已连接服务器");

			// 由于刚开始连接，用户还没有自己的id，所以发送源id和目标id都不写
//			mesg = new ControlMessage(Const.Login, "", 0, 0);
//			sendMessage(mesg);
//			Log.v("test", "发送登陆消息完毕");
			ifConnecting = true;
		} catch (IOException ex) {
			Logger.getLogger(Client.class.getName())
					.log(Level.SEVERE, null, ex);
			Log.v("test", "无法连接服务器");
			ifConnecting = false;
		}
	}

	/**
	 * 增加一条支线程
	 */
	public void run() {
		ControlMessage controlmsg;
		Message handMsg;
		UserInfo userInfo;
		int fromUserId;
		String jsonStr;
		String mesgVal;
		Log.v("test", "进入run方法");
		try {
			while (true) {
				Thread.sleep(50);
				if (in != null) {
					// 从接受的字符串还原出
					jsonStr = in.readUTF();
					//Log.v("test", "接收到的字符串为" );
					controlmsg = gson.fromJson(jsonStr, ControlMessage.class);
					mesgVal = controlmsg.getMesgVaL();
					fromUserId = controlmsg.getFromUserId();
					if (controlmsg.getType() == Const.textMessage) {

					} else if (controlmsg.getType() == Const.soundMessage) {// 接收到声音信息
						//Log.v("test", "接收到声音信息");
						byte[] soundBytes=gson.fromJson(mesgVal, byte[].class);
						
						//如果正在播放，或者播放列表的声音包已经够多了，就直接加到缓冲区
						if(ifTracking || trackingSoundList.size()>=Const.soundPakNum){
							buffSoundList.add(soundBytes);
						}else{
							//如果缓冲区里完全没有声音包则不必担心顺序掉乱，直接加入待播放列表中即可。
							if(buffSoundList.size()==0){
								trackingSoundList.add(soundBytes);
							}else{
								trackingSoundList.addAll(buffSoundList);
								buffSoundList.clear();
								trackingSoundList.add(soundBytes);
							}
						}

						//没有正在播放且数目足够就发送消息给主线程，通知其播放
						if(!ifTracking&&trackingSoundList.size()>=Const.soundPakNum){
							ifTracking=true;
						    handMsg=new Message();
							handMsg.what=Const.handlerTrack;
							sendHandlerMessage(handMsg);
						}
						
					} else if (controlmsg.getType() == Const.requestTalk) {// 对方请求谈话
						//将请求对话的用户id作为参数发送过去
						Bundle data=new Bundle();
						data.putInt("toUserId", controlmsg.getFromUserId());
						Log.v("test", "接收到请求对话");
						
					    handMsg=new Message();
						handMsg.what=Const.handlerRequstTall;
						handMsg.setData(data);
						sendHandlerMessage(handMsg);
						
					} else if (controlmsg.getType() == Const.refuseTalk) {// 对方拒绝谈话请求
						
					    handMsg=new Message();
						handMsg.what=Const.handlerRefuseTall;
						sendHandlerMessage(handMsg);
						
					} else if (controlmsg.getType() == Const.promiseTalk) {// 接受谈话请求
						
						Log.v("test", "接收到同意通话的请求");
					    handMsg=new Message();
						handMsg.what=Const.handlerPromiseTall;
						sendHandlerMessage(handMsg);
						
					}else if (controlmsg.getType() == Const.stopTalk) {// 结束谈话
						
					    handMsg=new Message();
						handMsg.what=Const.handlerStopTall;
						sendHandlerMessage(handMsg);
						
					} else if (controlmsg.getType() == Const.addUser) {// 增加一个用户，加入列表，界面也增加一个人

						userInfo = gson.fromJson(mesgVal, UserInfo.class);
						addUser(userInfo);

					} else if (controlmsg.getType() == Const.removeUser) {// 有用户注销登录

						userInfo = gson.fromJson(mesgVal, UserInfo.class);
						removeUser(userInfo.getUserId());

					} else if (controlmsg.getType() == Const.userInfoList) {// 初始化用户列表
						Log.v("test", "接收初始化列表消息" + mesgVal);

						ArrayList<UserInfo> userInfoListTemp;
						Type type = new TypeToken<ArrayList<UserInfo>>() {
						}.getType();
						userInfoListTemp = gson.fromJson(mesgVal, type);// userInfoListTemp.getClass());
						addUser(userInfoListTemp);

						Log.v("test", "初始化用户列表完毕");
					} else if (controlmsg.getType() == Const.userInfo) {// 获取自己的名称并保存信息
						Log.v("test", "接收用户自己的信息" + mesgVal);

						userInfo = gson.fromJson(mesgVal, UserInfo.class);
						myself = userInfo;
						addUser(userInfo);
						
						//接收到用户信息说明登陆成功，
					    handMsg =new Message();
						handMsg.what=Const.handlerLogin_success;
						sendHandlerMessage(handMsg);

					}else if (controlmsg.getType() == Const.Login_Fail) {// 登录失败
						Log.v("test", "接收用户自己的信息" + mesgVal);

						userInfo = gson.fromJson(mesgVal, UserInfo.class);
						myself = userInfo;
//						addUser(userInfo);
						
						//登陆失败，刷新界面
					    handMsg =new Message();
						handMsg.what=Const.handlerLogin_Fail;
						sendHandlerMessage(handMsg);

					} else if (controlmsg.getType() == Const.Logout) {
						in.close();
						in = null;
						out.close();
						out = null;
						socketType = Const.useUDP;
						Log.v("test", "已和服务器断开连接");
					}
				} else {
					in.close();
					in = null;
					out.close();
					out = null;

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param mesg
	 */
	public void sendMessage(ControlMessage mesg) {
		String jsonStr;

		// 设定发出者的id，即是自己的id
		if (myself != null) {
			mesg.setFromUserId(myself.getUserId());
		}

		jsonStr = gson.toJson(mesg);

		Log.v("test", "准备发送数据,type"+mesg.getType());

		// 连接仍保持则发
		if (in != null && out != null) {
			try {
				// 向服务器端端发送信息
				// byte[] buf=jsonStr.getBytes("UTF-8");
				// out.write(buf);
				out.writeUTF(jsonStr);
				out.flush();
				Log.v("test", "发送数据完毕");
			} catch (Exception ex) {
				Logger.getLogger(Client.class.getName()).log(Level.SEVERE,
						null, ex);
			}
		} else {
			// 提示
			Log.v("test", "已和服务器断开连接");
			reConnect();
		}

	}

	/**
	 * 设置目前正在运行的activity，以便通过接口和handler通知主线程更改界面
	 */
	public void setRunningActivity(MainActivity mainActivity) {
		activityHandler.setMainActivity(mainActivity);
	}

	/**
	 * 通过接口发送消息队列通知主线程更改界面
	 */
	public void sendHandlerMessage(Message msg) {
		activityHandler.sendMessage(msg);
	}

	// 程序关闭时发送消息给服务器注销登录
	private void appClosing() {
		ControlMessage mesg;
		mesg = new ControlMessage(Const.Logout, "", myself.getUserId(), 0);
		sendMessage(mesg);
	}

	private void reConnect() {
		// 重连
		connect();
	}

	// 根据用户名id找到User对象
	public UserInfo findUserInfo(int userId) {
		UserInfo userInfo = null;
		for (int i = 0; i < userInfoList.size(); i++) {
			userInfo = (UserInfo) userInfoList.get(i);
			if (userId == userInfo.getUserId()) {
				return userInfo;
			}
		}
		return userInfo;
	}

	/**
	 * 增加一个用户
	 * 
	 * @param userInfo
	 */
	private void addUser(UserInfo userInfo) {
		userInfoList.add(userInfo);

		// 通知主线程增加了用户，更新用户列表
		Message msg;
		msg = new Message();
		msg.what = Const.handlerAddUser;
		sendHandlerMessage(msg);

		// 开始添加user到列表中
		// String username = userInfo.getUserName();
	}

	/**
	 * 初始化整个用户列表
	 * 
	 * @param userInfoList
	 */
	private void addUser(ArrayList<UserInfo> userInfoList) {
		UserInfo userInfo;
		// this.userInfoList = userInfoList;
		String userName;
		for (int i = 0; i < userInfoList.size(); i++) {
			// 逐个添加到客户端的列表中去
			userInfo = userInfoList.get(i);
			this.userInfoList.add(userInfo);
			// userName = userInfo.getUserName();
		}

		// 通知主线程增加了用户，更新用户列表
		Message msg;
		msg = new Message();
		msg.what = Const.handlerAddUser;
		sendHandlerMessage(msg);
	}

	/**
	 * 删除一个用户
	 * 
	 * @param userId
	 */
	public void removeUser(int userId) {
		UserInfo userInfo;
		for (int i = 0; i < userInfoList.size(); i++) {
			userInfo = userInfoList.get(i);
			if (userInfo.getUserId() == userId) {
				userInfoList.remove(userInfo);
				break;
			}
		}
		// 通知主线程删除了用户，更新用户列表
		Message msg;
		msg = new Message();
		msg.what = Const.handlerReduceUser;
		sendHandlerMessage(msg);

	}
	
	public UserInfo getMyself() {
		return myself;
	}
	
	/**
	 * 登陆方法
	 */
	public void login(){
	}
	


	public boolean isIfConnecting() {
		return ifConnecting;
	}

	public void setIfConnecting(boolean ifConnecting) {
		this.ifConnecting = ifConnecting;
	}

	public ArrayList<String> getUserNameList() {
		ArrayList<String> userNameList = new ArrayList<String>();

		for (UserInfo userInfo : userInfoList) {
			userNameList.add(userInfo.getUserName());
		}

		return userNameList;
	}

	public ArrayList<UserInfo> getUserInfoList() {
		return userInfoList;
	}
	


	public void setUserInfoList(ArrayList<UserInfo> userInfoList) {
		this.userInfoList = userInfoList;
	}
	
	public LinkedList<byte[]> getTrackingSoundList() {
		return trackingSoundList;
	}
	
	public void setIfTracking(boolean ifTracking) {
		this.ifTracking = ifTracking;
	}
	
	/**
	 * 根据userId获取UserInfo对象的方法
	 * @param userId
	 * @return
	 */
	public UserInfo getUserInfoById(int userId){
		UserInfo userInfo=null;
		for(UserInfo userInfoTemp:userInfoList){
			if(userInfoTemp.getUserId()==userId){
				userInfo=userInfoTemp;
			}
		}
	
		return userInfo;
	}
}
