//
//  FixedAllowance.m
//  ConcurMobile
//
//  Created by Wes Barton on 2/23/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FixedAllowance.h"
#import "RXMLElement.h"
#import "ItineraryStop.h"


@implementation FixedAllowance

- (id)initWithXML:(RXMLElement *)allowance
{

    self.isFirstDay = (BOOL) [[ allowance child:@"IsFirstDay"].text boolValue];
    self.isLastDay = (BOOL) [[ allowance child:@"IsLastDay"].text boolValue];
    self.isLocked = (BOOL) [[ allowance child:@"IsLocked"].text boolValue];
    self.isReadOnly = (BOOL) [[ allowance child:@"IsReadOnly"].text boolValue];
    self.markedExcluded = (BOOL) [[ allowance child:@"MarkedExcluded"].text boolValue];
    self.inUseLock = (BOOL) [[ allowance child:@"InUseLock"].text boolValue];
    self.overnight = (BOOL) [[ allowance child:@"Overnight"].text boolValue];
    self.applyExtendedTripRule = (BOOL) [[ allowance child:@"ApplyExtendedTripRule"].text boolValue];
    self.applyPercentRule = (BOOL) [[ allowance child:@"ApplyPercentRule"].text boolValue];
    self.withinMunicipalArea = (BOOL) [[ allowance child:@"WithinMunicipalArea"].text boolValue];

    self.taDayKey = [ allowance child:@"TaDayKey"].text;
    self.itinKey = [ allowance child:@"ItinKey"].text;
    self.fixedRptKey = [ allowance child:@"FixedRptKey"].text;
    self.lodgingType = [ allowance child:@"LodgingType"].text;
    self.breakfastProvided = [ allowance child:@"BreakfastProvided"].text;
    self.lunchProvided = [ allowance child:@"LunchProvided"].text;
    self.dinnerProvided = [ allowance child:@"DinnerProvided"].text;
    self.location = [ allowance child:@"Location"].text;

    NSString *allowanceDateRawText = [ allowance child:@"AllowanceDate"].text;
    self.allowanceDateString = allowanceDateRawText;

    NSDate *allowanceDate = [ItineraryStop getNSDateFromItineraryRow:allowanceDateRawText];
    self.allowanceDate = allowanceDate;

    self.allowanceAmount = [ allowance child:@"AllowanceAmount"].text;

    self.aboveLimitAmount = [ allowance child:@"AboveLimitAmount"].text;
    self.mealsBaseAmount = [ allowance child:@"MealsBaseAmount"].text;

    [self parseBreakfastAmount:allowance];

    self.isCollapsed = YES;

    return self;
}

- (void)parseBreakfastAmount:(RXMLElement *)allowance {
//    NSNumberFormatter *f = [[NSNumberFormatter alloc]init];
    //Is there a breakfast amount?

    self.breakfastTransactionAmount = [NSDecimalNumber decimalNumberWithString:[allowance child:@"BreakfastTransactionAmount"].text];
    self.breakfastPostedAmount = [NSDecimalNumber decimalNumberWithString:[allowance child:@"BreakfastPostedAmount"].text];

    self.breakfastCrnKey = [[allowance child:@"BreakfastCrnKey"].text integerValue];
    self.breakfastCrnCode = [allowance child:@"BreakfastCrnCode"].text;
    self.breakfastCrnName = [allowance child:@"BreakfastCrnName"].text;

    self.breakfastErDirection = [allowance child:@"BreakfastErDirection"].text;

    NSString *exRate = [allowance child:@"BreakfastExchangeRate"].text;
    NSLog(@"exRate = %@", exRate);
    if(exRate == nil)
    {
        NSLog(@"Exchange Rate was Null");
        NSLog(@"self.breakfastCrnKey = %p", self.breakfastCrnKey);
    }
    else
    {
        self.breakfastExchangeRate = [NSDecimalNumber decimalNumberWithString:[allowance child:@"BreakfastExchangeRate"].text];
    }


//    NSLog(@"self.breakfastTransactionAmount = %@", self.breakfastTransactionAmount);
//    NSLog(@"self.breakfastPostedAmount = %@", self.breakfastPostedAmount);
//    NSLog(@"self.breakfastCrnKey = %p", self.breakfastCrnKey);
    NSLog(@"self.breakfastExchangeRateValue = %@", self.breakfastExchangeRate);
}

