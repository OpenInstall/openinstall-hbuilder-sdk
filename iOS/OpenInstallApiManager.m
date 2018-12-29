//
//  OpenInstallApiManager.m
//  HBuilder
//
//  Created by cooper on 2018/5/30.
//  Copyright © 2018年 DCloud. All rights reserved.
//

#import "OpenInstallApiManager.h"
#import "PDRCoreAppFrame.h"
#import "H5WEEngineExport.h"
#import "PDRToolSystemEx.h"
// 扩展插件中需要引入需要的系统库
#import <LocalAuthentication/LocalAuthentication.h>

@implementation OpenInstallApiManager

#pragma mark 这个方法在使用WebApp方式集成时触发，WebView集成方式不触发

/*
 * WebApp启动时触发
 * 需要在PandoraApi.bundle/feature.plist/注册插件里添加autostart值为true，global项的值设置为true
 */
- (void) onAppStarted:(NSDictionary*)options{
    NSLog(@"5+ WebApp启动时触发");
    // 可以在这个方法里向Core注册扩展插件的JS
    
    [OpenInstallSDK initWithDelegate:self];
    
}

-(void)registerWakeUpHandler:(PGMethod*)command{
    
    NSString* cbId = [command.arguments objectAtIndex:0];
    self.wakeupId = cbId;
}
-(void)getInstall:(PGMethod*)command{
    
    NSString* cbId = [command.arguments objectAtIndex:0];
    float outtime = 10.0f;
    
    if (command.arguments.count > 2) {
        if([[command.arguments objectAtIndex:1] isKindOfClass:[NSNumber class]]){
            NSNumber *timeResult = (NSNumber *)[command.arguments objectAtIndex:1];
            outtime = [timeResult floatValue];
        }
    }
    
    [[OpenInstallSDK defaultManager] getInstallParmsWithTimeoutInterval:outtime completed:^(OpeninstallData * _Nullable appData) {
        NSLog(@"OpenInstall安装参数返回值:bindData:%@,channelCode:%@",appData.data,appData.channelCode);
        
        NSDictionary *installDicResult = @{@"channelCode":appData.channelCode?:@"",@"bindData":appData.data?:@""};

        PDRPluginResult *result = [PDRPluginResult resultWithStatus:PDRCommandStatusOK messageAsDictionary:installDicResult];
        [self toCallback:cbId withReslut:[result toJSONString]];

    }];
}
-(void)reportRegister:(PGMethod*)command{
    
    [OpenInstallSDK reportRegister];
    
}
-(void)reportEffectPoint:(PGMethod*)command{
    
    NSString* pointId = [command.arguments objectAtIndex:0];
    NSNumber* value = [command.arguments objectAtIndex:1];
    long pointValue = [value longValue];
    [[OpenInstallSDK defaultManager] reportEffectPoint:pointId effectValue:pointValue];
}


+(void)universalLinkHandler:(NSURL *)url{
    
    [OpenInstallSDK defaultManager];
    if (url) {
        if ([url isKindOfClass:[NSURL class]]) {
            NSUserActivity *activity = [[NSUserActivity alloc]initWithActivityType:NSUserActivityTypeBrowsingWeb];
            activity.webpageURL = url;
            [OpenInstallSDK continueUserActivity:activity];
        }
    }
    
}
+(void)schemeUrlHandler:(NSURL *)url{
    
    [OpenInstallSDK defaultManager];
    if (url) {
        if ([url isKindOfClass:[NSURL class]]) {
            [OpenInstallSDK handLinkURL:url];
        }
    }
    
}
-(void)getWakeUpParams:(OpeninstallData *)appData{
    NSLog(@"OpenInstall拉起参数返回值:bindData:%@,channelCode:%@",appData.data,appData.channelCode);
    NSDictionary *wakeUpDicResult = @{@"channelCode":appData.channelCode?:@"",@"bindData":appData.data?:@""};
    PDRPluginResult *result = [PDRPluginResult resultWithStatus:PDRCommandStatusOK messageAsDictionary:wakeUpDicResult];
    [self toCallback:self.wakeupId withReslut:[result toJSONString]];
    //    [self asyncWriteJavascript:[NSString stringWithFormat:@"OpeninstallUrlCallBack(%@)",json]];
}

@end
