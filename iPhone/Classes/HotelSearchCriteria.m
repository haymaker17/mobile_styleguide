//
//  HotelSearchCriteria.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelSearchCriteria.h"
#import "OfficeLocationResult.h"
#import "EntityHotel.h"

@implementation HotelSearchCriteria

-(id)init
{
	self = [super init];
	if (self)
    {
        // Set the dates
        // Checkin date: today
        // Checkout date: tomorrow
        self.checkinDate = [NSDate date];
        [self setNextDayCheckout];
        
        // Set the default distance and units
        self.distanceValue = @5;
        self.isMetricDistance = @NO;
        
        // Set the default containing-words value
        self.containingWords = @"";
        
        // Smoking codes and names (indexes must match up)
        self.smokingPreferenceCodes = @[@"0", @"N", @"S"];
        self.smokingPreferenceNames = @[[Localizer getLocalizedText:@"No preference"]
                                       , [Localizer getLocalizedText:@"Non-smoking"]
                                       , [Localizer getLocalizedText:@"Smoking"]];
        
        // Set the default smoking index
        self.smokingIndex = 0;
	}
	return self;
}

-(void)setNextDayCheckout
{
	NSTimeInterval secondsInDay = 60.0 * 60.0 * 24.0; // 60 seconds per minute * 60 minutes per hour * 24 hours per day
	self.checkoutDate = [NSDate dateWithTimeInterval:secondsInDay sinceDate:self.checkinDate];
}

#pragma mark -
#pragma mark Last Entity
-(EntityHotel *) loadEntity
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityHotel" inManagedObjectContext:[ExSystem sharedInstance].context];
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
        NSLog(@"Whoops, couldn't save hotel: %@", [error localizedDescription]);
    }
}


-(void) clearEntity:(EntityHotel *) ent
{
    [[ExSystem sharedInstance].context deleteObject:ent];
}

-(void)readFromSettings
{
	if ([ExSystem sharedInstance].entitySettings == nil)
		return;
	
	EntityHotel *lastHotel = [self loadEntity];
	if (lastHotel == nil)
		return;
	
	NSString* location = lastHotel.location;
	NSString* latitude = lastHotel.latitude;
	NSString* longitude = lastHotel.longitude;
	
	if (location == nil || latitude == nil || longitude == nil)
	{
		return; // We don't even have a location, so don't try to load anything else
	}
	else
	{
		NSString* city = lastHotel.city;
		NSString* state = lastHotel.state;
		NSString* country = lastHotel.country;
		
        if (city != nil && state != nil)
        {
            OfficeLocationResult* lastOfficeLocation = [[OfficeLocationResult alloc] init];
            lastOfficeLocation.city = city;
            lastOfficeLocation.state = state;
            lastOfficeLocation.country = country;
            self.locationResult = lastOfficeLocation;
        }
        else
        {
            LocationResult *lastLocation = [[LocationResult alloc] init];
            self.locationResult = lastLocation;
        }
		
		self.locationResult.location = location;
		self.locationResult.latitude = latitude;
		self.locationResult.longitude = longitude;
	}
	
	NSDate* lastCheckinDate = lastHotel.checkinDate;
	if (lastCheckinDate != nil)
	{
        // MOB-17430 Log entry added to aid in diagnosing a problem we have with historical searches being submitted
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelSearchCriteria::readFromSettings: [DATECHECK] lastCheckinDate value %@", lastCheckinDate] Level:MC_LOG_DEBU];

        // Check that the check-in date is no more than an hour ago
        if ([lastCheckinDate timeIntervalSinceNow] > -3600)
        {
            self.checkinDate = lastCheckinDate;
        }
	}
	
	NSDate* lastCheckoutDate = lastHotel.checkoutDate;
	if (lastCheckoutDate != nil)
	{
        // MOB-17430 Log entry added to aid in diagnosing a problem we have with historical searches being submitted
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"HotelSearchCriteria::readFromSettings: [DATECHECK] lastCheckoutDate value %@", lastCheckoutDate] Level:MC_LOG_DEBU];
        // Check that the check-out date is not in the past
        if ([lastCheckoutDate timeIntervalSinceNow] > 1)
        {
            self.checkoutDate = lastCheckoutDate;
        }
	}
	
	NSString* distanceStr = lastHotel.distance;
	if (distanceStr != nil)
		self.distanceValue = @([distanceStr integerValue]);

	NSString* isMetricDistanceStr = lastHotel.isMetricDistance;
	if (isMetricDistanceStr != nil)
		self.isMetricDistance = @([isMetricDistanceStr isEqualToString:@"YES"]);
	
	self.containingWords = lastHotel.containingWords;
	
	NSString* smokingPreferenceCode = lastHotel.smokingPreferenceCode;
	if ([smokingPreferenceCode isEqualToString:@"0"])
		self.smokingIndex = 0;
	else if ([smokingPreferenceCode isEqualToString:@"N"])
		self.smokingIndex = 1;
	else if ([smokingPreferenceCode isEqualToString:@"S"])
		self.smokingIndex = 2;
}

-(void)writeToSettings
{
    EntityHotel *lastHotel = [self loadEntity];
	
	if (self.locationResult == nil)
		return;	// No location data to write.
	
	if(lastHotel == nil)
       lastHotel = [NSEntityDescription insertNewObjectForEntityForName:@"EntityHotel" inManagedObjectContext:[ExSystem sharedInstance].context];
	
	lastHotel.location = self.locationResult.location;
	lastHotel.latitude = self.locationResult.latitude;
	lastHotel.longitude = self.locationResult.longitude;
	
	if ([self.locationResult isKindOfClass:[OfficeLocationResult class]])
	{
		OfficeLocationResult* officeLocation = (OfficeLocationResult*)self.locationResult;
		if (officeLocation.city != nil)
			lastHotel.city = officeLocation.city;
		if (officeLocation.state != nil)
			lastHotel.state = officeLocation.state;
		if (officeLocation.country != nil)
			lastHotel.country = officeLocation.country;
	}
    else
    {
        // Reset these fields, otherwise the app will think the last location
        // was an office location
        lastHotel.city = nil;
        lastHotel.state = nil;
        lastHotel.country = nil;
    }
	
	if (self.checkinDate != nil)
		lastHotel.checkinDate = self.checkinDate;

	if (self.checkoutDate != nil)
		lastHotel.checkoutDate = self.checkoutDate;
	
	if (self.distanceValue != nil)
		lastHotel.distance = [NSString stringWithFormat:@"%i", [self.distanceValue intValue]];

	if (self.isMetricDistance != nil)
		lastHotel.isMetricDistance = ([self.isMetricDistance boolValue] ? @"YES" : @"NO");
	
	lastHotel.containingWords = self.containingWords;

	lastHotel.smokingPreferenceCode = (self.smokingPreferenceCodes)[self.smokingIndex];
	
    [self saveEntity];

}


@end