/*
<?
xml version = "1.0"
encoding = "UTF-8" ?
><Response>
<Header>
<Version>1.0</Version><Log>
<Level> None<
/Level></Log><TravelerUID>15679184</TravelerUID><ExpenseUID>2360</ExpenseUID><CliqSessionID>6ECC38FB

- D9AB

-

4A5F

- BE3A

- C36F3770749A

</CliqSessionID>
<LoginID> wes
.german@randomverbs.com</LoginID>
<EntityID> p0195717zmbc<
/EntityID><CompanyID>130439</CompanyID><SUVersion>102.0</SUVersion>
<IsMobile> Y<
/IsMobile>
<IsTestUser> N<
/IsTestUser>
<SkipVersionCheck> Y<
/SkipVersionCheck><HmcUserKey />
<RequestOrigin> MOBILE<
/RequestOrigin><PerfData>
<TotalDuration>50</TotalDuration><DBPerfItemTotal>33</DBPerfItemTotal></PerfData></Header><Body>
<Status> SUCCESS<
/Status>
<StatusText> TravelAllowance
.GetFixedAllowances.Success</StatusText><Control>
<ShowUserEntryOfBreakfastAmount> Y<
/ShowUserEntryOfBreakfastAmount>
<ShowUserEntryOfMealsAmount> N<
/ShowUserEntryOfMealsAmount>
<ShowBreakfastProvidedCheckBox> N<
/ShowBreakfastProvidedCheckBox>
<ShowLunchProvidedCheckBox> N<
/ShowLunchProvidedCheckBox>
<ShowDinnerProvidedCheckBox> N<
/ShowDinnerProvidedCheckBox>
<ShowBreakfastProvidedPickList> Y<
/ShowBreakfastProvidedPickList>
<ShowLunchProvidedPickList> Y<
/ShowLunchProvidedPickList>
<ShowDinnerProvidedPickList> Y<
/ShowDinnerProvidedPickList>
<ShowOvernightCheckBox> N<
/ShowOvernightCheckBox>
<ShowOvernightAsNightAllowance> N<
/ShowOvernightAsNightAllowance>
<ShowAboveLimit> N<
/ShowAboveLimit>
<ShowMealsBaseAmount> N<
/ShowMealsBaseAmount>
<ShowLodgingTypePickList> N<
/ShowLodgingTypePickList>
<ShowPercentRuleCheckBox> N<
/ShowPercentRuleCheckBox>
<ShowExtendedTripCheckBox> N<
/ShowExtendedTripCheckBox>
<ShowMunicipalityCheckBox> N<
/ShowMunicipalityCheckBox>
<ShowExcludeCheckBox> Y<
/ShowExcludeCheckBox>
<ShowAllowanceAmount> Y<
/ShowAllowanceAmount>
<ApplyExtendedTripRuleLabel> Extended
Trip</ApplyExtendedTripRuleLabel>
<ApplyPercentRuleLabel> Use
Percent Rule<
/ApplyPercentRuleLabel>
<ExcludeLabel> Exclude<
/ExcludeLabel>
<LodgingTypeLabel> Lodging
Type</LodgingTypeLabel>
<MunicipalAreaLabel> Within
Municipal Area<
/MunicipalAreaLabel>
<BreakfastProvidedLabel> Breakfast
Provided</BreakfastProvidedLabel>
<DinnerProvidedLabel> Dinner
Provided</DinnerProvidedLabel>
<LunchProvidedLabel> Lunch
Provided</LunchProvidedLabel>
<OvernightLabel> Overnight<
/OvernightLabel></Control><FixedAllowances>
<FixedAllowanceRow>
<IsFirstDay> Y<
/IsFirstDay>
<IsLastDay> N<
/IsLastDay>
<IsLocked> N<
/IsLocked>
<IsReadOnly> N<
/IsReadOnly><TaDayKey>7</TaDayKey>
<ItinKey> nA5czX$snTkXDHlKph7kCph7g<
/ItinKey>
<MarkedExcluded> N<
/MarkedExcluded>
<FixedRptKey> nMvbr$p2vS05h0XkjrVeB3fjTO<
/FixedRptKey>
<InUseLock> N<
/InUseLock><AllowanceDate>2014

-

03

-

20 00:00</AllowanceDate>
<Overnight> N<
/Overnight>
<ApplyExtendedTripRule> N<
/ApplyExtendedTripRule>
<ApplyPercentRule> N<
/ApplyPercentRule>
<LodgingType> HOTEL<
/LodgingType>
<WithinMunicipalArea> N<
/WithinMunicipalArea><AllowanceAmount>0.91330000</AllowanceAmount>
<BreakfastProvided> PRO<
/BreakfastProvided>
<LunchProvided> NPR<
/LunchProvided>
<DinnerProvided> TAX<
/DinnerProvided>
<BreakfastTransactionAmount>3.00000000</BreakfastTransactionAmount>
<BreakfastPostedAmount>2.15670000</BreakfastPostedAmount>
<BreakfastCrnKey>1</BreakfastCrnKey>
<BreakfastCrnCode> USD</BreakfastCrnCode>
<BreakfastCrnName> US, Dollar</BreakfastCrnName>
<BreakfastExchangeRate>0.71890000000000</BreakfastExchangeRate>
<BreakfastErDirection> M</BreakfastErDirection>
<Location> Bonn, GERMANY<
/Location><AboveLimitAmount>0.91330000</AboveLimitAmount><MealsBaseAmount>6.00000000</MealsBaseAmount></FixedAllowanceRow><FixedAllowanceRow>
<IsFirstDay> N<
/IsFirstDay>
<IsLastDay> Y<
/IsLastDay>
<IsLocked> N<
/IsLocked>
<IsReadOnly> N<
/IsReadOnly><TaDayKey>8</TaDayKey>
<ItinKey> nA5czX$snTkXDHlKph7kCph7g<
/ItinKey>
<MarkedExcluded> N<
/MarkedExcluded>
<FixedRptKey> nMvbr$p2vS05h0XkjrVeB3fjTO<
/FixedRptKey>
<InUseLock> N<
/InUseLock><AllowanceDate>2014

-

03

-

21 00:00</AllowanceDate>
<Overnight> N<
/Overnight>
<ApplyExtendedTripRule> N<
/ApplyExtendedTripRule>
<ApplyPercentRule> N<
/ApplyPercentRule>
<LodgingType> HOTEL<
/LodgingType>
<WithinMunicipalArea> N<
/WithinMunicipalArea><AllowanceAmount>4.40000000</AllowanceAmount>
<BreakfastProvided> TAX<
/BreakfastProvided>
<LunchProvided> NPR<
/LunchProvided>
<DinnerProvided> NPR<
/DinnerProvided><BreakfastTransactionAmount>0.00000000</BreakfastTransactionAmount><BreakfastPostedAmount>0.00000000</BreakfastPostedAmount><BreakfastCrnKey>2</BreakfastCrnKey>
<BreakfastCrnCode> EUR<
/BreakfastCrnCode>
<BreakfastCrnName> Euro<
/BreakfastCrnName><BreakfastExchangeRate>1.00000000000000</BreakfastExchangeRate>
<BreakfastErDirection> M<
/BreakfastErDirection>
<Location> Bonn, GERMANY<
/Location><AboveLimitAmount>4.40000000</AboveLimitAmount><MealsBaseAmount>6.00000000</MealsBaseAmount></FixedAllowanceRow></FixedAllowances></Body></Response>

*/

