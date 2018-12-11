package com.nhb.open.login.bean;

import java.io.Serializable;

/**
 * qq返回验证数据
 */
public class QQResponse implements Serializable {
    private int ret = 0;
    private String msg = "";
    private int errcode = 0;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }
}
