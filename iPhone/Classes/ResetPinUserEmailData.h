//
//  ResetPinUserEmailData.h
//  ConcurMobile
//
//  Created by Sally Yan on 7/1/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DataConstants.h"

@interface ResetPinUserEmailData : MsgResponderCommon
{
    ActionStatus            *actionStatus;
    NSString                *status;
    NSString                *clientGUID;
    NSString                *errMsg;
}

@property (nonatomic, strong) ActionStatus      *actionStatus;
@property (nonatomic, strong) NSString          *status;
@property (nonatomic, strong) NSString          *serverGUID;
@property (nonatomic, strong) NSString          *clientGUID;
@property (nonatomic, strong) NSString          *errMsg;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

@end