/*

<FixedAllowanceRow>
<TaDayKey>68</TaDayKey>
<FixedRptKey>nduJxVGsLdSPM3udh2Ydit$sA</FixedRptKey>
<IsFirstDay>Y</IsFirstDay>
<BreakfastProvided>NPR</BreakfastProvided>
</FixedAllowanceRow>

 */

- (NSString *)createUpdateSegmentXML:(AllowanceControl *)control
{
    NSMutableString *block = [[NSMutableString alloc] init];
    /*
    <FixedAllowanceRow>
    <TaDayKey>68</TaDayKey>
    <FixedRptKey>nduJxVGsLdSPM3udh2Ydit$sA</FixedRptKey>
    <IsFirstDay>Y</IsFirstDay>
    <BreakfastProvided>NPR</BreakfastProvided>
    </FixedAllowanceRow>
       */

    [block appendString:@"<FixedAllowanceRow>"];

    [block appendString:[NSString stringWithFormat:@"<TaDayKey>%@</TaDayKey>",self.taDayKey]];

    [block appendString:[NSString stringWithFormat:@"<FixedRptKey>%@</FixedRptKey>",self.fixedRptKey]];

    [block appendString:[NSString stringWithFormat:@"<IsFirstDay>%@</IsFirstDay>",self.isFirstDay ? @"Y":@"N"]];

    if(control.showBreakfastProvidedCheckBox || control.showBreakfastProvidedPickList)
    {
        [block appendString:[NSString stringWithFormat:@"<BreakfastProvided>%@</BreakfastProvided>",self.breakfastProvided]];
    }
    if(control.showLunchProvidedCheckBox || control.showLunchProvidedPickList)
    {
        [block appendString:[NSString stringWithFormat:@"<LunchProvided>%@</LunchProvided>",self.lunchProvided]];
    }
    if(control.showDinnerProvidedCheckBox || control.showDinnerProvidedPickList)
    {
        [block appendString:[NSString stringWithFormat:@"<DinnerProvided>%@</DinnerProvided>",self.dinnerProvided]];
    }

    [block appendString:[NSString stringWithFormat:@"<MarkedExcluded>%@</MarkedExcluded>",self.markedExcluded ? @"Y":@"N"]];

    if(control.showLodgingTypePickList)
    {
        [block appendString:[NSString stringWithFormat:@"<LodgingType>%@</LodgingType>",self.lodgingType]];
    }

    //<Overnight>Y</Overnight>
    if(control.showOvernightCheckBox)
    {
        [block appendString:[NSString stringWithFormat:@"<Overnight>%@</Overnight>",self.overnight ? @"Y":@"N"]];
    }

    //<ApplyExtendedTripRule>N</ApplyExtendedTripRule>
    if(control.showExtendedTripCheckBox)
    {
        [block appendString:[NSString stringWithFormat:@"<ApplyExtendedTripRule>%@</ApplyExtendedTripRule>",self.applyExtendedTripRule ? @"Y":@"N"]];
    }

    //<ApplyPercentRule>N</ApplyPercentRule>
    if(control.showPercentRuleCheckBox)
    {
        [block appendString:[NSString stringWithFormat:@"<ApplyPercentRule>%@</ApplyPercentRule>",self.applyPercentRule ? @"Y":@"N"]];
    }



    if(control.showUserEntryOfBreakfastAmount || control.showUserEntryOfMealsAmount)
    {
        [block appendString:[NSString stringWithFormat:@"<BreakfastTransactionAmount>%@</BreakfastTransactionAmount>",[self.breakfastTransactionAmount stringValue]]];

        [block appendString:[NSString stringWithFormat:@"<BreakfastPostedAmount>%@</BreakfastPostedAmount>",[self.breakfastPostedAmount stringValue]]];

        [block appendString:[NSString stringWithFormat:@"<BreakfastCrnCode>%@</BreakfastCrnCode>",self.breakfastCrnCode]];

        if(self.breakfastExchangeRate != nil)
        {
            NSString *er = [self.breakfastExchangeRate stringValue];
            NSLog(@"er = %@", er);
            [block appendString:[NSString stringWithFormat:@"<BreakfastExchangeRate>%@</BreakfastExchangeRate>", er]];
        }
        else
        {
            //todo handle error condition
        }

        [block appendString:[NSString stringWithFormat:@"<BreakfastErDirection>%@</BreakfastErDirection>",self.breakfastErDirection]];
    }


    [block appendString:@"</FixedAllowanceRow>"];

    NSLog(@"block = %@", block);

    return block;
}

