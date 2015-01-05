//
//  CarSearchCriteria.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CarSearchCriteria.h"
#import "LocationResult.h"
#import "ExtendedHour.h"

@implementation CarSearchCriteria


@synthesize pickupLocationResult;
@synthesize dropoffLocationResult;

@synthesize pickupDate;
@synthesize dropoffDate;

@synthesize pickupExtendedHour;
@synthesize dropoffExtendedHour;

@synthesize isOffAirport;
@synthesize carTypeIndex;
@synthesize smokingIndex;

@synthesize carTypeCodes;
@synthesize carTypeNames;
@synthesize carTypeCodesAndNames;

@synthesize smokingPreferenceCodes;
@synthesize smokingPreferenceNames;


+(NSInteger)hourFromDate:(NSDate*)date
{
	NSCalendar *calendar = [NSCalendar currentCalendar];
	calendar.timeZone = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];  // MOB-9802 travel dates should be GMT based
	NSDateComponents *components = [calendar components:(NSHourCalendarUnit) fromDate:date];
	NSInteger hour = components.hour;
	return hour;
}

+(NSString*)hourStringFromInteger:(NSInteger)hourInt
{
	NSInteger hourInTwentyFour = hourInt;
	NSInteger hourInTwelve = hourInTwentyFour % 12;
	
	NSString* hourType = (hourInTwelve == hourInTwentyFour ? @"AM" : @"PM");
	
	if (hourInTwelve == 0)
		hourInTwelve = 12;
	
	NSString* hourString = [NSString stringWithFormat:@"%li %@", (long)hourInTwelve, hourType];
	return hourString;
}

-(id)init
{
	self = [super init];
	
    if (self)
    {
        //MOB-9882 Show correct car types
        // Car type codes and names (indexes must match up)
        self.carTypeCodes = [NSMutableArray arrayWithObjects:
                           @" "
                         , @"M*"
                         , @"E*"
                         , @"E*H"
                         , @"C*"
                         , @"C*H"
                         , @"I*"
                         , @"I*H"
                         , @"S*"
                         , @"S*H"
                         , @"F*"
                         , @"F*H"
                         , @"MV"
                         , @"P*"
                         , @"L*"
                         , @"IF"
                         , @"SF"
                         , @"FF"
                         , @"FP"
                         , nil];
        self.carTypeNames = [NSMutableArray arrayWithObjects:
						   [Localizer getLocalizedText:@"Any Car Class"]
						 , [Localizer getLocalizedText:@"Mini"]
						 , [Localizer getLocalizedText:@"Economy"]
                         , [Localizer getLocalizedText:@"Economy Hybrid"]
						 , [Localizer getLocalizedText:@"Compact"]
                         , [Localizer getLocalizedText:@"Compact Hybrid"]
						 , [Localizer getLocalizedText:@"Intermediate"]
                         , [Localizer getLocalizedText:@"Intermediate Hybrid"]
						 , [Localizer getLocalizedText:@"Standard"]
                         , [Localizer getLocalizedText:@"Standard Hybrid"]
						 , [Localizer getLocalizedText:@"Full"]
                         , [Localizer getLocalizedText:@"Full Hybrid"]
                         , [Localizer getLocalizedText:@"Mini Van"]
						 , [Localizer getLocalizedText:@"Premium"]
						 , [Localizer getLocalizedText:@"Luxury"]
                         , [Localizer getLocalizedText:@"Intermediate SUV"]
                         , [Localizer getLocalizedText:@"Standard SUV"]
                         , [Localizer getLocalizedText:@"Full SUV"]
                         , [Localizer getLocalizedText:@"Full Pickup"]
						 , nil];
  
        //setup car description look up table
        self.carTypeCodesAndNames = [[NSMutableDictionary alloc] init];
        for (int i = 0; i < [carTypeCodes count]; i++)
        {
            (self.carTypeCodesAndNames)[carTypeCodes[i]] = carTypeNames[i];
        }
        
        // Smoking codes and names (indexes must match up)
        self.smokingPreferenceCodes = @[@"0", @"N", @"S"];
        self.smokingPreferenceNames = @[[Localizer getLocalizedText:@"No preference"]
                                       , [Localizer getLocalizedText:@"Non-smoking"]
                                       , [Localizer getLocalizedText:@"Smoking"]];
        
        // Set the dates
        self.pickupDate = [NSDate date];
        [self setNextDayDropoff];
        
        // Set the times
        pickupExtendedHour = 9; //AnytimeExtendedHour;
        dropoffExtendedHour = 9; //AnytimeExtendedHour;
        
        // Set the default car type index
        self.carTypeIndex = 0;
        
        // Set the default smoking index
        self.smokingIndex = 0;
	}
	return self;
}

-(void)setNextDayDropoff
{
	NSTimeInterval secondsInDay = 60.0 * 60.0 * 24.0; // 60 seconds per minute * 60 minutes per hour * 24 hours per day
	self.dropoffDate = [NSDate dateWithTimeInterval:secondsInDay sinceDate:pickupDate];
}

