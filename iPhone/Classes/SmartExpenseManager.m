//
//  SmartExpenseManager.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 9/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SmartExpenseManager.h"
#import "CardTransaction.h"
#import "EntitySmartExpenseCctKeys.h"
#import "EntitySmartExpensePctKeys.h"

@implementation SmartExpenseManager

@synthesize meKeysPairedWithSmartExpenseCctKeys;
@synthesize meKeysPairedWithSmartExpensePctKeys;

@synthesize smartExpenseCctKeys;
@synthesize smartExpensePctKeys;

@synthesize formerSmartExpenseCctKeys;
@synthesize formerSmartExpensePctKeys;

@synthesize expenses;

static SmartExpenseManager* smartExpenseManager = nil;

+(SmartExpenseManager*)getInstance
{
	if (!smartExpenseManager)
		smartExpenseManager = [[SmartExpenseManager alloc] init];

	return smartExpenseManager;
}

-(BOOL)tryMakeSmartExpense:(CardTransaction*)cardTransaction mobileExpenseKey:(NSString*)meKey
{
	if (cardTransaction != nil && meKey != nil)
	{
		if ([self isFormerSmartExpense:cardTransaction])
		{
			cardTransaction.smartExpenseMeKey = nil;
			return NO;
		}
		else
		{
			if ([cardTransaction isCorporateCardTransaction])
			{
				smartExpenseCctKeys[cardTransaction.cctKey] = meKey;
				meKeysPairedWithSmartExpenseCctKeys[meKey] = cardTransaction.cctKey;
				return YES;
			}
			else if ([cardTransaction isPersonalCardTransaction])
			{
				smartExpensePctKeys[cardTransaction.pctKey] = meKey;
				meKeysPairedWithSmartExpensePctKeys[meKey] = cardTransaction.pctKey;
				return YES;
			}
		}
	}
	return NO;
}

-(BOOL)isSmartExpenseCctKey:(NSString*)cctKey
{
	return (nil != smartExpenseCctKeys[cctKey]);
}

-(BOOL)isSmartExpensePctKey:(NSString*)pctKey
{
	return (nil != smartExpensePctKeys[pctKey]);
}

-(BOOL)isSmartExpense:(CardTransaction*)cardTransaction
{
	if (cardTransaction)
	{
		if ([cardTransaction isCorporateCardTransaction])
			return (nil != smartExpenseCctKeys[cardTransaction.cctKey]);
		else if ([cardTransaction isPersonalCardTransaction])
			return (nil != smartExpensePctKeys[cardTransaction.pctKey]);
	}
	
	return NO;
}

-(NSString*)splitSmartExpense:(CardTransaction*)cardTransaction settingsData:(SettingsData*)settings
{
	NSString* meKey = nil;
	return meKey;
}

-(BOOL)isFormerSmartExpense:(CardTransaction*)cardTransaction
{
	if (cardTransaction)
	{
		if ([cardTransaction isCorporateCardTransaction])
			return (nil != formerSmartExpenseCctKeys[cardTransaction.cctKey]);
		else if ([cardTransaction isCorporateCardTransaction])
			return (nil != formerSmartExpensePctKeys[cardTransaction.pctKey]);
	}
	
	return NO;
}

-(NSUInteger)getSmartExpenseCount
{
	return ([smartExpenseCctKeys count] + [smartExpensePctKeys count]);
}

-(BOOL)doesMobileEntryBelongToIntactSmartExpense:(OOPEntry*)mobileEntry
{
	if (mobileEntry == nil || mobileEntry.meKey == nil || [mobileEntry isKindOfClass:[CardTransaction class]])
		return NO;
	
	return	meKeysPairedWithSmartExpenseCctKeys[mobileEntry.meKey] ||
			meKeysPairedWithSmartExpensePctKeys[mobileEntry.meKey];
}

-(OOPEntry*)getMobileExpensePairedWithCardTransaction:(CardTransaction*)cardTransaction
{
	NSString *meKey = nil;
	
	if ([cardTransaction isCorporateCardTransaction])
		meKey = smartExpenseCctKeys[cardTransaction.cctKey];
	else if ([cardTransaction isPersonalCardTransaction])
		meKey = smartExpensePctKeys[cardTransaction.pctKey];

	if (meKey != nil && expenses != nil)
		return expenses[[NSString stringWithFormat:@"ME%@", meKey]];
	
	return nil;
}

-(void)setCurrentExpenses:(NSDictionary*)oopes
{
	[self init];
	
	[self readFromSettings];
	
	NSArray* allOopes = [oopes allValues];

	for (OOPEntry *oope in allOopes)
	{
		if ([oope isCardTransaction])
		{
			CardTransaction *cardTransaction = (CardTransaction*)oope;
			if (cardTransaction.smartExpenseMeKey != nil)
			{
				[self tryMakeSmartExpense:cardTransaction mobileExpenseKey:cardTransaction.smartExpenseMeKey];
			}
		}
	}
	
	self.expenses = oopes;
	
	[self purgeKeysThatDoNotReferenceCurrentExpenses:oopes];

	[self writeToSettings];
}

