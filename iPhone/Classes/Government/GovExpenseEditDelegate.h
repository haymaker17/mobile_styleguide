//
//  GovExpenseEditDelegate.h
//  ConcurMobile
//
//  Created by charlottef on 2/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol GovExpenseEditDelegate <NSObject>

-(void) updatedExpense:(NSString*) expenseId;

@end
