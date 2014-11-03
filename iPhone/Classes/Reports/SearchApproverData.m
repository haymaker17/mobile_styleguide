//
//  SearchApproverData.m
//  ConcurMobile
//
//  Created by yiwen on 8/26/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "SearchApproverData.h"


@implementation SearchApproverData
@synthesize rptKey, searchField, query, approverList, approver;

static NSMutableDictionary* xmlToPropertyMap = nil;

// Initialize msgId to msg class mapping here
+ (void)initialize
{
	if (self == [SearchApproverData class]) 
	{
        // Perform initialization here.
		xmlToPropertyMap = [[NSMutableDictionary alloc] init];
		xmlToPropertyMap[@"Email"] = @"Email";
		xmlToPropertyMap[@"EmpKey"] = @"EmpKey";
		xmlToPropertyMap[@"FirstName"] = @"FirstName";
		xmlToPropertyMap[@"LastName"] = @"LastName";
		xmlToPropertyMap[@"LoginId"] = @"LoginId";
	}
}

-(NSString *)getMsgIdKey
{
	return SEARCH_APPROVER_DATA;
}

-(void) flushData
{
	[super flushData];
	self.approverList = [[NSMutableArray alloc] init];
    self.approver = nil;
}



-(NSString *)makeXMLBody
{
	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@""];
	[bodyXML appendString:@"<ApproverSearchCriteria>"];
	[bodyXML appendString:[NSString stringWithFormat:@"<FieldName>%@</FieldName>", self.searchField]];
	[bodyXML appendString:[NSString stringWithFormat:@"<Query>%@</Query>", [NSString stringByEncodingXmlEntities:self.query]]];
	[bodyXML appendString:[NSString stringWithFormat:@"<RptKey>%@</RptKey>", self.rptKey]];
	[bodyXML appendString:@"</ApproverSearchCriteria>"];
    return bodyXML;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
	self.searchField = parameterBag[@"SEARCH_FIELD"];
    if (![self.searchField length])
        self.searchField = @"LAST_NAME";
	self.query = parameterBag[@"QUERY"];
    if (self.query == nil)
        self.query = @"";
	self.rptKey = parameterBag[@"RPT_KEY"];
	
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/SearchApproversV2",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
    [msg setBody:[self makeXMLBody]];

	return msg;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	[super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"ApproverInfo"])
	{
		self.approver = [[ApproverInfo alloc] init];
	}
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    
	if ([elementName isEqualToString:@"ApproverInfo"])
	{
		[self.approverList addObject:self.approver];
		self.approver = nil;
	}
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
	NSString* propName = xmlToPropertyMap[currentElement];
	if (propName != nil)
	{
		[self.approver setValue:buildString forKey:propName];
	}
	
}

@end
