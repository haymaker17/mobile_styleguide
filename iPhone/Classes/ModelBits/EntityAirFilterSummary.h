//
//  EntityAirFilterSummary.h
//  ConcurMobile
//
//  Created by Sally Yan on 4/28/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityAirFilter, EntityAirRules, EntityAirViolation;

@interface EntityAirFilterSummary : NSManagedObject

@property (nonatomic, retain) NSDate * roundArrivalTime;
@property (nonatomic, retain) NSDate * departureTime;
@property (nonatomic, retain) NSNumber * isInstantPurchase;
@property (nonatomic, retain) NSNumber * pref;
@property (nonatomic, retain) NSString * airlineCode;
@property (nonatomic, retain) NSNumber * travelPoints;
@property (nonatomic, retain) NSString * fareId;
@property (nonatomic, retain) NSString * violationJustification;
@property (nonatomic, retain) NSNumber * duration;
@property (nonatomic, retain) NSString * choiceId;
@property (nonatomic, retain) NSNumber * maxEnforcementLevel;
@property (nonatomic, retain) NSString * departureIata;
@property (nonatomic, retain) NSNumber * refundable;
@property (nonatomic, retain) NSString * arrivalIata;
@property (nonatomic, retain) NSString * roundArrivalIata;
@property (nonatomic, retain) NSString * rateType;
@property (nonatomic, retain) NSString * ruleMessage;
@property (nonatomic, retain) NSNumber * isUsingPointsAgainstViolations;
@property (nonatomic, retain) NSString * crnCode;
@property (nonatomic, retain) NSDate * arrivalTime;
@property (nonatomic, retain) NSString * airlineName;
@property (nonatomic, retain) NSNumber * roundNumStops;
@property (nonatomic, retain) NSString * enforcementLevel;
@property (nonatomic, retain) NSNumber * numStops;
@property (nonatomic, retain) NSString * gdsName;
@property (nonatomic, retain) NSNumber * fare;
@property (nonatomic, retain) NSNumber * roundDuration;
@property (nonatomic, retain) NSString * violationReason;
@property (nonatomic, retain) NSNumber * canUseTravelPoints;
@property (nonatomic, retain) NSNumber * durationTotal;
@property (nonatomic, retain) NSDate * roundDepartureTime;
@property (nonatomic, retain) NSString * roundDepartureIata;
@property (nonatomic, retain) NSNumber * isFusionRecommendedFlight;
@property (nonatomic, retain) EntityAirViolation *relAirViolationCurrent;
@property (nonatomic, retain) NSSet *AirFilter;
@property (nonatomic, retain) NSSet *relAirViolations;
@property (nonatomic, retain) NSSet *AirRules;
@end

@interface EntityAirFilterSummary (CoreDataGeneratedAccessors)

- (void)addAirFilterObject:(EntityAirFilter *)value;
- (void)removeAirFilterObject:(EntityAirFilter *)value;
- (void)addAirFilter:(NSSet *)values;
- (void)removeAirFilter:(NSSet *)values;

- (void)addRelAirViolationsObject:(EntityAirViolation *)value;
- (void)removeRelAirViolationsObject:(EntityAirViolation *)value;
- (void)addRelAirViolations:(NSSet *)values;
- (void)removeRelAirViolations:(NSSet *)values;

- (void)addAirRulesObject:(EntityAirRules *)value;
- (void)removeAirRulesObject:(EntityAirRules *)value;
- (void)addAirRules:(NSSet *)values;
- (void)removeAirRules:(NSSet *)values;

@end
