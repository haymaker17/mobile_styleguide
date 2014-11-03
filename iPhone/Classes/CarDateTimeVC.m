//
//  CarDateTimeVC.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 9/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CarDateTimeVC.h"


@implementation CarDateTimeVC

@synthesize parentVC;
@synthesize isPickupDate;


-(void) onDone
{
	if (isPickupDate)
		[parentVC changePickupDate:datePicker.date extendedHour:selectedExtendedHour];
	else
		[parentVC changeDropoffDate:datePicker.date extendedHour:selectedExtendedHour];

	[super onDone];
}

-(NSString*)getTitleLabel
{
	NSString *key = (isReturn ? @"Drop-off" : @"Pick-up");
	return [Localizer getLocalizedText:key];
}

-(NSString*)getDepartureDateLabel
{
	return [Localizer getLocalizedText:@"Date"];
}

-(NSString*)getDepartureTimeLabel
{
	return [Localizer getLocalizedText:@"Time"];
}

- (void)dealloc
{
	[parentVC release];
    [super dealloc];
}


@end

