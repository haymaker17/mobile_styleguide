//
//  FareViolationDetails.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 18/03/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ViolationReason.h"

@protocol FareViolationDetails <NSObject>

@property (nonatomic, strong) NSString *violationReason;
@property (nonatomic, strong) NSString *violationJustification;

- (NSArray *)violationTextsAsArray;
- (NSString *)violationTextsNewLineSeparated;
- (void)setViolationReasonUserSelection:(ViolationReason *)reason;
- (NSString *)getViolationReasonDescription;
- (NSString *)getViolationReasonCode;
- (NSString *)getFareType;

@end
