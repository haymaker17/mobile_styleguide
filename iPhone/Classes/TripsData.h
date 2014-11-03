//
//  Trips.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/14/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "TripData.h"
#import "BookingData.h"
#import "SegmentData.h"
#import "FlightStatsData.h"
#import "Msg.h"
#import "EntityTrip.h"
#import "TripManager.h"
#import "OfferManager.h"
#import "EntityViolation.h"


@interface TripsData : MsgResponder 
{
	NSXMLParser				*dataParser;
	NSMutableDictionary		*trips;
	NSMutableArray			*keys;

    BOOL                    inMultiWebLink;
	EntityTrip				*trip;
    EntityBooking           *booking;
    EntitySegment           *segment;
    EntityFlightStats       *flightStats;
    EntityOffer             *offer;
    EntityOfferLocation     *offerLocation;
    EntityOfferTimeRange    *offerTimeRange;
    EntityOfferOverlay      *offerOverlay;
	NSString				*path;
	NSString				*currentElement;

	NSString				*inSegment, *inBooking, *inAirlineTicket, *inActions;
	NSString				*isInElement;
	NSString				*errorInfo, *errorCode;
	BOOL					inFlightStats, inOffer;
	NSMutableString			*buildString;
//    OfferData               *offer;
    
//    NSMutableDictionary     *allowAddBooking;
}

@property BOOL inMultiWebLink;
@property BOOL inOffer;
//@property (nonatomic, strong) OfferData                 *offer;
//@property (nonatomic, strong) MultiWebLinkData			*link;
//@property (nonatomic, strong) OfferLocation             *offerLocation;
//@property (nonatomic, strong) OfferTimeRange            *offerTimeRange;
@property (nonatomic, strong) NSMutableString			*buildString;

@property (nonatomic, strong) NSString					*path;

@property (nonatomic, strong) NSMutableDictionary		*trips;
@property (nonatomic, strong) NSMutableArray			*keys;
@property (nonatomic, strong) NSString					*inSegment;
@property (nonatomic, strong) NSString					*inBooking;
@property (nonatomic, strong) NSString					*inAirlineTicket;
@property (nonatomic, strong) NSString                  *inActions;
@property (nonatomic, strong) EntityTrip				*trip;
@property (nonatomic, strong) EntityBooking             *booking;
@property (nonatomic, strong) EntitySegment             *segment;
@property (nonatomic, strong) EntityFlightStats         *flightStats;
@property (nonatomic, strong) EntityOffer				*offer;
@property (nonatomic, strong) EntityOfferLocation		*offerLocation;
@property (nonatomic, strong) EntityOfferTimeRange		*offerTimeRange;
@property (nonatomic, strong) EntityOfferOverlay		*offerOverlay;
@property (nonatomic, strong) EntityViolation           *violation;
@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, copy) NSString					*errorInfo;
@property (nonatomic, copy) NSString					*errorCode;
//@property (nonatomic, strong) NSMutableDictionary       *allowAddBooking;
@property (nonatomic, strong, readonly) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, strong) NSString *tripStateMessage;
@property BOOL inFlightStats;

//- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

-(void) forceFeedFlightStats;

- (void)saveContext;

@end
