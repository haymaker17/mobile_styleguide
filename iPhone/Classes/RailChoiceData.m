//
//  RailChoiceData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "RailChoiceData.h"
#import "HotelViolation.h"

@implementation RailChoiceData

-(id)init
{
    self = [super init];
    if (self)
    {
        self.segments = [[NSMutableArray alloc] initWithObjects:nil];
        self.seats = [[NSMutableArray alloc] initWithObjects:nil];
        self.violations = [[NSMutableArray alloc] initWithObjects:nil];
    }
	return self;
}

-(void) addSeat:(NSString *)seatBaseFare Cost:(NSString *)seatCost CurrencyCode:(NSString *)seatCurrencyCode Description:(NSString *)seatDescription
{
	NSArray *seat = @[seatBaseFare, seatCost, seatCurrencyCode, seatDescription];
	
	for(NSArray *a in self.seats)
	{
		if ([a isEqualToArray:seat]) {
			return;
		}
	}	 
    
	[self.seats addObject:seat];
}

@end
