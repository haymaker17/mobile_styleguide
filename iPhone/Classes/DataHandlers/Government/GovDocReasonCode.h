//
//  GovDocReasonCode.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovDocReasonCode : NSObject
{
    NSString                *code;
    NSString                *comments;
}

@property (nonatomic, strong) NSString * code;
@property (nonatomic, strong) NSString * comments;

@end
