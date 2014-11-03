//
//  ExpenseReceiptData.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/20/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ExpenseReceiptData.h"


@implementation ExpenseReceiptData
@synthesize expenseEntry, reportEntry,reportData,previousMeKeyForReportEntry,receiptImageForCaching,receiptImageId,receiptStoreImageUrl,invoiceKey;

-(ExpenseReceiptData*)init
{
    self = [super init];
	if (self)
	{
		self.previousMeKeyForReportEntry = nil;
		self.expenseEntry = nil;
		self.reportEntry = nil;
		self.reportData = nil;
		self.receiptImageForCaching = nil;
		self.receiptImageId = nil;
        self.receiptStoreImageUrl = nil;
        self.invoiceKey = nil;
	}
	
	return self;
}

@end
