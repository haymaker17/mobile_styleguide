//
//  AddToReportData.h
//  ConcurMobile
//
//  Created by yiwen on 4/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ActionStatus.h"
#import "ActiveReportDetailData.h"
#import "Msg.h"

// MOB-4583 Inherit ArchivedResponder protocol from ActiveReportDetailData
@interface AddToReportData : ActiveReportDetailData {

	NSArray                 *meKeys;
	NSArray                 *pctKeys;
	NSArray                 *cctKeys;
    NSArray                 *rcKeys;
	NSDictionary			*meAtnMap;
	
	// Selected RptKey or ReportName
	NSString				*reportName;
	
	ActionStatus			*reportStatus;
	NSMutableDictionary		*meStatusDict;
	NSMutableDictionary		*pctStatusDict;
	NSMutableDictionary     *cctStatusDict; // MOB-12351 detect cct failure
    
	BOOL					inReportStatus, inEntries, inPcTransactions, inCcTransactions, inSmartExpenses;
	ActionStatus			*curStatus;
}

@property (nonatomic, strong) NSArray	*meKeys;
@property (nonatomic, strong) NSArray	*pctKeys;
@property (nonatomic, strong) NSArray	*cctKeys;
@property (nonatomic, strong) NSArray	*rcKeys;
@property (nonatomic, strong) NSDictionary	*meAtnMap;
@property (nonatomic, strong) NSString	*reportName;
@property (nonatomic, strong) ActionStatus		*reportStatus;
@property (nonatomic, strong) NSMutableDictionary	*meStatusDict;
@property (nonatomic, strong) NSMutableDictionary	*pctStatusDict;
@property (nonatomic, strong) NSMutableDictionary	*cctStatusDict;
@property (nonatomic, strong) NSMutableDictionary	*smartExpenseIdsStatusDict;
@property (nonatomic, strong) ActionStatus		*curStatus;
@property (nonatomic) BOOL inReportStatus;
@property (nonatomic) BOOL inEntriesStatus;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;
-(NSString*)getReportElementName;

-(BOOL) hasFailedEntry;

@end
