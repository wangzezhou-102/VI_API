package com.secusoft.web.task;

import com.alibaba.fastjson.JSONObject;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.APIService;
import com.secusoft.web.service.impl.APIServiceImpl;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TipTokenTask {
    private static final Logger log = LoggerFactory.getLogger(TipTokenTask.class);
    @Resource
    private APIService apiService;
    @Value("${spzn.host}")
    private String spznHost;
    //@Scheduled(cron="0/5 * * * * ?")
    public void syncDevice() {
        log.info("图像资源请求 start");
        HttpGet get = null;
        try {
            //HttpClient有很多，可以根据个人喜好选用
            HttpClient httpClient = APIServiceImpl.createSSLClientDefault();
            //根据http实际方法，构造HttpPost，HttpGet，HttpPut等
            get = new HttpGet("https://172.16.15.8:443/spzn/pic?picUrl=http://127.0.0.1:8106/spzn/static/123.jpg" );
            System.out.println("请求tip的路径:  "+"https://");
            // 构造消息头
            get.setHeader("Content-type", "application/json; charset=utf-8");
            // 填入双令牌
            //get.setHeader("X-trustuser-access-token", userAccessToken);
            get.setHeader("X-trustagw-access-token", "1565054761_3.0_1.2.3.4_DBEACA0FA77A8EFBDCFE23BC35342DF116DFC468");
            get.setHeader("Host", spznHost);
            // 发送http请求
            HttpResponse response1 = httpClient.execute(get);
            int statusCode = response1.getStatusLine().getStatusCode();
            HttpEntity entity1 = response1.getEntity();
            String resultStr = EntityUtils.toString(entity1);
            System.out.println("业务api对接返回数据：" + response1);
            System.out.println("请求返回实体:  "+resultStr);
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
        /*HttpGet get = null;
        //处理请求路径
        StringBuffer requestURL = request.getRequestURL();
        System.out.println("请求全路径：" + requestURL);
        int spzn = requestURL.indexOf("/spzn/pic");
        String requesturl = requestURL.substring(spzn);
        try {
            URL url = new URL("https://"+tipurl+requesturl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            X509TrustManager xtm = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    // TODO Auto-generated method stub
                    return null;
                }
                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                    // TODO Auto-generated method stub
                }
                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                    // TODO Auto-generated method stub
                }
            };
            TrustManager[] tm = {xtm};
            SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(null, tm, new SecureRandom());
            conn.setSSLSocketFactory(ctx.getSocketFactory());
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
            //设置请求方式为get
            conn.setRequestMethod(HttpMethod.GET.name());
            //conn.setRequestProperty("X-trustuser-access-token",userAccessToken);
            conn.setRequestProperty("X-trustagw-access-token",tipAccessToken);
            conn.setRequestProperty("Host",spznHost);
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
        }*/
        log.info("图像资源请求 end");

    }
}
