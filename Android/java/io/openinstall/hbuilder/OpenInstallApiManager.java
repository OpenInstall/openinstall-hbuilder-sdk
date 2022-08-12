package io.openinstall.hbuilder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.fm.openinstall.Configuration;
import com.fm.openinstall.OpenInstall;
import com.fm.openinstall.listener.AppInstallAdapter;
import com.fm.openinstall.listener.AppInstallRetryAdapter;
import com.fm.openinstall.listener.AppWakeUpListener;
import com.fm.openinstall.model.AppData;
import com.fm.openinstall.model.Error;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.dcloud.common.DHInterface.ISysEventListener;
import io.dcloud.common.DHInterface.IWebview;
import io.dcloud.common.DHInterface.StandardFeature;
import io.dcloud.common.util.JSUtil;

public class OpenInstallApiManager extends StandardFeature {

    private static final String TAG = "OpenInstallApiManager";
    private IWebview webview = null;
    private String wakeupCallBackID = null;
    private Intent wakeupIntent = null;
    private volatile boolean initialized = false;
    private Configuration configuration = null;

    @Override
    public void onStart(Context context, Bundle bundle, String[] strings) {
        super.onStart(context, bundle, strings);
    }

    public void config(IWebview pWebview, JSONArray array) {
        JSONObject options = array.optJSONObject(0);
        if (options != null) {
            Configuration.Builder builder = new Configuration.Builder();
            boolean adEnabled = options.optBoolean("adEnabled", false);
            builder.adEnabled(adEnabled);
            String oaid = options.optString("oaid", null);
            builder.oaid(setNull(oaid));
            String gaid = options.optString("gaid", null);
            builder.gaid(setNull(gaid));
            boolean macDisabled = options.optBoolean("macDisabled", false);
            if (macDisabled) {
                builder.macDisabled();
            }
            boolean imeiDisabled = options.optBoolean("imeiDisabled", false);
            if (imeiDisabled) {
                builder.imeiDisabled();
            }
            configuration = builder.build();

            Log.d(TAG, String.format("config adEnabled=%s, oaid=%s, gaid=%s, macDisabled=%s, imeiDisabled= %s",
                    configuration.isAdEnabled(), configuration.getOaid(), configuration.getGaid(),
                    configuration.isMacDisabled(), configuration.isImeiDisabled()));

        } else {
            Log.e(TAG, "options is null");
        }
    }

    public void clipBoardEnabled(IWebview pWebview, JSONArray array){
        boolean enabled = array.optBoolean(0, true);
        OpenInstall.clipBoardEnabled(enabled);
    }

    public void serialEnabled(IWebview pWebview, JSONArray array){
        boolean enabled = array.optBoolean(0, true);
        OpenInstall.serialEnabled(enabled);
    }

    public void init(IWebview pWebview, JSONArray array) {
        initialized(pWebview);
    }

    private String setNull(String res) {
        // 传入 null 或者 未定义，设置为 null
        if (res == null || res.equalsIgnoreCase("null")
                || res.equalsIgnoreCase("undefined")) {
            return null;
        }
        return res;
    }

    private void initialized(final IWebview pWebview) {
        OpenInstall.init(pWebview.getContext(), configuration);
        initialized = true;
        if (wakeupIntent != null) {
            getWakeUp(wakeupIntent, pWebview, wakeupCallBackID);
            wakeupIntent = null;
        }
    }

    public void registerWakeUpHandler(final IWebview pWebview, JSONArray array) {
        Log.d(TAG, "registerWakeUpHandler");
        String callBackID = array.optString(0);

        webview = pWebview;
        wakeupCallBackID = callBackID;
        // 自己注册了监听并处理 onNewIntent 事件
        pWebview.obtainApp().registerSysEventListener(new ISysEventListener() {
            @Override
            public boolean onExecute(SysEventType sysEventType, Object o) {
                if (sysEventType == SysEventType.onNewIntent) {
                    String dataString = (String) o;
                    // OpenInstall 原生 SDK 有判断了是否是 android.intent.action.VIEW
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(dataString));
                    if (webview != null && wakeupCallBackID != null) {
                        getWakeUp(intent, webview, wakeupCallBackID);
                    }
                }
                return false;
            }
        }, SysEventType.onNewIntent);

        Intent intent = pWebview.getActivity().getIntent();
        if (intent == null) {
            return;
        }
        getWakeUp(intent, pWebview, callBackID);
    }

    private void getWakeUp(Intent intent, final IWebview pWebview, final String callBackID) {
        if (initialized) {
            OpenInstall.getWakeUpAlwaysCallback(intent, new AppWakeUpListener() {
                @Override
                public void onWakeUpFinish(AppData appData, Error error) {
                    JSONObject dataJson = parseData(appData);
                    // 最后一个参数 boolean 表示 keepCallback
                    JSUtil.execCallback(pWebview, callBackID, dataJson, JSUtil.OK, true);
                }
            });
        } else {
            wakeupIntent = intent;
        }
    }

    public void getInstall(final IWebview pWebview, JSONArray array) {
        Log.d(TAG, "getInstall");
        final String callBackID = array.optString(0);
        int timeout = 10;
        if (!array.isNull(1)) {
            timeout = array.optInt(1);
        }
        OpenInstall.getInstall(new AppInstallAdapter() {
            @Override
            public void onInstall(AppData appData) {
                JSONObject dataJson = parseData(appData);
                JSUtil.execCallback(pWebview, callBackID, dataJson, JSUtil.OK, false);
            }
        }, timeout);
    }

    public void getInstallCanRetry(final IWebview pWebview, JSONArray array) {
        Log.d(TAG, "getInstallCanRetry");
        final String callBackID = array.optString(0);
        int timeout = 3;
        if (!array.isNull(1)) {
            timeout = array.optInt(1);
        }
        OpenInstall.getInstallCanRetry(new AppInstallRetryAdapter() {
            @Override
            public void onInstall(AppData appData, boolean retry) {
                JSONObject dataJson = parseData(appData);
                try {
                    dataJson.put("retry", retry);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSUtil.execCallback(pWebview, callBackID, dataJson, JSUtil.OK, false);
            }
        }, timeout);
    }

    public void reportRegister(IWebview pWebview, JSONArray array) {
        OpenInstall.reportRegister();
    }

    public void reportEffectPoint(IWebview pWebview, JSONArray array) {
        String pointId = array.optString(0);
        long pointValue = array.optLong(1);
        if (TextUtils.isEmpty(pointId)) {
            Log.d(TAG, "reportEffectPoint pointId is empty");
        } else {
            Log.d(TAG, String.format("reportEffectPoint(%s, %d)", pointId, pointValue));
            OpenInstall.reportEffectPoint(pointId, pointValue);
        }
    }

    private JSONObject parseData(AppData appData) {
        JSONObject dataJson = new JSONObject();
        if (appData != null) {
            try {
                dataJson.put("channelCode", appData.getChannel());
                dataJson.put("bindData", appData.getData());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return dataJson;
    }

}
