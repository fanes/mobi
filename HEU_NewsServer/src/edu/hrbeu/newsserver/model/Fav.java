package edu.hrbeu.newsserver.model;

public class Fav {
	private int FavID;
	private int UserID;
	private int NewsID;
	
	public int getFavID() {
		return FavID;
	}
	public void setFavID(int favID) {
		FavID = favID;
	}
	public int getUserID() {
		return UserID;
	}
	public void setUserID(int userID) {
		UserID = userID;
	}
	public int getNewsID() {
		return NewsID;
	}
	public void setNewsID(int newsID) {
		NewsID = newsID;
	}
	
}
