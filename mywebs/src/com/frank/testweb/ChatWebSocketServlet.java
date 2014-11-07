package com.frank.testweb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

public class ChatWebSocketServlet extends WebSocketServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 911879078000755859L;

	private final Map<Integer, WsOutbound> map = new HashMap<Integer, WsOutbound>();

	@Override
	protected StreamInbound createWebSocketInbound(String arg0, HttpServletRequest request) {
		String username = null;
		try {
			username = request.getParameter("username");
			if(username != null){
				username = new String(username.getBytes("ISO8859_1"),"UTF-8");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(username+"请求连接：");
		ChatMessageInbound cmi = new ChatMessageInbound(username);
		return cmi;
	}

	class ChatMessageInbound extends MessageInbound {
		private String userName = "匿名用户";
		
		public ChatMessageInbound(String userName) {
			if(userName != null && userName.length()>0){
				this.userName = userName;
			}
		}

		@Override
		protected void onOpen(WsOutbound outbound) {
			map.put(outbound.hashCode(), outbound);
			System.out.println("[上线]"+map.size());
			super.onOpen(outbound);
		}

		@Override
		protected void onClose(int status) {
			map.remove(getWsOutbound().hashCode());
			System.out.println("[下线]"+map.size());
			super.onClose(status);
		}

		@Override
		protected void onBinaryMessage(ByteBuffer buffer) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onTextMessage(CharBuffer buffer) throws IOException {

			String msg = buffer.toString();
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			msg = " <font color=green>"+userName+"  " + sdf.format(date) + "</font><br/> " + msg;
			broadcast(msg);
		}

		private void broadcast(String msg) {
			Set<Integer> set = map.keySet();
			for (Integer integer : set) {
				WsOutbound outbound = map.get(integer);
				CharBuffer buffer = CharBuffer.wrap(msg);
				try {
					outbound.writeTextMessage(buffer);
					outbound.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
