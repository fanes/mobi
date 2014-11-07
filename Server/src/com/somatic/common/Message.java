package com.somatic.common;

import java.io.Serializable;

public class Message implements Serializable{
	

	private int id;
	private String msg;
	private int ppt;
	
	public int getPpt() {
		return ppt;
	}
	public void setPpt(int ppt) {
		this.ppt = ppt;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
