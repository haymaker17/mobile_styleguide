//
//  AttendeeManager.m
//  ConcurMobile
//
//  Created by yiwen on 9/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "AttendeeManager.h"

@implementation AttendeeManager
@synthesize meAtnMap;

static AttendeeManager* sharedInstance;

+(AttendeeManager*)sharedInstance
{
	if (sharedInstance != nil) 
	{
		return sharedInstance;
	}
	else 
	{
		@synchronized (self)
		{
			if (sharedInstance == nil) 
			{
				NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
				NSString *documentsDirectory = paths[0];
				NSString *archivePath = [documentsDirectory stringByAppendingPathComponent:@"AttendeeManager.archive"];
				
				sharedInstance = [NSKeyedUnarchiver unarchiveObjectWithFile:archivePath];
				
				if (sharedInstance == nil)
				{
					sharedInstance = [[AttendeeManager alloc] init];
				}
				else {
					 // Increment retain count, since unarchive doesn't
				}

			}
		}
		return sharedInstance;
	}
}

-(id) init
{
    self = [super init];
    if (self) {
        meAtnMap = [[NSMutableDictionary alloc] init];        
    }

	return self;
}

- (BOOL) save
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = paths[0];
	
    NSString *archivePath = [documentsDirectory stringByAppendingPathComponent:@"AttendeeManager.archive"];
    BOOL result = [NSKeyedArchiver archiveRootObject:self toFile:archivePath];
    return result;
}
-(void) saveAttendees:(NSArray*)attendees forMe:(NSString*) meKey
{
	if (attendees == nil)
		[meAtnMap removeObjectForKey:meKey];
	else
		meAtnMap[meKey] = attendees;
	[self save];
}

-(NSArray*) getAttendeesForMeKey:(NSString*) meKey
{
	NSArray* result = meAtnMap[meKey];
	return result;
}

#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
	[coder encodeObject:meAtnMap forKey:@"meAtnMap"];
}

- (id)initWithCoder:(NSCoder *)coder {
	self.meAtnMap = [coder decodeObjectForKey:@"meAtnMap"];
    return self;
}

@end