+ (CXRequest *)getUpdateAllowancesRequest:(NSString *)rptKey {
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/UpdateFixedAllowances/%@", rptKey];
    // Create the request
    CXRequest *cxRequest = [[CXRequest alloc] initWithServicePath:path requestXML:@""];
    return cxRequest;
}

- (void)recalculateBreakfastPostedAmount
{
    // Called when the user entered breakfast amount fields change
    if(self.breakfastTransactionAmount != nil)
    {
        if(self.breakfastExchangeRate != nil)
        {
            self.breakfastPostedAmount = [self.breakfastTransactionAmount decimalNumberByMultiplyingBy:self.breakfastExchangeRate];
        }
    }

    NSLog(@"recalculateBreakfastPostedAmount = %@", self.breakfastPostedAmount);
}

+ (CXRequest *)getTaFixedAllowances:(NSString *)rptKey {

    // Compose the path
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/GetTaFixedAllowances/%@", rptKey];
    // NSLog(@"path = %@", path);

    // Create the request
    return [[CXRequest alloc] initWithServicePath:path];
}

+ (CXRequest *)getUpdatedFixedAllowanceAmounts:(NSString *)rptKey {

    // Compose the path
    NSString *path = [NSString stringWithFormat:@"Mobile/TravelAllowance/GetUpdatedFixedAllowanceAmounts/%@", rptKey];
    // NSLog(@"path = %@", path);

    // Create the request
    return [[CXRequest alloc] initWithServicePath:path requestXML:@""];
}

