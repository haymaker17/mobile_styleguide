//
//  TravelRequestVC.m
//  ConcurMobile
//
//  Created by laurent mery on 20/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "TravelRequestVC.h"
#import "Flurry.h"

@interface TravelRequestVC ()

@end

@implementation TravelRequestVC

@synthesize callerViewName;

- (void)viewDidLoad {
    [super viewDidLoad];
}


//TO BE OVERRIDED
+(NSString*)viewName{
	
	return @"not defined";
}

#pragma mark - Flurry

-(void)flurryLogEventActionFrom:(NSString*)sourceViewName action:(NSString*)action parameters:(NSDictionary*)parameters{
	
	NSString *eventString = [@"" stringByAppendingFormat:@"%@: %@", sourceViewName, action];
	
	[Flurry logEvent:eventString withParameters:parameters];
}

-(void)flurryLogEventOpenViewFrom:(NSString*)sourceViewName to:(NSString*)destinationViewName parameters:(NSMutableDictionary*)parameters{
	
	NSString *eventString = [@"" stringByAppendingFormat:@"%@: Open View", sourceViewName];
	
	if (parameters == nil) {
		
		parameters = [[NSMutableDictionary alloc]init];
	}
	[parameters addEntriesFromDictionary:@{@"ToView":destinationViewName}];
	
	[Flurry logEvent:eventString withParameters:parameters];
}

-(void)flurryLogEventReturnFromView:(NSString*)sourceViewName toOrNil:(NSString*)destinationViewName parameters:(NSMutableDictionary*)parameters{

	NSString *eventString = [@"" stringByAppendingFormat:@"%@: Return", sourceViewName];
	
	if (destinationViewName == nil) {
		
		destinationViewName = [NSString stringWithFormat:@"%@", callerViewName];
	}
	
	if (parameters == nil) {
		
		parameters = [[NSMutableDictionary alloc]init];
	}
	[parameters addEntriesFromDictionary:@{@"ToView":destinationViewName}];
	
	[Flurry logEvent:eventString withParameters:parameters];
	
}


-(void)flurryLogSpinnerStartTimefrom:(NSString*)sourceViewName action:(NSString*)action{
	
	NSString *eventString = [@"" stringByAppendingFormat:@"%@: %@ Spinner", sourceViewName, action];
	
	[Flurry logEvent:eventString timed:YES];
}

-(void)flurryLogSpinnerStopTimefrom:(NSString*)sourceViewName action:(NSString*)action parameters:(NSDictionary*)parameters{
	
	NSString *eventString = [@"" stringByAppendingFormat:@"%@: %@ Spinner", sourceViewName, action];

	[Flurry endTimedEvent:eventString withParameters:parameters];
}

#pragma mark - Spinner

-(void)waitIn{
	
	[WaitViewController showWithText:@"" animated:YES];
}

-(void)waitOut{
	
	[WaitViewController hideAnimated:YES withCompletionBlock:nil];
}

@end
