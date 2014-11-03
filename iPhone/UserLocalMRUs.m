//
//  UserLocalMRUs.m
//  ConcurMobile
//
//  Created by yiwen on 8/1/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "UserLocalMRUs.h"

static UserLocalMRUs *sharedInstance;

@interface UserLocalMRUs(Private)
- (NSMutableDictionary*) loadDict;
- (void) saveDict;
@end

@implementation UserLocalMRUs
@synthesize dict;

NSString* const MRU_ENTRY_TRANS_DATE = @"MRU_ENTRY_TRANS_DATE";
NSString* const MRU_ENTRY_LOC_NAME = @"MRU_ENTRY_LOC_NAME";
NSString* const MRU_ENTRY_MOD_DATE = @"MRU_ENTRY_MOD_DATE";
NSString* const MRU_ENTRY_LOC_LI_KEY = @"MRU_ENTRY_LOC_LI_KEY";


+(UserLocalMRUs*)sharedInstance
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
				sharedInstance = [[UserLocalMRUs alloc] init];
			}
		}
		return sharedInstance;
	}
}

-(id)init
{
	if (self = [super init]) 
	{
        self.dict = [self loadDict];
        if (self.dict == nil)
        {
            self.dict = [[NSMutableDictionary alloc] init];
        }
    }
    return self;
}

-(NSObject*) getMRUItem:(NSString*) key
{

    if (self.dict != nil)
    {
        NSString* lastModDateKey = [NSString stringWithFormat:@"%@ModDate", key];
        NSDate * lastModDate = (self.dict)[lastModDateKey];
        NSDate * lastAcceptedDate = [NSDate dateWithTimeIntervalSinceNow:-60*60*24*2];
        if (lastModDate != nil && [lastModDate compare:lastAcceptedDate] != NSOrderedAscending)
            return dict[key];
    }

    return nil;
}

-(void) saveMRUItem:(NSObject*)value withKey:(NSString*)key
{
    (self.dict)[key] = value;
    (self.dict)[[NSString stringWithFormat:@"%@ModDate", key]] = [NSDate date];

    [self saveDict];
}

- (NSMutableDictionary*) loadDict
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = paths[0];
	NSString *path = [documentsDirectory stringByAppendingPathComponent:@"MobileLocalMRU.plist"];
    
	__autoreleasing NSMutableDictionary *t = [[NSMutableDictionary alloc] initWithContentsOfFile:path];
    return t;
}

-(void) saveDict
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = paths[0];
	NSString *path = [documentsDirectory stringByAppendingPathComponent:@"MobileLocalMRU.plist"];
    
    [self.dict writeToFile:path atomically: YES];
}

@end
