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

            Log.d(TAG, String.format("adEnabled=%s, oaid=%s, gaid=%s, macDisabled=%s, imeiDisabled= %s",
                    configuration.isAdEnabled(), configuration.getOaid(), configuration.getGaid(),
                    configuration.isMacDisabled(), configuration.isImeiDisabled()));

        } else {
            Log.d(TAG, "options is null");
        }
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

//    public void requestPermission(final IWebview pWebview, JSONArray array) {
//        Log.d(TAG, "requestPermission");
//        final String callBackID = array.optString(0);
//        int result = PermissionUtil.checkSelfPermission(pWebview.getActivity(), Manifest.permission.READ_PHONE_STATE);
//        if (result != 0) {
//            Log.d(TAG, "Permission result = " + result + ", request READ_PHONE_STATE permission");
//            PermissionUtil.requestSystemPermissions(pWebview.getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, 999, new PermissionUtil.Request() {
//                @Override
//                public void onGranted(String s) {
//                    Log.d(TAG, "onGranted = " + s);
//                    JSUtil.execCallback(pWebview, callBackID, "true", JSUtil.OK, false);
//                }
//
//                @Override
//                public void onDenied(String s) {
//                    Log.d(TAG, "onDenied = " + s);
//                    JSUtil.execCallback(pWebview, callBackID, "false", JSUtil.OK, false);
//                }
//            });
//        } else {
//            JSUtil.execCallback(pWebview, callBackID, "true", JSUtil.OK, false);
//        }
//    }

    private void initialized(final IWebview pWebview) {
        OpenInstall.init(pWebview.getContext(), configuration);
        initialized = true;
        if (wakeupIntent != null) {
            OpenInstall.getWakeUp(wakeupIntent, new AppWakeUpAdapter() {
                @Override
                public void onWakeUp(AppData appData) {
                    JSONObject dataJson = new JSONObject();
                    try {
                        dataJson.put("channelCode", appData.getChannel());
                        dataJson.put("bindData", appData.getData());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JSUtil.execCallback(pWebview, wakeupCallBackID, dataJson, JSUtil.OK, false);
                    wakeupIntent = null;
                }
            });
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

    private void getWakeUp(Intent intent, final IWebview pWebview, final String callBackID) {
        if (initialized) {
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
        } else {
            wakeupIntent = intent;
        }
    }

    public void getInstall(final IWebview pWebview, JSONArray array) {
        Log.d(TAG, "getInstall");
        final String callBackID = array.optString(0);
        int timeout = -1;
        if (!array.isNull(1)) {
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
        }, timeout);
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
