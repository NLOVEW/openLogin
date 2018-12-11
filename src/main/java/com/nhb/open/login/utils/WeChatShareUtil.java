package com.nhb.open.login.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nhb.open.login.bean.WeChatShare;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * 微信朋友圈分享
 */
public class WeChatShareUtil {
    private static Logger logger = Logger.getLogger(WeChatShareUtil.class);

    /**
     * 根据微信公众号信息获取 accessToken
     * @param appId
     * @param secret
     * @return
     */
    private static String getAccessToken(String appId,String secret) {
        //获取access_token填写client_credential
        String grant_type = "client_credential";
        //这个url链接地址和参数皆不能变   访问链接
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type="+grant_type+"&appid="+appId+"&secret="+secret;
        try {
            OkHttpClient httpClient = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().url(url).get().build();
            Response response = httpClient.newCall(request).execute();
            String data = response.body().string();
            if (data != null){
                JSONObject object = JSON.parseObject(data);
                return object.getString("access_token");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取ticket
     * @param accessToken
     * @return
     */
    private static String getTicket(String accessToken) {
        //这个url链接和参数不能变
        String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+ accessToken +"&type=jsapi";
        try {
            OkHttpClient httpClient = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().url(url).get().build();
            Response response = httpClient.newCall(request).execute();
            String data = response.body().string();
            if (response.body().string() != null){
                JSONObject object = JSON.parseObject(data);
                return object.getString("ticket");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static WeChatShare getWeChatShare(String url, String appId, String secret) {
        WeChatShare weChatShare = new WeChatShare();
        String ticket = getTicket(getAccessToken(appId,secret));
        Map<String, String> ret = WeChatSignUtil.sign(ticket, url);
        weChatShare.setTicket(ret.get("jsapi_ticket"));
        weChatShare.setSignature(ret.get("signature"));
        weChatShare.setNoncestr(ret.get("nonceStr"));
        weChatShare.setTimestamp(ret.get("timestamp"));
        return weChatShare;
    }
}
