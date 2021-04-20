//
//  OpenInstallStorage.m
//  HBuilder
//
//  Created by Mr.Huang on 2021/4/16.
//  Copyright © 2021 DCloud. All rights reserved.
//

#import "OpenInstallStorage.h"

@implementation OpenInstallStorage

+(instancetype)share
{
    static OpenInstallStorage *storage = nil;
    static dispatch_once_t oneToken;
    dispatch_once(&oneToken, ^{
        storage = [[OpenInstallStorage alloc]init];
        storage.isInit = NO;
        storage.userActivity = nil;
        storage.urlScheme = nil;
        storage.wakeupDic = [[NSDictionary alloc]init];
        storage.wakeupId = nil;
    });
    return  storage;
}

-(void)universalLinkHandler:(NSUserActivity *)activity{
    if (self.isInit) {
        [OpenInstallSDK continueUserActivity:activity];
    }else{
        self.userActivity = activity;
    }
}
-(void)schemeUrlHandler:(NSURL *)url{
    if (self.isInit) {
        [OpenInstallSDK handLinkURL:url];
    }else{
        self.urlScheme = url;
    }
}

@end