/*
21AF863EF93D</CliqSessionID>
<LoginID> acsontos
@outtask.com</LoginID>
<EntityID> phos123488<
/EntityID><CompanyID>1</CompanyID><SUVersion>102.0</SUVersion>
<IsMobile> Y<
/IsMobile>
<IsTestUser> N<
/IsTestUser>
<SkipVersionCheck> Y<
/SkipVersionCheck><HmcUserKey />
<RequestOrigin> MOBILE<
/RequestOrigin><PerfData>
<TotalDuration>25</TotalDuration><DBPerfItemTotal>6</DBPerfItemTotal></PerfData></Header><Body>
<Status> SUCCESS</Status>
<StatusText> TravelAllowance.GetUpdatedFixedAllowanceAmounts.Success</StatusText><FixedAllowanceAmounts>
<TaDayKey>79</TaDayKey>
<AllowanceAmount>85.00000000</AllowanceAmount>
<AboveLimitAmount>85.00000000</AboveLimitAmount>
</FixedAllowanceAmounts>
</Body></Response>
  */

/*
<Body>
<Status>SUCCESS</Status>
<StatusText>TravelAllowance.GetFixedAllowances.Success</StatusText>
</Body>
*/

+ (NSString *)parseUpdateAllowancesResult:(NSString *)result {

    RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
    if (rootXML != nil) {

    }

    RXMLElement *body = [rootXML child:@"Body"];

    NSString *status = [body child:@"Status"].text;
    NSString *statusText = [body child:@"StatusText"].text;

    if([status isEqualToString:@"FAILURE"])
    {
        return statusText;
    }
    return nil;
}
/*

<Action>
<GetUpdatedFixedAllowanceAmounts>
<LangCode> en</LangCode>
<EmpKey>2360</EmpKey>
<RptKey>32</RptKey>
<TaDayKey>113</TaDayKey>
<MarkedExcluded> N</MarkedExcluded>
<FixedRptKey>32</FixedRptKey>
<IsFirstDay> N</IsFirstDay>
<IsLastDay> Y</IsLastDay>
<LunchProvided> NPR</LunchProvided>
<DinnerProvided> NPR</DinnerProvided>
<BreakfastTransactionAmount>0</BreakfastTransactionAmount>
<BreakfastPostedAmount>0</BreakfastPostedAmount>
<BreakfastCrnCode> EUR</BreakfastCrnCode>
<BreakfastErDirection> M</BreakfastErDirection>
<BreakfastProvided> NPR</BreakfastProvided>
</GetUpdatedFixedAllowanceAmounts></Action></Body></Request>
*/



