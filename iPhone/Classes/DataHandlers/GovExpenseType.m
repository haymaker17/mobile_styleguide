//
//  GovExpenseType.m
//  ConcurMobile
//
//  Created by ernest cho on 9/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovExpenseType.h"
#import "GovExpenseFormData.h"

@implementation GovExpenseType

@synthesize docType, expenseDescription, expenseForm;

-(id)init
{
	self = [super init];
    if (self)
    {
        self.docType = @"VCH";
        //self.expenseForm = [[GovExpenseFormData alloc] init];
    }
	return self;
}
 

@end
