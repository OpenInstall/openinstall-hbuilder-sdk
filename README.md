# openinstall-hbuilder-sdk

uni-app集成openinstall请前往DCloud插件市场 https://ext.dcloud.net.cn/plugin?id=692 

针对使用了移动广告效果监测功能的集成，需要参考 [广告平台接入补充文档](#ad)

集成或使用有任何问题，请 [联系我们](https://www.openinstall.io/)

## Android 集成指南

集成 openinstall SDK 到 Hbuilder Android 项目中，请参考 [Android 集成指南](README/Android.md)

## iOS 集成指南

集成 openinstall SDK 到 Hbuilder iOS 项目中，请参考 [iOS 集成指南](README/iOS.md)

## 插件使用

调用openinstall相关api时，需要引入 JS 文件
``` html
<script type="text/javascript" src="./js/openinstall.js"></script>
```

### 1 初始化
App 启动时，请确保用户同意《隐私政策》之后，再调用初始化；如果用户不同意，则不进行openinstall SDK初始化。参考 [应用合规指南](https://www.openinstall.io/doc/rules.html) 
``` js
    plus.openinstall.init();
```
> **注意：** 插件内部不再自动初始化，请开发者在合适的时机主动调用初始化接口；不调用初始化，后续所有api调用都会失败

### 2 快速安装和一键拉起

在应用启动时，注册拉起回调。这样当 App 被拉起时，会回调传入的方法，并在回调中获取拉起数据
``` js
document.addEventListener('plusready',function(){
    plus.openinstall.registerWakeUpHandler(function(data){
            console.log("wakeup : channelCode= " + data.channelCode + ", bindData=" + data.bindData);
        });

});
```

### 3 携带参数安装（高级版功能）

在应用需要安装参数时，调用以下 api 获取由 SDK 保存的安装参数
``` js
function getInstall(){
    plus.openinstall.getInstall(function(data){
        console.log("getInstall : channelCode= " + data.channelCode + ", bindData=" + data.bindData);
    }, 8);
}
```

### 4 渠道统计（高级版功能）

SDK 会自动完成访问量、点击量、安装量、活跃量、留存率等统计工作。其它业务相关统计由开发人员使用 api 上报

#### 4.1 注册统计
请确保在用户注册成功后，调用接口上报注册量
``` js
function reportRegister(){
    plus.openinstall.reportRegister();
}
```

#### 4.2 效果点统计
统计终端用户对某些特殊业务的使用效果，如充值金额，分享次数等等。调用接口前，请先进入 openinstall 控制台的 “效果点管理” 中添加对应的效果点，第一个参数对应控制台中的 **效果点ID**
``` js
function reportEffectPoint(){
    plus.openinstall.reportEffectPoint("effect_test", 1);
}
```

## 导出apk/ipa包并上传
集成完毕后，导出iOS/Android安装包上传[openinstall控制台](https://developer.openinstall.io/)，openinstal会检查应用的集成配置  
![上传ipa安装包](https://res.cdn.openinstall.io/doc/upload-ipa-jump.png)


上传完成后即可开始在线模拟测试，体验完整的App安装/跳转流程  
![在线测试](https://res.cdn.openinstall.io/doc/js-test.png)


---
<a id=ad></a>

## 广告平台接入补充文档

### Android 平台

1、针对广告平台接入，新增配置接口，在调用 `init` 之前调用。参考 [广告平台对接Android集成指引](https://www.openinstall.io/doc/ad_android.html)
``` js
    var options = {
        adEnabled: true, 
    }
    plus.openinstall.configAndroid(options);
```
传入参数说明：   
| 参数名| 参数类型 | 描述 |  
| --- | --- | --- |
| adEnabled| bool | 广告平台接入开关（必须） |
| macDisabled | bool | 是否禁止 SDK 获取 mac 地址 |
| imeiDisabled | bool | 是否禁止 SDK 获取 imei |
| gaid | string | 通过 google api 获取到的 advertisingId，SDK 将不再获取gaid |
| oaid | string | 通过移动安全联盟获取到的 oaid，SDK 将不再获取oaid |

2、为了精准地匹配到渠道，需要获取设备唯一标识码（IMEI），因此需要在 `AndroidManifest.xml` 中添加权限声明
``` xml
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
```
3、在权限申请成功后，再进行openinstall初始化
> **注意：** 插件内部不再提供权限申请功能，并且 `init(permission)` 接口已移除，请开发者自行进行权限申请

### iOS 平台

1、将 `iOS/OpenInstallApiManager.m` 文件替换为 `ad-track/OpenInstallApiManager.m ` 文件  

2、需要在Info.plist文件中配置权限  
``` xml
<key>NSUserTrackingUsageDescription</key>
<string>请允许，以获取和使用您的IDFA</string>
```

> **备注：** 2021年，iOS14.5苹果公司将正式启用idfa新隐私政策，详情可参考：[广告平台对接iOS集成指引](https://www.openinstall.io/doc/ad_ios.html)

3、ASA渠道相关详细文档参考：[ASA渠道使用指南](https://www.openinstall.io/doc/asa.html)
