//
//  DefaultAttendeeData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 2/21/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "DefaultAttendeeData.h"
#import "ExpenseTypesManager.h"

@implementation DefaultAttendeeData

-(NSString *)getMsgIdKey
{
	return DEFAULT_ATTENDEE_DATA;
}

#pragma mark -
#pragma mark Message request
- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetDefaultAttendee",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	return msg;
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	for (AttendeeData* attendee in attendees)
	{
		NSString* atnTypeCode = [attendee getNonNullableValueForFieldId:@"AtnTypeCode"];
		if ([atnTypeCode isEqualToString:@"SYSEMP"])
		{
			[[ExpenseTypesManager sharedInstance] setAttendeeRepresentingThisEmployee:attendee];
			break;
		}
	}
}

@end
