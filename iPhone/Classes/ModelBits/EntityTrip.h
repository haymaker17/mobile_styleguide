//
//  EntityTrip.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 02/10/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityBooking, EntitySegment, EntityViolation;

@interface EntityTrip : NSManagedObject

@property (nonatomic, retain) NSString * tripComments;
@property (nonatomic, retain) NSNumber * hasCar;
@property (nonatomic, retain) NSNumber * allowCancel;
@property (nonatomic, retain) NSDate * tripEndDateLocal;
@property (nonatomic, retain) NSString * approvalStatus;
@property (nonatomic, retain) NSNumber * isWithdrawn;
@property (nonatomic, retain) NSString * cliqbookState;
@property (nonatomic, retain) NSString * orgUnitName;
@property (nonatomic, retain) NSNumber * tripIsPersonal;
@property (nonatomic, retain) NSString * clientLocator;
@property (nonatomic, retain) NSString * tripStatus;
@property (nonatomic, retain) NSNumber * hasRide;
@property (nonatomic, retain) NSString * itinLocator;
@property (nonatomic, retain) NSNumber * isExpensed;
@property (nonatomic, retain) NSNumber * hasEvent;
@property (nonatomic, retain) NSNumber * tripIsPrivate;
@property (nonatomic, retain) NSDate * tripStartDateUtc;
@property (nonatomic, retain) NSDate * tripStartDateLocal;
@property (nonatomic, retain) NSString * tripStateMessages;
@property (nonatomic, retain) NSString * authNum;
@property (nonatomic, retain) NSString * bookingId;
@property (nonatomic, retain) NSString * approverName;
@property (nonatomic, retain) NSNumber * hasAir;
@property (nonatomic, retain) NSNumber * hasRail;
@property (nonatomic, retain) NSString * approverId;
@property (nonatomic, retain) NSString * tripKey;
@property (nonatomic, retain) NSString * recordLocator;
@property (nonatomic, retain) NSString * tripDescription;
@property (nonatomic, retain) NSString * cliqbookTripId;
@property (nonatomic, retain) NSNumber * hasHotel;
@property (nonatomic, retain) NSDate * tripEndDateUtc;
@property (nonatomic, retain) NSNumber * allowAddAir;
@property (nonatomic, retain) NSNumber * allowAddRail;
@property (nonatomic, retain) NSString * bookedById;
@property (nonatomic, retain) NSNumber * hasParking;
@property (nonatomic, retain) NSNumber * isItinLoaded;
@property (nonatomic, retain) NSNumber * hasDining;
@property (nonatomic, retain) NSNumber * allowAddHotel;
@property (nonatomic, retain) NSString * itinSourceName;
@property (nonatomic, retain) NSString * tripName;
@property (nonatomic, retain) NSNumber * allowAddCar;
@property (nonatomic, retain) NSString * travelPointsPosted;
@property (nonatomic, retain) NSSet *relSegment;
@property (nonatomic, retain) NSSet *relBooking;
@property (nonatomic, retain) NSSet *relViolation;
@end

@interface EntityTrip (CoreDataGeneratedAccessors)

- (void)addRelSegmentObject:(EntitySegment *)value;
- (void)removeRelSegmentObject:(EntitySegment *)value;
- (void)addRelSegment:(NSSet *)values;
- (void)removeRelSegment:(NSSet *)values;

- (void)addRelBookingObject:(EntityBooking *)value;
- (void)removeRelBookingObject:(EntityBooking *)value;
- (void)addRelBooking:(NSSet *)values;
- (void)removeRelBooking:(NSSet *)values;

- (void)addRelViolationObject:(EntityViolation *)value;
- (void)removeRelViolationObject:(EntityViolation *)value;
- (void)addRelViolation:(NSSet *)values;
- (void)removeRelViolation:(NSSet *)values;

@end
