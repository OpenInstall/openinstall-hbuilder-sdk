package io.openinstall.hbuilder;

import android.Manifest;
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
import io.dcloud.common.adapter.util.PermissionUtil;
import io.dcloud.common.util.JSUtil;

public class OpenInstallApiManager extends StandardFeature {

    private static final String TAG = "OpenInstallApiManager";
    private IWebview webview = null;
    private String wakeupCallBackID = null;
    private Intent wakeupIntent = null;
    private volatile boolean callInit = false;
    private volatile boolean initialized = false;
    private Configuration configuration = null;

    @Override
    public void onStart(Context context, Bundle bundle, String[] strings) {
        super.onStart(context, bundle, strings);
//        Log.d(TAG, "init");
//        OpenInstall.init(context);
    }

    public void config(IWebview pWebview, JSONArray array) {
        JSONObject options = array.optJSONObject(0);
        if (options != null) {
            boolean adEnabled = options.optBoolean("adEnabled", false);
            String oaid = options.optString("oaid", null);
            oaid = setNull(oaid);
            String gaid = options.optString("gaid", null);
            gaid = setNull(gaid);

            boolean macDisabled = options.optBoolean("macDisabled", false);
            boolean imeiDisabled = options.optBoolean("imeiDisabled", false);

            Log.d(TAG, String.format("adEnabled=%s, oaid=%s, gaid=%s, macDisabled=%s, imeiDisabled= %s",
                    adEnabled, oaid == null ? "未传入" : oaid, gaid == null ? "未传入" : gaid,
                    macDisabled, imeiDisabled));

            Configuration.Builder builder = new Configuration.Builder();
            if (macDisabled) {
                builder.macDisabled();
            }
            if (imeiDisabled) {
                builder.imeiDisabled();
            }
            configuration = builder.adEnabled(adEnabled).oaid(oaid).gaid(gaid).build();
        } else {
            Log.d(TAG, "options is null");
        }
    }

    public void init(IWebview pWebview, JSONArray array) {
        boolean permission = array.optBoolean(0, false);
        init(pWebview, permission);
    }

    private void init(IWebview pWebview, boolean permission) {
        Log.d(TAG, "init, need permission is " + permission);
        callInit = true;
        if (permission) {
            initWithPermission(pWebview);
        } else {
            initialized(pWebview);
        }
    }

    private String setNull(String res) {
        // 传入 null 或者 未定义，设置为 null
        if (res == null || res.equalsIgnoreCase("null")
                || res.equalsIgnoreCase("undefined")) {
            return null;
        }
        return res;
    }

    private void initWithPermission(final IWebview pWebview) {
        Log.d(TAG, "initWithPermission");
        final Context context = pWebview.getContext();
        int result = PermissionUtil.checkSelfPermission(pWebview.getActivity(), Manifest.permission.READ_PHONE_STATE);
        if (result != 0) {
            Log.d(TAG, "Permission result = " + result + ", request READ_PHONE_STATE permission");
            PermissionUtil.requestSystemPermissions(pWebview.getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, 999, new PermissionUtil.Request() {
                @Override
                public void onGranted(String s) {
                    Log.d(TAG, "onGranted = " + s);
                    initialized(pWebview);
                }

                @Override
                public void onDenied(String s) {
                    Log.d(TAG, "onDenied = " + s);
                    initialized(pWebview);
                }
            });
        } else {
            initialized(pWebview);
        }
    }


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
        if (!callInit) {
            boolean permission = array.optBoolean(1, false);
            init(pWebview, permission);
        }
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
