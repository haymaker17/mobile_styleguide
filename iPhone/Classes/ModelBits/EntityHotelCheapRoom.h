//
//  EntityHotelCheapRoom.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 27/08/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityHotelBooking, EntityHotelViolation;

@interface EntityHotelCheapRoom : NSManagedObject

@property (nonatomic, retain) NSString * summary;
@property (nonatomic, retain) NSNumber * rate;
@property (nonatomic, retain) NSString * crnCode;
@property (nonatomic, retain) NSString * sellSource;
@property (nonatomic, retain) NSNumber * isViolation;
@property (nonatomic, retain) NSNumber * travelPoints;
@property (nonatomic, retain) NSString * bicCode;
@property (nonatomic, retain) NSNumber * depositRequired;
@property (nonatomic, retain) EntityHotelBooking *relHotelBooking;
@property (nonatomic, retain) EntityHotelBooking *relHotelBookingViolation;
@property (nonatomic, retain) NSSet *relViolation;
@end

@interface EntityHotelCheapRoom (CoreDataGeneratedAccessors)

- (void)addRelViolationObject:(EntityHotelViolation *)value;
- (void)removeRelViolationObject:(EntityHotelViolation *)value;
- (void)addRelViolation:(NSSet *)values;
- (void)removeRelViolation:(NSSet *)values;

@end
