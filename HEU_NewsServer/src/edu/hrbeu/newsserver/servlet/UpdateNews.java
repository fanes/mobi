package edu.hrbeu.newsserver.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;
import org.dom4j.Node;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.hrbeu.newsserver.common.HTMLOVRUtil;
import edu.hrbeu.newsserver.common.HTTPDownload;
import edu.hrbeu.newsserver.common.XmlUtil;
import edu.hrbeu.newsserver.dao.NewsDAO;
import edu.hrbeu.newsserver.model.Constant;
import edu.hrbeu.newsserver.model.News;

public class UpdateNews extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		//每次更新数据库前清空原有表的数据
		//清空后用户评论会丢失
		//不清空新闻出现重复数据
		//V1.0版本不考虑重复数据问题，以Title确定ID
		/*Connection con = null;
		Statement stm = null;
		
		try {
			con = JdbcUtil.getConnection();
			stm = con.createStatement();
			stm.executeUpdate("TRUNCATE TABLE news");
		}catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.closeResource(null, stm, con);
		}*/
		
		XmlUtil xu = new XmlUtil();
		URL url;
		News news = new News();
		String regEx_ini = "[\\u4e00-\\u9fa5]";
		Pattern p_ini = Pattern.compile(regEx_ini);
		Matcher m_ini;
		String savePath = "\\";
		ArrayList<String> urls = new ArrayList<String>();
		
		//头条新闻
		urls.add("http://news.163.com/special/00011K6L/rss_newstop.xml");
		//国内新闻
		urls.add("http://news.163.com/special/00011K6L/rss_gn.xml");
		//国际新闻
		urls.add("http://news.163.com/special/00011K6L/rss_gj.xml");
		//社会新闻
		urls.add("http://news.163.com/special/00011K6L/rss_sh.xml");
		/*//军事新闻
		urls.add("http://news.163.com/special/00011K6L/rss_war.xml");
		//深度新闻
		urls.add("http://news.163.com/special/00011K6L/rss_hotnews.xml");
		//探索新闻
		urls.add("http://news.163.com/special/00011K6L/rss_discovery.xml");*/
		
		//遍历各类别新闻RSS列表，并解析对应XML
		Iterator<String> urllist = urls.iterator();
		NewsDAO ud = new NewsDAO();
		while(urllist.hasNext()){
			try {
				url = new URL((String)urllist.next());
				List list = xu.getXmlInfo(Constant.RSS_DOM_CHILRDEN_ROOT, url);
				for (Iterator iter = list.iterator(); iter.hasNext();) {
					Element element = (Element) iter.next();
					
					Node abs = element.selectSingleNode(Constant.RSS_DOM_CHILRDEN_ROOT_DESCRIPTION);
					m_ini = p_ini.matcher(abs.getText().toString());
					
					int abs_ini = 0;
					if(m_ini.find())
						abs_ini = abs.getText().toString().indexOf(m_ini.group());					
					String abs_sub = abs.getText().toString().substring(abs_ini, abs.getText().indexOf("......")+6);
					
					if(abs_sub.equals("......")){ 
						continue;
					}
					Node time=element.selectSingleNode(Constant.RSS_DOM_CHILRDEN_ROOT_PUBDATE);
					Node node = element.selectSingleNode(Constant.RSS_DOM_CHILRDEN_ROOT_TITLE);				
					Node link = element.selectSingleNode(Constant.RSS_DOM_CHILRDEN_ROOT_LINK);
									
					news.setTitle(node.getText());
					news.setAbstract(abs_sub.substring(0,30)+"...");
					news.setDatetime(time.getText());				
					news.setLink(link.getText());
					if(url.toString().indexOf("top")!=-1){
						news.setCategory("头条新闻");
						news.setProvider("网易头条");
						}
					else if(url.toString().indexOf("gn")!=-1){
						news.setCategory("国内新闻");
						news.setProvider("网易国内");
						}
					else if(url.toString().indexOf("gj")!=-1){
						news.setCategory("国际新闻");
						news.setProvider("网易国际");
						}
					else if(url.toString().indexOf("sh")!=-1){
						news.setCategory("社会新闻");
						news.setProvider("网易社会");
						}
					else if(url.toString().indexOf("war")!=-1){
						news.setCategory("军事新闻");
						news.setProvider("网易军事");
						}
					else if(url.toString().indexOf("hot")!=-1){
						news.setCategory("深度新闻");
						news.setProvider("网易深度");
						}
					else if(url.toString().indexOf("dis")!=-1){
						news.setCategory("探索新闻");
						news.setProvider("网易探索");
						}
					
					
					//将该条新闻对应HTML页面缓存至服务器
					new HTTPDownload(news.getLink(),savePath,"NewsCache");
					//更新数据库
					ud.updateNews(news);
					//将缓存页面内新闻部分重新制成HTML页面，保存至服务器WebRoot\\NewsDetails\\文件夹下
					new HTMLOVRUtil("\\NewsCache", request.getSession().getServletContext().getRealPath("")+"\\NewsDetails\\", ud.getNewsIDByTitle(news.getTitle())+".html");
					//补充更新StorageLoc
					ud.updateStorageLocByID("http://10.0.2.2:8080/HEU_NewsServer/NewsDetails/"+ud.getNewsIDByTitle(news.getTitle())+".html", ud.getNewsIDByTitle(news.getTitle()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//将数据库内新闻按类别以xml形式存储至服务器根目录
		String[] cate = {"头条新闻","国内新闻","国际新闻","社会新闻"};
		for(int i = 0;i <= 3;i ++ ){
			ArrayList<News> list = ud.getNewsByCategory(cate[i]);
			xu.generateXML(list, cate[i], request.getSession().getServletContext().getRealPath("")+"\\");
		}
		
		RequestDispatcher rd = request.getRequestDispatcher("/index.jsp");
		rd.forward(request, response);
	}
}
