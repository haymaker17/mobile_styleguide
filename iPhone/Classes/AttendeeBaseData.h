//
//  AttendeeBaseData.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 2/21/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "AttendeeData.h"
#import "ExSystem.h"
#import "AttendeeGroup.h"
#import "ActionStatus.h"

@interface AttendeeBaseData : MsgResponderCommon
{
    ActionStatus			*status;
	AttendeeData			*currentAttendee;
	NSMutableArray			*attendees;
    AttendeeGroup           *currentGroup;
}

@property (nonatomic, strong) ActionStatus				*status;
@property (nonatomic, strong) AttendeeData				*currentAttendee;
@property (nonatomic, strong) NSMutableArray			*attendees;
@property (nonatomic, strong) AttendeeGroup             *currentGroup;
@end
