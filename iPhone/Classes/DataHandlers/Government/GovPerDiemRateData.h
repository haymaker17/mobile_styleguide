//
//  GovPerDiemRateData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "GovPerDiemRate.h"

@interface GovPerDiemRateData : MsgResponderCommon
{
    NSString            *crnCode;
    NSDate              *effectiveDate;
    NSDate              *expirationDate;
    NSString            *location;
    NSString            *stateOrCountryCode;
    GovPerDiemRate      *currentPerDiemRate;
}

@property (nonatomic, strong) NSString                  *crnCode;
@property (nonatomic, strong) NSString                  *location;
@property (nonatomic, strong) NSDate                    *effectiveDate;
@property (nonatomic, strong) NSDate                    *expirationDate;
@property (nonatomic, strong) NSString                  *stateOrCountryCode;
@property (nonatomic, strong) GovPerDiemRate            *currentPerDiemRate;

@end
