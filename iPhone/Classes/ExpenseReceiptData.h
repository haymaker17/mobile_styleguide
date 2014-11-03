//
//  ExpenseReceiptData.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/20/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "OOPEntry.h"
#import "EntryData.h"
#import "ReportData.h"

//TODO: ReciptSourceData
@interface ExpenseReceiptData : NSObject 
{
	OOPEntry *expenseEntry;
	EntryData *reportEntry;
	ReportData *reportData;
    NSString *invoiceKey;
	NSString *previousMeKeyForReportEntry, *receiptImageId, *receiptStoreImageUrl;
	UIImage *__weak receiptImageForCaching;
}

@property (nonatomic,strong) OOPEntry *expenseEntry;
@property (nonatomic,strong) EntryData *reportEntry;
@property (nonatomic,strong) ReportData *reportData;
@property (nonatomic,strong) NSString *invoiceKey;
@property (nonatomic,strong) NSString *previousMeKeyForReportEntry;
@property (nonatomic,strong) NSString *receiptImageId;
@property (nonatomic,strong) NSString *receiptStoreImageUrl;
@property (nonatomic,weak) UIImage *receiptImageForCaching;

@end
