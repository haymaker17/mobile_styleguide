//
//  AttendeeSearchDelegate.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 12/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AttendeeData.h"

@protocol AttendeeSearchDelegate

-(void) attendeeSelected:(AttendeeData*)attendee;

@optional
-(void) attendeesSelected:(NSArray*)attendees;

@end
