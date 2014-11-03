//
//  TripData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BookingData.h"
#import "OfferData.h"
#import "EntityTrip.h"
#import "EntityFlightStats.h"

@interface TripData : NSObject 
{
	NSString				*itinSourceName, *bookedById, *tripName, *orgUnitName, *tripDescription, *tripComments, *tripStartDateLocal
	,*tripEndDateLocal, *tripStartDateUtc, *tripEndDateUtc, *tripIsPersonal, *tripIsPrivate, *tripStatus, *tripKey, *cliqbookTripId, *clientLocator, *itinLocator;
	NSMutableDictionary		*bookings, *offers;
	NSMutableArray			*bookingKeys;
	BookingData				*booking; //the current booking of the trip
	BOOL					hasAir, hasRail, hasEvent, hasDining, hasCar, hasRide, hasUndefined, hasHotel, hasParking, hasValidOffers, isExpensed;
	int						bookingId;
    int                     state; // 101 is waiting for approval
}

@property (strong, nonatomic) NSMutableDictionary		*offers;
@property (nonatomic, strong) NSString *itinSourceName; 
@property (nonatomic, strong) NSString *itinLocator;
@property (nonatomic, strong) NSString *tripName; 
@property (nonatomic, strong) NSString *tripStartDateLocal; 
@property (nonatomic, strong) NSString *tripEndDateLocal; 
@property (nonatomic, strong) NSString *tripKey;
@property (nonatomic, strong) NSString *cliqbookTripId;
@property (nonatomic, strong) NSString *clientLocator;

@property (nonatomic, strong) NSMutableDictionary *bookings;
@property (nonatomic, strong) NSMutableArray *bookingKeys;
@property (nonatomic, strong) BookingData *booking;

@property int state;

@property BOOL hasAir;
@property BOOL hasRail;
@property BOOL hasEvent;
@property BOOL hasDining;
@property BOOL hasCar;
@property BOOL hasRide;
@property BOOL hasUndefined;
@property BOOL hasHotel;
@property BOOL hasParking;
@property BOOL hasValidOffers;
@property BOOL isExpensed;

-(id) init;
-(void)initWithBookings;
//-(void)addBooking;
-(void)finishBooking;
//-(BookingData*)getPrimaryBooking;
-(NSString *)getBookingRecLocs;
-(NSString *)getRecLocsBySegmentType:(NSString *)segType;
//-(NSArray *)getBookingsOrderByType;
+ (NSMutableDictionary *)getSegmentsOrderByDate:(EntityTrip *) trip;
//-(SegmentData *) getSegment:(NSString *)segKey;
//-(NSArray *)getAirSegmentsInDateOrder;
-(NSMutableString *) getAgencyPCC;
-(NSMutableString *) getBookingSources;
+(NSMutableString *) getCompanyAccountingCodes:(EntityTrip *) trip;
+(NSMutableString *) getRecordLocators:(EntityTrip *) trip;
+(NSMutableString *) getTravelConfigIds:(EntityTrip *) trip;
+(NSMutableString *) getTypes:(EntityTrip *) trip;
-(BOOL) isWaitingForApproval;

-(int) getSegmentCount;


#pragma mark - APIs to handle EntityTrip
+ (NSArray*) makeSegmentsArray:(EntityTrip *) trip;
+ (EntityBooking*)getPrimaryBooking:(EntityTrip *) trip;
+ (NSMutableDictionary *)makeSegmentDictGroupedByDate:(EntityTrip *) trip;
+ (NSMutableArray *)makeSegmentArrayGroupedByDate:(EntityTrip *) trip;

#pragma mark - APIs for booking defaults
+(NSMutableDictionary*)getHotelAndCarDefaultsFromFlightInTripSegments:(NSDictionary*)segmentDict withKeys:(NSArray*)keys;
+(NSMutableDictionary*)getHotelAndCarDefaultsFromRailInTripSegments:(NSDictionary*)segmentDict withKeys:(NSArray*)keys;
+(NSMutableDictionary*)getHotelAndCarDefaultsFromHotelInTripSegments:(NSDictionary*)segmentDict withKeys:(NSArray*)keys;
+(NSMutableDictionary*)getHotelAndCarDefaultsFromCarInTripSegments:(NSDictionary*)segmentDict withKeys:(NSArray*)keys;
+(void)addHotelAndCarLocationsToDictionary:(NSMutableDictionary*)dictionary address:(NSString*)address locationName:(NSString*)locationName iataCode:(NSString *)iataCode;
+(EntitySegment*)getFirstSegmentOfSegmentType:(NSString*)segmentType inTripSegments:(NSDictionary*)segmentDict withKeys:(NSArray*)keys;
+(EntitySegment*)getLastSegmentOfSegmentType:(NSString*)segmentType inTripSegments:(NSDictionary*)segmentDict withKeys:(NSArray*)keys;

#pragma - Utility function, flight status
+ (BOOL) isFlightDelayedOrCancelled:(EntityFlightStats *) flightStat;

+ (NSString*) getFirstDestination:(EntityTrip *) trip;

-(void)setFromEntityTrip:(EntityTrip*) oneTrip;

@end
