//
//  ExpenseTypesManager.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/30/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExpenseTypesCollection.h"
#import "AttendeeData.h"
#import "ExMsgRespondDelegate.h"
#import "MsgControl.h"

@interface ExpenseTypesManager : NSObject <ExMsgRespondDelegate>
{
	NSMutableDictionary		*expenseTypeCollections;			// Key: "version/polKey"    Value: ExpenseTypesCollection
	NSMutableDictionary		*expenseTypeCollectionsForChild;	// Key: "version/polKey"    Value: ExpenseTypesCollection
	AttendeeData			*attendeeRepresentingThisEmployee;
    NSMutableDictionary     *expKeyNameDict;                    // Key: "expKey", Value:"expName"
}

@property (nonatomic, strong) NSMutableDictionary*	expenseTypeCollections;
@property (nonatomic, strong) NSMutableDictionary*	expenseTypeCollectionsForChild;
@property (nonatomic, strong) NSMutableDictionary*  expKeyNameDict;
@property (nonatomic, strong) AttendeeData*				attendeeRepresentingThisEmployee;

+(ExpenseTypesManager*)sharedInstance;
+(NSString*) keyFromVersion:(NSString*)version policyKey:(NSString*)polKey;

-(void) clearCache;
-(void) addExpenseTypes:(ExpenseTypesData*)expenseTypesData;
-(ExpenseTypesCollection*)expenseTypesForVersion:(NSString*)version policyKey:(NSString*)polKey forChild:(BOOL)isForChild;
-(ExpenseTypeData*)expenseTypeForVersion:(NSString*)version policyKey:(NSString*)polKey expenseKey:(NSString*)expKey forChild:(BOOL)isForChild;

-(void) loadExpenseTypes:(NSString*) polKey msgControl:(MsgControl*) msgControl;

// Function for find the key-value pair for expKey and expName
-(NSString*) getExpNameByExpKey: (NSString *)expKey;

@end

