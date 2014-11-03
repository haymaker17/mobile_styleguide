//
//  TripItValidateAccessTokenData.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "ActionStatus.h"

@class MsgControl;
@class Msg;

@interface TripItValidateAccessTokenData : MsgResponderCommon
{
	ActionStatus    *actionStatus;
    BOOL            isTripItLinked;
    BOOL            isTripItEmailAddressConfirmed;
}

@property (nonatomic, strong) ActionStatus *actionStatus;
@property (nonatomic, assign) BOOL isTripItLinked;
@property (nonatomic, assign) BOOL isTripItEmailAddressConfirmed;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

@end

