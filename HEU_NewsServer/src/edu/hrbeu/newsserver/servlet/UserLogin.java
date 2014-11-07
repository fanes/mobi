package edu.hrbeu.newsserver.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import edu.hrbeu.newsserver.dao.UserDAO;
import edu.hrbeu.newsserver.model.ResponseJson;

public class UserLogin extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public UserLogin() {
		super();
	}


	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		ResponseJson json = new ResponseJson ();
		Gson gson = new Gson();
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		UserDAO ud = new UserDAO();
		int ver_rs = ud.verifyUserReturnID(username ,password);
		
		if(ver_rs!=-1){
			json.status = 200;
			json.userid = ver_rs;
			json.msg = "登录成功";
		}
		else{
			json.status = 403;
			json.msg = "登录失败，请重试";
		}
		String res = gson.toJson(json);
		response.getWriter().write(res);
		return;
	}

}
