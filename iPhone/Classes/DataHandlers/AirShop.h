//
//  AirShop.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/4/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "TrainStationData.h"
#import "RailChoiceData.h"
#import "RailChoiceSegmentData.h"
#import "RailChoiceTrainData.h"
#import "AirlineEntry.h"
#import "EntityAirRules.h"
#import "Benchmark.h"

@interface AirShop : MsgResponder 
{
	NSXMLParser				*dataParser;
	NSString				*currentElement, *path;
	
	NSString				*isInElement;
	NSMutableDictionary		*items, *stopChoices, *vendors, *prefRankings, *airportCityCodes, *rateTypeChoices;
	NSMutableArray			*aStops, *aRateTypes;
	RailChoiceData			*obj;
    BOOL isInStops, isInAirlineEntry, isRoundTrip, isInRateTypes;
    AirlineEntry *airlineEntry;
    NSString    *numStops;
    NSString    *rateTypes;
    NSMutableString *buildString;
    BOOL isInVendorCodes, isInPrefRanking;
    NSString *vendorCode, *vendorName;
    NSString *cityCode, *cityName;
    NSString *rankAirlineCode, *rankValue;
    BOOL isInAirportCityCodes;
    EntityAirRules  *airRule;
}
@property (nonatomic, strong) EntityAirRules  *airRule;
@property (nonatomic, strong) NSString					*cityCode;
@property (nonatomic, strong) NSString					*cityName;
@property BOOL isInPrefRanking;
@property BOOL isInStops;
@property BOOL isInRateTypes;
@property BOOL isInAirlineEntry;
@property BOOL isInVendorCodes;
@property BOOL isRoundTrip;
@property BOOL isInAirportCityCodes;
@property BOOL isInBenchmark;
@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableDictionary		*items;
@property (nonatomic, strong) NSMutableDictionary		*vendors;
@property (nonatomic, strong) NSMutableDictionary		*airportCityCodes;
@property (nonatomic, strong) RailChoiceData			*obj;
@property (nonatomic, strong) NSMutableArray			*aStops;
@property (nonatomic, strong) NSMutableArray            *aRateTypes;
@property (nonatomic, strong) NSMutableDictionary		*stopChoices;
@property (nonatomic, strong) NSMutableDictionary       *rateTypeChoices;
@property (nonatomic, strong) AirlineEntry *airlineEntry;
@property (nonatomic, strong) Benchmark    *benchmark;
@property (nonatomic, strong) NSString                  *travelPointsInBank;
@property (nonatomic, strong) NSString    *numStops;
@property (nonatomic, strong) NSString    *rateTypes;
@property (nonatomic, strong) NSMutableString *buildString;
@property (nonatomic, strong) NSString *vendorCode;
@property (nonatomic, strong) NSString *vendorName;

@property (nonatomic, strong) NSMutableDictionary *prefRankings;
@property (nonatomic, strong) NSString *rankAirlineCode;
@property (nonatomic, strong) NSString *rankValue;

//- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;
-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag;

@end