-(void)updateAllowedCarType:(NSArray *)allowedCarType
{
    NSUInteger idx = 0;
    CarType *currentCarType = nil;
    if ([allowedCarType count] > 0)
    {
        // PRE: "Any car class" is the only checked option in user travel setting.
        // If first item in the object has EMPTY string for carTypeCode, means show all type of cars
        currentCarType = allowedCarType[idx];
        if (![currentCarType.carTypeCode length] && [currentCarType.carTypeName length]) {
            return;
        }
        else
        {
            [self.carTypeCodes removeAllObjects];
            [self.carTypeNames removeAllObjects];
            
            // Untill MWS return correct description of codes. Look up description in the table
            NSString       *carDescription = nil;
            for (idx = 0; idx < [allowedCarType count]; idx++)
            {
                currentCarType = allowedCarType[idx];
                
                carDescription = (self.carTypeCodesAndNames)[currentCarType.carTypeCode];
                if (carDescription == nil) {
                    carDescription = currentCarType.carTypeName;
                }
                
                if (currentCarType.carTypeCode != nil && carDescription != nil)
                {
                    [self.carTypeCodes addObject:currentCarType.carTypeCode];
                    [self.carTypeNames addObject:carDescription];
                }
            }
        }

    }
}

#pragma mark -
#pragma mark Last Entity
-(EntityCar *) loadEntity
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityCar" inManagedObjectContext:[ExSystem sharedInstance].context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *a = [[ExSystem sharedInstance].context executeFetchRequest:fetchRequest error:&error];
    
    if([a count] > 0)
        return a[0];
    else
        return nil;
}


-(void) saveEntity
{
    NSError *error;
    if (![[ExSystem sharedInstance].context save:&error]) {
        NSLog(@"Whoops, couldn't save car: %@", [error localizedDescription]);
    }
}


-(void) clearEntity:(EntityCar *) ent
{
    [[ExSystem sharedInstance].context deleteObject:ent];
}


-(void)readFromSettings
{
	
	EntityCar* lastCar = [self loadEntity];
	
    if (lastCar == nil)
		return;
	
	NSString* pickupLocation = lastCar.pickupLocation; // [lastCar objectForKey:@"PICKUP_LOCATION"];
	NSString* pickupLatitude = lastCar.pickupLatitude; // [lastCar objectForKey:@"PICKUP_LATITUDE"];
	NSString* pickupLongitude = lastCar.pickupLongitude; // [lastCar objectForKey:@"PICKUP_LONGITUDE"];
    NSString* pickupIata = lastCar.pickupIata;
	
	if (pickupLocation == nil || pickupLatitude == nil || pickupLongitude == nil)
	{
		return; // We don't even have a pickup location, so don't try to load anything else
	}
	else
	{
		LocationResult *lastPickupLocation = [[LocationResult alloc] init];
		self.pickupLocationResult = lastPickupLocation;
		
		pickupLocationResult.location = pickupLocation;
		pickupLocationResult.latitude = pickupLatitude;
		pickupLocationResult.longitude = pickupLongitude;
        pickupLocationResult.iataCode = pickupIata;
	}
	
	NSString* dropoffLocation = lastCar.dropoffLocation; // [lastCar objectForKey:@"DROPOFF_LOCATION"];
	NSString* dropoffLatitude = lastCar.dropoffLatitude;// [lastCar objectForKey:@"DROPOFF_LATITUDE"];
	NSString* dropoffLongitude = lastCar.dropoffLongitude; // [lastCar objectForKey:@"DROPOFF_LONGITUDE"];
    NSString* dropOffIata = lastCar.dropoffIata;
	
	if (dropoffLocation != nil && dropoffLatitude != nil && dropoffLongitude != nil)
	{
		LocationResult *lastDropoffLocation = [[LocationResult alloc] init];
		self.dropoffLocationResult = lastDropoffLocation;
		
		dropoffLocationResult.location = dropoffLocation;
		dropoffLocationResult.latitude = dropoffLatitude;
		dropoffLocationResult.longitude = dropoffLongitude;
        dropoffLocationResult.iataCode = dropOffIata;
	}
	
	NSDate* pickupDateLast = lastCar.pickupDate; // [lastCar objectForKey:@"PICKUP_DATE"];
	if (pickupDateLast != nil)
	{
        // Check that the pick up date is no more than an hour ago
        if ([pickupDateLast timeIntervalSinceNow] > -3600)
        {
            self.pickupDate	= pickupDateLast; //date;
        }
	}
	
	NSDate* dropoffDateLast = lastCar.dropoffDate; // [lastCar objectForKey:@"DROPOFF_DATE"];
	if (dropoffDateLast != nil)
	{
        // Check that the drop-off date is not in the past
        if ([dropoffDateLast timeIntervalSinceNow] > 1)
        {
            self.dropoffDate = dropoffDateLast;
        }
	}
	
    //NSLog(@"%d", [lastCar.pickupExtendedHour intValue]);
	NSNumber* pickupExtendedHourNumber = @([lastCar.pickupExtendedHour intValue]); // [lastCar objectForKey:@"PICKUP_EXTENDED_HOUR"];
	if (pickupExtendedHourNumber != nil)
		self.pickupExtendedHour	= [pickupExtendedHourNumber intValue];
	
	NSNumber* dropoffExtendedHourNumber = lastCar.dropoffExtendedHour; // [lastCar objectForKey:@"DROPOFF_EXTENDED_HOUR"];
	if (dropoffExtendedHourNumber != nil)
	{
		self.dropoffExtendedHour	= [dropoffExtendedHourNumber intValue];
	}
	
	NSString* isOffAirportStr = lastCar.isOffAirport; // [lastCar objectForKey:@"IS_OFF_AIRPORT"];
	if (isOffAirportStr != nil)
		self.isOffAirport = [isOffAirportStr isEqualToString:@"YES"];

    // default car type choice to be first
    self.carTypeIndex = 0;
	
	// Look up the car type code to get the index into the car type array
	NSString* carTypeCode = lastCar.carTypeCode; // [lastCar objectForKey:@"CAR_TYPE_CODE"];
	if (carTypeCode != nil)
	{
		NSUInteger numCarTypes = [carTypeCodes count];
		for (int i = 0; i < numCarTypes; i++)
		{
			NSString *eachCarTypeCode = carTypeCodes[i];
			if ([eachCarTypeCode isEqualToString:carTypeCode])
			{
				self.carTypeIndex = i;
				break;
			}
		}
	}
	
	NSString* smokingPreferenceCode = lastCar.smokingPreferenceCode; // [lastCar objectForKey:@"SMOKING_PREFERENCE_CODE"];
	if ([smokingPreferenceCode isEqualToString:@"0"])
		self.smokingIndex = 0;
	else if ([smokingPreferenceCode isEqualToString:@"N"])
		self.smokingIndex = 1;
	else if ([smokingPreferenceCode isEqualToString:@"S"])
		self.smokingIndex = 2;
}

