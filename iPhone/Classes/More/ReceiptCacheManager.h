//
//  ReceiptCacheManager.h
//  ConcurMobile
//
//  Created by yiwen on 11/23/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "ReportData.h"
#import "EntryData.h"
#import "ExMsgRespondDelegate.h"

//@protocol ReceiptCacheManagerDelegate
//-(void) doneReceiptDownload;
//@end

@interface ReceiptCacheManager : NSObject<ExMsgRespondDelegate>
{
    NSMutableArray*    vcs;
    NSMutableArray*    receiptsForVc;    // vc => list of receipt info
    NSMutableArray*    queue;       // receiptIds
}

@property (nonatomic, strong) NSMutableArray* vcs;
@property (nonatomic, strong) NSMutableArray* receiptsForVc;
@property (nonatomic, strong) NSMutableArray* queue;

+(ReceiptCacheManager*)sharedInstance;

-(void) retrieveReportReceipts:(ReportData*)rpt forObject:(NSObject*)vc;

-(void) cancelReceiptsRetrieval:(NSObject*)vc;

-(void) entryReceiptDownloaded:(EntryData*)rpe;
-(void) entryReceiptUpdated:(EntryData*)rpe;

-(void) clearReportReceipts:(NSString*) rptKey;

-(NSArray*) getReportReceiptInfos:(NSString*) rptKey; // Array of EntityReportReceiptInfo

-(BOOL) isReportDone:(ReportData*) rpt withVc:(NSObject*) vc;

@end
