package com.secusoft.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.support.FingerTookit;
import com.secusoft.web.core.util.QuartzCronDateUtil;
import com.secusoft.web.core.util.QuartzUtil;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.APIService;
import com.secusoft.web.task.TokenTask;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class APIServiceImpl implements APIService {
	@Value("${spzn.appid}")
	private String appid;
	@Value("${spzn.appkey}")
	private String appkey;
	@Value("${tip.url}")
	private String tipurl;
	@Value("${spzn.host}")
	private String spznHost;
	//token保存
	private Map<String,String> tokenMap = new HashMap<>();
	//指纹生成
	private FingerTookit fingerTookit;
	
	//计算过期时间,重新获取tiptoken
	public void requestTipToken(HttpSession session) {
		int count = 1;
		//匹配时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		//提前过期时间获取tiptoken
		String dateCron = QuartzCronDateUtil.getDateCron(calendar.getTime());
		StringBuffer dateCronBuffer = new StringBuffer(dateCron);
		//计算时间间隔
		Integer expiresIn = (Integer) session.getAttribute("expiresIn");
		if (expiresIn > 0) {
			int m = expiresIn / 60;
			if (m >= 1) {
				int h = m / 60;
				if (h >= 1) {//1小时以上的时间间隔
					if (h >= 24) {//过期时间超过24小时
					
					} else {
						int i = dateCronBuffer.indexOf(" ", 6);
						dateCronBuffer.insert(i, "/" + h);
					}
				} else {//3600s 为界限
					int i = dateCronBuffer.indexOf(" ", 3);
					dateCronBuffer.insert(i, "/" + m);
				}
			} else {
				//在第一个空格前设置时间间隔
				int i = dateCronBuffer.indexOf(" ");
				dateCronBuffer.insert(i, "/" + expiresIn);
			}
		}
		QuartzUtil.addJob("tiptoken" + count, TokenTask.class, dateCronBuffer.toString(), session);//添加任务
		count++;
	}
	//解析idToken
	public DingdangUserRetriever.User resolveIdToken(HttpSession session) throws JoseException, IOException {
		String publicKey = "{\"kty\":\"RSA\",\"kid\":\"1346614912326510837\",\"alg\":\"RS256\",\"n\":\"hOdf08cku1cEddGWHjOxalfqqmrMJ5LotXT28r0pgsw82uZiSNhi4kr1qVB7z3vUeqh0TffekWxsxGc0VXGoYrPYRkkS08old8CNZQjl7AbnY179kwPilburFuMXioYO55UgvXm2mpCBL8RKGiDSORlVXruBYhxGxZ8yAaloIPVZMTIBjhKtq_fc9K1fygjR7Q3BJJkDcLU92P1Jb8_EbpvRhkHzjKi-FcXbflPWY8dMQpksInp9c-AUByVvYQD3me94yVpyOcwVNUhT5sDUOHhbWjs0gkllY86GRqIHMpNk8VDI7BiXTny-etm7AGyU0_AJlwn4JcsERCqozH7n6w\",\"e\":\"AQAB\"}";
		String userAccessToken = (String) session.getAttribute("userAccessToken");
		if (StringUtils.isNotEmpty(userAccessToken)) {
			//解析id_token
			DingdangUserRetriever retriever = new DingdangUserRetriever(userAccessToken, publicKey);
			DingdangUserRetriever.User token = retriever.retrieve();
			System.out.println("token信息");
			System.out.println("token的uuid:" + token.getUdAccountUuid());
			System.out.println("token的access_token:" + token.getAzp());
			return token;
		}
		return null;
	}
	/**
	 * 获取API访问令牌
	 * @return Map<String, Object> 返回信息
	 */
	@Override
	public ResultVo getTipAccessToken(HttpSession session) {
		System.out.println("获取tip访问令牌");
		String userAccessToken = (String) session.getAttribute("userAccessToken");
		//检查参数
		if (StringUtils.isEmpty(userAccessToken)) {
			throw new InvalidParameterException("userAccessToken empty");
		}
		//填充消息
		JSONObject jobj = new JSONObject();
		jobj.put("app_id", appid);
		jobj.put("primary_token", userAccessToken);
        /*//challenge和mid可以不传(建议传，提高安全性)
        if(!StringUtils.isEmpty(challenge) && !StringUtils.isEmpty(mid)) {
            jobj.put("challenge", challenge);
            jobj.put("mid", mid);
        }*/
		//生成指纹
		fingerTookit = new FingerTookit(appid, appkey);
		String fingerprint = fingerTookit.buildFingerprint(jobj);
		jobj.put("fingerprint", fingerprint);
		System.out.println("获取tip传参:" + jobj.toString());
		//发送请求
		HttpPost post = null;
		try {
			//https不验证证书
			HttpClient httpClient = createSSLClientDefault();
			post = new HttpPost("https://" + tipurl + "/sts/token");
			// 构造消息头
			post.setHeader("Content-type", "application/json; charset=utf-8");
			// 构建消息实体
			StringEntity entity = new StringEntity(jobj.toJSONString(), Charset.forName("UTF-8"));
			entity.setContentEncoding("UTF-8");
			// 发送Json格式的数据请求
			entity.setContentType("application/json");
			post.setEntity(entity);
			HttpResponse response = httpClient.execute(post);
			System.out.println("获取tip请求响应：" + response);
			// 检验http返回码
			int statusCode = response.getStatusLine().getStatusCode();
			//TIP返回201 (400 500 表示失败)
			if (statusCode == HttpStatus.SC_CREATED) {
				String result = null;
				result = EntityUtils.toString(response.getEntity(), "UTF-8");
				JSONObject responseObj = JSONObject.parseObject(result);
				//校验指纹
				boolean b = fingerTookit.checkFingerprint(responseObj);
				//获取tip_access_token
				String access_token = (String) responseObj.get("access_token");
				Integer expiresIn = (Integer) responseObj.get("expires_in");
				String uid = (String) responseObj.get("uid");
				//保存过期时间
				session.setAttribute("expiresIn", expiresIn);
				session.setAttribute("uid", uid);
				//保存tip_access_token
				session.setAttribute("tipAccessToken", access_token);
				System.out.println("TIP访问令牌：" + access_token);
				System.out.println("开始解析idToken:");
				resolveIdToken(session);
				return ResultVo.success(responseObj);
			}
			System.out.println("tip令牌访问获取失败状态: " + statusCode);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (post != null) {
				try {
					post.releaseConnection();
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		//TODO return err
		return null;
	}
	public static CloseableHttpClient createSSLClientDefault() {
		try {
			//使用 loadTrustMaterial() 方法实现一个信任策略，信任所有证书
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				// 信任所有
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();
			//NoopHostnameVerifier类:  作为主机名验证工具，实质上关闭了主机名验证，它接受任何
			//有效的SSL会话并匹配到目标主机。
			HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return HttpClients.createDefault();
	}
	/**
	 * 业务API 对接
	 */
	@Override
	public ResultVo requestAPI(JSONObject jsonObject, HttpServletRequest request) {
		System.out.println("业务请求发送开始：");
		HttpSession session = request.getSession();
		String tipAccessToken = (String) session.getAttribute("tipAccessToken");
		String userAccessToken = (String) session.getAttribute("userAccessToken");
		//判断是否 有令牌
		if (StringUtils.isEmpty(tipAccessToken) || StringUtils.isEmpty(userAccessToken)) {
			getTipAccessToken(session);
		}
		HttpPost post = null;
		//处理请求路径
		StringBuffer requestURL = request.getRequestURL();
		System.out.println("请求全路径：" + requestURL);
		int spzn = requestURL.indexOf("/spzn/");
		String requesturl = requestURL.substring(spzn);
		try {
			//HttpClient有很多，可以根据个人喜好选用
			HttpClient httpClient = createSSLClientDefault();
			//根据http实际方法，构造HttpPost，HttpGet，HttpPut等
			post = new HttpPost("https://" + tipurl + requesturl);
			// 构造消息头
			post.setHeader("Content-type", "application/json; charset=utf-8");
			// 填入双令牌
			post.setHeader("X-trustuser-access-token", userAccessToken);
			post.setHeader("X-trustagw-access-token", tipAccessToken);
			post.setHeader("Host", spznHost);
           /* System.out.println("Header中tap访问令牌：" + userAccessToken);
            System.out.println("Header中tip访问令牌：" + tipAccessToken);
            System.out.println("Header中Host主机：" + spznHost);*/
			// 构建消息实体
			StringEntity entity = new StringEntity(JSONObject.toJSONString(jsonObject), Charset.forName("UTF-8"));
			entity.setContentEncoding("UTF-8");
			// 发送Json格式的数据请求
			entity.setContentType("application/json");
			post.setEntity(entity);
			// 发送http请求
			HttpResponse response = httpClient.execute(post);
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity entity1 = response.getEntity();
			String resultStr = EntityUtils.toString(entity1);
            /*在携带accesstokefn访问业务API时，如果返回header里面包括Return-By-ApiGateway
            字段且http状态码为403且body中json内容中的status字段值为13001时（accesstoken过
            期） token 过期判断*/
//			System.out.println("业务api对接返回数据：" + response + " 返回实体：" + resultStr);
			ResultVo result = JSONObject.parseObject(resultStr, ResultVo.class);
			//返回结果
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (post != null) {
				try {//断开链接
					post.releaseConnection();
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	/**
	 * 获取图片
	 */
	@Override
	public void requestAPI(HttpServletRequest request, HttpServletResponse response) {
		try {
			URL url = new URL("http://127.0.0.1:8106/pic?picUrl=http://127.0.0.1:8105/static/123.jpg");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(HttpMethod.GET.name());
			conn.setConnectTimeout(5000);
			//通过输入流获取图片数据
			InputStream inStream = conn.getInputStream();
			byte data[] = readInputStream(inStream);
			inStream.close();
			//设置返回的文件类型
			response.setContentType(MediaType.IMAGE_JPEG_VALUE);
			OutputStream os = response.getOutputStream();
			os.write(data);
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	protected static byte[] readInputStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		return outStream.toByteArray();
	}
	public static void main(String[] args) {
		String appId = "de7329";
		String appKey = "8ecb08d49659dd77";
		JSONObject jobj = new JSONObject();
		jobj.put("app_id", appId);
		FingerTookit t = new FingerTookit(appId, appKey);
		System.out.println(t.buildFingerprint(jobj));
	}
}
