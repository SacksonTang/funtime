package com.rzyou.funtime.common.sms.ouyi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;


public class HttpClient {
	//定义常量
	//dc 数据类型
	private static final int DATACODING = 15; //定死
	//rf 响应格式
	private static final int REPSPONSEFORMAT = 2; //定死
	//rd 是否需要状态报告
	private static final int REPORTDATA = 1; //定死
	//tf 短信内容的传输编码
	private static final int TRANSFERENCODING = 3; //定死
	//serviceaddress service端地址 //变量可根据实际情况改变
	public static String serviceaddress = "http://61.129.57.37:7891/";
	
	public static String SendPOST(String function, Map<String, Object> paramMap) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(serviceaddress+function);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// conn.setRequestProperty("Charset", "UTF-8");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			paramMap.put("dc", DATACODING);
			paramMap.put("rf", REPSPONSEFORMAT);
			paramMap.put("rd", REPORTDATA);
			paramMap.put("tf", TRANSFERENCODING);
			// 设置请求属性
			String param = "";
			if (paramMap != null && paramMap.size() > 0) {
				Iterator<String> ite = paramMap.keySet().iterator();
				while (ite.hasNext()) {
					String key = ite.next();// key
					Object value = paramMap.get(key);
					param += key + "=" + value + "&";
				}
				param = param.substring(0, param.length() - 1);
			}

			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
	
	

};
