package com.secusoft.web.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLHandshakeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于HttpClient4.5封装的一个HttpClient线程池
 * ForeverChen
 *
 */
@Service
public class MyHttpClientPool {
	private static final Logger log = LoggerFactory.getLogger(MyHttpClientPool.class);
	private volatile static MyHttpClientPool HttpClientConnectionPool;
	//	public static final int MAX_TOTAL_CONNECTIONS = 50;
//	public static final int MAX_ROUTE_CONNECTIONS = 20;
//	public static final int CONNECT_TIMEOUT = 3000; // 连接时间
//	public static final int SOCKET_TIMEOUT = 5000; // 获取内容时间
	public static final int MAX_TOTAL_CONNECTIONS = 50;
	public static final int MAX_ROUTE_CONNECTIONS = 20;
	public static final int CONNECT_TIMEOUT = 3000; // 连接时间
	public static final int SOCKET_TIMEOUT = 10000; // 获取内容时间
	private static PoolingHttpClientConnectionManager cm = null;
	private static CloseableHttpClient httpclient;

	/**
	 * 初始化连接池
	 */
	static{
		try {
			cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);
			cm.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);// 默认设置为2

			// 客户端请求的默认设置
			RequestConfig defaultRequestConfig = RequestConfig.custom()
					.setSocketTimeout(SOCKET_TIMEOUT)
					.setConnectTimeout(CONNECT_TIMEOUT)
					.setConnectionRequestTimeout(CONNECT_TIMEOUT)
					.setRedirectsEnabled(false)
					.setCookieSpec(CookieSpecs.STANDARD_STRICT)
					.build();

			// 请求重试处理
			HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
				public boolean retryRequest(IOException exception,
											int executionCount, HttpContext context) {
					if (executionCount >= 2) {// 如果超过最大重试次数，那么就不要继续了
						return false;
					}

					if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
						return true;
					}

