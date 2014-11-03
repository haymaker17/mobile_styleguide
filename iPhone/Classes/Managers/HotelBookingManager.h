//
//  HotelBookingManager.h
//  ConcurMobile
//
//  Created by Paul Kramer on 9/23/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityHotelBooking.h"
#import "HotelViolation.h"
#import "EntityHotelRoom.h"
#import "EntityHotelFee.h"
#import "EntityHotelDetail.h"
#import "EntityHotelViolation.h"

@interface HotelBookingManager : NSObject
{
    NSManagedObjectContext      *_context;
    NSString *entityName;
}

@property (nonatomic, strong) NSManagedObjectContext *context;
@property (nonatomic, strong) NSString *entityName;

-(void) saveIt:(EntityHotelBooking *) obj;
-(BOOL) hasAny;
-(EntityHotelBooking *) makeNew;
-(EntityHotelBooking *) fetchFirst;
-(NSArray *) fetchAll;
-(EntityHotelBooking *) fetchOrMake:(NSString *)key;
-(EntityHotelBooking *) fetchByPropertyId:(NSString *)propertyId;
-(void) deleteAll;
-(void) deletePartialResults;
-(void) deleteObj:(EntityHotelBooking *)obj;


+(HotelBookingManager*)sharedInstance;
-(id)init;
-(EntityHotelBooking *) fetchMostSevre:(NSString *)key;
-(NSArray *) fetchByFareId:(NSString *)key;
-(EntityHotelDetail *) makeNewDetail;
-(EntityHotelRoom *) makeNewRoom;
-(EntityHotelViolation*) fetchHighestEnforcement:(EntityHotelRoom*) room;
-(NSArray *) fetchViolationsByRoom:(EntityHotelRoom*) room;
-(EntityHotelFee *) makeNewFee;
-(EntityHotelViolation*) makeNewViolation;
-(void) deleteRooms:(EntityHotelBooking *)hotelBooking;
-(NSArray *) fetchedAllSorted:(int)sortOrder;
- (BOOL)isAnyHotelRecommended;

-(EntityHotelViolation *) fetchViolationByCode:(NSString *)key;
@end
