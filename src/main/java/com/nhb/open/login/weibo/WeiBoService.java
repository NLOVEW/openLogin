package com.nhb.open.login.weibo;


import com.alibaba.fastjson.JSON;
import com.nhb.open.login.bean.WeiBoAccessToken;
import com.nhb.open.login.bean.WeiBoUser;
import com.nhb.open.login.utils.StringUtils;
import okhttp3.*;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 微博 登录
 */
public class WeiBoService {
    private static Logger logger = Logger.getLogger(WeiBoService.class);

    /**
     * @param url
     * @param title
     * @param pic
     * @param appKey   在微博平台申请的appkey
     * @param response
     */
    public void shareToWeiBo(String url, String title, String pic, String appKey, HttpServletResponse response) {
        String requestUrl = "http://service.weibo.com/share/share.php?" +
                "url=" + url + "&sharesource=weibo&title=" + title + "&pic=" + pic + "&appkey=" + appKey;
        try {
            response.sendRedirect(requestUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取code
     *
     * @param appKey
     * @param redirectUri
     * @param response
     */
    public void getCode(String appKey, String redirectUri, HttpServletResponse response) {
        String url = "https://api.weibo.com/oauth2/authorize?"
                + "client_id=" + appKey + "&redirect_uri=" + redirectUri;
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String callBack(HttpServletRequest request) {
        String errorCode = request.getParameter("error_code");
        if (!StringUtils.isEmpty(errorCode)) {
            logger.error("微博登录授权失败");
        } else {
            String code = request.getParameter("code");
            return code;
        }
        return null;
    }

    /**
     * @param appKey
     * @param secret
     * @param code        回调函数获取的code
     * @param redirectUri 相同的回调路径
     * @return
     */
    public WeiBoAccessToken getAccessToken(String appKey, String secret, String code, String redirectUri) {
        try {
            String url = "https://api.weibo.com/oauth2/access_token";
            OkHttpClient httpClient = new OkHttpClient.Builder().build();
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("client_id", appKey);
            builder.add("client_secret", secret);
            builder.add("grant_type", "authorization_code");
            builder.add("code", code);
            builder.add("redirect_uri", redirectUri);
            RequestBody requestBody = builder.build();
            Request request = new Request.Builder().url(url).post(requestBody).build();
            Response response = httpClient.newCall(request).execute();
            String data = response.body().string();
            logger.info("data:"+data);
            if (data != null) {
                return JSON.parseObject(data, WeiBoAccessToken.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取用户信息
     *
     * @param accessToken
     * @param uId
     * @return
     */
    public WeiBoUser getUserMessage(String accessToken, String uId) {
        try {
            String url = "https://api.weibo.com/2/users/show.json?" +
                    "access_token=" + accessToken + "&uid=" + uId;
            OkHttpClient httpClient = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().url(url).get().build();
            Response response = httpClient.newCall(request).execute();
            String data = response.body().string();
            logger.info("data:"+data);
            if (data != null) {
                return JSON.parseObject(data, WeiBoUser.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
