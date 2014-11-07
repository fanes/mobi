package edu.hrbeu.newsserver.common;

import java.io.*;

import org.apache.commons.lang3.StringUtils;

public class HTMLOVRUtil {
	
	public HTMLOVRUtil(String OriginalFile,String FilePath,String FileName){
		 File file = new File(OriginalFile);
	     BufferedReader reader = null;
	     RandomAccessFile newFile = null;
	     String savePath = FilePath;
	     
	     try {
	    	 reader = new BufferedReader(new FileReader(file));
	    	 String tempString = null;
	    	 
	    	 if(StringUtils.isEmpty(savePath) || StringUtils.isBlank(savePath)){
				 return;
			 }
			 File directory = new File(savePath);
			 if(!directory.isDirectory()){
			    directory.mkdirs();
			 }
			   
			 int line_ini=1, line_span=1;
			 
	    	 while ((tempString = reader.readLine()) != null) {
	    		 if(tempString.indexOf("<div id=\"endText\">") >= 0)
	    			 break;
	    		 line_ini++;
	         }
	    	 while ((tempString = reader.readLine()) != null) {
	    		 if(tempString.indexOf("<!-- \u5206\u9875 -->") >= 0)
	    			 break;
	    		 line_span++;
	         }
	    	 newFile = new RandomAccessFile(savePath + FileName, "rw");
	    	 newFile.write("<html><head>".getBytes("utf-8"));
	    	 newFile.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />".getBytes("utf-8"));
	    	 newFile.write("</head><body>\r\n".getBytes("utf-8"));
	    	 for(int i=line_ini;i<=line_span+line_ini;i++){
	    		 String currentline = readAppointedLineNumber(file, i);
	    		 currentline += "\r\n";
				 newFile.write(currentline.getBytes("utf-8"));
	    	 }
	    	 newFile.write("</body></html>".getBytes("utf-8"));
	            reader.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	        }
	}

static String readAppointedLineNumber(File sourceFile, int lineNumber)
        throws IOException {
    FileReader in = new FileReader(sourceFile);
    LineNumberReader reader = new LineNumberReader(in);
    String s = "";
    String result = null;
    int lines = 0;
    while (s != null) {
        lines++;
        s = reader.readLine();
        if((lines - lineNumber) == 0) {
        	result = s;
        }
    }
    reader.close();
    in.close();
	return result;
}
}