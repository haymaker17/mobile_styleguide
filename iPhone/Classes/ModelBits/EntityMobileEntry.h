//
//  EntityMobileEntry.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 10/1/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityMobileEntry : NSManagedObject

@property (nonatomic, retain) NSString * expKey;
@property (nonatomic, retain) NSString * rcKey;
@property (nonatomic, retain) NSString * authorizationRefNo;
@property (nonatomic, retain) NSString * cardTypeName;
@property (nonatomic, retain) NSString * merchantCity;
@property (nonatomic, retain) NSString * merchantName;
@property (nonatomic, retain) NSString * hasRichData;
@property (nonatomic, retain) NSString * merchantState;
@property (nonatomic, retain) NSString * category;
@property (nonatomic, retain) NSString * expName;
@property (nonatomic, retain) NSDate * transactionDate;
@property (nonatomic, retain) NSString * vendorName;
@property (nonatomic, retain) NSString * smartExpenseMeKey;
@property (nonatomic, retain) NSString * comment;
@property (nonatomic, retain) NSString * key;
@property (nonatomic, retain) NSString * locationName;
@property (nonatomic, retain) NSString * accountNumberLastFour;
@property (nonatomic, retain) NSString * cardTypeCode;
@property (nonatomic, retain) NSString * cctType;
@property (nonatomic, retain) NSNumber * isHidden;
@property (nonatomic, retain) NSString * doingBusinessAs;
@property (nonatomic, retain) NSDecimalNumber * transactionAmount;
@property (nonatomic, retain) NSString * crnCode;
@property (nonatomic, retain) NSString * pctKey;
@property (nonatomic, retain) NSString * cctKey;
@property (nonatomic, retain) NSString * receiptImageId;
@property (nonatomic, retain) NSNumber * isMergedSmartExpense;
@property (nonatomic, retain) NSString * smartExpenseId;
@property (nonatomic, retain) NSString * merchantCtryCode;
@property (nonatomic, retain) NSString * transactionDescription;
@property (nonatomic, retain) NSString * hasReceipt;
@property (nonatomic, retain) NSString * cardName;
@property (nonatomic, retain) NSString * localId;
@property (nonatomic, retain) NSString * localReceiptImageId;
@property (nonatomic, retain) NSString * pcaKey;
@property (nonatomic, retain) NSString * mobileReceiptImageId;
@property (nonatomic, retain) NSString * cctReceiptImageId;
@property (nonatomic, retain) NSString * eReceiptImageId;
@property (nonatomic, retain) NSString * ereceiptId;
@property (nonatomic, retain) NSString * ereceiptSource;
@property (nonatomic, retain) NSString * ereceiptType;
@property (nonatomic, retain) NSString * statKey;

@end
