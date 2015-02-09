package com.zrquan.mobile.support.volley;

import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.RequestFuture;
import com.zrquan.mobile.ZrquanApplication;
import com.zrquan.mobile.support.util.LogUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyJsonRequest {
    protected static String Tag = "VolleyJsonRequest";
    private static final String ACCESS_TOKEN_HEADER_KEY = "Zrquan-Token";

    private static String accessToken;

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setAccessToken(String accessToken) {
        VolleyJsonRequest.accessToken = accessToken;
    }

    public static void get(String url, final ResponseHandler responseHandler) {
        request(url, null, null, responseHandler);
    }

    public static void get(String url, final Map<String, String> extHeaders, final ResponseHandler responseHandler) {
        request(url, extHeaders, null, responseHandler);
    }

    public static void post(String url, JSONObject params, final ResponseHandler responseHandler) {
        request(url, null, params, responseHandler);
    }

    public static void post(String url, final Map<String, String> extHeaders, JSONObject params, final ResponseHandler responseHandler) {
        request(url, extHeaders, params, responseHandler);
    }

    public static void request(String url, final Map<String, String> extHeaders, JSONObject params, final ResponseHandler responseHandler) {
        request(url, extHeaders, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            responseHandler.onResponse(response);
                        } catch (Exception e) {
                            LogUtils.d(Tag, e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        responseHandler.onErrorResponse(error);
                        LogUtils.d(Tag, "Volley Response Error:", error);
                    }
                });
    }

    public static void request(String url, final Map<String, String> extHeaders, JSONObject params
            ,  Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JsonObjectRequest req = new JsonObjectRequest(url, params, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                if (!TextUtils.isEmpty(accessToken)) {
                    headers.put(ACCESS_TOKEN_HEADER_KEY, accessToken);
                }
                if (extHeaders != null) {
                    headers.putAll(extHeaders);
                }
                LogUtils.d("headers=" + headers);
                return headers;
            }
        };

        // add the request object to the queue to be executed
        ZrquanApplication.getInstance().addToRequestQueue(req);
    }

    public static interface ResponseHandler {
        public void onResponse(JSONObject response);

        public void onErrorResponse(VolleyError error);
    }
}
