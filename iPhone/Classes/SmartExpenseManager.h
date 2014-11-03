//
//  SmartExpenseManager.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 9/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CardTransaction.h"

@class SettingsData;

@interface SmartExpenseManager : NSObject
{
	NSMutableDictionary *meKeysPairedWithSmartExpenseCctKeys;
	NSMutableDictionary *meKeysPairedWithSmartExpensePctKeys;

	NSMutableDictionary	*smartExpenseCctKeys;
	NSMutableDictionary	*smartExpensePctKeys;
	
	NSMutableDictionary	*formerSmartExpenseCctKeys;
	NSMutableDictionary	*formerSmartExpensePctKeys;
	
	NSDictionary		*expenses;
}

@property (nonatomic, strong) NSMutableDictionary	*meKeysPairedWithSmartExpenseCctKeys;
@property (nonatomic, strong) NSMutableDictionary	*meKeysPairedWithSmartExpensePctKeys;

@property (nonatomic, strong) NSMutableDictionary	*smartExpenseCctKeys;
@property (nonatomic, strong) NSMutableDictionary	*smartExpensePctKeys;

@property (nonatomic, strong) NSMutableDictionary	*formerSmartExpenseCctKeys;
@property (nonatomic, strong) NSMutableDictionary	*formerSmartExpensePctKeys;

@property (nonatomic, strong) NSDictionary			*expenses;

-(BOOL)tryMakeSmartExpense:(CardTransaction*)cardTransaction mobileExpenseKey:(NSString*)meKey;

-(BOOL)isSmartExpenseCctKey:(NSString*)cctKey;
-(BOOL)isSmartExpensePctKey:(NSString*)pctKey;
-(BOOL)isSmartExpense:(CardTransaction*)cardTransaction;
-(NSString*)splitSmartExpense:(CardTransaction*)cardTransaction settingsData:(SettingsData*)settings;
-(BOOL)isFormerSmartExpense:(CardTransaction*)cardTransaction;
-(NSUInteger)getSmartExpenseCount;

-(BOOL)doesMobileEntryBelongToIntactSmartExpense:(OOPEntry*)mobileEntry;

-(OOPEntry*)getMobileExpensePairedWithCardTransaction:(CardTransaction*)cardTransaction;
-(void)setCurrentExpenses:(NSDictionary*)oopes;
-(void)purgeKeysThatDoNotReferenceCurrentExpenses:(NSDictionary*)oopes;

-(void)writeToSettings;
-(void)readFromSettings;

+(SmartExpenseManager*)getInstance;

#pragma mark -
#pragma mark CoreData Meths
-(NSArray*) loadSmartCCTs;
-(NSArray*) loadSmartPCTs;
-(void) saveSmartCCT:(NSString *)cctKey meKey:(NSString *)meKey;
-(void) saveSmartPCT:(NSString *)pctKey meKey:(NSString *)meKey;
-(void) clearEntity:(NSManagedObject *)ent;
-(NSMutableDictionary *)makeCCTDict;
-(NSMutableDictionary *)makePCTDict;

@end
