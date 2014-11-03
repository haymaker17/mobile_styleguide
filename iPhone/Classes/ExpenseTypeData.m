//
//  ExpenseTypeData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ExpenseTypeData.h"


@implementation ExpenseTypeData

@synthesize expKey,expName, parentExpName, parentExpKey, formKey;
@synthesize access, itemizeFormKey, vendorListKey, supportsAttendees, userAsAtnDefault;
@synthesize itemizeType, itemizeStyle, expCode, itemizationUnallowExpKeys, hasPostAmountCalculation, hasTaxForm;
@synthesize unallowAtnTypeKeys, displayAtnAmounts, displayAddAtnOnForm;
@synthesize allowEditAtnAmt, allowEditAtnCount, allowNoShows;
//-(id) init
//{
//	
//}

-(BOOL) isChildOnly
{
	return [@"CH" isEqualToString:self.access];
}

-(BOOL) isParentOnly
{
	return [@"PA" isEqualToString:self.access];
}

-(BOOL) itemizeRequired
{
	return [@"REQD" isEqualToString:self.itemizeType];
}

-(BOOL) itemizeNotAllowed
{
	return [@"NALW" isEqualToString:self.itemizeType];
}

-(BOOL) isPersonalCarMileage
{
    // MOB-13059 Only check expCode
    return [@"PCARMILE" isEqualToString:self.expCode];
}

-(BOOL) isCompanyCarMileage
{
    // MOB-13059 MOB-13059 Only check expCode for company car mileage
    return [@"COCARMILE" isEqualToString:self.expCode];
}

-(BOOL) usesHotelItemizeWizard
{
    // MOB-8751 Do not use LODNG as critiria to use hotel itemization wizard
    return /*[self.expKey isEqualToString:@"LODNG"] &&*/ [@"lodging" isEqualToString:self.itemizeStyle];
}

- (BOOL) serverDoesPostAmountCalculation
{
    if (self.hasPostAmountCalculation != nil && [self.hasPostAmountCalculation isEqualToString:@"Y"]) {
        return YES;
    }
    return NO;
}


- (BOOL) hasVATForm
{
    if (self.hasTaxForm != nil && [self.hasTaxForm isEqualToString:@"Y"]) {
        return YES;
    }
    return NO;
}

@end
