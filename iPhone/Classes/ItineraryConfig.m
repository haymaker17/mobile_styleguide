//
//  ItineraryConfig.m
//  ConcurMobile
//
//  Created by Wes Barton on 2/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryConfig.h"
#import "CXRequest.h"


@implementation ItineraryConfig

- (id)initWithTaConfigXML:(RXMLElement *)configX
{
    NSString *tacKey = [configX child:@"tacKey"].text;
    NSLog(@"tacKey = %@", tacKey);
    self.tacKey = tacKey;

    self.defaultBreakfastToProvided =  [[configX child:@"defaultBreakfastToProvided"].text boolValue];
    self.defaultLunchToProvided =  [[configX child:@"defaultLunchToProvided"].text boolValue];
    self.defaultDinnerToProvided =  [[configX child:@"defaultDinnerToProvided"].text boolValue];

    self.displayQuickItinPage =  [[configX child:@"displayQuickItinPage"].text boolValue];

    self.useBorderCrossTime = [[configX child:@"useBorderCrossTime"].text boolValue];

    self.useExtendedTripRule = [[configX child:@"useExtendedTripRule"].text boolValue];
    self.useShortDistance = [[configX child:@"useShortDistance"].text boolValue];

    self.tripLengthList = [[configX child:@"tripLengthList"].text boolValue];

    self.defaultLnKey = [configX child:@"defaultLnKey"].text;
    self.defaultLocName = [configX child:@"defaultLocName"].text;

    self.tripLengthListValues = [[NSMutableDictionary alloc]init];
    self.tripLengthListKeys = [[NSMutableArray alloc]init];
    RXMLElement *systemList = [configX child:@"travelAllowanceSystemList"];
    if(systemList != nil)
    {
        self.tripLengthListLabel = [systemList child:@"listLabel"].text;

        NSArray *listValues = [systemList children:@"listValues"];
        for (RXMLElement *listValue in listValues) {
            NSString *code = [listValue child:@"code"].text;
            NSString *value = [listValue child:@"value"].text;
            NSLog(@"-----code = %@", code);
            NSLog(@"----value = %@", value);


            [self.tripLengthListKeys addObject:code];
            [self.tripLengthListValues setValue:value forKey:code];
        }
    }

    return self;
}


+ (CXRequest *)getTAConfig {

    // Compose the path
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/GetTAConfigForEmployee"];

    // Create the request
    return [[CXRequest alloc] initWithServicePath:path];

}

/*<Body>
<TaConfig>
<bikMealsDeduction> N</bikMealsDeduction>
<combineMealsAndLodgingRate> N</combineMealsAndLodgingRate>
<configCode> TAC100007</configCode>
<deductForProvidedBreakfast> Y</deductForProvidedBreakfast>
<deductForProvidedDinner> N</deductForProvidedDinner>
<deductForProvidedLunch> N</deductForProvidedLunch>
<defaultBreakfastToProvided> Y</defaultBreakfastToProvided>
<defaultDinnerToProvided> N</defaultDinnerToProvided>
<defaultLunchToProvided> N</defaultLunchToProvided>
<displayBaseMealsRate> N</displayBaseMealsRate>
<displayCompanyAndGovernment> N</displayCompanyAndGovernment>
<displayQuickItinPage> Y</displayQuickItinPage>
<displayWizard> ALWS</displayWizard>
<doubleDipCheck> Y</doubleDipCheck>
<exchangeRateDay> EXPE</exchangeRateDay>
<govtCompRateTypes> GOVT</govtCompRateTypes>
<isDeleted> N</isDeleted>
<lodgingTat> FIXED</lodgingTat>
<mealsTat> FIXED</mealsTat>
<rfKey>2</rfKey>
<sameDay> NONE</sameDay>
<singleRowCheck> Y</singleRowCheck>
<tacKey>7</tacKey>
<useBorderCrossTime> N</useBorderCrossTime>
<useExtendedTripRule> N</useExtendedTripRule>
<useLodgingType> N</useLodgingType>
<useOvernight> N</useOvernight>
<usePercentRule> N</usePercentRule>
<useWithinMunicipalArea> N</useWithinMunicipalArea>
<userEntryOfBreakfastAmount> N</userEntryOfBreakfastAmount>
<userEntryOfMealsAmount> N</userEntryOfMealsAmount>
<userEntryOfRateLocation> Y</userEntryOfRateLocation>
<travelAllowanceSystemList>
<listLabel>Trip Length</listLabel>
<listValues>
    <code>1</code><value>3 months or less</value>
</listValues>
<listValues>
    <code>2</code><value>Over 3 months</value>
</listValues>
<listValues>
    <code>3</code><value>Over 48 months</value>
</listValues>
</travelAllowanceSystemList>
<tripLengthList>Y</tripLengthList>

</TaConfig>
</Body>*/

+ (NSMutableArray *)parseTAConfigXML:(NSString *)result {
    NSMutableArray *ret = [[NSMutableArray alloc]init];

    RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
    if (rootXML != nil) {

    }

    RXMLElement *body = [rootXML child:@"Body"];
    NSArray *configs = [body children:@"TaConfig"];

    if ([configs count] > 0)
    {
        if ([configs count] > 1)
        {
            NSLog(@"Too many TA Configs");
        }
        RXMLElement *config =[configs objectAtIndex:0];

        ItineraryConfig *itineraryConfig = [[ItineraryConfig alloc] initWithTaConfigXML:config];

        [ret addObject:itineraryConfig];

    }

    return ret;
}


@end
