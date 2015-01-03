//
//  CTEProfile.h
//  ConcurSDK
//
//  Created by Ray Chi on 12/8/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEProfileEmergencyContact.h"
#import "CTEProfileCreditCard.h"
#import "CTEProfileBankAccount.h"
#import "CTEProfilePersonalInfo.h"
#import "CTEError.h"

@interface CTEProfile : NSObject

@property (nonatomic,strong,readonly) CTEProfilePersonalInfo     *personalInfo;
@property (nonatomic,strong,readonly) CTEProfileBankAccount      *bankAccount;
@property (nonatomic,strong,readonly) CTEProfileEmergencyContact *emergencyContact;
@property (nonatomic,strong,readonly) NSArray                    *creditCards;           // Array of Credit Cards

// Initiate with User ID, which you can get it from SDK
- (id)initWithUserId:(NSString *)userId;

// Get all profile data
- (void)getProfileWithSuccess:(void (^)(CTEProfile *profile))success
                      failure:(void (^)(CTEError *error))failure;

// Get personal information
- (void)getPersonalInfoWithSuccess:(void (^)(CTEProfilePersonalInfo *personalInfo))success
                           failure:(void (^)(CTEError *error))failure;

// Get emergency contact information
- (void)getEmergencyContactWithSuccess:(void (^)(CTEProfileEmergencyContact *emergencyContact))success
                              failure:(void (^)(CTEError *error))failure;

// Get bank account
- (void)getBankAccountWithSuccess:(void (^)(CTEProfileBankAccount *bankAccount))success
                          failure:(void (^)(CTEError *error))failure;

// Get credit cards
- (void)getCreditCardsWithSuccess:(void (^)(NSArray *creditCards))success
                          failure:(void (^)(CTEError *error))failure;


// Credit card validation
+ (BOOL)isValidCreditCardNumber:(NSString *)creditCardNo;     // check credit card number
+ (BOOL)isValidExpireDateWithYearMonthString:(NSString *)str; // check input date string, e.g "10/18", "10/2018"


@end
