//
//  OpenInstallStorage.h
//  HBuilder
//
//  Created by Mr.Huang on 2021/4/16.
//  Copyright © 2021 DCloud. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "OpenInstallSDK.h"

NS_ASSUME_NONNULL_BEGIN

@interface OpenInstallStorage : NSObject

@property (nonatomic, strong)NSUserActivity *_Nullable userActivity;
@property (nonatomic, strong)NSURL *_Nullable urlScheme;
@property (nonatomic, assign)BOOL isInit;

@property (nonatomic, copy)NSString *_Nullable wakeupId;
@property (nonatomic, strong)NSDictionary *_Nullable wakeupDic;

+(instancetype)share;

-(void)universalLinkHandler:(NSUserActivity *)activity;
-(void)schemeUrlHandler:(NSURL *)url;

@end

NS_ASSUME_NONNULL_END
