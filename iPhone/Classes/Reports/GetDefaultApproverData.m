//
//  GetDefaultApproverData.m
//  ConcurMobile
//
//  Created by yiwen on 12/6/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "GetDefaultApproverData.h"

@implementation GetDefaultApproverData
@synthesize approver;
static NSMutableDictionary* xmlToPropertyMap = nil;

// Initialize msgId to msg class mapping here
+ (void)initialize
{
	if (self == [GetDefaultApproverData class]) 
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
	return GET_DEFAULT_APPROVER_DATA;
}

-(void) flushData
{
	[super flushData];
    self.approver = nil;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetDefaultApprover", [ExSystem sharedInstance].entitySettings.uri];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}




- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
	
    if ([elementName isEqualToString:@"ApproverInfo"])
    {
        self.approver = [[ApproverInfo alloc] init];
    }
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    if (self.approver != nil)
	{
        NSString* propName = xmlToPropertyMap[currentElement];
        if (propName != nil)
        {
            [self.approver setValue:buildString forKey:propName];
        }
    }
    
}	

@end
