//
//  TripItUnlink.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 4/23/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "ActionStatus.h"

@class MsgControl;
@class Msg;

@interface TripItUnlink : MsgResponderCommon
{
	ActionStatus *actionStatus;
}

@property (nonatomic, strong) ActionStatus *actionStatus;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

-(BOOL) isActionStatusSuccess;

@end
