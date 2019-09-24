package com.secusoft.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.secusoft.web.core.exception.BizExceptionEnum;
import com.secusoft.web.core.support.FingerTookit;
import com.secusoft.web.core.util.QuartzUtil;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.APIService;
import com.secusoft.web.task.TokenTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Slf4j
@Service
public class APIServiceImpl implements APIService {
    //quartz 目前存在问题
    @Value("${spzn.appid}")
    private String appId;
    @Value("${spzn.appkey}")
    private String appKey ;
    @Value("${tip.url}")
    private String tipUrl;
    @Value("${spzn.host}")
    private String spznHost;
    //token保存
    private Map<String,String> tokenMap = new HashMap<>();
    //指纹生成
    private FingerTookit fingerTookit;

    //计算过期时间,重新获取tiptoken
    public void reTipToken(HttpSession session) {
        synchronized (session) {
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            String cron = "0/5 * * * * ?";
            log.info("ApiServiceImpl中session 信息: {}" ,session.getId());
            QuartzUtil.addJob("tiptoken"+ uuid, TokenTask.class, cron, session);//添加任务
        }
    }
    /**
     * 获取API访问令牌
     *
     * @return Map<String, Object> 返回信息
     */
    @Override
    public void getTipAccessToken(HttpSession session) {
        log.info("获取TIP访问令牌");
        String userAccessToken = (String) session.getAttribute("userAccessToken");
        //检查参数
        if (StringUtils.isEmpty(userAccessToken)) {
            //throw new InvalidParameterException("userAccessToken empty");
            return;
        }
        //填充消息
        JSONObject jobj = new JSONObject();
        jobj.put("app_id", appId);
        jobj.put("primary_token", userAccessToken);
        //challenge和mid可以不传(建议传，提高安全性)
        //生成指纹
        fingerTookit = new FingerTookit(appId,appKey);
        log.info("api中jobj: {}",jobj);
        String fingerprint = fingerTookit.buildFingerprint(jobj);
        jobj.put("fingerprint", fingerprint);
        log.info("获取tip传参:  {}", jobj.toString());
        //发送请求
        HttpPost post = null;
        try {
            //https不验证证书
            HttpClient httpClient = createSSLClientDefault();
            post = new HttpPost("https://" + tipUrl + "/sts/token");
            // 构造消息头
            post.setHeader("Content-type", "application/json; charset=utf-8");
            // 构建消息实体
            StringEntity entity = new StringEntity(jobj.toJSONString(), Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            post.setEntity(entity);
            HttpResponse response = httpClient.execute(post);
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
                //保存过期时间
                session.setAttribute("expiresIn", expiresIn);
                //保存tip_access_token
                session.setAttribute("tipAccessToken", access_token);
                log.info("TIP访问令牌：  {}", access_token);
                log.info("TIP过期时间:  {}", expiresIn);
            } else {
                log.info("tip令牌访问获取失败状态: {}", statusCode);
            }
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
    }
    /**
     * 业务API 对接
     */
    @Override
    public ResultVo requestAPI(Object param, HttpServletRequest request) {
        log.info("业务请求发送开始...");
        HttpSession session = request.getSession();
        log.info("requestApi 中的session: {}",session.getId());
        String tipAccessToken = (String) session.getAttribute("tipAccessToken");
        String userAccessToken = (String) session.getAttribute("userAccessToken");
        String idToken = (String)session.getAttribute("idToken");
        //判断是否有令牌
        if (StringUtils.isEmpty(tipAccessToken)) {
            getTipAccessToken(session);
        }
        HttpPost post = null;
        HttpGet get = null;
        //处理请求路径
        String requestURI = request.getRequestURI();
        log.info("请求相对路径： {}", requestURI);
        String queryString = request.getQueryString();
        try {
            //HttpClient有很多，可以根据个人喜好选用
            HttpClient httpClient = createSSLClientDefault();
            String url = "https://" + tipUrl + requestURI;
            //根据http实际方法，构造HttpPost，HttpGet，HttpPut等
            if ( StringUtils.isNotEmpty(queryString)) {
                url = url + "?" + queryString;
            }
            log.info("业务请求的完整路径： {}" , url);
            // 构造消息头
            post = new HttpPost(url);
            post.setHeader("Content-type", "application/json; charset=utf-8");
            // 填入双令牌
            post.setHeader("X-trustuser-access-token", userAccessToken);
            post.setHeader("X-trustagw-access-token", tipAccessToken);
            post.setHeader("Host", spznHost);
            if (StringUtils.isEmpty(userAccessToken) && StringUtils.isEmpty(idToken)) {
                ResultVo resultVo = new ResultVo();
                resultVo.setCode(610);
                resultVo.setMessage("会话超时");
                return resultVo;
            }
            post.setHeader("idToken", idToken);
            String ipAddr = getIpAddr(request);
            post.setHeader("x-forwarded-for", ipAddr);
            // 构建消息实体
            StringEntity entity = new StringEntity(JSONObject.toJSONString(param), Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            post.setEntity(entity);
            // 发送http请求
            HttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity1 = response.getEntity();
            String resultStr = EntityUtils.toString(entity1);
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
        return ResultVo.failure(BizExceptionEnum.SERVER_ERROR);
    }
    /**
     * 获取图片
     */
    @Override
    public void requestImage(HttpServletRequest request, HttpServletResponse response) {
        log.info("请求图像资源开始...");
        HttpSession session = request.getSession();
        String tipAccessToken = (String)session.getAttribute("tipAccessToken");
        String idToken = (String)session.getAttribute("idToken");
        log.info("tiptoken：" + tipAccessToken);
        HttpGet get = null;
        String url = "https://" + tipUrl + "/spzn/pic";
        String picUrl = null;
        try {
            picUrl = request.getQueryString();
            if(StringUtils.isNotEmpty(picUrl)) {
                url += "?" + picUrl;
            }
            log.info("请求图像完整路径:    {}",url);
            //HttpClient有很多，可以根据个人喜好选用
            HttpClient httpClient = createSSLClientDefault();
            //根据http实际方法，构造HttpPost，HttpGet，HttpPut等
            get = new HttpGet(url);
            // 构造消息头
            get.setHeader("Content-type", "application/json; charset=utf-8");
            // 填入双令牌
            get.setHeader("X-trustagw-access-token", tipAccessToken);
            get.setHeader("Host", spznHost);
            get.setHeader("idToken", idToken);
            // 发送http请求
            HttpResponse response1 = httpClient.execute(get);
            int statusCode = response1.getStatusLine().getStatusCode();
            if(statusCode == 200){
                HttpEntity ht = response1.getEntity();
                InputStream inStream = ht.getContent();
                byte data[] = readInputStream(inStream);
                inStream.close();
                //设置返回的文件类型
                response.setContentType(MediaType.IMAGE_JPEG_VALUE);
                OutputStream os = response.getOutputStream();
                os.write(data);
                os.flush();
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (get != null) {
                try {//断开链接
                    get.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //SSL 证书验证 用于https协议
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
     * 获取图片(暂无使用)
     */
    @Override
    public void requestAPIURL(HttpServletRequest request, HttpServletResponse response) {
        log.info("请求图像资源开始：");
        HttpSession session = request.getSession();
        String tipAccessToken = (String)session.getAttribute("tipAccessToken");
        log.info("tiptoken："+tipAccessToken);
        String picUrl = null;
        try {
            picUrl = request.getQueryString();
            URL url = new URL("https://" + tipUrl + "/spzn/pic?" + picUrl);
            log.info("请求图像完整路径: https://" + tipUrl + "/spzn/pic?" + picUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }
                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                        }
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            conn.setSSLSocketFactory(new MySocketFactory(sslContext.getSocketFactory()));
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
            //设置请求方式为get
            conn.setRequestMethod(HttpMethod.GET.name());
            conn.setRequestProperty("X-trustagw-access-token",tipAccessToken);
            conn.setRequestProperty("Host",spznHost);
            conn.setConnectTimeout(5000);
            conn.connect();
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

    private static class MySocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;
        public MySocketFactory(SSLSocketFactory sslSocketFactory) {
            this.delegate = sslSocketFactory;
        }
        // 返回默认启用的密码套件。除非一个列表启用，对SSL连接的握手会使用这些密码套件。
        // 这些默认的服务的最低质量要求保密保护和服务器身份验证
        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        // 返回的密码套件可用于SSL连接启用的名字
        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(final Socket socket, final String host, final int port,
                                   final boolean autoClose) throws IOException {
            final Socket underlyingSocket = delegate.createSocket(socket, host, port, autoClose);
            return overrideProtocol(underlyingSocket);
        }

        @Override
        public Socket createSocket(final String host, final int port) throws IOException {
            final Socket underlyingSocket = delegate.createSocket(host, port);
            return overrideProtocol(underlyingSocket);
        }

        @Override
        public Socket createSocket(final String host, final int port, final InetAddress localAddress,
                                   final int localPort) throws
                IOException {
            final Socket underlyingSocket = delegate.createSocket(host, port, localAddress, localPort);
            return overrideProtocol(underlyingSocket);
        }

        @Override
        public Socket createSocket(final InetAddress host, final int port) throws IOException {
            final Socket underlyingSocket = delegate.createSocket(host, port);
            return overrideProtocol(underlyingSocket);
        }

        @Override
        public Socket createSocket(final InetAddress host, final int port, final InetAddress localAddress,
                                   final int localPort) throws
                IOException {
            final Socket underlyingSocket = delegate.createSocket(host, port, localAddress, localPort);
            return overrideProtocol(underlyingSocket);
        }

        private Socket overrideProtocol(final Socket socket) {
            if (!(socket instanceof SSLSocket)) {
                throw new RuntimeException("An instance of SSLSocket is expected");
            }
            ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1"});
            return socket;
        }
    }

    protected static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        return outStream.toByteArray();
    }
    /**
     * 获取用户真实IP地址，不使用request.getRemoteAddr()的原因是有可能用户使用了代理软件方式避免真实IP地址,
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值
     *
     * @return ip
     */
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        //log.info("x-forwarded-for ip: " + ip);
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            /*if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }*/
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            log.info("Proxy-Client-IP ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
            log.info("WL-Proxy-Client-IP ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
            log.info("HTTP_CLIENT_IP ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            log.info("HTTP_X_FORWARDED_FOR ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
            log.info("X-Real-IP ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            log.info("getRemoteAddr ip: " + ip);
        }
        log.info("客户端PC真实IP: " + ip);
        return ip;
    }
    /**
     * 下载资源文件
     */
    @Override
    public void requestFile(HttpServletRequest request, HttpServletResponse response) {
        log.info("请求资源开始...");
        HttpSession session = request.getSession();
        String tipAccessToken = (String)session.getAttribute("tipAccessToken");
        log.info("tiptoken：" + tipAccessToken);
        HttpGet get = null;
        String url = "https://" + tipUrl + "/spzn/file";
        String fileUrl = null;
        try {
	        fileUrl = request.getQueryString();
            if(StringUtils.isNotEmpty(fileUrl)) {
                url += "?" + fileUrl;
            }
            log.info("请求图像完整路径:    {}",url);
            //HttpClient有很多，可以根据个人喜好选用
            HttpClient httpClient = createSSLClientDefault();
            //根据http实际方法，构造HttpPost，HttpGet，HttpPut等
            get = new HttpGet(url);
            // 构造消息头
            get.setHeader("Content-type", "application/json; charset=utf-8");
            // 填入双令牌
            get.setHeader("X-trustagw-access-token", tipAccessToken);
            get.setHeader("Host", spznHost);
            // 发送http请求
            HttpResponse response1 = httpClient.execute(get);
            int statusCode = response1.getStatusLine().getStatusCode();
            if(statusCode == 200){
                HttpEntity ht = response1.getEntity();
                InputStream inStream = ht.getContent();
                byte data[] = readInputStream(inStream);
                inStream.close();
                //设置返回的文件类型
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                OutputStream os = response.getOutputStream();
                os.write(data);
                os.flush();
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (get != null) {
                try {//断开链接
                    get.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
