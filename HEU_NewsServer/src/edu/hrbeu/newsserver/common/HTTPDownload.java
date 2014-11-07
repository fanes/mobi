package edu.hrbeu.newsserver.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;

public class HTTPDownload {
	private String targetURL;
	private String savePath;
	private String fileName;
	
	public HTTPDownload(String targetURL,String savePath,String fileName){
		this.targetURL = targetURL;
		this.savePath = savePath;
		this.fileName = fileName;
		download();
		}
	
	private void download(){
		URL url = null;
		RandomAccessFile outfile = null;
		InputStream infile = null;
		URLConnection con = null;
		byte [] buffer = null;
		  
		try {
			url = new URL(targetURL);
		   
			
			//判断保存的路径是否存在
			if(StringUtils.isEmpty(savePath) || StringUtils.isBlank(savePath))
				return;
						   
			//创建文件目录
			File directory = new File(savePath);
			if(!directory.isDirectory())
				directory.mkdirs();
			
			//取文件并保存
			outfile = new RandomAccessFile(savePath + fileName, "rw");
			con = url.openConnection();
			infile = con.getInputStream();
			buffer = new byte[1024];
			int nStartPos=0;  
			int nRead=0;
			long nEndPos = con.getContentLength();
			while((nRead = infile.read(buffer, 0, 1024)) > 0 && nStartPos < nEndPos){
				outfile.write(buffer,0,nRead);
				nStartPos += nRead;  
		   }
		} catch (MalformedURLException e) {
		   e.printStackTrace();
		} catch (FileNotFoundException e) {
		   e.printStackTrace();
		} catch (IOException e) {
		   e.printStackTrace();
		} finally {
			try {
				outfile.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				outfile = null;
			}
			try {
				infile.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				infile = null;
			}
			con = null;
			buffer = null;
			url = null;
		}
	}
}
