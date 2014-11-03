//
//  UserConfig.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "UserConfig.h"
#import "CreditCard.h"
#import "Policy.h"
#import "ExpenseConfirmation.h"

@implementation UserConfig

@dynamic carCreditCards;
@dynamic hotelCreditCards;
@dynamic railCreditCards;
@dynamic airCreditCards;
@dynamic taxiCreditCards;
@dynamic limoCreditCards;

@synthesize classOfServices;
@synthesize allowedCarTypes;

@synthesize expensePolicies, expenseConfirmations, attendeeTypes;
@synthesize yodleePaymentTypes;

static UserConfig* userConfigSingleton = nil;


#pragma mark -
#pragma mark Singleton
+(UserConfig*)getSingleton
{
	return userConfigSingleton;
}

+(void)setSingleton:(UserConfig*)userConfig
{
	if (userConfigSingleton == userConfig)
		return;
		
	userConfigSingleton = userConfig;
}


#pragma mark -
#pragma mark Lifecycle
-(id)init
{
	self = [super init];
	if (self)
    {
        self.classOfServices = [[NSMutableArray alloc] initWithObjects:nil];
        self.allowedCarTypes = [[NSMutableArray alloc] initWithObjects:nil];
        self.expensePolicies = [[NSMutableDictionary alloc] init];
        self.expenseConfirmations = [[NSMutableDictionary alloc] init];
        self.attendeeTypes = [[NSMutableDictionary alloc] init];
        self.yodleePaymentTypes = [[NSMutableArray alloc] init];
        self.travelPointsConfig = [[NSMutableDictionary alloc] init];
    }
	return self;
}

#pragma mark - Policy Confirmation

-(ExpenseConfirmation*) submitConfirmationForPolicy:(NSString*)polKey
{
    NSString* confKey = nil;
    Policy* policy = (self.expensePolicies)[polKey];

    if (policy == nil || ![policy.submitConfirmationKey length])
        return nil;
        
    confKey = policy.submitConfirmationKey;
    ExpenseConfirmation* expConf = (self.expenseConfirmations)[confKey];
    return  expConf;
}

-(ExpenseConfirmation*) approvalConfirmationForPolicy:(NSString*)polKey
{
    NSString* confKey = nil;
    Policy* policy = (self.expensePolicies)[polKey];
    if (policy == nil || ![policy.approvalConfirmationKey length])
        return nil;
    
    confKey = policy.approvalConfirmationKey;
    ExpenseConfirmation* expConf = (self.expenseConfirmations)[confKey];
    return  expConf;
}

@end
