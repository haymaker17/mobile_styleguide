//
//  SmartExpenseManager2.h
//  ConcurMobile
//
//  Created by ernest cho on 8/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityMobileEntry.h"

@interface SmartExpenseManager2 : NSObject

-(id) initWithContext:(NSManagedObjectContext *)context;
-(void) mergeSmartExpenses;
-(void) splitSmartExpense:(EntityMobileEntry *)smartExpense;

@end
