//
//  ItineraryConfig.h
//  ConcurMobile
//
//  Created by Wes Barton on 2/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RXMLElement.h"
#import "CXRequest.h"

@interface ItineraryConfig : NSObject

- (id)initWithTaConfigXML:(RXMLElement *)config;

+ (NSMutableArray *)parseTAConfigXML:(NSString *)result;

+ (CXRequest *)getTAConfig;


//<TaConfig>
//<bikMealsDeduction> N</bikMealsDeduction>
//<combineMealsAndLodgingRate> N</combineMealsAndLodgingRate>
//<configCode> TAC100007</configCode>
@property NSString *configCode;

//<deductForProvidedBreakfast> Y</deductForProvidedBreakfast>
//<deductForProvidedDinner> N</deductForProvidedDinner>
//<deductForProvidedLunch> N</deductForProvidedLunch>

//<defaultBreakfastToProvided> Y</defaultBreakfastToProvided>
@property BOOL defaultBreakfastToProvided;

//<defaultDinnerToProvided> N</defaultDinnerToProvided>
@property BOOL defaultDinnerToProvided;

//<defaultLunchToProvided> N</defaultLunchToProvided>
@property BOOL defaultLunchToProvided;

//<displayBaseMealsRate> N</displayBaseMealsRate>
//<displayCompanyAndGovernment> N</displayCompanyAndGovernment>

//<displayQuickItinPage> Y</displayQuickItinPage>
@property BOOL displayQuickItinPage;

//<displayWizard> ALWS</displayWizard>
@property NSString *displayWizard;

//<doubleDipCheck> Y</doubleDipCheck>

//<exchangeRateDay> EXPE</exchangeRateDay>
@property NSString *exchangeRateDay;

//<govtCompRateTypes> GOVT</govtCompRateTypes>
@property NSString *govtCompRateTypes;

//<isDeleted> N</isDeleted>

//<lodgingTat> FIXED</lodgingTat>
@property NSString *lodgingTat;

//<mealsTat> FIXED</mealsTat>
@property NSString *mealsTat;

//<rfKey>2</rfKey>

//<sameDay> NONE</sameDay>
@property NSString *sameDay;

//<singleRowCheck> Y</singleRowCheck>

//<tacKey>7</tacKey>
@property NSString *tacKey;

//<useBorderCrossTime> N</useBorderCrossTime>
@property BOOL useBorderCrossTime;

//<useExtendedTripRule> N</useExtendedTripRule>
@property BOOL useExtendedTripRule;

@property BOOL useShortDistance;

@property BOOL tripLengthList;

@property NSString *defaultLnKey;
@property NSString *defaultLocName;



//<useLodgingType> N</useLodgingType>
//<useOvernight> N</useOvernight>
//<usePercentRule> N</usePercentRule>
//<useWithinMunicipalArea> N</useWithinMunicipalArea>
//<userEntryOfBreakfastAmount> N</userEntryOfBreakfastAmount>
//<userEntryOfMealsAmount> N</userEntryOfMealsAmount>
//<userEntryOfRateLocation> Y</userEntryOfRateLocation>
//</TaConfig>




@property(nonatomic, copy) NSString *tripLengthListLabel;
@property NSMutableArray *tripLengthListKeys;
@property NSMutableDictionary *tripLengthListValues;
@end
