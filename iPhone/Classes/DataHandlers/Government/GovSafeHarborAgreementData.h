//
//  GovSafeHarborAgreementData.h
//  ConcurMobile
//
//  Created by Shifan Wu on 1/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MsgResponderCommon.h"

@interface GovSafeHarborAgreementData : MsgResponderCommon
{
    NSString        *agreeValue;     // true or false
}

@property (nonatomic, strong) NSString      *agreeValue;
@end
