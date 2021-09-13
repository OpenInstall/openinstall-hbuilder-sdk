//
//  OpeninstallData.h
//  OpenInstallSDK
//
//  Created by cooper on 2018/4/17.
//  Copyright © 2018年 cooper. All rights reserved.
//

#import <Foundation/Foundation.h>

extern NSString *const OP_Idfa_Id;
extern NSString *const OP_ASA_Token;

@interface OpeninstallData : NSObject<NSCopying>

- (instancetype)initWithData:(NSDictionary *)data
                 channelCode:(NSString *)channelCode;
                

@property (nonatomic,strong) NSDictionary *data;//动态参数
@property (nonatomic,copy) NSString *channelCode;//渠道编号


@end
