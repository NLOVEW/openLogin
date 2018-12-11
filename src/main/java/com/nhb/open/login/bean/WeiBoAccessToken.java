package com.nhb.open.login.bean;

import java.io.Serializable;

/**
 * 微博 口令
 */
public class WeiBoAccessToken implements Serializable {
    private String access_token;
    private String expires_in;
    private String remind_in;
    private String uid;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }

    public String getRemind_in() {
        return remind_in;
    }

    public void setRemind_in(String remind_in) {
        this.remind_in = remind_in;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "WeiBoAccessToken{" +
                "access_token='" + access_token + '\'' +
                ", expires_in='" + expires_in + '\'' +
                ", remind_in='" + remind_in + '\'' +
                ", uid='" + uid + '\'' +
                '}';
    }
}
