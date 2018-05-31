//
//  OpenInstallApiManager.h
//  HBuilder
//
//  Created by cooper on 2018/5/30.
//  Copyright © 2018年 DCloud. All rights reserved.
//

#include "PGPlugin.h"
#include "PGMethod.h"
#import <Foundation/Foundation.h>
#import "OpenInstallSDK.h"

@interface OpenInstallApiManager : PGPlugin<OpenInstallDelegate>

@property (nonatomic, copy)NSString *wakeupId;

+(void)universalLinkHandler:(NSURL *)url;
+(void)schemeUrlHandler:(NSURL *)url;

-(void)registerWakeUpHandler:(PGMethod*)command;
-(void)getInstall:(PGMethod*)command;
-(void)reportRegister:(PGMethod*)command;
-(void)reportEffectPoint:(PGMethod*)command;

@end