-(void)purgeKeysThatDoNotReferenceCurrentExpenses:(NSDictionary*)oopes
{
	if (oopes == nil)
		return;

	NSMutableArray *obsoleteKeys = [[NSMutableArray alloc] initWithObjects:nil];
	for (NSString *cctKey in formerSmartExpenseCctKeys)
	{
		NSString *prefixedCctKey = [NSString stringWithFormat:@"CC%@", cctKey];
		if (!oopes[prefixedCctKey])
		{
			[obsoleteKeys addObject:cctKey];
		}
	}
	for (NSString* obsoleteCctKey in obsoleteKeys)
	{
		[formerSmartExpenseCctKeys removeObjectForKey:obsoleteCctKey];
	}
	
	[obsoleteKeys removeAllObjects];
	for (NSString *pctKey in formerSmartExpensePctKeys)
	{
		NSString *prefixedPctKey = [NSString stringWithFormat:@"PC%@", pctKey];;
		if (!oopes[prefixedPctKey])
		{
			[obsoleteKeys addObject:pctKey];
		}
	}
	for (NSString* obsoletePctKey in obsoleteKeys)
	{
		[formerSmartExpensePctKeys removeObjectForKey:obsoletePctKey];
	}
	
}

#pragma mark -
#pragma mark CoreData Meths
-(NSArray*) loadSmartCCTs
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntitySmartExpenseCctKeys" inManagedObjectContext:[ExSystem sharedInstance].context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *a = [[ExSystem sharedInstance].context executeFetchRequest:fetchRequest error:&error];
    return a;
}

-(NSArray*) loadSmartPCTs
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntitySmartExpensePctKeys" inManagedObjectContext:[ExSystem sharedInstance].context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *a = [[ExSystem sharedInstance].context executeFetchRequest:fetchRequest error:&error];
    return a;
}


-(void) saveSmartCCT:(NSString *)cctKey meKey:(NSString *)meKey
{
    EntitySmartExpenseCctKeys *entity = [NSEntityDescription insertNewObjectForEntityForName:@"EntitySmartExpenseCctKeys" inManagedObjectContext:[ExSystem sharedInstance].context];
    entity.cctKey = cctKey;
    entity.meKey = meKey;
    NSError *error;
    if (![[ExSystem sharedInstance].context save:&error]) {
        NSLog(@"Whoops, couldn't save cctKey: %@", [error localizedDescription]);
    }
}

-(void) saveSmartPCT:(NSString *)pctKey meKey:(NSString *)meKey
{
    EntitySmartExpensePctKeys *entity = [NSEntityDescription insertNewObjectForEntityForName:@"EntitySmartExpenseCctKeys" inManagedObjectContext:[ExSystem sharedInstance].context];
    entity.PctKey = pctKey;
    entity.meKey = meKey;
    NSError *error;
    if (![[ExSystem sharedInstance].context save:&error]) {
        NSLog(@"Whoops, couldn't save pctKey: %@", [error localizedDescription]);
    }
}


-(void) clearEntity:(NSManagedObject *)ent
{
    [[ExSystem sharedInstance].context deleteObject:ent];
}


-(NSMutableDictionary *)makeCCTDict
{
	NSArray *a = [self loadSmartCCTs];
    __autoreleasing NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
                                 
    for(EntitySmartExpenseCctKeys *e in a)
    {
        NSString *prefixedCctKey = [NSString stringWithFormat:@"CC%@", e.cctKey];
        NSString *prefixedMeKey = [NSString stringWithFormat:@"ME%@", e.meKey];
         NSMutableDictionary *d = [[NSMutableDictionary alloc] initWithObjectsAndKeys:e.cctKey, prefixedCctKey, e.meKey, prefixedMeKey, nil];
        dict[e.cctKey] = d;
    }
    
    return dict;
}  

-(NSMutableDictionary *)makePCTDict
{
    return [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
}  


-(void)writeToSettings
{
	
    NSArray *a = [self loadSmartCCTs];
    for(EntitySmartExpenseCctKeys *v in a)
    {
        [self clearEntity:v];
    }
    
    a = [self loadSmartPCTs];
    for(EntitySmartExpenseCctKeys *v in a)
    {
        [self clearEntity:v];
    }
    
    for(NSMutableDictionary *d in formerSmartExpenseCctKeys)
    {
        [self saveSmartCCT:d[@"CCT_KEY"] meKey:d[@"ME_KEY"]];
    }
    
    for(NSMutableDictionary *d in formerSmartExpensePctKeys)
    {
        [self saveSmartPCT:d[@"PCT_KEY"] meKey:d[@"ME_KEY"]];
    }
    
}

-(void)readFromSettings
{

    self.formerSmartExpenseCctKeys = [self makeCCTDict];
    self.formerSmartExpensePctKeys = [self makePCTDict];
		
}

-(id)init
{
    self = [super init];
    if (self) {
        self.meKeysPairedWithSmartExpenseCctKeys = [[NSMutableDictionary alloc] init];
        
        self.meKeysPairedWithSmartExpensePctKeys = [[NSMutableDictionary alloc] init];
        
        self.smartExpenseCctKeys = [[NSMutableDictionary alloc] init];
        
        self.smartExpensePctKeys = [[NSMutableDictionary alloc] init];
        
        self.formerSmartExpenseCctKeys = [[NSMutableDictionary alloc] init];
        
        self.formerSmartExpensePctKeys = [[NSMutableDictionary alloc] init];
    }
	return self;
}


@end
