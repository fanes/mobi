package edu.hrbeu.newsserver.dao;

import java.sql.*;
import java.util.ArrayList;

import edu.hrbeu.newsserver.common.JdbcUtil;
import edu.hrbeu.newsserver.model.News;

public class NewsDAO {
	/**
	 * @param news - A new News Entity that shall be inserted into database*/
	public void updateNews(News news){
		Connection con = null;
		PreparedStatement stm = null;
		
		try {
			con = JdbcUtil.getConnection();
			stm = con.prepareStatement("INSERT INTO news (Title, Category, Abstract, DateTime, StorageLoc, Link, Provider) VALUES (?, ?, ?, ?, ?, ?, ?)");
			stm.setString(1, news.getTitle());
			stm.setString(2, news.getCategory());
			stm.setString(3, news.getAbstract());
			stm.setString(4, news.getDatetime());
			stm.setString(5, news.getStorageLoc());
			stm.setString(6, news.getLink());
			stm.setString(7, news.getProvider());
			
			stm.executeUpdate();
			
		}catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.closeResource(null, stm, con);
		}
	}
	/**
	 * @param title - Represent current the title of news
	 * @return Corresponding NewsID*/
	public int getNewsIDByTitle(String title){
		Connection con = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		int result = 0;
		try {
			con = JdbcUtil.getConnection();
			stm = con.prepareStatement("SELECT NewsID FROM news WHERE Title=?");
			stm.setString(1, title);
			rs = stm.executeQuery();
			while(rs.next()){
				result = rs.getInt("NewsID");
			}			
		}catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.closeResource(null, stm, con);
		}
		return result;
}
	/**
	 * @param Loc - The local storage location of the target news.
	 * @param ID - The NewsID of target news.*/
	public void updateStorageLocByID(String Loc, int ID){
		Connection con = null;
		PreparedStatement stm = null;
		
		try {
			con = JdbcUtil.getConnection();
			stm = con.prepareStatement("UPDATE news SET StorageLoc = ? WHERE NewsID= ?");
			stm.setString(1, Loc);
			stm.setInt(2, ID);
			
			stm.executeUpdate();
			
		}catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.closeResource(null, stm, con);
		}
	}
	
	/**
	 * @param Category - Target category of news.
	 * @return A News list of a certain category.*/
	public ArrayList<News> getNewsByCategory(String Category){
		Connection con = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		ArrayList<News> result = new ArrayList<News>();
		try {
			con = JdbcUtil.getConnection();
			stm = con.prepareStatement("SELECT NewsID, Title, Category, Abstract, Provider, DateTime, StorageLoc, Link FROM news WHERE Category = ?");
			stm.setString(1, Category);
			
			rs = stm.executeQuery();
			
			while(rs.next()){
				News news = new News();
				news.setNewsID(rs.getInt("NewsID"));
				news.setTitle(rs.getString("Title"));
				news.setCategory(rs.getString("Category"));
				news.setAbstract(rs.getString("Abstract"));
				news.setProvider(rs.getString("Provider"));
				news.setDatetime(rs.getString("DateTime"));
				news.setStorageLoc(rs.getString("StorageLoc"));
				news.setLink(rs.getString("Link"));
				
				result.add(news);
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
}
