package io.openinstall.hbuilder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.fm.openinstall.OpenInstall;
import com.fm.openinstall.listener.AppInstallAdapter;
import com.fm.openinstall.listener.AppWakeUpAdapter;
import com.fm.openinstall.model.AppData;

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

    @Override
    public void onStart(Context context, Bundle bundle, String[] strings) {
        super.onStart(context, bundle, strings);
        Log.d(TAG, "init");
        OpenInstall.init(context);
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
                    Intent intent = new Intent();
                    intent.setData(Uri.parse(dataString));
                    if (webview != null && wakeupCallBackID != null) {
                        getWakeUp(intent, webview, wakeupCallBackID);
                    }
                }
                return false;
            }
        }, SysEventType.onNewIntent);

        Intent intent = pWebview.getActivity().getIntent();
        if (intent == null || TextUtils.isEmpty(intent.getDataString())) {
            return;
        }
        getWakeUp(intent, pWebview, callBackID);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 由于 5+sdk 的 bug 导致 onNewIntent 未被回调
//        if (webview != null && wakeupCallBackID != null) {
//            getWakeUp(intent, webview, wakeupCallBackID);
//        }
    }

    private void getWakeUp(Intent intent, final IWebview pWebview, final String callBackID) {
        OpenInstall.getWakeUp(intent, new AppWakeUpAdapter() {
            @Override
            public void onWakeUp(AppData appData) {
                JSONObject dataJson = new JSONObject();
                try {
                    dataJson.put("channelCode", appData.getChannel());
                    dataJson.put("bindData", appData.getData());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSUtil.execCallback(pWebview, callBackID, dataJson, JSUtil.OK, false);
            }
        });
    }

    public void getInstall(final IWebview pWebview, JSONArray array) {
        Log.d(TAG, "getInstall");
        final String callBackID = array.optString(0);
        int timeout = -1;
        if (array.isNull(1)) {
            timeout = array.optInt(1);
        }
        OpenInstall.getInstall(new AppInstallAdapter() {
            @Override
            public void onInstall(AppData appData) {
                JSONObject dataJson = new JSONObject();
                try {
                    dataJson.put("channelCode", appData.getChannel());
                    dataJson.put("bindData", appData.getData());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSUtil.execCallback(pWebview, callBackID, dataJson, JSUtil.OK, false);
            }
        }, timeout * 1000);
    }

    public void reportRegister(IWebview pWebview, JSONArray array) {
        Log.d(TAG, "reportRegister");
        OpenInstall.reportRegister();
    }

    public void reportEffectPoint(IWebview pWebview, JSONArray array) {
        Log.d(TAG, "reportEffectPoint");
        String pointId = array.optString(0);
        long pointValue = array.optLong(1);
        OpenInstall.reportEffectPoint(pointId, pointValue);
    }

}
