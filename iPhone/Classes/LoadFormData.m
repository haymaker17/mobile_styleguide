//
//  LoadFormData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/5/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "LoadFormData.h"
#import "DataConstants.h"

@implementation LoadFormData
@synthesize path;

-(NSString *)getMsgIdKey
{
	return FORM_DATA;
}

#pragma mark -
#pragma mark Lifecycle


#pragma mark -
#pragma mark Message request
- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	NSString *formKey = (NSString*)parameterBag[@"FORM_KEY"];
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetFormDefinition/%@/TRAVELER",[ExSystem sharedInstance].entitySettings.uri, formKey];
	
	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

#pragma mark -

#pragma mark Message response
-(void) respondToXMLData:(NSData *)data
{
	//NSData *temp = data;
	//[self parseXMLFileAtData:data];
}

@end
