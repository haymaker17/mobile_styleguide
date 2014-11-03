//
//  ReportReceiptInfoManager.h
//  ConcurMobile
//
//  Created by yiwen on 11/18/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

// Used by SingleUser app to manage line item receipt cache

#import "BaseManager.h"
#import "EntityReportReceiptInfo.h"

@interface ReportReceiptInfoManager : BaseManager 
{
    
}

+(ReportReceiptInfoManager*)sharedInstance;
-(EntityReportReceiptInfo *) makeNew;

-(void) clearAll;
-(EntityReportReceiptInfo *) getEntryReceiptInfo:(NSString*) rpeKey withRptKey:(NSString *)rptKey;
-(EntityReportReceiptInfo *) fetchOrMake:(NSString *) rpeKey withRptKey:(NSString *)rptKey;

-(void) updateEntryReceiptInfo:(NSString*) rpeKey withRptKey:(NSString *)rptKey withPath:(NSString*)path withReceiptId:(NSString*) receiptId;

-(NSArray*) getEntryReceiptInfoForRpt:(NSString*) rptKey;

@end
