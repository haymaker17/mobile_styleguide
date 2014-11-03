//
//  SaveReportData.m
//  ConcurMobile
//
//  Created by yiwen on 11/23/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SaveReportData.h"
#import "DataConstants.h"

@implementation SaveReportData
@synthesize report, fields;


-(NSString *)getMsgIdKey
{
	return SAVE_REPORT_DATA;
}


-(BOOL) isFieldEmpty:(NSString*)val
{
	return ![val length];
}

-(NSString *)makeXMLBody
{//knows how to make a post

	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] 
								initWithString:@"<Report xmlns='http://schemas.datacontract.org/2004/07/Snowbird' xmlns:i='http://www.w3.org/2001/XMLSchema-instance'>"];

	if (![self isFieldEmpty:self.report.rptKey])
		[bodyXML appendString:[NSString stringWithFormat:@"<RptKey>%@</RptKey>", self.report.rptKey]];
	if (![self isFieldEmpty:self.report.polKey])
		[bodyXML appendString:[NSString stringWithFormat:@"<PolKey>%@</PolKey>", self.report.polKey]];

	[bodyXML appendString:@"<Fields>"];

	int nFields = [self.fields count];
	for (int ix = 0; ix < nFields; ix ++)
	{
		FormFieldData* ff = (self.fields)[ix];
		
		[bodyXML appendString:@"<FormField>"];
		
		if (![self isFieldEmpty:ff.access])
			[bodyXML appendString:[NSString stringWithFormat:@"<Access>%@</Access>", ff.access]];
		if (![self isFieldEmpty:ff.ctrlType])
			[bodyXML appendString:[NSString stringWithFormat:@"<CtrlType>%@</CtrlType>", ff.ctrlType]];
		if (![self isFieldEmpty:ff.dataType])
			[bodyXML appendString:[NSString stringWithFormat:@"<DataType>%@</DataType>", ff.dataType]];
		if (![self isFieldEmpty:ff.ftCode])
			[bodyXML appendString:[NSString stringWithFormat:@"<FtCode>%@</FtCode>", ff.ftCode]];
		if (![self isFieldEmpty:ff.iD])
			[bodyXML appendString:[NSString stringWithFormat:@"<Id>%@</Id>", ff.iD]];
		if (![self isFieldEmpty:ff.liCode])
			[bodyXML appendString:[NSString stringWithFormat:@"<LiCode>%@</LiCode>", [NSString stringByEncodingXmlEntities:ff.liCode]]];
		if (![self isFieldEmpty:ff.liKey])
			[bodyXML appendString:[NSString stringWithFormat:@"<LiKey>%@</LiKey>", ff.liKey]];
		if (![self isFieldEmpty:ff.listKey])
			[bodyXML appendString:[NSString stringWithFormat:@"<ListKey>%@</ListKey>", ff.listKey]];
		if (![self isFieldEmpty:ff.parLiKey])
			[bodyXML appendString:[NSString stringWithFormat:@"<ParLiKey>%@</ParLiKey>", ff.parLiKey]];
		NSString* serverValue = [ff getServerValue];
		if (![self isFieldEmpty:serverValue])
			[bodyXML appendString:[NSString stringWithFormat:@"<Value>%@</Value>", [NSString stringByEncodingXmlEntities:serverValue]]];

		[bodyXML appendString:@"</FormField>"];

	}
	[bodyXML appendString:@"</Fields></Report>"];
	return bodyXML;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.report = parameterBag[@"REPORT"];
	self.fields = parameterBag[@"FIELDS"];  // Fields with updated data
	self.roleCode = parameterBag[@"ROLE_CODE"];
	NSString* cpDown = parameterBag[@"COPY_DOWN_TO_CHILD_FORMS"];
	if ([self isFieldEmpty:roleCode])
		self.roleCode = ROLE_EXPENSE_TRAVELER;
	
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/SaveReport/%@", 
				[ExSystem sharedInstance].entitySettings.uri, self.roleCode];
	if ([@"Y" isEqualToString:cpDown])
		self.path = [NSString stringWithFormat:@"%@/%@", self.path, @"CopyDownToChildForms"];

	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	
	return msg;
}




@end
