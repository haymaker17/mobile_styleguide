//
//  MRUManager.h
//  ConcurMobile
//
//  Created by yiwen on 9/23/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BaseManager.h"
#import "EntityMRU.h"

// Most recently used list manager
@interface MRUManager : BaseManager { }

+(MRUManager*)sharedInstance;

// currency MRU
-(void) saveCurrency:(ListItem *)listItem;
-(NSArray *) getCurrencies;
-(ListItem *) getLastUsedCurrency;

// location MRU
-(void) saveLocation:(ListItem *)listItem;
-(NSArray *) getLocations;
-(ListItem *) getLastUsedLocation;

// expense types MRU.  Incomplete and not used yet.  The expense code still uses the old methods.
-(void) saveExpenseType:(ExpenseTypeData *)expenseType;
-(NSArray *) getExpenseTypes;

// old MRU handling, this is still used under the hood.
-(NSArray *) getMRUsByType:(NSString *)tType;
-(EntityMRU *) addMRUForType:(NSString *)tType value:(NSString *)tVal key:(NSInteger *)tKey code:(NSString *)tCode;

@end
