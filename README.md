# openinstall-hbuilder-sdk

使用 dcloud 的 5+sdk 集成 openinstall

## Android 集成指南

集成 openinstall SDK 到 Hbuilder Android 项目中，请参考 [Android 集成指南][README-Android]

## iOS 集成指南

集成 openinstall SDK 到 Hbuilder iOS 项目中，请参考 [iOS 集成指南][README-iOS]

[README-Android]: README/README-Android.md
[README-iOS]: README/README-iOS.md
## 插件使用

#### 引入 JS 文件
``` html
<script type="text/javascript" src="./js/openinstall.js"></script>
```
#### 获取拉起数据
在应用启动时，注册拉起回调。这样当 App 被拉起时，会回调传入的方法，并在回调中获取拉起数据
``` js
document.addEventListener('plusready',function(){
    plus.openinstall.registerWakeUpHandler(function(data){
                console.log("wakeup : channelCode= "
                    + data.channelCode + ", bindData=" + data.bindData);
                alert("wakeup : channelCode= " + data.channelCode + ", bindData=" + data.bindData);
            });

},false);
```
#### 获取安装来源数据  
在需要获取安装来源数据时，调用以下代码，在回调中获取参数
``` js
function getInstall(){
    plus.openinstall.getInstall(function(data){
        console.log("getInstall : channelCode= "
                + data.channelCode + ", bindData=" + data.bindData);
    }, 8);
}
```
#### 其他统计代码
用户注册成功后，调用以下代码，上报注册统计
``` js
function reportRegister(){
    plus.openinstall.reportRegister();
}
```
统计终端用户对某些特殊业务的使用效果，如充值金额，分享次数等等，调用以下代码
``` js
function reportEffectPoint(){
    plus.openinstall.reportEffectPoint("effect_test", 1);
}
```
