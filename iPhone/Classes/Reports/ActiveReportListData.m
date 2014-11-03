//
//  ActiveReportListData.m
//  ConcurMobile
//
//  Created by yiwen on 4/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ActiveReportListData.h"


@implementation ActiveReportListData
@synthesize unsubmittedRpts;

-(id )init
{
    self = [super init];
	if (self)
    {
        unsubmittedRpts = nil;
    }
    return self;
}


-(NSString *)getMsgIdKey
{
	return ACTIVE_REPORTS_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetActiveReports",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

- (NSArray *) getUnsubmittedReports
{
	if (unsubmittedRpts == nil && objDict != nil)
	{
		// Generate a list of unsubmitted reports from active reports
		NSPredicate* condition = [NSPredicate predicateWithFormat:@"(apsKey like %@) OR (apsKey like %@)", @"A_NOTF", @"A_RESU"];
		self.unsubmittedRpts = [[objDict allValues] filteredArrayUsingPredicate:condition];
	}
	return unsubmittedRpts;
}

-(void) flushData
{
	self.unsubmittedRpts = nil;
	[super flushData];
}


@end
