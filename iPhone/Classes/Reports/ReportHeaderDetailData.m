//
//  ReportHeaderDetailData.m
//  ConcurMobile
//
//  Created by yiwen on 3/2/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReportHeaderDetailData.h"
#import "DataConstants.h"

@implementation ReportHeaderDetailData

-(NSString *)getMsgIdKey
{
	return REPORT_HEADER_DETAIL_DATA;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.roleCode = parameterBag[@"ROLE_CODE"];
	self.rptKey = parameterBag[@"RPT_KEY"];
	if (self.rptKey == nil)
		self.rptKey = parameterBag[@"ID_KEY"];
	
	if (![roleCode length])
		self.roleCode = ROLE_EXPENSE_TRAVELER;
	
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetReportDetailV4/%@/%@", 
				[ExSystem sharedInstance].entitySettings.uri, self.rptKey, self.roleCode];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}



-(void)fillInfoToPropagateMsg:(NSMutableDictionary*) parameterBag forMsgId:(NSString*)msgId
{
    if (![msgId isEqualToString:ACTIVE_REPORT_DETAIL_DATA])
        return;
    parameterBag[@"REPORT"] = self.rpt;
    parameterBag[@"ID_KEY"] = self.rptKey;
    parameterBag[@"RECORD_KEY"] = self.rptKey;
    parameterBag[@"REFRESH_ALL"] = @"YES";
}

#pragma mark -
#pragma mark PartialReportDataBase Methods
-(ReportData*) updateReportObject:(ReportData*) obj
{
	// Update the report fields and exceptions object
	if (obj.rptKey == nil)
		return self.rpt;
	
	[obj copyHeaderDetail:self.rpt];
	return obj;
}


@end
