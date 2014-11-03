//
//  GovDocExpenseCatInfo.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovDocExpenseCatInfo : NSObject
{
    NSString                *expenseCategory;
    NSDecimalNumber         *totalExpCatCost;
    
    NSMutableArray          *expenses;
}

@property (nonatomic, strong) NSString              *expenseCategory;
@property (nonatomic, strong) NSDecimalNumber       *totalExpCatCost;
@property (nonatomic, strong) NSMutableArray        *expenses;

@end
