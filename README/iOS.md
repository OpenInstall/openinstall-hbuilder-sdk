## iOS 集成指南

#### 离线打包
如果要集成使用非基座包下的第三方 SDK，就必须使用离线打包。可以参考 [官方文档](http://ask.dcloud.net.cn/article/41) 进行离线打包

#### 拷贝相关文件

拷贝Openinstall官方SDK(libOpenInstallSDK.a,OpeninstallSDK.h,OpeninstallData.h)和插件类(OpenInstallApiManager.h,OpenInstallApiManager.m,OpenInstallStorage.h,OpenInstallStorage.m)到项目工程主目录下  

注意：老版本5+SDK环境下，在iOS中过早的调用openinstall.js下的方法(plus.openinstall.xxxxx)，例如在首页窗口未加载完就调用的话，有用户出现过TypeError:undefined is not an object的错误，具体情况以实际测试为准  

#### 关联 JS 插件名和 iOS 原生类
修改 `PandoraAPI.bundle` 中 `feature.plist` 文件，在其中添加JS插件别名和Native插件类的对应关系，SDK基座会根据对应关系查找并生成相应的Native对象并执行对应的方法。
``` xml
	
    <key>openinstall</key>
    <dict>
	<key>autostart</key>
	<true/>
	<key>global</key>
	<true/>
	<key>class</key>
	<string>OpenInstallApiManager</string>
    </dict>

```

在应用的 `manifest.json` 文件中还需要添加扩展插件的应用使用权限
``` json
{
  "@platforms": [
    "android",
    "iPhone",
    "iPad"
  ],
  "id": "H5E1BA598",
  "name": "OpenInstallPlugin",
  
  "permissions": {
    "Console": {
      "description": "跟踪调试输出日志"
    },
    "Events": {
      "description": "应用扩展事件"
    },
    
    "openinstall": {
      "description": "openinstall插件"
    }
  },
  
}
```

#### openinstall 的配置

##### 初始化配置
根据 `openinstall` 官方文档，在 `Info.plist` 文件中配置 `appKey` 键值对，如下：

``` xml
	<key>com.openinstall.APP_KEY</key>
	<string>“从openinstall官网后台获取应用的appkey”</string>
```

#### 以下为 `一键拉起` 功能的相关配置和代码
##### universal links配置

对于 iOS，为确保能正常跳转，AppID 必须开启 Associated Domains 功能，请到 [苹果开发者网站](https://developer.apple.com)，选择 Certificate, Identifiers & Profiles，选择相应的 AppID，开启 Associated Domains。注意：当 AppID 重新编辑过之后，需要更新相应的 mobileprovision 证书。

![associatedDev](https://res.cdn.openinstall.io/doc/assciationDev.png)

在 Xcode 中配置 openinstall 为当前应用生成的关联域名（Associated Domains）：

![添加associatedDomains](https://res.cdn.openinstall.io/doc/ios-associated-domains.png)
  
在AppDelegate中引入头文件，并添加通用链接(Universal Link)回调方法，委托openinstall插件来处理

``` objc
    #import "OpenInstallStorage.h"
    
    - (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions{

        //app在杀死的情况下，一键跳转的参数处理方法
        NSDictionary *optionsDic = [launchOptions valueForKey:UIApplicationLaunchOptionsUserActivityDictionaryKey];
        [optionsDic enumerateKeysAndObjectsUsingBlock:^(NSString * _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
            if ([key isEqualToString:@"UIApplicationLaunchOptionsUserActivityKey"]) {
                [[OpenInstallStorage share] universalLinkHandler:(NSUserActivity *)obj];
                *stop = YES;
            }
        }];
    }

    -(BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void (^)(NSArray * _Nullable))restorationHandler{
      //app在启动并退到后台时，一键跳转的参数处理方法
      [[OpenInstallStorage share] universalLinkHandler:userActivity];
      //其他第三方回调；
      return YES;
    }

```

##### scheme配置

在 `Info.plist` 文件中，在 `CFBundleURLTypes` 数组中添加应用对应的 scheme，或者在工程“TARGETS-Info-URL Types”里快速添加
（scheme的值详细获取位置：openinstall应用控制台->iOS集成->iOS应用配置）  

``` xml
	<key>CFBundleURLTypes</key>
	<array>
	    <dict>
		<key>CFBundleTypeRole</key>
		<string>Editor</string>
		<key>CFBundleURLName</key>
		<string>openinstall</string>
		<key>CFBundleURLSchemes</key>
		<array>
		    <string>"从openinstall官网后台获取应用的scheme"</string>
		</array>
	    </dict>
	</array>
```

![scheme配置](https://res.cdn.openinstall.io/doc/ios-scheme.png)


在 `AppDelegate` 中引入头文件，并添加 `scheme` 的回调方法，委托 openinstall 插件来处理

``` objc
//适用目前所有iOS版本
- (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)url
  sourceApplication:(NSString *)sourceApplication
         annotation:(id)annotation {
    
    [[OpenInstallStorage share] schemeUrlHandler:url];

    [self application:application handleOpenURL:url];
    return YES;
}

//iOS9以上，会优先走这个方法
- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(nonnull NSDictionary *)options{
    
    [[OpenInstallStorage share] schemeUrlHandler:url];
    //其他第三方回调；
     return YES;
}
```

