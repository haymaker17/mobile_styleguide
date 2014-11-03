//
//  AllowanceControl.m
//  ConcurMobile
//
//  Created by Wes Barton on 2/21/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AllowanceControl.h"
#import "RXMLElement.h"

@implementation AllowanceControl

- (id)initWithControlXML:(RXMLElement *)control
{
//  <ShowUserEntryOfBreakfastAmount>N</ShowUserEntryOfBreakfastAmount>
    self.showUserEntryOfBreakfastAmount = (BOOL *) [[control child:@"ShowUserEntryOfBreakfastAmount"].text boolValue];

//  <ShowUserEntryOfMealsAmount>N</ShowUserEntryOfMealsAmount>
    self.showUserEntryOfMealsAmount = (BOOL *) [[control child:@"ShowUserEntryOfMealsAmount"].text boolValue];

//  <ShowBreakfastProvidedCheckBox>Y</ShowBreakfastProvidedCheckBox>
    self.showBreakfastProvidedCheckBox = (BOOL *) [[control child:@"ShowBreakfastProvidedCheckBox"].text boolValue];

//  <ShowLunchProvidedCheckBox>N</ShowLunchProvidedCheckBox>
    self.showLunchProvidedCheckBox = (BOOL *) [[control child:@"ShowLunchProvidedCheckBox"].text boolValue];

//  <ShowDinnerProvidedCheckBox>N</ShowDinnerProvidedCheckBox>
    self.showDinnerProvidedCheckBox = (BOOL *) [[control child:@"ShowDinnerProvidedCheckBox"].text boolValue];

//  <ShowBreakfastProvidedPickList>N</ShowBreakfastProvidedPickList>
    self.showBreakfastProvidedPickList = (BOOL *) [[control child:@"ShowBreakfastProvidedPickList"].text boolValue];

//  <ShowLunchProvidedPickList>N</ShowLunchProvidedPickList>
    self.showLunchProvidedPickList = (BOOL *) [[control child:@"ShowLunchProvidedPickList"].text boolValue];

//  <ShowDinnerProvidedPickList>N</ShowDinnerProvidedPickList>
    self.showDinnerProvidedPickList = (BOOL *) [[control child:@"ShowDinnerProvidedPickList"].text boolValue];

//  <ShowOvernightCheckBox>N</ShowOvernightCheckBox>
    self.showOvernightCheckBox = (BOOL *) [[control child:@"ShowOvernightCheckBox"].text boolValue];

//  <ShowOvernightAsNightAllowance>N</ShowOvernightAsNightAllowance>
    self.showOvernightAsNightAllowance = (BOOL *) [[control child:@"ShowOvernightAsNightAllowance"].text boolValue];

//  <ShowAboveLimit>N</ShowAboveLimit>
    self.showAboveLimit = (BOOL *) [[control child:@"ShowAboveLimit"].text boolValue];

//    <ShowMealsBaseAmount>N</ShowMealsBaseAmount>
    self.showMealsBaseAmount = (BOOL *) [[control child:@"ShowMealsBaseAmount"].text boolValue];

//    <ShowLodgingTypePickList>N</ShowLodgingTypePickList>
    self.showLodgingTypePickList = (BOOL *) [[control child:@"ShowLodgingTypePickList"].text boolValue];

    self.lodgingTypeValues = [[NSMutableArray alloc]init];
    self.lodgingTypeDictionary = [[NSMutableDictionary alloc]init];
    RXMLElement *lodgingTypeValues = [control child:@"LodgingTypeValues"];
    NSArray *lodgingTypes = [lodgingTypeValues children:@"LodgingType"];
    for (RXMLElement *lodgingType in lodgingTypes)
    {
        NSString *lodgingCode = [lodgingType child:@"Code"].text;
        NSString *lodgingLabel = [lodgingType child:@"Value"].text;

        if([lodgingCode isEqualToString:@"YRCPT"])
        {
            self.lodgingTypeGermany = YES;
            self.lodgingTypeNorway = NO;
        }
        else if([lodgingCode isEqualToString:@"HOTEL"])
        {
            self.lodgingTypeGermany = NO;
            self.lodgingTypeNorway = YES;
        }

        [self.lodgingTypeValues addObject:lodgingCode];
        [self.lodgingTypeDictionary setObject:lodgingLabel forKey:lodgingCode];
    }

//    <ShowPercentRuleCheckBox>N</ShowPercentRuleCheckBox>
    self.showPercentRuleCheckBox = (BOOL *) [[control child:@"ShowPercentRuleCheckBox"].text boolValue];

//    <ShowExtendedTripCheckBox>N</ShowExtendedTripCheckBox>
    self.showExtendedTripCheckBox = (BOOL *) [[control child:@"ShowExtendedTripCheckBox"].text boolValue];

//    <ShowMunicipalityCheckBox>N</ShowMunicipalityCheckBox>
    self.showMunicipalityCheckBox = (BOOL *) [[control child:@"ShowMunicipalityCheckBox"].text boolValue];

//    <ShowExcludeCheckBox>Y</ShowExcludeCheckBox>
    self.showExcludeCheckBox = (BOOL *) [[control child:@"ShowExcludeCheckBox"].text boolValue];

//    <ShowAllowanceAmount>Y</ShowAllowanceAmount>
    self.showAllowanceAmount = (BOOL *) [[control child:@"ShowAllowanceAmount"].text boolValue];

//    <ExcludeLabel>Exclude</ExcludeLabel>
    self.excludeLabel = [control child:@"ExcludeLabel"].text;

//    <LodgingTypeLabel>Lodging Type</LodgingTypeLabel>
    self.lodgingTypeLabel = [control child:@"LodgingTypeLabel"].text;

//    <ApplyPercentRuleLabel>Use Percent Rule</ApplyPercentRuleLabel>
    self.applyPercentRuleLabel = [control child:@"ApplyPercentRuleLabel"].text;

//    <ApplyExtendedTripRuleLabel>Extended Trip</ApplyExtendedTripRuleLabel>
    self.applyExtendedTripRuleLabel = [control child:@"ApplyExtendedTripRuleLabel"].text;

//    <MunicipalAreaLabel>Within Municipal Area</MunicipalAreaLabel>
    self.municipalAreaLabel = [control child:@"MunicipalAreaLabel"].text;

//    <OvernightLabel>Overnight</OvernightLabel>
    self.overnightLabel = [control child:@"OvernightLabel"].text;

//    <BreakfastProvidedLabel>Breakfast Provided</BreakfastProvidedLabel>
    self.breakfastProvidedLabel = [control child:@"BreakfastProvidedLabel"].text;

//    <LunchProvidedLabel>Lunch Provided</LunchProvidedLabel>
    self.lunchProvidedLabel = [control child:@"LunchProvidedLabel"].text;

//    <DinnerProvidedLabel>Dinner Provided</DinnerProvidedLabel>
    self.dinnerProvidedLabel = [control child:@"DinnerProvidedLabel"].text;

    return self;
}

