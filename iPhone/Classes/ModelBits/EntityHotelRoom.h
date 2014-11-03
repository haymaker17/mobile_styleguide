//
//  EntityHotelRoom.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 14/04/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityHotelBooking, EntityHotelViolation;

@interface EntityHotelRoom : NSManagedObject

@property (nonatomic, retain) NSNumber * maxEnforcementLevel;
@property (nonatomic, retain) NSNumber * isUsingPointsAgainstViolations;
@property (nonatomic, retain) NSNumber * canUseTravelPoints;
@property (nonatomic, retain) NSString * choiceId;
@property (nonatomic, retain) NSNumber * travelPoints;
@property (nonatomic, retain) NSNumber * depositRequired;
@property (nonatomic, retain) NSString * summary;
@property (nonatomic, retain) NSString * sellSource;
@property (nonatomic, retain) NSString * violationReason;
@property (nonatomic, retain) NSString * violationJustification;
@property (nonatomic, retain) NSString * rate;
@property (nonatomic, retain) NSString * bicCode;
@property (nonatomic, retain) NSString * crnCode;
@property (nonatomic, retain) NSString * gdsName;
@property (nonatomic, retain) EntityHotelBooking *relHotelBooking;
@property (nonatomic, retain) EntityHotelViolation *relHotelViolationCurrent;
@property (nonatomic, retain) NSSet *relHotelViolation;
@end

@interface EntityHotelRoom (CoreDataGeneratedAccessors)

- (void)addRelHotelViolationObject:(EntityHotelViolation *)value;
- (void)removeRelHotelViolationObject:(EntityHotelViolation *)value;
- (void)addRelHotelViolation:(NSSet *)values;
- (void)removeRelHotelViolation:(NSSet *)values;

@end
