package edu.hrbeu.newsserver.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import edu.hrbeu.newsserver.dao.UserDAO;
import edu.hrbeu.newsserver.model.ResponseJson;
import edu.hrbeu.newsserver.model.User;

public class UserReg extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request,response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		//获取request中的各项参数
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		//String weiboid = request.getParameter("weibo");
		
		//将参数转化为User实体对象
		User newuser = new User();
		newuser.setUsername(username);
		newuser.setPassword(password);
		//newuser.setWeiboID(weiboid);
		
		//进行数据库操作
		UserDAO ud = new UserDAO();
		boolean result = ud.UserReg(newuser);
		ResponseJson json = new ResponseJson();
		Gson gson = new Gson();
		//设置返回信息
		if(result==true){
			json.status = 200;
			json.msg = "注册成功，请您登录。";
		}else{
			json.status = 403;
			json.msg = "注册失败，请重试！";
		}
		String res = gson.toJson(json);
		response.getWriter().write(res);
		return;
	}

}
