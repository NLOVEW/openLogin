package com.nhb.open.login.wechat;

import com.alibaba.fastjson.JSON;
import com.nhb.open.login.bean.WeChatAccessToken;
import com.nhb.open.login.bean.WeChatResponse;
import com.nhb.open.login.bean.WeChatShare;
import com.nhb.open.login.bean.WeChatUser;
import com.nhb.open.login.utils.WeChatShareUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;


/**
 * 微信  登录是使用微信开放平台注册
 *       分享是使用公众平台注册
 */
public class WeChatService {
    private static Logger logger = Logger.getLogger(WeChatService.class);

    /**
     * 首先获取用户同意授权后的code
     * 用户允许授权后 重定向到redirect_uri的网址上，并且带上code和state参数
     * <p>
     * 传入参数：appid  redirect_uri
     *
     * @param response
     */
    public void getCode(String appId,String redirectUri, HttpServletResponse response) {
        try {
            String url = "https://open.weixin.qq.com/connect/qrconnect?appid="
                    + appId
                    + "&redirect_uri="
                    + URLEncoder.encode(redirectUri, "UTF-8")
                    + "&response_type=code&scope=snsapi_login&state=20150714#wechat_redirect";
            response.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * redirect_uri地址对应的回调地址
     * 返回微信用户的code
     *
     * @param request
     * @return String
     */
    public String callBack(HttpServletRequest request) {
        String code = request.getParameter("code");//客户端传回来的授权码
        String state = request.getParameter("state");
        if (code != null && state.equals("20150714")) {
            return code;
        }
        return null;
    }

    /**
     * 根据用户的code到微信获取access_token等信息
     * <p>
     * 参数：appid secret code grant_type
     *
     * @param code
     * @param appId
     * @param secret
     * @return
     */
    public WeChatAccessToken getAccessToken(String appId,String secret,String code) {
        try {
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                    + appId
                    + "&secret=" + secret
                    + "&code=" + code
                    + "&grant_type=authorization_code";
            OkHttpClient httpClient = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().url(url).get().build();
            Response response = httpClient.newCall(request).execute();
            String data = response.body().string();
            logger.info("data:"+data);
            WeChatAccessToken weChatAccessToken = JSON.parseObject(data, WeChatAccessToken.class);
            if (weChatAccessToken.getOpenid() != null) {
                return weChatAccessToken;
            }
        } catch (IOException e) {
            logger.error("微信登录获取access_token错误：请检查传入的参数^<>^");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 重新设置access_token后期时间 请求后会变成30天
     * access_token已超时，那么进行refresh_token会获取一个新的access_token，新的超时时间
     * access_token未超时，那么进行refresh_token不会改变access_token，但超时时间会刷新，相当于续期access_token
     *
     * @param appId
     * @param refreshToken
     */
    public WeChatAccessToken refreshToken(String appId, String refreshToken) {
        try {
            String url = "https://api.weixin.qq.com/sns/oauth2/refresh_token?" +
                    "appid=" + appId
                    + "&grant_type=refresh_token&"
                    + "refresh_token=" + refreshToken;
            OkHttpClient httpClient = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url(url).get().build();
            Response response = httpClient.newCall(request).execute();
            String data = response.body().string();
            WeChatAccessToken weChatAccessToken = JSON.parseObject(data, WeChatAccessToken.class);
            if (weChatAccessToken.getOpenid() != null && weChatAccessToken.getUnionid() != null) {
                return weChatAccessToken;
            }
        } catch (IOException e) {
            logger.error("微信登录获取access_token错误：请检查传入的参数^<>^");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 验证access_token是否过期
     *
     * @param accessToken
     * @param openId
     * @return
     */
    public boolean checkAccessToken(String accessToken, String openId) {
        try {
            String url = "https://api.weixin.qq.com/sns/auth?access_token=" +
                    accessToken + "&openid=" + openId;
            OkHttpClient httpClient = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url(url).get().build();
            Response response = httpClient.newCall(request).execute();
            String data = response.body().string();
            if (response.code() == 200) {
                WeChatResponse message = JSON.parseObject(data, WeChatResponse.class);
                if (message.getErrmsg().equals("ok")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取用户信息
     *
     * @param accessToken
     * @param openId
     * @return
     */
    public WeChatUser getUserMessage(String accessToken, String openId) {
        try {
            String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" +
                    accessToken + "&openid=" + openId;
            OkHttpClient httpClient = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url(url).get().build();
            Response response = httpClient.newCall(request).execute();
            String data = response.body().string();
            WeChatUser openUser = JSON.parseObject(data, WeChatUser.class);
            if (openUser.getNickname() != null) {
                return openUser;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取分享的信息
     * @param url       例如 www.baidu.com
     * @param appId     此appId 与以上 appId不同  此处用的是微信公众号的appid
     * @param secret
     * @param request
     * @return
     */
    public WeChatShare shareToWeChat(String url, String appId, String secret,HttpServletRequest request) {
        //项目名称 请求页面或其他地址 参数
        url = url+request.getContextPath()+request.getServletPath()+"?" + (request.getQueryString());
        WeChatShare weChatShare = WeChatShareUtil.getWeChatShare(url, appId, secret);
        return weChatShare;
    }

    //todo ---------------微信客户端获取微信用户信息--------用户微信公众号
    /**
     * 首先获取用户同意授权后的code
     * 用户允许授权后 重定向到redirect_uri的网址上，并且带上code和state参数
     * <p>
     * 传入参数：appid  redirect_uri
     *
     * @param response
     */
    public void getCodeForClient(String appId,String redirectUri, HttpServletResponse response) {
        try {
            String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appId+"&redirect_uri="+ URLEncoder.encode(redirectUri,"UTF-8")+"&response_type=code&scope=snsapi_userinfo&state=20150714#wechat_redirect";
            response.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
