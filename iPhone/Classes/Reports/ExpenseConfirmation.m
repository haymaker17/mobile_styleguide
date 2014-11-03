//
//  ExpenseConfirmation.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 4/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "ExpenseConfirmation.h"

@implementation ExpenseConfirmation
@synthesize confirmationKey, text, title;

static NSMutableDictionary * expenseConfirmationXmlToPropertyMap;

+ (NSMutableDictionary*) getXmlToPropertyMap
{
	return expenseConfirmationXmlToPropertyMap;
}

+ (void)initialize
{
	if (self == [ExpenseConfirmation class]) 
	{
        // Perform initialization here.
		expenseConfirmationXmlToPropertyMap = [[NSMutableDictionary alloc] init];
		expenseConfirmationXmlToPropertyMap[@"ConfirmationKey"] = @"ConfirmationKey";
		expenseConfirmationXmlToPropertyMap[@"Text"] = @"Text";
		expenseConfirmationXmlToPropertyMap[@"Title"] = @"Title";
    }
}


@end
