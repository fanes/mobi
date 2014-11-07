package edu.hrbeu.newsserver.common;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.hrbeu.newsserver.model.News;

public class XmlUtil {

	public Document parse(URL url) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(url);
		return document;
	}

	public List getXmlInfo(String path, URL url) {
		List info = new ArrayList();
		try {
			Document document = parse(url);
			info = document.selectNodes(path);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return info;
	}

	public Element getFirstNodeTitle(String path, URL url) {
		List list = getXmlInfo(path, url);
		Element element = (Element) list.get(0);
		return element;
	}

	public void generateXML(ArrayList<News> list, String Category, String SavePath){
		RandomAccessFile XML = null;
	    //若无存储位置则直接返回
		if(StringUtils.isEmpty(SavePath) || StringUtils.isBlank(SavePath)){
			 return;
		 }
		File directory = new File(SavePath);
		//若指定存储目录不存在，则创建目录
		if(!directory.isDirectory()){
			directory.mkdirs();
		 }
		//按类别生成XML文件
		String Cate;
		if(Category.equals("头条新闻"))
			Cate = "NewsTop";
		else if(Category.equals("国内新闻"))
			Cate = "NationalNews";
		else if(Category.equals("国际新闻"))
			Cate = "InternationalNews";
		else 
			Cate = "Society";
		try {
			//写入标准XML头
			XML = new RandomAccessFile(SavePath + Cate + ".xml", "rw");
			XML.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "\r\n").getBytes("utf-8"));
			//根结点
			XML.write(("<root>"+"\r\n").getBytes("utf-8"));
		} catch (UnsupportedEncodingException exception) {
			exception.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		for(News news:list){
			//写入每个新闻结点信息
			try {
				XML.write(("<news id=\""+news.getNewsID()+"\">"+"\r\n").toString().getBytes("utf-8"));
				XML.write(("<id>"+news.getNewsID()+"</id>"+"\r\n").toString().getBytes("utf-8"));
				XML.write(("<title>"+news.getTitle()+"</title>"+"\r\n").getBytes("utf-8"));
				XML.write(("<category>"+news.getCategory()+"</category>"+"\r\n").getBytes("utf-8"));
				XML.write(("<abstract>"+news.getAbstract()+"</abstract>"+"\r\n").getBytes("utf-8"));
				XML.write(("<provider>"+news.getProvider()+"</provider>"+"\r\n").getBytes("utf-8"));
				XML.write(("<pubtime>"+news.getDatetime()+"</pubtime>"+"\r\n").getBytes("utf-8"));
				XML.write(("<link>"+news.getStorageLoc()+"</link>"+"\r\n").getBytes("utf-8"));
				XML.write(("<origlink>"+news.getLink()+"</origlink>"+"\r\n").getBytes("utf-8"));
				XML.write("</news> \r\n".getBytes("utf-8"));
			}catch (IOException e) {
	            e.printStackTrace();
	        } 
		}
		try {			
			XML.write("</root> \r\n".getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
