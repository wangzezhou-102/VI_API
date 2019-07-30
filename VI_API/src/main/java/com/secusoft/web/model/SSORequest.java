package com.secusoft.web.model;


/**
 * 单点登录请求(OIDC协议)
 * @author wangzezhou
 * @date 2019-07-19
 */
public class SSORequest {
    //封装的id_token
    private String idToken;
    //访问令牌 标志目标系统被允许访问的唯一字符串
    private String accessToken;
    //令牌类型 bearer类型时 不需要密码验证 accessToken
    private String tokenType;
    //有效期 单位s
    private Integer expiresIn;
    //公钥
    private String publicKey;

    public String getIdToken() { return idToken; }

    public void setIdToken(String idToken) { this.idToken = idToken; }

    public String getAccessToken() { return accessToken; }

    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }

    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Integer getExpiresIn() { return expiresIn; }

    public void setExpiresIn(Integer expiresIn) { this.expiresIn = expiresIn; }

    public String getPublicKey() { return publicKey; }

    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
}
