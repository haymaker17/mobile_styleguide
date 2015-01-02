//
//  ItineraryStop.h
//  ConcurMobile
//
//  Created by Wes Barton on 1/22/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CXRequest.h"

@class RXMLElement;
@class Itinerary;
@class ItineraryConfig;

@interface ItineraryStop : NSObject


+ (CXRequest *)deleteItineraryStop:(NSString *)itinKey irKey:(NSString *)irKey;
+ (BOOL )wasDeleteItineraryStopSuccessful:(NSString *)result;

+ (ItineraryStop *)parseItineraryRow:(RXMLElement *)row;

+ (NSDate *) getNSDateFromItineraryRow:(NSString *) dateString;

+ (NSString *)parseSaveResultForStatus:(NSString *)result;

+ (NSString *)parseSaveResultForStatusTextLocalized:(NSString *)result;

+ (NSString *)parseSaveResultForStatusText:(NSString *)result;


+ (NSString *)composeItineraryRow:(ItineraryStop *)stop formatter:(NSDateFormatter *)formatter;

//+ (NSString *)composeAddStopToItinerary:(ItineraryStop *)stop itinerary:(Itinerary *)itinerary formatter:(NSDateFormatter *)formatter;



+ (void)applyStopNumbers:(NSMutableArray *)stops;

+ (void)processItineraryRowXML:(RXMLElement *)itin stops:(NSMutableArray *)stops;

+ (void)defaultDatesForStop:(ItineraryStop *)stop;

+ (void)copyDatesFromStopArrival:(ItineraryStop *)to from:(ItineraryStop *)from;

+ (void)defaultDepartureLocationForStop:(ItineraryConfig *)config stop:(ItineraryStop *)stop;

+ (ItineraryStop *)getNewStop:(ItineraryConfig *)config itinerary:(Itinerary *)itinerary;

- (BOOL)isRowFailureDateTime;

+ (void)defaultSingleDayTimesForFirstStop:(ItineraryStop *)stop;
+ (void)defaultSingleDayTimesForLastStop:(ItineraryStop *)stop;

- (BOOL)isRowFailureBorderDateTime;

//Informatino passed back from the server
@property NSString *status;
@property NSString *statusText;
@property NSString *statusTextLocalized;
@property BOOL isFailed;

// <IrKey>n9DN2Jy06b6K9Xk8R7OUp45k</IrKey>
@property NSString *irKey;

// <ArrivalDateTime>2013-12-19 14:00</ArrivalDateTime>
@property (nonatomic, strong) NSDate *arrivalDate;

// <ArrivalLocation>Louisville, Mississippi</ArrivalLocation>
@property NSString *arrivalLocation;

// <ArrivalLnKey>27637</ArrivalLnKey>
@property NSString *arrivalLnKey;

// <DepartDateTime>2013-12-19 12:00</DepartDateTime>
@property (nonatomic, strong) NSDate *departureDate;
@property BOOL showDepartureDateCalendar;
@property BOOL showArrivalDateCalendar;


// <DepartLocation>Seattle, Washington</DepartLocation>
@property NSString *departureLocation;

// <DepartLnKey>29928</DepartLnKey>
@property NSString *departLnKey;


// <ArrivalRlKey>44</ArrivalRlKey>
@property NSString *arrivalRlKey;

// <ArrivalRateLocation>UNITED STATES</ArrivalRateLocation>
@property NSString *arrivalRateLocation;

// <IsArrivalRateLocationEditable>N</IsArrivalRateLocationEditable>
@property BOOL arrivalRateLocationEditable;

// <IsRowLocked>N</IsRowLocked>
@property BOOL rowLocked;

// <BorderCrossDateTime>2013-12-19 12:00</BorderCrossDateTime>
@property (nonatomic, strong) NSDate *borderCrossDate;

@property NSNumber *stopNumber;


//@property BOOL completed;
//@property (readonly) NSDate *creationDate;

@property(nonatomic) BOOL justCreated;
@property(nonatomic, copy) NSString *taImportId;
@end