-(void)writeToSettings
{
    EntityCar *lastCar = [self loadEntity];
	
	if (self.pickupLocationResult == nil)
		return;	// No location data to write.
	
    if(lastCar == nil)
        lastCar = [NSEntityDescription insertNewObjectForEntityForName:@"EntityCar" inManagedObjectContext:[ExSystem sharedInstance].context];
	
	if (self.pickupLocationResult == nil)
	{
		LocationResult *newLocation = [[LocationResult alloc] init];
		self.pickupLocationResult = newLocation;
	}
	lastCar.pickupLocation = self.pickupLocationResult.location;// forKey:@"PICKUP_LOCATION"];
	lastCar.pickupLatitude = self.pickupLocationResult.latitude;// forKey:@"PICKUP_LATITUDE"];
	lastCar.pickupLongitude = self.pickupLocationResult.longitude;// forKey:@"PICKUP_LONGITUDE"];
    lastCar.pickupIata = self.pickupLocationResult.iataCode;

	if (self.dropoffLocationResult == nil)
	{
		LocationResult *newLocation = [[LocationResult alloc] init];
		self.dropoffLocationResult = newLocation;
	}
	lastCar.dropoffLocation = self.dropoffLocationResult.location;// forKey:@"DROPOFF_LOCATION"];
	lastCar.dropoffLatitude = self.dropoffLocationResult.latitude;// forKey:@"DROPOFF_LATITUDE"];
	lastCar.dropoffLongitude = self.dropoffLocationResult.longitude;// forKey:@"DROPOFF_LONGITUDE"];
    lastCar.dropoffIata = self.dropoffLocationResult.iataCode;
	
	if (self.pickupDate != nil)
		lastCar.pickupDate = self.pickupDate;// forKey:@"PICKUP_DATE"];

	if (self.dropoffDate != nil)
		lastCar.dropoffDate = self.dropoffDate;// forKey:@"DROPOFF_DATE"];
	
	lastCar.pickupExtendedHour = @(self.pickupExtendedHour);// forKey:@"PICKUP_EXTENDED_HOUR"];
	
	lastCar.dropoffExtendedHour = @(self.dropoffExtendedHour);// forKey:@"DROPOFF_EXTENDED_HOUR"];
	
	lastCar.isOffAirport = (self.isOffAirport ? @"YES" : @"NO");// forKey:@"IS_OFF_AIRPORT"];
	
	lastCar.carTypeCode = (self.carTypeCodes)[self.carTypeIndex];// forKey:@"CAR_TYPE_CODE"];
	
	lastCar.smokingPreferenceCode = (self.smokingPreferenceCodes)[self.smokingIndex];// forKey:@"SMOKING_PREFERENCE_CODE"];
	
	[self saveEntity];

}


@end
