//
//  AirFilter.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/8/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RailChoiceData.h"
#import "AirlineEntry.h"
#import "EntityAirFilter.h"
#import "EntityAirFilterSummary.h"
#import "AirFilterManager.h"
#import "AirFilterSummaryManager.h"
#import "AirShop.h"
#import "EntityAirRules.h"
#import "EntityAirViolation.h"
#import "AirViolationManager.h"

@interface AirFilter : MsgResponder 
{
	NSXMLParser				*dataParser;
	NSString				*currentElement, *path;
	
	NSString				*isInElement;
	NSMutableDictionary		*items, *stopChoices, *vendors;
	NSMutableArray			*aStops;
	RailChoiceData			*obj;
    BOOL isInStops, isInAirlineEntry;
    AirlineEntry *airlineEntry;
    NSString    *numStops;
    NSMutableString *buildString;
    BOOL isInVendorCodes;
    NSString *vendorCode, *vendorName;
    
    NSString *crnCode, *fare, *fareId;
    EntityAirFilter *airFilter;
    EntityAirFilterSummary *airFilterSummary;
    int iAirSeg, iFlight, iNumStops, iDuration, iRoundNumStops, iRoundDuration;
    float elapsedTime;
    AirShop *airShop;
    EntityAirRules  *airRule;
    EntityAirViolation *airViolation;
    BOOL inViolation;
}
@property (nonatomic, strong) EntityAirRules  *airRule;
@property (nonatomic, strong) AirShop *airShop;
@property (nonatomic, strong) EntityAirViolation *airViolation;
@property float elapsedTime;
@property int iNumStops;
@property int iDuration;
@property int iRoundNumStops;
@property int iRoundDuration;
@property int iAirSeg;
@property int iFlight;
@property BOOL isInStops;
@property BOOL isInAirlineEntry;
@property BOOL isInVendorCodes;
@property BOOL inViolation;
@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableDictionary		*items;
@property (nonatomic, strong) NSMutableDictionary		*vendors;
@property (nonatomic, strong) RailChoiceData			*obj;
@property (nonatomic, strong) NSMutableArray			*aStops;
@property (nonatomic, strong) NSMutableDictionary		*stopChoices;
@property (nonatomic, strong) AirlineEntry *airlineEntry;
@property (nonatomic, strong) NSString    *numStops;
@property (nonatomic, strong) NSMutableString *buildString;
@property (nonatomic, strong) NSString *vendorCode;
@property (nonatomic, strong) NSString *vendorName;

@property (nonatomic, strong) NSString *crnCode;
@property (nonatomic, strong) NSString *fare;
@property (nonatomic, strong) NSString *fareId;
@property (nonatomic, strong) EntityAirFilter *airFilter;
@property (nonatomic, strong) EntityAirFilterSummary *airFilterSummary;


//- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;
-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag;

-(void)figureOutRankingsForFlights;
@end
