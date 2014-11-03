//
//  GovDeleteExpFromDocData.m
//  ConcurMobile
//
//  Created by charlottef on 1/24/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovDeleteExpFromDocData.h"
#import "EntityGovExpenseExtension.h"

@implementation GovDeleteExpFromDocData

@synthesize expenseId, docName, docType, status;

-(NSString*) getMsgIdKey
{
    return GOV_DELETE_EXP_FROM_DOC;
}

-(Msg*) newMsg:(NSMutableDictionary*)parameterBag
{
    self.expenseId = [parameterBag objectForKey:@"EXPENSE_ID"];
    self.docName = [parameterBag objectForKey:@"DOC_NAME"];
    self.docType = [parameterBag objectForKey:@"DOC_TYPE"];
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/DeleteTMExpense", [ExSystem sharedInstance].entitySettings.uri];
    Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
    
    [msg setBody:[self makeXMLBody]];
    [msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	return msg;
}

-(NSString *) makeXMLBody
{
    __autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<DeleteTMExpenseRequest>"];
    [bodyXML appendString:[NSString stringWithFormat:@"<docName>%@</docName>", self.docName]];
    [bodyXML appendString:[NSString stringWithFormat:@"<docType>%@</docType>", self.docType]];
    [bodyXML appendString:[NSString stringWithFormat:@"<expId>%@</expId>", self.expenseId]];
    [bodyXML appendString:@"</DeleteTMExpenseRequest>"];
    return bodyXML;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
	if ([elementName isEqualToString:@"ActionStatus"])
	{
        self.status = [[ActionStatus alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    if ([currentElement isEqualToString:@"Status"])
	{
		[self.status setStatus:buildString];
	}
    else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		[self.status setErrMsg:buildString];
	}
}

@end
