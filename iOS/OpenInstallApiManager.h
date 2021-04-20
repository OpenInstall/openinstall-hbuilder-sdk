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
#import "OpenInstallStorage.h"

@interface OpenInstallApiManager : PGPlugin<OpenInstallDelegate>

-(void)registerWakeUpHandler:(PGMethod*)command;
-(void)getInstall:(PGMethod*)command;
-(void)reportRegister:(PGMethod*)command;
-(void)reportEffectPoint:(PGMethod*)command;

@end
