//
//  SaveAttendeeData.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 12/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "AttendeeData.h"
#import "Msg.h"
#import "ActionStatus.h"

@interface SaveAttendeeData : MsgResponderCommon
{
    ActionStatus			*status;
	AttendeeData			*attendeeToSave;
	AttendeeData			*savedAttendee;
    NSMutableArray          *duplicateAttendees;
    AttendeeData            *dupAttendee;
    BOOL                    inDuplicateAttendees;
}

@property (nonatomic, strong) ActionStatus				*status;
@property (nonatomic, strong) AttendeeData				*attendeeToSave;
@property (nonatomic, strong) AttendeeData				*savedAttendee;

@property (nonatomic, strong) AttendeeData              *dupAttendee;
@property (nonatomic, strong) NSMutableArray            *duplicateAttendees;

-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;
-(NSString *)makeXMLBody;
-(BOOL) isFieldEmpty:(NSString*)val;

@end
