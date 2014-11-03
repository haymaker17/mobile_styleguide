//
//  ReceiptManager.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 5/23/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BaseManager.h"
#import "EntityReceipt.h"

#define RECEIPT_TYPE_RECEIPT_STORE @"RS"
#define RECEIPT_TYPE_REPORT_ENTRY_RECEIPT @"RPE"
#define RECEIPT_TYPE_REPORT_RECEIPT @"RPT"
#define RECEIPT_TYPE_MOBILE_ENTRY_RECEIPT @"ME"
#define RECEIPT_TYPE_INVOICE_RECEIPT @"INV"

#define RECEIPT_CACHE_LIMIT 35 
//Debug use 3 as limit
//#define RECEIPT_CACHE_LIMIT 3

@interface ReceiptManager : NSObject {
    NSString   *entityName;
    NSManagedObjectContext      *_context;
}

@property (nonatomic, strong) NSString *entityName;
@property (nonatomic, strong) NSManagedObjectContext *context;

+(ReceiptManager*)sharedInstance;
-(ReceiptManager*)init;
-(EntityReceipt *) makeNew;

-(void) clearAll;
-(NSManagedObject *) fetchReceipt:(NSString *)key;
-(void) saveReceipt;
-(void) deleteObj:(NSManagedObject *)obj;
-(BOOL) hasAnyReceipt;
@end