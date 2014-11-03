//
//  GovDocExpenseCatInfo.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovDocExpenseCatInfo.h"

@implementation GovDocExpenseCatInfo
@synthesize expenseCategory, expenses, totalExpCatCost;

-(id)init
{
    if (self = [super init])
    {
        self.expenses = [[NSMutableArray alloc] init];
    }
    return self;
}
@end
