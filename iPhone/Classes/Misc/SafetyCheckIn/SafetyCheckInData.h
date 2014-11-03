//
//  SafetyCheckInData.h
//  ConcurMobile
//
//  Created by yiwen on 8/8/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "ActionStatus.h"

@interface SafetyCheckInData : MsgResponderCommon 
{
    ActionStatus			*status;

}
@property (nonatomic, strong) ActionStatus				*status;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

@end