/*
<?
xml version = "1.0"
encoding = "UTF-8" ?
>
<Response>
- DA109C655142

</CliqSessionID>
<LoginID> wes
.german@randomverbs.com</LoginID>
<EntityID> p0195717zmbc<
/EntityID><CompanyID>130439</CompanyID><SUVersion>102.0</SUVersion>
<IsMobile> N<
/IsMobile>
<IsTestUser> N<
/IsTestUser>
<SkipVersionCheck> Y<
/SkipVersionCheck><HmcUserKey/>
<RequestOrigin> UNDEFINED<
/RequestOrigin><CteMtRequestId>635318495246769705</CteMtRequestId></Header><Body>
<Status> SUCCESS</Status>
<StatusText> TravelAllowance.GetUpdatedFixedAllowanceAmounts.Success</StatusText>
<FixedAllowanceAmounts>
<TaDayKey>113</TaDayKey>
<AllowanceAmount>6.00000000</AllowanceAmount>
<AboveLimitAmount>6.00000000</AboveLimitAmount>
</FixedAllowanceAmounts>
</Body></Response>
  */

+ (NSString *)parseGetUpdatedFixedAllowanceAmountsResult:(NSString *)result
{
    RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
    if (rootXML != nil) {

    }

    RXMLElement *body = [rootXML child:@"Body"];

    NSString *status = [body child:@"Status"].text;
    NSString *statusText = [body child:@"StatusText"].text;

    if([status isEqualToString:@"FAILURE"])
    {
        return statusText;
    }
    return nil;
}

+ (FixedAllowance *)extractUpdatedFixedAllowance:(NSString *)result 
{
    FixedAllowance *allowance = [[FixedAllowance alloc] init];

    RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
    if (rootXML != nil) {

    }

    RXMLElement *body = [rootXML child:@"Body"];
    RXMLElement *fixedAllowanceAmounts = [body child:@"FixedAllowanceAmounts"];

    allowance.taDayKey = [fixedAllowanceAmounts child:@"TaDayKey"].text;
    allowance.allowanceAmount = [fixedAllowanceAmounts child:@"AllowanceAmount"].text;
    allowance.aboveLimitAmount = [fixedAllowanceAmounts child:@"AboveLimitAmount"].text;


    return allowance;
}

+ (CXRequest *)getUpdatedFixedAllowanceAmounts:(FixedAllowance *)allowance rptKey:(NSString *)rptKey allowanceControl:(AllowanceControl *)allowanceControl{
    // Compose the path
    CXRequest *cxRequest= [FixedAllowance getUpdatedFixedAllowanceAmounts:rptKey];
    cxRequest.requestXML = [FixedAllowance composeGetUpdatedFixedAllowanceAmountsXml:allowance allowanceControl:allowanceControl];

    return cxRequest;
}

