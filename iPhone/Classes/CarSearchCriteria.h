//
//  CarSearchCriteria.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


#import <UIKit/UIKit.h>
#import "LocationResult.h"
#import "EntityCar.h"
#import "CarType.h"

@class SettingsData;

@interface CarSearchCriteria : NSObject
{
	LocationResult	*pickupLocationResult;
	LocationResult	*dropoffLocationResult;
	
	NSDate			*pickupDate;
	NSDate			*dropoffDate;
	
	NSInteger		pickupExtendedHour;
	NSInteger		dropoffExtendedHour;
	
	BOOL			isOffAirport;
	NSUInteger		carTypeIndex;
	NSUInteger		smokingIndex;
	
	NSMutableArray	*carTypeCodes;
	NSMutableArray	*carTypeNames;
    
    // Untill MWS return correct description of codes. Use this as lookup table
    NSMutableDictionary   *carTypeCodesAndNames;
	
	NSArray			*smokingPreferenceCodes;
	NSArray			*smokingPreferenceNames;
}

@property (nonatomic, strong) LocationResult	*pickupLocationResult;
@property (nonatomic, strong) LocationResult	*dropoffLocationResult;
@property (nonatomic, strong) NSDate			*pickupDate;
@property (nonatomic, strong) NSDate			*dropoffDate;
@property (nonatomic) NSInteger					pickupExtendedHour;
@property (nonatomic) NSInteger					dropoffExtendedHour;
@property (nonatomic) BOOL						isOffAirport;
@property (nonatomic) NSUInteger				carTypeIndex;
@property (nonatomic) NSUInteger				smokingIndex;

@property (nonatomic, strong) NSMutableArray	*carTypeCodes;
@property (nonatomic, strong) NSMutableArray	*carTypeNames;
@property (nonatomic, strong) NSMutableDictionary      *carTypeCodesAndNames;

@property (nonatomic, strong) NSArray			*smokingPreferenceCodes;
@property (nonatomic, strong) NSArray			*smokingPreferenceNames;

+(NSInteger)hourFromDate:(NSDate*)date;
+(NSString*)hourStringFromInteger:(NSInteger)hourInt;

-(void)setNextDayDropoff;

-(void)updateAllowedCarType:(NSArray *) allowedCarType;

-(void)readFromSettings;
-(void)writeToSettings;

-(id)init;


#pragma mark -
#pragma mark Last Entity
-(EntityCar *) loadEntity;
-(void) saveEntity;
-(void) clearEntity:(EntityCar *) ent;

@end