- (BOOL)hasMealAllowances
{
    // Change this to cache the value
    return self.showBreakfastProvidedCheckBox || self.showBreakfastProvidedPickList
            || self.showLunchProvidedCheckBox || self.showLunchProvidedPickList
            || self.showDinnerProvidedCheckBox || self.showDinnerProvidedPickList;
}

- (BOOL)hasOtherAllowances
{

    return self.showOvernightCheckBox;
}

- (void)printContents {
    NSLog(@"self.breakfastProvidedLabel = %@", self.breakfastProvidedLabel);
    NSLog(@"self.showBreakfastProvidedCheckBox = %p", self.showBreakfastProvidedCheckBox);
    NSLog(@"self.showBreakfastProvidedPickList = %p", self.showBreakfastProvidedPickList);
    NSLog(@"self.showUserEntryOfBreakfastAmount = %p", self.showUserEntryOfBreakfastAmount);

    NSLog(@"self.lunchProvidedLabel = %@", self.lunchProvidedLabel);
    NSLog(@"self.showLunchProvidedCheckBox = %p", self.showLunchProvidedCheckBox);
    NSLog(@"self.showLunchProvidedPickList = %p", self.showLunchProvidedPickList);

    NSLog(@"self.showUserEntryOfBreakfastAmount = %p", self.showUserEntryOfBreakfastAmount);

}

+ (AllowanceControl *)parseAllowanceControlXML:(NSString *)result
{
    RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
    if (rootXML != nil) {

    }

    RXMLElement *body = [rootXML child:@"Body"];
    RXMLElement *control = [body child:@"Control"];
    AllowanceControl *allowanceControl = [[AllowanceControl alloc] initWithControlXML:control];
    return allowanceControl;
}


@end
