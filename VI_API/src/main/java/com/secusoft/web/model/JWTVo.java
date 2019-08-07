package com.secusoft.web.model;

/**
 * id_token封装
 * @author wangzezhou
 * @date 2019-07-19
 */
public class JWTVo {
    //目标系统url
    private String iss;
    //用户名
    private String sub;
    //id_token受众，包含client_id
    private String aub;
    //随机字符串 jwt的唯一身份标识，主要用来作为一次性token,回避重放攻击
    private String jti;
    //过期时间
    private long exp;
    //构建时间
    private long iat;
    //身份证号
    private String idcard;
    //警员号
    private String policeid;

    public String getIss() { return iss; }

    public void setIss(String iss) { this.iss = iss; }

    public String getSub() { return sub; }

    public void setSub(String sub) { this.sub = sub; }

    public String getAub() { return aub; }

    public void setAub(String aub) { this.aub = aub; }

    public String getJti() { return jti; }

    public void setJti(String jti) { this.jti = jti; }

    public long getExp() { return exp; }

    public void setExp(long exp) { this.exp = exp; }

    public long getIat() { return iat; }

    public void setIat(long iat) { this.iat = iat; }

    public String getIdcard() { return idcard; }

    public void setIdcard(String idcard) { this.idcard = idcard; }

    public String getPoliceid() { return policeid; }

    public void setPoliceid(String policeid) { this.policeid = policeid; }
}
