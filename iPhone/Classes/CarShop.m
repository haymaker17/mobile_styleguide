//
//  CarShop.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/30/10.
//  Copyright 2010 Concur. All rights reserved.
//


#import "CarShop.h"
#import "Car.h"
#import "CarResult.h"
#import "CarChain.h"
#import "CarDescription.h"


@implementation CarShop


@synthesize pickupIata;
@synthesize pickupDate;
@synthesize dropoffIata;
@synthesize dropoffDate;
@synthesize carResults;
@synthesize carChains;
@synthesize carDescriptions;
@synthesize carLocations;
@synthesize cars;


-(id)init;
{
    self = [super init];
    if (self) {
        self.carResults = [[NSMutableArray alloc] initWithObjects:nil];
        self.carChains = [[NSMutableDictionary alloc] init];
        self.carDescriptions = [[NSMutableDictionary alloc] init];
        self.carLocations = [[NSMutableDictionary alloc] init];
        self.cars = [[NSMutableArray alloc] initWithObjects:nil];

    }
	return self;
}

// Called after the car shop has been populated with the car choices returned from the server
-(void)didPopulate
{
	int carCount = [carResults count];
	
	// For each CarResult object, create a corresponding Car object.
	// The Cars array will have the same ordering as the CarResults array.
	for (int i = 0; i < carCount; i++)
	{
		Car *car = [[Car alloc] init];
		car.carShop = self;
		car.carResult = carResults[i];
		car.carChain = carChains[car.carResult.chainCode];
		car.carDescription = carDescriptions[car.carResult.carType];

		if (pickupIata != nil)
		{
            NSString *key = [NSString stringWithFormat:@"%@-%@", pickupIata, [car.carChain.code uppercaseString]];
            //NSLog(@"car key = %@", key);
			CarLocation* carLocation = carLocations[key];
			car.carPickupLocation = carLocation;
		}
		
		[self.cars addObject:car];
	}
}



@end
