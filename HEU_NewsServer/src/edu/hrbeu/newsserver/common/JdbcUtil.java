package edu.hrbeu.newsserver.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcUtil {
	public static Connection getConnection(){
		Connection con = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/heu_newsserver?useUnicode=true&amp;characterEncoding=utf8","root","root");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;
	}
	public static void closeResource(ResultSet rs, Statement stm ,Connection con){
		try {
		if(rs!=null)	{rs.close();	}
		if(stm!=null)	{stm.close();	}
		if(con!=null)	{con.close();	}
		} catch (SQLException e) {
			e.printStackTrace();
		}}
}
