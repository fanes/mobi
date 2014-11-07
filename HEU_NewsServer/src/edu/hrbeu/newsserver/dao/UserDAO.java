package edu.hrbeu.newsserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.hrbeu.newsserver.common.JdbcUtil;
import edu.hrbeu.newsserver.model.User;


public class UserDAO {
	/**
	 * User Register Method.
	 * Not fully completed due to inability of transferring Sina Weibo's account. 
	 * @param user - A new User entity which needs to register.
	 * @return return true if success in inserting into database; return false if not.
	 * */
	public boolean UserReg(User user){
		Connection con = null;
		PreparedStatement stm0 = null;
		PreparedStatement stm = null;
		Boolean result = true;
		try {
			con = JdbcUtil.getConnection();
			
			stm0 = con.prepareStatement("SELECT Username FROM user WHERE Username = ?");
			stm0.setString(1, user.getUsername());
			ResultSet rs = stm0.executeQuery();
			if(rs.next()){
				result = false;
			}
			if(result){
				stm = con.prepareStatement("INSERT INTO user (Username,Password) VALUES (?,?)");
				stm.setString(1, user.getUsername());
				stm.setString(2, user.getPassword());
				//stm.setString(3, user.getWeiboID());
				
				stm.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			result = false;
		}finally{
			JdbcUtil.closeResource(null, stm, con);
		}
	return result;
	}
	/**
	 * Called when verify the account which is trying to login.
	 * @param name - The request username to be verified.
	 * @param password - The request password to be verified.
	 * @return Return UserID if verification successes; Return -1 if not.
	 * */
	public int verifyUserReturnID(String name, String password){

		Connection con = null;
		PreparedStatement stm = null;
		ResultSet rs = null;
		int rs_id = -1;
		con = JdbcUtil.getConnection();
				
		try {
			stm = con.prepareStatement("SELECT UserID FROM user WHERE Username = ? AND Password = ?");
			stm.setString(1, name);
			stm.setString(2, password);
			rs = stm.executeQuery();
			
			while(rs.next()){
				rs_id = rs.getInt("UserID");
			}
			
		}catch (SQLException e) {
			e.printStackTrace();
		}finally{
			JdbcUtil.closeResource(rs, stm, con);
		}
	return rs_id;
	}

}
