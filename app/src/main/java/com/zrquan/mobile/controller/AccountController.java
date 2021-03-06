package com.zrquan.mobile.controller;

import android.content.Context;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import com.google.gson.JsonObject;

import com.zrquan.mobile.ZrquanApplication;
import com.zrquan.mobile.dao.AccountDao;
import com.zrquan.mobile.event.AccountEvent;
import com.zrquan.mobile.model.Account;
import com.zrquan.mobile.support.enums.EventCode;
import com.zrquan.mobile.support.enums.EventType;
import com.zrquan.mobile.support.enums.ServerCode;
import com.zrquan.mobile.support.util.UrlUtils;
import com.zrquan.mobile.support.volley.VolleyJsonRequest;
import com.zrquan.mobile.support.volley.VolleyRequestBase;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class AccountController {

    public static void sendVerifyCode(String phoneNum) {
        sendVerifyCode(phoneNum, false);
    }

    //发送验证码
    public static void sendVerifyCode(String phoneNum, boolean ignoreMobileCheck) {
        // pass second argument as "null" for GET requests
        Map<String, String> params = new HashMap<>();
        params.put("mobile", phoneNum);
        if (ignoreMobileCheck) {
            params.put("ignore_mobile_check", "true");
        }
        String url = UrlUtils.getUrlWithParams("users/send_verify_code", params);
        VolleyJsonRequest.get(url, new VolleyRequestBase.ResponseHandler() {
            @Override
            public void onResponse(JsonObject response) {
                String code = response.get("code").getAsString();
                AccountEvent accountEvent = new AccountEvent();
                accountEvent.setEventType(EventType.AE_NET_SEND_VERIFY_CODE);
                if (code.equals(ServerCode.S_OK.name())) {
                    accountEvent.setEventCode(EventCode.S_OK);
                    accountEvent.setServerCode(ServerCode.S_OK);
                    JsonObject results = response.get("results").getAsJsonObject();
                    String verifyCode = results.get("verify_code").getAsString();
                    accountEvent.setVerifyCode(verifyCode);
                } else {
                    accountEvent.setEventCode(EventCode.FA_SERVER_ERROR);
                    accountEvent.setServerCode(ServerCode.valueOf(code));
                }
                EventBus.getDefault().post(accountEvent);
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    //根据AccessToken验证当前已登录用户
    public static void verifyAccount(final String accessToken) {
        VolleyRequestBase.setAccessToken(accessToken);
        String url = UrlUtils.getUrl("users/verify");

        VolleyJsonRequest.get(url, new VolleyRequestBase.ResponseHandler() {
            @Override
            public void onResponse(JsonObject response) {
                String code = response.get("code").getAsString();
                if (code.equals(ServerCode.S_OK.name())) {
                    AccountEvent accountEvent = new AccountEvent();
                    accountEvent.setEventType(EventType.AE_NET_VERIFY_JWT);
                    accountEvent.setEventCode(EventCode.S_OK);
                    accountEvent.setServerCode(ServerCode.S_OK);
                    JsonObject results = response.get("results").getAsJsonObject();
                    JsonObject userInfo = results.get("user").getAsJsonObject();
                    accountEvent.setUserInfo(userInfo);
                    EventBus.getDefault().post(accountEvent);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    AccountEvent accountEvent = new AccountEvent();
                    accountEvent.setEventType(EventType.AE_NET_VERIFY_JWT);
                    accountEvent.setEventCode(EventCode.FA_SERVER_TIMEOUT);
                    EventBus.getDefault().post(accountEvent);
                }
            }
        });
    }

    //用户注册
    public static void registerAccount(String verifyCode, String phoneNum, String password, String trueName
            , int industryId, String latestSchoolName) {
        String url = UrlUtils.getUrl("users/registration");

        JsonObject params = new JsonObject();
        params.addProperty("verify_code", verifyCode);
        params.addProperty("mobile", phoneNum);
        params.addProperty("password", password);
        params.addProperty("name", trueName);
        params.addProperty("industry_id", industryId);
        params.addProperty("latest_school_name", latestSchoolName);

        VolleyJsonRequest.post(url, params, new VolleyRequestBase.ResponseHandler() {
            @Override
            public void onResponse(JsonObject response) {
                String code = response.get("code").getAsString();
                AccountEvent accountEvent = new AccountEvent();
                accountEvent.setEventType(EventType.AE_NET_REGISTER);
                if (code.equals(ServerCode.S_OK.name())) {
                    accountEvent.setEventCode(EventCode.S_OK);
                    accountEvent.setServerCode(ServerCode.S_OK);
                } else {
                    accountEvent.setEventCode(EventCode.FA_SERVER_ERROR);
                    accountEvent.setServerCode(ServerCode.valueOf(code));
                }
                EventBus.getDefault().post(accountEvent);
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    //用户登陆
    public static void loginAccount(String mobile, String password) {
        String url = UrlUtils.getUrl("users/session");

        JsonObject params = new JsonObject();
        params.addProperty("mobile", mobile);
        params.addProperty("password", password);

        VolleyJsonRequest.post(url, params, new VolleyRequestBase.ResponseHandler() {
            @Override
            public void onResponse(JsonObject response) {
                    String code = response.get("code").getAsString();
                    AccountEvent accountEvent = new AccountEvent();
                    accountEvent.setEventType(EventType.AE_NET_LOGIN);
                    if (code.equals(ServerCode.S_OK.name())) {
                        accountEvent.setEventCode(EventCode.S_OK);
                        accountEvent.setServerCode(ServerCode.S_OK);
                        JsonObject results = response.get("results").getAsJsonObject();
                        String token = results.get("token").getAsString();
                        accountEvent.setToken(token);
                        JsonObject userInfo = results.get("user").getAsJsonObject();
                        accountEvent.setUserInfo(userInfo);
                    } else {
                        accountEvent.setEventCode(EventCode.FA_SERVER_ERROR);
                        accountEvent.setServerCode(ServerCode.valueOf(code));
                    }
                    EventBus.getDefault().post(accountEvent);
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    //注销账户
    public static void logoutAccount(Context context) {
        Account account = ZrquanApplication.getInstance().getAccount();
        account.setVerified(false);
        account.setAccessToken("");
        ZrquanApplication.getInstance().setAccount(account);
        new AccountDao().saveAccount(context, account);
    }

    //重设密码
    public static void resetPassword(String mobile, String newPassword, String verifyCode) {
        String url = UrlUtils.getUrl("users/reset_password");

        JsonObject params = new JsonObject();
        params.addProperty("mobile", mobile);
        params.addProperty("new_password", newPassword);
        params.addProperty("verify_code", verifyCode);

        VolleyJsonRequest.post(url, params, new VolleyRequestBase.ResponseHandler() {
            @Override
            public void onResponse(JsonObject response) {
                String code = response.get("code").getAsString();
                AccountEvent accountEvent = new AccountEvent();
                accountEvent.setEventType(EventType.AE_NET_RESET_PASSWORD);
                if (code.equals(ServerCode.S_OK.name())) {
                    accountEvent.setEventCode(EventCode.S_OK);
                    accountEvent.setServerCode(ServerCode.S_OK);
                } else {
                    accountEvent.setEventCode(EventCode.FA_SERVER_ERROR);
                    accountEvent.setServerCode(ServerCode.valueOf(code));
                }
                EventBus.getDefault().post(accountEvent);
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }
}
