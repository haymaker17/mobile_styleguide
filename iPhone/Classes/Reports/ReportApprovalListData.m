//
//  ReportApprovalListData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReportApprovalListData.h"
#import "MCLogging.h"

@implementation ReportApprovalListData

-(NSString *)getMsgIdKey
{
	return REPORT_APPROVAL_LIST_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetReportsToApprove",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}


- (NSString*) getReportElementName
{
	return @"ReportToApprove";
}



@end
