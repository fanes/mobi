package com.liuzhou.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;

public class PublishService {

	public boolean sendSms(String phoneNo, String message) {
		boolean result = false;
		String errorString = "Success";

		HttpURLConnection connection = null;
		BufferedReader br = null;
		String sendSmsAPIURLString = null;
		try {
			//活动[{eventName}]详情:[日期]{firstDateOfEvent}~{lastDateOfEvent},[时间]{startTimeOfEvent}~{endTimeOfEvent},[入场]{startTimeOfEntry}~,[场地]{eventPlace}
			//t3test {tenantName}提醒您,活动[{eventName}]时间变更:{firstDateOfEvent}~{lastDateOfEvent},{startTimeOfEvent}~{endTimeOfEvent}
			//验证码{verifyCodeForRegistMember},请继续完成您的操作
//(phoneNo, "t3test 活动:大胃王, 06月14日 ～ 06月19日 01:00~03:00,您的验证码是"+message+"，人民广场.");
			String smsMessage = URLEncoder.encode(message, "UTF-8");

			sendSmsAPIURLString = this.getSendSmsAPIURLString(phoneNo,
					smsMessage);
			URL sendSmsAPIURL = new URL(sendSmsAPIURLString);

			connection = (HttpURLConnection) sendSmsAPIURL.openConnection();
			connection.setRequestMethod("GET");

			InputStream is = sendSmsAPIURL.openStream();

			InputStreamReader isr = new InputStreamReader(is);
			br = new BufferedReader(isr);

			String resultString = br.readLine();
			
			System.out.println("*****return: "+ resultString);

		} catch (UnsupportedEncodingException e) {
			errorString = "encode not supported(encode: utf-8)";
		} catch (MalformedURLException e) {
			errorString = "illegal url: " + sendSmsAPIURLString;
		} catch (IOException e) {
			errorString = "failed to connect to TuiLiFang";
		} finally {
			System.out.println("888" + this.getClass() + errorString);
			// close stream
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (connection != null) {
				connection.disconnect();
			}
		}
		return result;
	}

	private String getSendSmsAPIURLString(String cell, String message) {
		String publisherAPIKey = "d260151820e352a679d320c93ca66a28";

		String publisherAPIURLTemplate = "http://www.tui3.com/api/send/?r=json&p=1&t={0}&c={1}&k={2}";

		String publisherAPIURL = MessageFormat.format(publisherAPIURLTemplate,
				cell, message, publisherAPIKey);

		return publisherAPIURL;
	}

}