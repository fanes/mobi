package cn.edu.hrbeu.heu_newsclient.model;

public class User {
	private int UserID;
	private String Username;
	private String Password;
	private String WeiboID;
	public int getUserID() {
		return UserID;
	}
	public void setUserID(int userID) {
		UserID = userID;
	}
	public String getUsername() {
		return Username;
	}
	public void setUsername(String username) {
		Username = username;
	}
	public String getPassword() {
		return Password;
	}
	public void setPassword(String password) {
		Password = password;
	}
	public String getWeiboID() {
		return WeiboID;
	}
	public void setWeiboID(String weiboID) {
		WeiboID = weiboID;
	}
	
}
