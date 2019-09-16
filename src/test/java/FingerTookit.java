/*
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Encoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.TreeMap;

@Slf4j
public class FingerTookit {
	//应用信息
    private String appId ;

    private String appKey ;

    public FingerTookit(String appId, String appKey){
    	 this.appId = appId;
         this.appKey = appKey;
    }

    public static void main(String[] args) {
		FingerTookit fingerTookit = new FingerTookit("b87ed6","e759979e1de17011");
		JSONObject jobj = new JSONObject();
		jobj.put("app_id", "b87ed6");
		//challenge和mid可以不传(建议传，提高安全性)
		//生成指纹
		log.info("api中jobj: {}",jobj);
		String fingerprint = fingerTookit.buildFingerprint(jobj);
		jobj.put("fingerprint", fingerprint);
		log.info("获取tip传参:  {}", jobj.toString());

	}

	*/
/**
	 * 生成指纹
	 * @param json
	 * @return
	 *//*

	public String buildFingerprint(JSONObject json) {
		//使用TreeMap，按key字典排序
		TreeMap<String, Object> params = new TreeMap<String, Object>(json);
		log.info("无key 中的json: {}",json);
		StringBuffer queryStr = new StringBuffer("");
        params.put("app_id", this.appId);
		params.put("app_key", this.appKey);
		log.info("Fingerprint中: {}", params);
		Iterator<String> itr = params.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			if (!key.equals("fingerprint")) {
				//queryStr += (key + "=" + params.get(key).toString() + "&");
				queryStr.append(key + "=" +params.get(key).toString() + "&");
			}
		}
		//去掉最后多余&
		String substring = queryStr.substring(0, queryStr.length() - 1);
		String fingerprint = SHA1(substring);//转base64
		return fingerprint;
	}
	
	*/
/**
	 * 检查返回值中的指纹
	 * 
	 * @param json
	 * @return 检查通过返回true,失败返回false
	 *//*

	public boolean checkFingerprint(JSONObject json) {
		String fingerprint = (String) json.get("fingerprint");
		if (StringUtils.isEmpty(fingerprint)) {
			return false;
		}
		String genFingerprint = buildFingerprint(json);
		if (!fingerprint.equals(genFingerprint)) {
			log.info("fingerprint=" + fingerprint + "  genFingerprint="+genFingerprint);
			return false;
		}
		return true;
	}

	*/
/**
	 * 对拼接的串做SHA1计算
	 * @param decript
	 * @return
	 *//*

    private static String SHA1(String decript){
        try {
            String ret = "";
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            byte[] sha1bytes = sha1.digest(decript.getBytes());
            if (sha1bytes != null) {//base64加密
                ret = new BASE64Encoder().encode(sha1bytes);
            }
            return ret;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }         
}

*/
