package com.liuzhou.util;

public class MessageTest {

	public static void main(String[] args) {
		PublishService ps = new PublishService();
		ps.sendSms("13321829267", "验证码88888,请继续完成您的操作");

	}

}
