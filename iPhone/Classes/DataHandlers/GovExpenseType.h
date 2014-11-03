//
//  GovExpenseType.h
//  ConcurMobile
//
//  Created by ernest cho on 9/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class GovExpenseFormData;

@interface GovExpenseType : NSObject
{
    NSString *docType;
    NSString *expenseDescription;
    GovExpenseFormData *expenseForm;
}

@property (strong, nonatomic) NSString *docType;
@property (strong, nonatomic) NSString *expenseDescription;
@property (strong, nonatomic) GovExpenseFormData *expenseForm;

-(id) init;

@end
