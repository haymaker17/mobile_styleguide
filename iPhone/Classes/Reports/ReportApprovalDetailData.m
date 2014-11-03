//
//  ReportApprovalDetailData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReportApprovalDetailData.h"
#import "MCLogging.h"
#import "FormFieldData.h"
#import "DataConstants.h"
//-
@implementation ReportApprovalDetailData


-(NSString *)getMsgIdKey
{
	return APPROVE_REPORT_DETAIL_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetReportDetailV2/%@/%@",
				[ExSystem sharedInstance].entitySettings.uri,  parameterBag[@"ID_KEY"], ROLE_EXPENSE_MANAGER ];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}




@end
