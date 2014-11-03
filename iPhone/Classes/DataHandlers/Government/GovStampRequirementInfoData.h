//
//  GovStampRequirementInfoData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "GovStampRequirementInfo.h"

@interface GovStampRequirementInfoData : MsgResponderCommon
{
    NSString                        *stampName;
    GovStampRequirementInfo         *reqInfo;
}

@property (nonatomic, strong) NSString                          *stampName;
@property (nonatomic, strong) GovStampRequirementInfo           *reqInfo;

- (Msg*) newMsg:(NSMutableDictionary*)parameterBag;

@end