					if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
						return false;
					}
					HttpRequest request = (HttpRequest) context.getAttribute(HttpClientContext.HTTP_REQUEST);
					boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
					if (idempotent) {// 如果请求被认为是幂等的，那么就重试
						return true;
					}

					return false;
				}

			};

			httpclient = HttpClients.custom()
					.setConnectionManager(cm)
					.setDefaultRequestConfig(defaultRequestConfig)
					.setRetryHandler(httpRequestRetryHandler)
					.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private MyHttpClientPool(){}

	/**
	 * 获取MyHttpClientPool对象，这是单例方法
	 *
	 * @return
	 */
	public static MyHttpClientPool getClientConnectionPool() {
		if (HttpClientConnectionPool == null) {
			synchronized (MyHttpClientPool.class) {
				if (HttpClientConnectionPool == null) {
					HttpClientConnectionPool = new MyHttpClientPool();
				}
			}
		}
		return HttpClientConnectionPool;
	}

	/**
	 * 获取HttpClient。在获取之前，确保MyHttpClientPool对象已创建。
	 * @return
	 */
	public static CloseableHttpClient getHttpClient() {
		return httpclient;
	}

	/**
	 * 关闭整个连接池
	 */
	public static void close() {
		if (cm != null) {
			cm.shutdown();
		}
		if(httpclient != null){
			try {
				httpclient.close();
			} catch (IOException e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get方法封装，发get送请求，获取响应内容
	 */
	public static String fetchByGetMethod(String getUrl, Map<String, String> headMap) {
		String charset = null;
		HttpGet httpget = null;
		String html = null;
		try {
			httpget = new HttpGet(getUrl);
			httpget.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
			httpget.addHeader("Accept", "text/html, application/xhtml+xml, */*");
			httpget.addHeader("Accept-Language", "zh-CN");
			httpget.addHeader("Accept-Encoding", "gzip, deflate");
			if (headMap != null) {
				for (Map.Entry<String, String> entry : headMap.entrySet()) {
					httpget.addHeader(entry.getKey(), entry.getValue());
				}
			}
			HttpResponse response = null;

			response = httpclient.execute(httpget);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				log.error("statusCode=" + statusCode);
				log.error(getUrl + "HttpGet Method failed: " + response.getStatusLine());
				return null;
			}

			HttpEntity entity = response.getEntity();
			if (entity == null) {
				return null;
			}

			byte[] bytes = getByteArrayOutputStream(entity.getContent());
			if (bytes == null) {
				return null;
			}

			// 从content-type中获取编码方式
			Header header = response.getFirstHeader("Content-Type");
			if (header != null) charset = getCharSet2(header.getValue());
			if (charset != null && !"".equals(charset)) {
				charset = charset.replace("\"", "");
				if ("gb2312".equalsIgnoreCase(charset)) charset = "GBK";
				html = new String(bytes, Charset.forName(charset));
			} else {//从Meta中获取编码方式
				html = new String(bytes, Charset.forName("utf-8"));
				charset = getCharSet(html);
				if (charset != null && !charset.equalsIgnoreCase("utf-8")) {
					if ("gbk2312".equalsIgnoreCase(charset)) charset = "GBK";
					html = new String(bytes, charset);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			log.error(getUrl + "抓取失败");
			e.printStackTrace();
		} finally {
			httpget.abort();
		}

		return html;
	}

	/**
	 * Post方法封装，自定义头信息，发送post请求，获取响应内容
	 *
	 * @param postUrl
	 * @param paramsEntity
	 * @param headMap
	 * @return
	 */
	public static String fetchByPostMethod(String postUrl, StringEntity paramsEntity, Map<String, String> headMap) {
		String resultStr = null;
		HttpPost httpPost = new HttpPost(postUrl);
		httpPost.addHeader("Accept", "Application/json");
		httpPost.addHeader("Content-Type", "Application/json;charset=utf-8");
		//httpPost.addHeader("Content-Length",contentLength);//StringEntity中已经包含参数长度属性无需再次设置
		httpPost.addHeader("Connection", "keep-alive");
		httpPost.setEntity(paramsEntity);
		System.out.println("httpClientEntity:"+httpPost.getEntity());
		for (Map.Entry<String, String> entry : headMap.entrySet()) {
			httpPost.addHeader(entry.getKey(), entry.getValue());
		}
		HttpResponse response;
		try {
			response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			resultStr = EntityUtils.toString(entity, "utf-8");
			EntityUtils.consume(entity);
		} catch (Exception e) {
			log.error(postUrl + "发送异常，" + e.getMessage());
		} finally {
			httpPost.abort();
		}
		return resultStr;
	}

	/**
	 * Get方法封装，发get送请求，获取响应内容
	 */
	public static String fetchByGetMethod(String getUrl){
		String charset = null;
		HttpGet httpget = null;
		String html = null;
		try {
			httpget = new HttpGet(getUrl);
			httpget.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
			httpget.addHeader("Accept", "text/html, application/xhtml+xml, */*");
			httpget.addHeader("Accept-Language", "zh-CN");
			httpget.addHeader("Accept-Encoding", "gzip, deflate");

			HttpResponse response = null;

			response = httpclient.execute(httpget);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				log.error("statusCode=" + statusCode);
				log.error(getUrl + "HttpGet Method failed: " + response.getStatusLine());
				return null;
			}

			HttpEntity entity = response.getEntity();
			if(entity == null){
				return null;
			}

			byte[] bytes = getByteArrayOutputStream(entity.getContent());
			if(bytes == null){
				return null;
			}

			// 从content-type中获取编码方式
			Header header=response.getFirstHeader("Content-Type");
			if(header != null) charset = getCharSet2(header.getValue());
			if(charset != null && !"".equals(charset)){
				charset = charset.replace("\"", "");
				if("gb2312".equalsIgnoreCase(charset)) charset = "GBK";
				html = new String(bytes,Charset.forName(charset));
			}else{//从Meta中获取编码方式
				html = new String(bytes,Charset.forName("utf-8"));
				charset = getCharSet(html);
				if(charset != null && !charset.equalsIgnoreCase("utf-8")){
					if("gbk2312".equalsIgnoreCase(charset)) charset = "GBK";
					html = new String(bytes,charset);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			log.error(getUrl + "抓取失败");
			e.printStackTrace();
		} finally{
			httpget.abort();
		}

		return html;
	}

	/**
	 * Post方法封装，发送post请求，获取响应内容
	 * @param postUrl
	 * @param paramsEntity
	 * @return
	 */
	public static String fetchByPostMethod(String postUrl, StringEntity paramsEntity){
		String resultStr = null;
		HttpPost httpPost = new HttpPost(postUrl);
		httpPost.setEntity(paramsEntity);
		httpPost.addHeader("Accept","Application/json");
		httpPost.addHeader("Content-Type","Application/json;charset=utf-8");
		//httpPost.addHeader("Content-Length",contentLength);//StringEntity中已经包含参数长度属性无需再次设置
		httpPost.addHeader("Connection","keep-alive");

		HttpResponse response;
		try {
			response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			resultStr = EntityUtils.toString(entity,"utf-8");
			EntityUtils.consume(entity);
		}catch (ConnectException ce){
			log.error(ce.getMessage());
//			System.err.println("========================服务器请求失败========================");

		} catch (IOException e) {
			log.error(e.getMessage());
//			e.printStackTrace();
		} finally{
			httpPost.abort();
		}
		return resultStr;
	}

	public static byte[] getByteArrayOutputStream(InputStream is) {
		ByteArrayOutputStream bios = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		try {
			int len = -1;
			while ((len = is.read(buffer)) != -1) {
				bios.write(buffer, 0, len);
				buffer = new byte[4096];
			}
			return bios.toByteArray();
		} catch (Exception e) {
			log.error(e.getMessage());
			return null;
		} finally {
			try {
				if(is != null) is.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (bios != null) {
				try {
					bios.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 正则获取字符编码
	 * @param content
	 * @return
	 */
	private static String getCharSet(String content){
		String regex = "charset=\\s*\"*(\\S*?)\"";
		Pattern pattern = Pattern.compile(regex,Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);
		if(matcher.find())
			return matcher.group(1);
		else
			return null;
	}

	/**
	 * 正则获取字符编码
	 * @param content_type
	 * @return
	 */
	private static String getCharSet2(String content_type){
		String regex = "charset=\\s*(\\S*[^;])";
		Pattern pattern = Pattern.compile(regex,Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content_type);
		if(matcher.find())
			return matcher.group(1);
		else
			return null;
	}


//	private static final String vmcUrl;
//
//	private static final String vmcUsername;
//	private static final String vmcPassword;
//	private static final String desKey;

//	static {
//		String separator = System.getProperty("file.separator");
//		String configPath = System.getProperty("user.dir")+separator+"config"+separator+"application.properties";
//		Properties properties = new Properties();
//		try {
//			InputStream in = new BufferedInputStream(new FileInputStream(
//					new File(configPath)));
//			properties.load(in);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		 vmcUrl = properties.getProperty("vmc.server");
//		 vmcUsername = properties.getProperty("vmc.loginname");
//		 vmcPassword = properties.getProperty("vmc.password");
//		desKey = properties.getProperty("vmc.deskey");
//
//	}
//	public String login(Boolean relogin){
//
//
//		JSONObject json = new JSONObject();
//		if(relogin){
//			json.put("CommandId","10131007");
//		}else {
//			json.put("CommandId","10131005");
//		}
//		json.put("UserName",vmcUsername);
//		json.put("UserPasswd",vmcPassword);
//		json.put("Token", String.valueOf(System.currentTimeMillis()));
//		json.put("Type","1");
//		String LoginJsonStr = json.toString();
////		System.out.println("=======================login=======================");
//		String resultJsonStr = sendToVMC(LoginJsonStr);
//		if(resultJsonStr==null){
//			return null;
//		}
//		JSONObject resultJson = JSONObject.parseObject(resultJsonStr);
//		String resultCode = resultJson.get("ResultCode").toString();
//		if(!"0".equals(resultCode)){
//			return null;
//		}
//		String loginID = resultJson.get("LoginId").toString();
//		return loginID;
//
//	}
//	public String logout(String loginID){
//
//		JSONObject logoutjson = new JSONObject();
//		logoutjson.put("CommandId","10131006");
//		logoutjson.put("LoginId",loginID);
//		logoutjson.put("Token", String.valueOf(System.currentTimeMillis()));
//		logoutjson.put("Type","1");
//		String LogoutJsonStr = logoutjson.toString();
////		System.out.println("=======================logout=======================");
//		String resultJsonStr = sendToVMC(LogoutJsonStr);
//		if(resultJsonStr==null){
//			return null;
//		}
//		// JSONObject resultJson = JSONObject.parseObject(resultJsonStr);
//		return resultJsonStr;
//
//	}
//
//	public String sendToVMC(String jsonStr){
//		log.info("SEND:"+jsonStr);
//		/**
//		 * 加密：DES加密--->Base64加密
//		 */
//		byte[] key = desKey.getBytes();
//		byte[] finalKey = new byte[8];
//		for (int i = 0; i < 8; i++) {
//			finalKey[i] = key[i];
//		}
//		byte[] desRequestBody = DES.encryptDES(jsonStr.getBytes(), finalKey);
//		String base64RequestBody = new String(Base64.encodeBase64(desRequestBody));
//		StringEntity stringEntity = new StringEntity(base64RequestBody , Charset.forName("UTF-8"));
//		String resultStr = MyHttpClientPool.getClientConnectionPool().fetchByPostMethod(vmcUrl , stringEntity );
//		if(resultStr==null){
//			return null;
//		}
//		byte[] desResult = Base64.decodeBase64(resultStr);
//		byte[] result = DES.decryptDES(desResult,finalKey);
//
//		String resultJsonStr = new String(result);
//		log.info("RESULT:"+resultJsonStr);
//
//		return resultJsonStr;
//	}

	public static void main(String[] args) {
		String str=fetchByGetMethod("http://api.map.baidu.com/telematics/v3/weather?location=杭州&output=json&ak=H7W5CxI0BPzKtwGcBHmpGPAz50xP1Qjw");
		System.out.println(str);
		JSONObject obj= JSON.parseObject(str);
		JSONArray arrar=((JSONObject) obj).getJSONArray("results");
		System.out.println(arrar);
		JSONObject obj1=arrar.getJSONObject(0);
		System.out.println(obj1.get("pm25"));
		JSONArray arr1=obj1.getJSONArray("weather_data");
		System.out.println(arr1);
		JSONObject obj2=arr1.getJSONObject(0);
		System.out.println(obj2.get("temperature"));
		System.out.println(obj2.get("wind"));
	}


}