package com.nhb.open.login.qq;

import com.alibaba.fastjson.JSON;
import com.nhb.open.login.bean.QQAccessToken;
import com.nhb.open.login.bean.QQResponse;
import com.nhb.open.login.bean.QQUser;
import okhttp3.*;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;


/**
 * qq 请求
 */
public class QQService {
    private static Logger logger = Logger.getLogger(QQService.class);

    /**
     * 引导用户进行qq登录授权
     *
     * @param appId       //申请应用的appId
     * @param redirectUri //回调重定向地址
     */
    public void getCode(String appId, String redirectUri, HttpServletResponse response) {
        try {
            String url = "https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id="
                    + appId + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") + "&state=20150714";
            logger.info("getCodeUrl:"+url);
            response.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 回调函数 返回用户的code
     *
     * @param request
     */
    public String callBack(HttpServletRequest request) {
        //客户端传回来的授权码
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        if (code != null && state.equals("20150714")) {
            return code;
        }
        return null;
    }

    /**
     * 返回qq的AccessToken对象
     *
     * @param appId
     * @param secret
     * @param code
     * @param redirectUri
     */
    public QQAccessToken getAccessToken(String appId, String secret,String code, String redirectUri) {
        try {
            //获取accessToken
            String url = "https://graph.qq.com/oauth2.0/token";
            OkHttpClient httpClient = new OkHttpClient().newBuilder().build();
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("client_id", appId);
            builder.add("client_secret", secret);
            builder.add("grant_type", "authorization_code");
            builder.add("code", code);
            builder.add("redirect_uri", redirectUri);
            RequestBody requestBody = builder.build();
            Request request = new Request.Builder().url(url).post(requestBody).build();
            Response response = httpClient.newCall(request).execute();
            String data = response.body().string();
            logger.info("data:"+data);
            QQAccessToken accessToken = new QQAccessToken();
            String[] split = data.split("&");
            accessToken.setAccess_token(split[0].split("=")[1]);
            accessToken.setExpires_in(split[1].split("=")[1]);
            accessToken.setRefresh_token(split[2].split("=")[1]);
            //获取openId
            Request request1 = new Request.Builder().url("https://graph.qq.com/oauth2.0/me?access_token=" + accessToken.getAccess_token()).get().build();
            Response response1 = httpClient.newCall(request1).execute();
            String value = response1.body().string();
            logger.info("value:"+value);
            String[] split1 = value.split("\\(");
            String[] split2 = split1[1].split("\\)");
            for (String s : split2){
                logger.info("第二次截取："+s);
            }
            QQAccessToken qqAccessToken = JSON.parseObject(split2[0], QQAccessToken.class);
            accessToken.setOpenid(qqAccessToken.getOpenid());
            if (accessToken != null) {
                return accessToken;
            }
        } catch (Exception e) {
            logger.error("QQ登录获取access_token错误：请检查传入的参数^<>^");
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  获取qq用户信息
     * @param openId
     * @param appId
     * @param accessToken
     * @return
     */
    public QQUser getUserMessage(String openId, String appId, String accessToken) {
        try {
            String url = "https://graph.qq.com/user/get_user_info?"
                    + "openid=" + openId + "&oauth_consumer_key=" + appId + "&access_token=" + accessToken
                    + "&format=json";
            OkHttpClient httpClient = new OkHttpClient();
            Request request = new Request.Builder().url(url).get().build();
            Response response = httpClient.newCall(request).execute();
            String data = response.body().string();
            QQUser user = JSON.parseObject(data, QQUser.class);
            if (user != null) {
                return user;
            }
        } catch (IOException e) {
            logger.error("QQ登录获取用户信息错误：请检查传入的参数^<>^");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 分享到qq空间 成功返回true
     *
     * @param accessToken
     * @param appId
     * @param openId
     * @param title          feeds的标题，最长36个中文字，超出部分会被截断
     * @param url            分享所在网页资源的链接，点击后跳转至第三方网页。请以http://开头
     * @param site           分享的来源网站名称，请填写网站申请接入时注册的网站名称
     * @param fromUrl        分享的来源网站对应的网站地址url。请以http://开头
     * @param otherParamters 需要发表到分享的信息,可选参数 此方法可以接收随意多的String参数，参数按照官方文档
     * @return
     */
    public boolean shareToQzone(String accessToken, String appId, String openId, String title, String url,
                                String site, String fromUrl, String... otherParamters) {
        String requestUrl = "https://graph.qq.com/share/add_share";
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("access_token", accessToken);
        builder.add("oauth_consumer_key", appId);
        builder.add("format", "json");
        builder.add("openid", openId);
        builder.add("title", title);
        builder.add("url", url);
        builder.add("site", site);
        builder.add("fromUrl", fromUrl);
        String[] parameters = otherParamters;
        for (int i = 0; i < otherParamters.length; ++i) {
            String parameter = parameters[i];
            if (parameter.indexOf("comment") == 0) {
                builder.add("comment", parameter.substring(8));
            } else if (parameter.indexOf("summary") == 0) {
                builder.add("summary", parameter.substring(8));
            } else if (parameter.indexOf("images") == 0) {
                builder.add("images", parameter.substring(7));
            } else if (parameter.indexOf("type") == 0) {
                builder.add("type", parameter.substring(5));
            } else if (parameter.indexOf("playurl") == 0) {
                builder.add("playurl", parameter.substring(8));
            } else {
                if (parameter.indexOf("nswb") != 0) {
                    try {
                        throw new Exception("you pass one illegal parameter");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                builder.add("nswb", parameter.substring(5));
            }
        }
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder().url(requestUrl).post(requestBody).build();
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        try {
            Response response = httpClient.newCall(request).execute();
            String data = response.body().string();
            QQResponse qqResponse = JSON.parseObject(data, QQResponse.class);
            if (qqResponse.getRet() == 0) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     *
     * @param url  你的网址
     * @param title  你的分享标题
     * @param pics   你的分享图片       base64格式
     * @param summary  你的分享描述信息
     */
    public void shareToQzone(String url,String title,String pics,String summary,HttpServletResponse response){
        String requestUrl = "https://sns.qzone.qq.com/cgi-bin/qzshare/cgi_qzshare_onekey?"
                +"url="+url+"&sharesource=qzone&title="+title+"&pics="+pics+"&summary="+summary;
        try {
            response.sendRedirect(requestUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分享到好友
     * @param url
     * @param title
     * @param pics
     * @param summary
     * @param desc      你的分享简述
     * @param response
     */
    public void shareToFriend(String url,String title,String pics,String summary,String desc,HttpServletResponse response){
        String requestUrl = "http://connect.qq.com/widget/shareqq/index.html?" +
                "url="+url+"&sharesource=qzone&title="+title+"&pics="+pics+
                "&summary="+summary+"&desc=+"+desc;
        try {
            response.sendRedirect(requestUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
