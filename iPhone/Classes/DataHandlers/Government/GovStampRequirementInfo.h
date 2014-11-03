//
//  GovStampRequirementInfo.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/20/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovStampRequirementInfo : NSObject
{
    NSString            *stampName;
    NSNumber            *reasonRequired;  // Bool
}

@property (nonatomic, strong) NSString              *stampName;
@property (nonatomic, strong) NSNumber              *reasonRequired;

+ (void) registerStampReasonInfo:(GovStampRequirementInfo*) info;
+ (NSNumber* ) requiresReason:(NSString*)stampName;

@end
