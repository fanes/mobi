package edu.hrbeu.newsserver.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import edu.hrbeu.newsserver.dao.FavDAO;
import edu.hrbeu.newsserver.model.News;
import edu.hrbeu.newsserver.model.ResponseJson;

public class FavNews extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		String flag = request.getParameter("flag");
		ResponseJson json = new ResponseJson ();
		Gson gson = new Gson();
		
		if(flag!=null){
			if(flag.equalsIgnoreCase("newfav")){
				switch(newFav(Integer.parseInt(request.getParameter("userid")), Integer.parseInt(request.getParameter("newsid")))){
				case 1:{
					json.status = 403;
                    json.msg = "您已经收藏该新闻！";
				}
				case 0:{
					json.status = 404;
                    json.msg = "服务器未响应，请稍候重试！";
				} 
				case 2:{
					json.userid = Integer.parseInt(request.getParameter("userid"));
					json.status = 200;
                    json.msg = "收藏成功";
				}
				}
				String res = gson.toJson(json);
				response.getWriter().write(res);
				return;
			} else if(flag.equalsIgnoreCase("query")){
				ArrayList<News> al = queryFav(Integer.parseInt(request.getParameter("userid")));
				if(al.isEmpty()){
					json.status = 403;
					json.msg = "未查询到结果";
					
				}else{
					json.status = 200;
					json.msg = "查询成功";
					json.data = al;
				}
				String res = gson.toJson(json);
				response.getWriter().write(res);
				return;
				}
		}
	}
	
	private int newFav(int userid, int newsid){
		FavDAO fd = new FavDAO();
		int result = fd.addFav(userid, newsid);
		return result;
	}
	
	private ArrayList<News> queryFav(int userid){
		FavDAO fd = new FavDAO();
		ArrayList<News> list = fd.queryFavByUserIdReturnNewsList(userid);
		return list;
	}
}
