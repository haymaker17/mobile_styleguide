//
//  AttendeeManager.h
//  ConcurMobile
//
//  Created by yiwen on 9/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AttendeeData.h"

@interface AttendeeManager : NSObject 
{
	NSMutableDictionary* meAtnMap;	// MeKey =>AtnUId list
}

@property (strong, nonatomic) NSMutableDictionary* meAtnMap;

+(AttendeeManager*)sharedInstance;
-(void) saveAttendees:(NSArray*)attendees forMe:(NSString*) meKey;
-(BOOL) save;
-(NSArray*) getAttendeesForMeKey:(NSString*) meKey;
@end
