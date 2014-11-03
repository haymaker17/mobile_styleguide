//
//  ExpenseTypeData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface ExpenseTypeData : NSObject 
{
	NSString			*expKey, *expName, *parentExpName, *parentExpKey, *formKey;
	NSString			*access, *itemizeFormKey, *vendorListKey;
	NSString			*supportsAttendees, *userAsAtnDefault;
	NSString			*itemizeType, *itemizeStyle;
    NSString            *expCode;
    NSString            *itemizationUnallowExpKeys;
    NSString            *unallowAtnTypeKeys;
    NSString            *displayAtnAmounts;
    NSString            *displayAddAtnOnForm;
    NSString            *allowEditAtnAmt, *allowEditAtnCount, *allowNoShows;
}

@property (strong, nonatomic) NSString *expKey;
@property (strong, nonatomic) NSString *expName;
@property (strong, nonatomic) NSString *parentExpName;
@property (strong, nonatomic) NSString *parentExpKey;
@property (strong, nonatomic) NSString *formKey;
@property (strong, nonatomic) NSString *access;
@property (strong, nonatomic) NSString *itemizeFormKey;
@property (strong, nonatomic) NSString *vendorListKey;
@property (strong, nonatomic) NSString *supportsAttendees;
@property (strong, nonatomic) NSString *userAsAtnDefault;
@property (strong, nonatomic) NSString *itemizeType;
@property (strong, nonatomic) NSString *itemizeStyle;
@property (strong, nonatomic) NSString *expCode;
@property (strong, nonatomic) NSString *itemizationUnallowExpKeys;
@property (strong, nonatomic) NSString *unallowAtnTypeKeys;
@property (strong, nonatomic) NSString *displayAtnAmounts;
@property (strong, nonatomic) NSString *displayAddAtnOnForm;
@property (strong, nonatomic) NSString *allowEditAtnAmt;
@property (strong, nonatomic) NSString *allowEditAtnCount;
@property (strong, nonatomic) NSString *allowNoShows;

// server calculates the amount when payment type = CASH && cctKey/pctKey are null
@property (strong, nonatomic, readwrite) NSString *hasPostAmountCalculation;
@property (strong, nonatomic, readwrite) NSString *hasTaxForm;

//-(id) init;

-(BOOL) isChildOnly;
-(BOOL) isParentOnly;
-(BOOL) itemizeRequired;
-(BOOL) itemizeNotAllowed;
-(BOOL) isPersonalCarMileage;
-(BOOL) isCompanyCarMileage;
-(BOOL) usesHotelItemizeWizard;
- (BOOL) serverDoesPostAmountCalculation;
- (BOOL) hasVATForm;

@end
