package edu.hrbeu.newsserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.hrbeu.newsserver.common.JdbcUtil;
import edu.hrbeu.newsserver.model.News;

public class FavDAO {
	/**
	 * @param userid - Represent the current user.
	 * @param newsid - Represent the current news.
	 * @return 0 - Success processed.  1 - Data already exists in database.  2 - SQLException thrown.*/
	public int addFav(int userid, int newsid){
		Connection con = null;
		PreparedStatement stm = null;
		PreparedStatement stm0 = null;
		//设置初始返回值
		int result = 0;
		//建立数据库连接
		con = JdbcUtil.getConnection();
		try {
			//通过查询输入UserID的所有收藏新闻条目的NewsID，判断输入项对应的目标关系是否已经存在
			stm0 = con.prepareStatement("SELECT UserID, NewsID FROM favorite WHERE UserID = ?");
			stm0.setInt(1, userid);
			ResultSet rs = stm0.executeQuery();
			//若输入NewsID及UserID已有收藏关系，返回“1”，即数据条目已存在
			while(rs.next()){
				int rsnid = rs.getInt("NewsID");
				if(rsnid == newsid){
					result = 1;
					}
			} 
			//若输入项对应的目标关系不存存在，添加收藏关系
			if(result==0){
				stm = con.prepareStatement("INSERT INTO favorite (UserID, NewsID) VALUES (?, ?)");
				stm.setInt(1, userid);
				stm.setInt(2, newsid);
				stm.executeUpdate();
				}
				} catch (SQLException e) {
					e.printStackTrace();
					//若抛出异常，置返回结果为“2”，即抛出SQL异常
					result = 2;
				} finally {
					//关闭资源
					JdbcUtil.closeResource(null, stm, con);
				}
		return result;
	}
	/**
	 * @param userid - Represent the current user.
	 * @return A favorite News list of current user.
	 * */
	public ArrayList<News> queryFavByUserIdReturnNewsList(int userid){
		ArrayList<News> list = new ArrayList<News>();
		Connection con = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		
		try{
			con = JdbcUtil.getConnection();
			
			stm = con.prepareStatement("SELECT news.NewsID, news.Title, news.Abstract, news.DateTime, news.Provider, news.Category, news.StorageLoc, news.Link " +
					"FROM favorite, news " +
					"WHERE favorite.NewsID=news.NewsID AND favorite.UserID = ?");
			stm.setInt(1, userid);
			
			rs = stm.executeQuery();
			
			while(rs.next()){
				News news = new News();
				news.setNewsID(rs.getInt("NewsID"));
				news.setTitle(rs.getString("Title"));
				news.setAbstract(rs.getString("Abstract"));
				news.setDatetime(rs.getString("DateTime"));
				news.setStorageLoc(rs.getString("StorageLoc"));
				news.setLink(rs.getString("Link"));
				news.setProvider(rs.getString("Provider"));
				news.setCategory(rs.getString("Category"));
				list.add(news);
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}finally{
			JdbcUtil.closeResource(null, stm, con);
		}
		
		return list;
		
	}
}
