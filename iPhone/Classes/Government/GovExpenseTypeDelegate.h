//
//  GovExpenseTypeDelegate.h
//  ConcurMobile
//
//  Created by ernest cho on 10/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "GovExpenseType.h"

@protocol GovExpenseTypeDelegate <NSObject>

-(void) updateExpenseType:(GovExpenseType*) newExpenseType;

@end
