//
//  ExpenseTypesManager.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/30/11.
//  Copyright 2011 Concur. All rights reserved.
//
//	Updated by Pavan 11/16/2012 for MRUExpense types
//

#import "ExpenseTypesManager.h"
#import "DataConstants.h"
#import "MsgHandler.h"
#import "MCLogging.h"
#import "MRUManager.h"
#import "DataConstants.h"

@implementation ExpenseTypesManager

@synthesize expenseTypeCollections;
@synthesize expenseTypeCollectionsForChild;
@synthesize attendeeRepresentingThisEmployee;
@synthesize expKeyNameDict;

static ExpenseTypesManager* sharedInstance;

+(ExpenseTypesManager*) sharedInstance
{
	if (sharedInstance == nil) 
	{
		@synchronized (self)
		{
			if (sharedInstance == nil) 
			{
				sharedInstance = [[ExpenseTypesManager alloc] init];
			}
		}
	}
	return sharedInstance;
}

+(NSString*) keyFromVersion:(NSString*)version policyKey:(NSString*)polKey;
{
	NSString *nonNullableVersion = (version == nil ? @"" : version);
	NSString *nonNullablePolKey = (polKey == nil ? @"" : polKey);
	return [NSString stringWithFormat:@"%@/%@", nonNullableVersion, nonNullablePolKey];
}

-(void) clearCache
{
	[expenseTypeCollections removeAllObjects];
	[expenseTypeCollectionsForChild removeAllObjects];
	attendeeRepresentingThisEmployee = nil;
    // MOB:MOB-8451 - clear mru entries also
    
    // Cache clearing currently happens upon Logout, but we don't want the MRU to be wiped.
    // In the future, when cache clearing is more selective (and not auotomatic upon logout)
    // we might consider wiping the MRU.
    //
    //[[MRUManager sharedInstance]deleteMRUsByType:@"mru_expenseType"];
}

-(id) init
{
	self = [super init];
	if (self)
    {
        self.expenseTypeCollections = [[NSMutableDictionary alloc] init];
	
        self.expenseTypeCollectionsForChild = [[NSMutableDictionary alloc] init];
        
	}
	return self;
}

-(void) addExpenseTypes:(ExpenseTypesData*)etsData
{
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"AddExpenseTypes %@ %@", etsData.polKey, etsData.version] Level:MC_LOG_DEBU];

	NSString *key = [ExpenseTypesManager keyFromVersion:etsData.version policyKey:etsData.polKey];
	
	ExpenseTypesCollection *col = [ExpenseTypesCollection collectionFromData:etsData forChild:NO];
	expenseTypeCollections[key] = col;

	ExpenseTypesCollection *colForChild = [ExpenseTypesCollection collectionFromData:etsData forChild:YES];
	expenseTypeCollectionsForChild[key] = colForChild;

    // MOB-21206 Follow Android team's logic, get expName by using expKey because server return wrong expName sometimes
    if(self.expKeyNameDict == nil){
        self.expKeyNameDict = etsData.ets;
    }
}

-(ExpenseTypesCollection*)expenseTypesForVersion:(NSString*)version policyKey:(NSString*)polKey forChild:(BOOL)isForChild
{
	NSString *key = [ExpenseTypesManager keyFromVersion:version policyKey:polKey];
    
	ExpenseTypesCollection *etCol;
    
    if (isForChild)
	{
		etCol = expenseTypeCollectionsForChild[key];
	}
	else
	{
		etCol =  expenseTypeCollections[key];
	}

    
    //    MRU expense types
    //    MOB-8451: Add MRU expense types here
    NSString* commonlyUsedStr = [Localizer getLocalizedText:MRUKEY];
    
	NSMutableArray *mruExpenses = [[NSMutableArray alloc] initWithObjects:nil]; //make array with an et in it
    
    NSArray *mruExpenseTypes = [[MRUManager sharedInstance]getMRUsByType:@"mru_expenseType"];
    // This loop iterates all ets elements and inserts into commonly used block.
    
    // Get mru expensetype list and get corresponding object for that key.
    for(EntityMRU *entry in mruExpenseTypes)
    {
        ExpenseTypeData *mruExptype = (etCol.expenseTypeByExpenseKey)[entry.code];
        // MOB-11391 : Get parent expensekey
        // Use  MRU expense parent to get list of expenses for each group
        // check if current expense key is in the list of expenses using key.
        NSArray *expItems = (NSArray*) (etCol.parents)[mruExptype.parentExpName];
        for (ExpenseTypeData* et in expItems)
        {
            if([et.expKey isEqualToString:mruExptype.expKey]  )
                [mruExpenses addObject:et];
        }
    }
    // Only if there is some mru's available add mru sub section
    if([mruExpenses count]>0)
    {
        //if there is already an MRU at index zero then do only update
        if(![(etCol.aKeys)[0] isEqualToString:MRUKEY] )
        {
            [etCol.aKeys insertObject:MRUKEY atIndex:0];
            [etCol.aParentNames insertObject:commonlyUsedStr atIndex:0];
        }
        //MOB-11157 : mruExpenses are already sorted by date.
        (etCol.keysForParent)[commonlyUsedStr] = MRUKEY;
        (etCol.parents)[commonlyUsedStr] = mruExpenses; //add aray to the parent name as key
    }
    else
    {
        // there are no mru expense for this policy, so check if the mrukey is present in akeys and remove it
        if ([etCol.aKeys count] > 0){
            if([(etCol.aKeys)[0] isEqualToString:MRUKEY])
            {
                [etCol.aKeys removeObjectAtIndex:0];
                [etCol.aParentNames removeObjectAtIndex:0];
            }
        }
    }
    
    return etCol;
}

-(ExpenseTypeData*)expenseTypeForVersion:(NSString*)version policyKey:(NSString*)polKey expenseKey:(NSString*)expKey forChild:(BOOL)isForChild
{
    if (![expKey length])
        return nil;
    
	ExpenseTypesCollection *etCol = [self expenseTypesForVersion:version policyKey:polKey forChild:isForChild];
	ExpenseTypeData *et = (etCol.expenseTypeByExpenseKey)[expKey];
	return et;
}

-(void) loadExpenseTypes:(NSString*) polKey msgControl:(MsgControl*) msgControl
{
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"loadExpenseTypes%@", polKey] Level:MC_LOG_DEBU];
	ExpenseTypesCollection* etCol = [self expenseTypesForVersion:@"V3" policyKey:polKey forChild:NO];
	if (etCol == nil)
	{
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"V3", @"VERSION", polKey, @"POL_KEY", nil];
		if (polKey == nil)
			[msgControl createMsg:EXPENSE_TYPES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
		else
			[msgControl createMsg:EXPENSE_TYPES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
}

-(NSString*) getExpNameByExpKey: (NSString *)expKey
{
    if([expKey isEqualToString:@"UNDEF"]){
        return @"Undefined";
    }
    else{
        ExpenseTypeData *tmp = expKeyNameDict[expKey];
        return tmp.expName;
    }
}

#pragma mark - ExMsgRespondDelegate Method

-(void) didProcessMessage:(Msg *)msg
{
	if ([msg.idKey isEqualToString:EXPENSE_TYPES_DATA] && !msg.didConnectionFail)
	{
		ExpenseTypesData *etsData = (ExpenseTypesData *)msg.responder;
		[self addExpenseTypes:etsData];
	}
}

- (void)dealloc
{
	[MsgHandler cancelAllRequestsForDelegate:self];
}


@end
