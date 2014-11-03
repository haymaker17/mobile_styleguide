//
//  SaveReportEntryData.h
//  ConcurMobile
//
//  Created by yiwen on 12/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReportEntryDetailData.h"
#import "EntryData.h"
#import "ActionStatus.h"
#import "ExpenseTypeData.h"

@interface SaveReportEntryData : ReportEntryDetailData // SaveReportData
{
	//NSString		*rpeKey;	// It's the value in <SavedRpeKey> From server response
	EntryData		*entry;
	NSArray			*fields;
	NSString		*formKey;
	NSString		*curExpKey; // Updated expKey
    ExpenseTypeData *curExpType;// expType for updated expKey for the rpt policy
	NSString		*rptTotalPosted;
	NSString		*rptTotalClaimed;
	NSString		*rptTotalApproved;
	NSArray			*attendees;
	ActionStatus	*actionStatus;

}

@property(nonatomic, strong) EntryData		*entry;
@property(nonatomic, strong) NSArray		*fields;
@property(nonatomic, strong) NSString		*formKey;
@property(nonatomic, strong) NSString		*curExpKey;
@property(nonatomic, strong) ExpenseTypeData*curExpType;
@property(nonatomic, strong) NSString		*rptTotalPosted;
@property(nonatomic, strong) NSString		*rptTotalClaimed;
@property(nonatomic, strong) NSString		*rptTotalApproved;
@property(nonatomic, strong) NSArray		*attendees;
@property(nonatomic, strong) ActionStatus	*actionStatus;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(ReportData*) updateReportObject:(ReportData*) rpt;

@end
