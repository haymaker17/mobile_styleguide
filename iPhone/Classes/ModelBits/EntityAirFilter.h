//
//  EntityAirFilter.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/12/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityAirFilterSummary, EntityAirRules, EntityAirViolation;

@interface EntityAirFilter : NSManagedObject

@property (nonatomic, strong) NSString * enforcementLevel;
@property (nonatomic, strong) NSString * flightNum;
@property (nonatomic, strong) NSNumber * pref;
@property (nonatomic, strong) NSDate * arrivalTime;
@property (nonatomic, strong) NSNumber * fare;
@property (nonatomic, strong) NSNumber * flightPos;
@property (nonatomic, strong) NSString * fareId;
@property (nonatomic, strong) NSNumber * segmentPos;
@property (nonatomic, strong) NSString * startIata;
@property (nonatomic, strong) NSString * operatingCarrier;
@property (nonatomic, strong) NSNumber * elapsedTime;
@property (nonatomic, strong) NSNumber * airMiles;
@property (nonatomic, strong) NSString * carrier;
@property (nonatomic, strong) NSString * ruleMessage;
@property (nonatomic, strong) NSNumber * distance;
@property (nonatomic, strong) NSDate * departureTime;
@property (nonatomic, strong) NSString * aircraftCode;
@property (nonatomic, strong) NSString * flightTime;
@property (nonatomic, strong) NSString * crnCode;
@property (nonatomic, strong) NSString * fltClass;
@property (nonatomic, strong) NSString * endIata;
@property (nonatomic, strong) NSString * bic;
@property (nonatomic, strong) NSNumber * numStops;
@property (nonatomic, strong) EntityAirFilterSummary *AirFilterSummary;
@property (nonatomic, strong) NSSet *relAirViolations;
@property (nonatomic, strong) NSSet *AirRules;
@end

@interface EntityAirFilter (CoreDataGeneratedAccessors)

- (void)addRelAirViolationsObject:(EntityAirViolation *)value;
- (void)removeRelAirViolationsObject:(EntityAirViolation *)value;
- (void)addRelAirViolations:(NSSet *)values;
- (void)removeRelAirViolations:(NSSet *)values;
- (void)addAirRulesObject:(EntityAirRules *)value;
- (void)removeAirRulesObject:(EntityAirRules *)value;
- (void)addAirRules:(NSSet *)values;
- (void)removeAirRules:(NSSet *)values;
@end
