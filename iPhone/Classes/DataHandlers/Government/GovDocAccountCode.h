//
//  GovDocAccountCode.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovDocAccountCode : NSObject
{
    NSString                *account;
    NSDecimalNumber         *amount;
}

@property (nonatomic, strong) NSString                  *account;
@property (nonatomic, strong) NSDecimalNumber           *amount;

@end
