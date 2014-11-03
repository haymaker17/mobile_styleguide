//
//  Policy.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 4/25/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Policy : NSObject
{
    NSString            *polKey;
    NSString            *approvalConfirmationKey;
    NSString            *submitConfirmationKey;
}

@property (nonatomic, strong) NSString      *polKey;
@property (nonatomic, strong) NSString      *approvalConfirmationKey;
@property (nonatomic, strong) NSString      *submitConfirmationKey;

+ (NSMutableDictionary*) getXmlToPropertyMap;

@end