+ (NSString *)composeGetUpdatedFixedAllowanceAmountsXml:(FixedAllowance *)allowance allowanceControl:(AllowanceControl *)allowanceControl {
    AllowanceControl *control = allowanceControl;

    NSMutableString *block = [[NSMutableString alloc] init];

//        <TaDayKey>113</TaDayKey>
    [block appendString:[NSString stringWithFormat:@"<TaDayKey>%@</TaDayKey>",allowance.taDayKey]];
//        <MarkedExcluded> N</MarkedExcluded>
    [block appendString:[NSString stringWithFormat:@"<MarkedExcluded>%@</MarkedExcluded>",allowance.markedExcluded ? @"Y":@"N"]];
//        <FixedRptKey>32</FixedRptKey>
    [block appendString:[NSString stringWithFormat:@"<FixedRptKey>%@</FixedRptKey>",allowance.fixedRptKey]];
//        <IsFirstDay> N</IsFirstDay>
    [block appendString:[NSString stringWithFormat:@"<IsFirstDay>%@</IsFirstDay>",allowance.isFirstDay ? @"Y":@"N"]];
//        <IsLastDay> Y</IsLastDay>
    [block appendString:[NSString stringWithFormat:@"<IsLastDay>%@</IsLastDay>",allowance.isLastDay ? @"Y":@"N"]];

    if(allowanceControl.showBreakfastProvidedCheckBox || allowanceControl.showBreakfastProvidedPickList)
    {
        //        <BreakfastProvided> NPR</BreakfastProvided>
        [block appendString:[NSString stringWithFormat:@"<BreakfastProvided>%@</BreakfastProvided>",allowance.breakfastProvided]];
    }

    if(allowanceControl.showLunchProvidedCheckBox || allowanceControl.showLunchProvidedPickList)
    {
//        <LunchProvided> NPR</LunchProvided>
        [block appendString:[NSString stringWithFormat:@"<LunchProvided>%@</LunchProvided>",allowance.lunchProvided]];
    }

    if(allowanceControl.showDinnerProvidedCheckBox || allowanceControl.showDinnerProvidedPickList)
    {
//        <DinnerProvided> NPR</DinnerProvided>
        [block appendString:[NSString stringWithFormat:@"<DinnerProvided>%@</DinnerProvided>",allowance.dinnerProvided]];
    }

    if(allowanceControl.showUserEntryOfBreakfastAmount || allowanceControl.showUserEntryOfMealsAmount)
    {
//        <BreakfastTransactionAmount>0</BreakfastTransactionAmount>
        [block appendString:[NSString stringWithFormat:@"<BreakfastTransactionAmount>%@</BreakfastTransactionAmount>",allowance.breakfastTransactionAmount]];
//        <BreakfastPostedAmount>0</BreakfastPostedAmount>
        [block appendString:[NSString stringWithFormat:@"<BreakfastPostedAmount>%@</BreakfastPostedAmount>",allowance.breakfastPostedAmount]];
//        <BreakfastCrnCode> EUR</BreakfastCrnCode>
        [block appendString:[NSString stringWithFormat:@"<BreakfastCrnCode>%@</BreakfastCrnCode>",allowance.breakfastCrnCode]];
//        <BreakfastErDirection> M</BreakfastErDirection>
        [block appendString:[NSString stringWithFormat:@"<BreakfastErDirection>%@</BreakfastErDirection>",allowance.breakfastErDirection]];
    }

    //<Overnight>Y</Overnight>
    if(allowanceControl.showOvernightCheckBox)
    {
        [block appendString:[NSString stringWithFormat:@"<Overnight>%@</Overnight>",allowance.overnight ? @"Y":@"N"]];
    }

    //<ApplyExtendedTripRule>N</ApplyExtendedTripRule>
    if(allowanceControl.showExtendedTripCheckBox)
    {
        [block appendString:[NSString stringWithFormat:@"<ApplyExtendedTripRule>%@</ApplyExtendedTripRule>",allowance.applyExtendedTripRule ? @"Y":@"N"]];
    }

    //<ApplyPercentRule>N</ApplyPercentRule>
    if(allowanceControl.showPercentRuleCheckBox)
    {
        [block appendString:[NSString stringWithFormat:@"<ApplyPercentRule>%@</ApplyPercentRule>",allowance.applyPercentRule ? @"Y":@"N"]];
    }


//    <LodgingType>YCOOK</LodgingType>
    if(allowanceControl.showLodgingTypePickList)
    {
        NSLog(@"allowance.lodgingtype = %@", allowance.lodgingType);
        [block appendString:[NSString stringWithFormat:@"<LodgingType>%@</LodgingType>",allowance.lodgingType]];
    }

//        </GetUpdatedFixedAllowanceAmounts>

    return block;
}

+ (NSMutableArray *)parseFixedAllowanceXML:(NSString *)result {

    NSMutableArray *allowanceRows = [[NSMutableArray alloc]init];

    RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
    if (rootXML != nil) {

    }

    RXMLElement *body = [rootXML child:@"Body"];
    RXMLElement *fixedAllowances = [body child:@"FixedAllowances"];
    [fixedAllowances iterate:@"FixedAllowanceRow" usingBlock:^(RXMLElement *allowanceElement){

        FixedAllowance *allowance = [[FixedAllowance alloc] initWithXML:allowanceElement];

        [allowanceRows addObject:allowance];

    }];

    return allowanceRows;
}


+ (NSMutableArray *)filterAllowancesByDay:(NSMutableArray *)allowances taDayKey:(NSString *)taDayKey
{
    NSMutableArray *ret = [[NSMutableArray alloc] init];
    if(taDayKey != nil)
    {
        for (FixedAllowance *allowance in allowances) {
            if([taDayKey isEqualToString:allowance.taDayKey])
            {
                [ret addObject:allowance];
            }
        }
    }
    else
    {
        return allowances;
    }

    return ret;
}
@end
