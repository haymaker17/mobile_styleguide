//
//  AllowanceControl.h
//  ConcurMobile
//
//  Created by Wes Barton on 2/21/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class RXMLElement;

@interface AllowanceControl : NSObject

@property BOOL *showUserEntryOfBreakfastAmount;

@property BOOL *showUserEntryOfMealsAmount;

@property BOOL *showBreakfastProvidedCheckBox;

@property BOOL *showLunchProvidedCheckBox;

@property BOOL *showDinnerProvidedCheckBox;

@property BOOL *showBreakfastProvidedPickList;

@property BOOL *showLunchProvidedPickList;

@property BOOL *showDinnerProvidedPickList;

@property BOOL *showOvernightCheckBox;

@property BOOL *showOvernightAsNightAllowance;

@property BOOL *showAboveLimit;

@property BOOL *showMealsBaseAmount;

@property BOOL *showLodgingTypePickList;

@property BOOL *showPercentRuleCheckBox;

@property BOOL *showExtendedTripCheckBox;

@property BOOL *showMunicipalityCheckBox;

@property BOOL *showExcludeCheckBox;

@property BOOL *showAllowanceAmount;

@property NSString *excludeLabel;

@property NSString *lodgingTypeLabel;

@property NSMutableArray *lodgingTypeValues;

@property(nonatomic, strong) NSMutableDictionary *lodgingTypeDictionary;

@property BOOL *lodgingTypeGermany;
@property BOOL *lodgingTypeNorway;

@property NSString *applyPercentRuleLabel;

@property NSString *applyExtendedTripRuleLabel;

@property NSString *municipalAreaLabel;

@property NSString *overnightLabel;

@property NSString *breakfastProvidedLabel;

@property NSString *lunchProvidedLabel;

@property NSString *dinnerProvidedLabel;


- (id)initWithControlXML:(RXMLElement *)control;
/*

<Control>
        <ShowUserEntryOfBreakfastAmount>N</ShowUserEntryOfBreakfastAmount>
        <ShowUserEntryOfMealsAmount>N</ShowUserEntryOfMealsAmount>
        <ShowBreakfastProvidedCheckBox>Y</ShowBreakfastProvidedCheckBox>
        <ShowLunchProvidedCheckBox>N</ShowLunchProvidedCheckBox>
        <ShowDinnerProvidedCheckBox>N</ShowDinnerProvidedCheckBox>
        <ShowBreakfastProvidedPickList>N</ShowBreakfastProvidedPickList>
        <ShowLunchProvidedPickList>N</ShowLunchProvidedPickList>
        <ShowDinnerProvidedPickList>N</ShowDinnerProvidedPickList>
        <ShowOvernightCheckBox>N</ShowOvernightCheckBox>
        <ShowOvernightAsNightAllowance>N</ShowOvernightAsNightAllowance>
        <ShowAboveLimit>N</ShowAboveLimit>
        <ShowMealsBaseAmount>N</ShowMealsBaseAmount>
        <ShowLodgingTypePickList>N</ShowLodgingTypePickList>
        <ShowPercentRuleCheckBox>N</ShowPercentRuleCheckBox>
        <ShowExtendedTripCheckBox>N</ShowExtendedTripCheckBox>
        <ShowMunicipalityCheckBox>N</ShowMunicipalityCheckBox>
        <ShowExcludeCheckBox>Y</ShowExcludeCheckBox>
        <ShowAllowanceAmount>Y</ShowAllowanceAmount>
        <ExcludeLabel>Exclude</ExcludeLabel>
        <LodgingTypeLabel>Lodging Type</LodgingTypeLabel>
        <ApplyPercentRuleLabel>Use Percent Rule</ApplyPercentRuleLabel>
        <ApplyExtendedTripRuleLabel>Extended Trip</ApplyExtendedTripRuleLabel>
        <MunicipalAreaLabel>Within Municipal Area</MunicipalAreaLabel>
        <OvernightLabel>Overnight</OvernightLabel>
        <BreakfastProvidedLabel>Breakfast Provided</BreakfastProvidedLabel>
        <LunchProvidedLabel>Lunch Provided</LunchProvidedLabel>
        <DinnerProvidedLabel>Dinner Provided</DinnerProvidedLabel>
    </Control>
 */

- (BOOL)hasMealAllowances;
- (BOOL)hasOtherAllowances;

- (void)printContents;

+ (AllowanceControl *)parseAllowanceControlXML:(NSString *)result;


@end
