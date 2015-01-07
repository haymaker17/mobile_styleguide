//
//  UserConfig.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExpenseConfirmation.h"

@interface UserConfig : NSObject
{
    // Class of Services
    NSMutableArray      *classOfServices;
    // car types allowed
    NSMutableArray      *allowedCarTypes;
    
    NSMutableDictionary *expensePolicies; // key = polKey
    NSMutableDictionary *expenseConfirmations; // key = confirmationKey
    NSMutableDictionary *attendeeTypes;  // key = AtnTypeKey
    
    NSMutableArray      *yodleePaymentTypes;  // MOB-12282 For Breeze/Standard, server returns YodleePaymentTypes for PCTs
}

@property (nonatomic, strong) NSMutableArray        *allowedCarTypes;
@property (nonatomic, strong) NSMutableArray        *classOfServices;
@property (nonatomic, strong) NSMutableArray        *yodleePaymentTypes;

@property (weak, nonatomic, readonly) NSArray* carCreditCards;
@property (weak, nonatomic, readonly) NSArray* hotelCreditCards;
@property (weak, nonatomic, readonly) NSArray* railCreditCards;
@property (weak, nonatomic, readonly) NSArray* airCreditCards;
@property (weak, nonatomic, readonly) NSArray* taxiCreditCards;
@property (weak, nonatomic, readonly) NSArray* limoCreditCards;

@property (nonatomic, strong) NSMutableDictionary   *expensePolicies;
@property (nonatomic, strong) NSMutableDictionary   *expenseConfirmations;
@property (nonatomic, strong) NSMutableDictionary   *attendeeTypes;
@property (nonatomic, strong) NSMutableDictionary   *travelPointsConfig;
@property (nonatomic, strong) NSMutableDictionary   *customTravelText;
@property (nonatomic) BOOL                          *showGDSNameInSearchResults;

+(UserConfig*)getSingleton;
+(void)setSingleton:(UserConfig*)userConfig;

-(ExpenseConfirmation*) submitConfirmationForPolicy:(NSString*)polKey;
-(ExpenseConfirmation*) approvalConfirmationForPolicy:(NSString*)polKey;


@end
