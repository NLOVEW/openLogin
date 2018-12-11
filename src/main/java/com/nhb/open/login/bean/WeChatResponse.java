package com.nhb.open.login.bean;

import java.io.Serializable;

/**
 * 微信验证返回数据
 */
public class WeChatResponse implements Serializable {
    private String errcode;
    private String errmsg;

    public String getErrcode() {
        return errcode;
    }

    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}
