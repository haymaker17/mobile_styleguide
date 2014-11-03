//
//  ExpenseTypeDelegate.h
//  ConcurMobile
//
//  Created by yiwen on 4/27/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExpenseTypeData.h"

@protocol ExpenseTypeDelegate <NSObject>
- (void)cancelExpenseType;
- (void)saveSelectedExpenseType:(ExpenseTypeData*) expenseType;
@optional
- (void)expenseTypeDlgDismissed;
@end
