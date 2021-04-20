//
//  OpenInstallApiManager.m
//  HBuilder
//
//  Created by cooper on 2018/5/30.
//  Copyright © 2018年 DCloud. All rights reserved.
//

#import "OpenInstallApiManager.h"


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
    [OpenInstallStorage share].isInit = YES;
}

-(void)registerWakeUpHandler:(PGMethod*)command{
    
    NSString* cbId = [command.arguments objectAtIndex:0];
    OpenInstallStorage *storage = [OpenInstallStorage share];
    storage.wakeupId = cbId;
    if (storage.wakeupDic.count != 0) {
        PDRPluginResult *result = [PDRPluginResult resultWithStatus:PDRCommandStatusOK messageAsDictionary:storage.wakeupDic];
        result.keepCallback = YES;
        [self toCallback:storage.wakeupId withReslut:[result toJSONString]];
        storage.wakeupDic = nil;
    }else{

        [OpenInstallSDK initWithDelegate:self];
        
        if (storage.userActivity) {
            [OpenInstallSDK continueUserActivity:storage.userActivity];
            storage.userActivity = nil;
        }
        if (storage.urlScheme) {
            [OpenInstallSDK handLinkURL:storage.urlScheme];
            storage.urlScheme = nil;
        }
    }

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
        
        NSString *channelID = @"";
        NSString *datas = @"";
        if (appData.data) {
            datas = [self jsonStringWithObject:appData.data];
        }
        if (appData.channelCode) {
            channelID = appData.channelCode;
        }
        NSDictionary *installDicResult = @{@"channelCode":channelID,@"bindData":datas};

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

-(void)getWakeUpParams:(OpeninstallData *)appData{
    NSLog(@"OpenInstall拉起参数返回值:bindData:%@,channelCode:%@",appData.data,appData.channelCode);
    NSString *channelID = @"";
    NSString *datas = @"";
    if (appData.data) {
        datas = [self jsonStringWithObject:appData.data];
    }
    if (appData.channelCode) {
        channelID = appData.channelCode;
    }
    NSDictionary *wakeUpDicResult = @{@"channelCode":channelID,@"bindData":datas};
    
    PDRPluginResult *result = [PDRPluginResult resultWithStatus:PDRCommandStatusOK messageAsDictionary:wakeUpDicResult];
    result.keepCallback = YES;
    OpenInstallStorage *storage = [OpenInstallStorage share];
    if (storage.wakeupId) {
        [self toCallback:storage.wakeupId withReslut:[result toJSONString]];
    }else{
        storage.wakeupDic = wakeUpDicResult;
    }
}


- (NSString *)jsonStringWithObject:(id)jsonObject{
    
    id arguments = (jsonObject == nil ? [NSNull null] : jsonObject);
    
    NSArray* argumentsWrappedInArr = [NSArray arrayWithObject:arguments];
    
    NSString* argumentsJSON = [self cp_JSONString:argumentsWrappedInArr];
    
    argumentsJSON = [argumentsJSON substringWithRange:NSMakeRange(1, [argumentsJSON length] - 2)];
    
    return argumentsJSON;
}
- (NSString *)cp_JSONString:(NSArray *)array{
    NSError *error = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:array
                                                       options:0
                                                         error:&error];
    
    NSString *jsonString = [[NSString alloc] initWithData:jsonData
                                                 encoding:NSUTF8StringEncoding];
    
    if ([jsonString length] > 0 && error == nil){
        return jsonString;
    }else{
        return @"";
    }
}

@end
