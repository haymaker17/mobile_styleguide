//
//  Itinerary.h
//  ConcurMobile
//
//  Created by Wes Barton on 2/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RXMLElement.h"
#import "ItineraryStop.h"

@class CXRequest;
@class ItineraryConfig;

@interface Itinerary : NSObject

@property NSString *itinKey;
@property NSString *itinName;
@property NSString *tacKey;
@property NSString *tacName;
@property NSString *shortDistanceTrip;
@property NSString *rptKey;
@property NSString *tripLength;
@property NSString *areAllRowsLocked;

@property NSMutableArray *stops;

//Use this to pass around the report currency
@property NSString *crnCode;

@property BOOL isCollapsed;

+ (Itinerary *)processItineraryXML:(RXMLElement *)itin rptKey:(NSString *)rptKey;
+ (NSString *)composeItineraryElementWithRow:(ItineraryStop *)stop itinerary:(Itinerary *)itinerary formatter:(NSDateFormatter *)formatter;


+ (NSMutableArray *)parseItinerariesXML:(NSString *)result rptKey:(NSString *)rptKey crnCode:(NSString *)crnCode;
+ (BOOL)isApproving:(NSString *)role;
+ (CXRequest *)getTAItinerariesRequest:(NSString *)reportKey roleCode:(NSString *)roleCode ;

+ (NSString *)composeUpdateItinerarySummary:(Itinerary *)itinerary formatter:(NSDateFormatter *)formatter;

+ (NSString *)composeItineraryWithRows:(Itinerary *)itinerary formatter:(NSDateFormatter *)formatter;

+ (NSString *)parseSaveResultForStatus:(NSString *)result;
+ (NSString *)parseSaveResultForStatusText:(NSString *)result;

+ (CXRequest *)deleteItinerary:(NSString *)itinKey rptKey:(NSString *)rptKey;
+ (CXRequest *)unassignItinerary:(NSString *)itinKey rptKey:(NSString *)key;

+ (BOOL )wasDeleteItinerarySuccessful:(NSString *)result;
+ (BOOL )wasUnassignItinerarySuccessful:(NSString *)result;

+ (Itinerary *)getNewItineraryForSingleDay:(ItineraryConfig *)config itineraryName:(NSString *)itineraryName rptKey:(NSString *)rptKey;
+ (Itinerary *)getNewItineraryRegular:(ItineraryConfig *)config reportName:(NSString *)reportName rptKey:(NSString *)rptKey;

+ (NSString *)getRptKey:(NSMutableDictionary *)paramBag;

+ (NSString *)getReportName:(NSMutableDictionary *)paramBag;

- (BOOL)hasFailures;
@end
