package com.yichang.chuanyin.server;
/**
 * 记录用户的姓名等基本信息
 * @author Administrator
 *
 */
public class UserInfo {
    private int userId;
    private String userName;
    private String password;
    
    public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public UserInfo(int userId,String userName){
    	this.userId=userId;
    	this.userName=userName;
    }
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
