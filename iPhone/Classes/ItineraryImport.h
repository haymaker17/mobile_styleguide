//
//  ItineraryImport.h
//  ConcurMobile
//
//  Created by Wes Barton on 5/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class CXRequest;
@class RXMLElement;

@interface ItineraryImport : NSObject


//<Header>
//<EndDate>2014-04-21 00:00:00.0</EndDate>
@property (nonatomic, strong) NSDate *endDate;
//<EndDateNoTime>2014-04-21 00:00:00.0</EndDateNoTime>
//<First>false</First>
@property BOOL first;
//<FirstOrLast>false</FirstOrLast>
@property BOOL firstOrLast;
//<HasAir>true</HasAir>
@property BOOL hasAir;
//<HasHotel>true</HasHotel>
@property BOOL hasHotel;
//<HasRail>false</HasRail>
@property BOOL hasRail;
//<HeaderType> Trip</HeaderType>
@property NSString *headerType;
//<HideFromList>false</HideFromList>
@property BOOL hideFromList;
//<IsConnection>false</IsConnection>
@property BOOL isConnection;
//<Last>false</Last>
@property BOOL last;
//<StartDate>2014-04-21 00:00:00.0</StartDate>
@property(nonatomic, strong) NSDate *startDate;
//<StartDateNoTime>2014-04-21 00:00:00.0</StartDateNoTime>
//<TaImportId> T3113</TaImportId>
@property NSString *taImportId;
//<ToBeDeleted>false</ToBeDeleted>
@property BOOL toBeDeleted;
//<TripId>3113</TripId>
@property NSString *tripId;
//<TripName> DemoTrip (DEMO00)</TripName>
@property NSString *tripName;
//</Header>

@property BOOL include;

@property NSMutableArray *rows;



+ (NSDate *) getNSDateFromImport:(NSString *) dateString;

- (id)initWithXML:(RXMLElement *)response;

+ (CXRequest *)getTravelAllowanceImport:(NSString *)reportKey;


+ (NSMutableArray *)parseImportXML:(NSString *)result;

+ (CXRequest *)getImportTravelAllowanceItinerary;

+ (NSString *)composeImportTravelAllowanceItinerary:(NSMutableArray *)importIds rptKey:(NSString *)rptKey itinKey:(NSString *)itinKey role:(NSString *)role;

+ (NSMutableArray *)parseItineraryImportResult:(NSString *)result rptKey:(NSString *)rptKey;
@end


