//
//  CCFlurryLogs.m
//  ConcurMobile
//
//  Created by laurent mery on 23/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//
//  class to format flurry log
//

#import "CCFlurryLogs.h"
#import "Flurry.h"

@implementation CCFlurryLogs

+(void)flurryLogEventActionFrom:(NSString*)sourceViewName action:(NSString*)action parameters:(NSDictionary*)parameters{
	
	NSString *eventString = [@"" stringByAppendingFormat:@"%@: %@", sourceViewName, action];
	
	[Flurry logEvent:eventString withParameters:parameters];
}

+(void)flurryLogEventOpenViewFrom:(NSString*)sourceViewName to:(NSString*)destinationViewName parameters:(NSMutableDictionary*)parameters{
	
	NSString *eventString = [@"" stringByAppendingFormat:@"%@: Open View", sourceViewName];
	
	if (parameters == nil) {
		
		parameters = [[NSMutableDictionary alloc]init];
	}
	[parameters addEntriesFromDictionary:@{@"ToView":destinationViewName}];
	
	[Flurry logEvent:eventString withParameters:parameters];
}

+(void)flurryLogEventReturnFromView:(NSString*)sourceViewName to:(NSString*)destinationViewName parameters:(NSMutableDictionary*)parameters{
	
	NSString *eventString = [@"" stringByAppendingFormat:@"%@: Return", sourceViewName];
	
	if (parameters == nil) {
		
		parameters = [[NSMutableDictionary alloc]init];
	}
	[parameters addEntriesFromDictionary:@{@"ToView":destinationViewName}];
	
	[Flurry logEvent:eventString withParameters:parameters];
	
}


+(void)flurryLogSpinnerStartTimefrom:(NSString*)sourceViewName action:(NSString*)action{
	
	NSString *eventString = [@"" stringByAppendingFormat:@"%@: %@ Spinner", sourceViewName, action];
	
	[Flurry logEvent:eventString timed:YES];
}

+(void)flurryLogSpinnerStopTimefrom:(NSString*)sourceViewName action:(NSString*)action parameters:(NSDictionary*)parameters{
	
	NSString *eventString = [@"" stringByAppendingFormat:@"%@: %@ Spinner", sourceViewName, action];
	
	[Flurry endTimedEvent:eventString withParameters:parameters];
}

@end
