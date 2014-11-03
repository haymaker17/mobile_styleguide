//
//  TripItExpenseTripData.h
//  ConcurMobile
//
//  Created by  on 4/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "ActionStatus.h"

@class MsgControl;
@class Msg;

@interface TripItExpenseTripData : MsgResponderCommon
{
    NSString        *rptKey;
	ActionStatus    *actionStatus;
}

@property (nonatomic, strong) ActionStatus  *actionStatus;
@property (nonatomic, strong) NSString      *rptKey;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(NSString *)makeXMLBody:(NSMutableDictionary*)pBag;

-(NSString *)makeItinLocatorXMLBody:(NSString*)locatorId;
@end

