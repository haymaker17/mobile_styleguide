//
//  DownloadUserConfig.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "Msg.h"
#import "ListItem.h"

@class CreditCard;
@class CarType;
@class UserConfig;
@class AffinityProgram;
@class AttendeeType;
@class Policy;
@class ExpenseConfirmation;

@interface DownloadUserConfig : MsgResponderCommon
{
    CarType                 *currentCarType;
	UserConfig				*userConfig;
    NSString                *allowFor;
    NSString                *defaultFor;
    NSString                *curClassOfServices;
    AttendeeType            *curAttendeeType;
    Policy                  *curExpensePolicy;
    ExpenseConfirmation     *curExpenseConfirmation;
    
    BOOL                    inYodleePaymentTypes;
    ListItem                *curYodleePaymentType;
}

@property (nonatomic, strong) CarType               *currentCarType;
@property (nonatomic, strong) UserConfig			*userConfig;
@property (nonatomic, strong) NSString              *allowFor;
@property (nonatomic, strong) NSString              *defaultFor;
@property (nonatomic, strong) NSString              *curClassOfServices;
@property (nonatomic, strong) AttendeeType          *curAttendeeType;
@property (nonatomic, strong) Policy                *curExpensePolicy;
@property (nonatomic, strong) ExpenseConfirmation   *curExpenseConfirmation;
@property (nonatomic, strong) ListItem              *curYodleePaymentType;

-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;

+(void)populateDictionary:(NSMutableDictionary*)dict FromCommaDelimitedList:(NSString*)list;
+(void)populateArray:(NSMutableArray*)array FromSpaceDelimitedList:(NSString*)listOfAllowedClasses;

@end
