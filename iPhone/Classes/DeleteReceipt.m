//
//  DeleteReceipt.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 3/2/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "DeleteReceipt.h"
#import "DataConstants.h"
//-

@implementation DeleteReceipt
@synthesize receiptImageId,dataParser,status,currentElement;

-(NSString *)getMsgIdKey
{
	return DELETE_RECEIPT;
}


-(NSString *)makeXMLBody
{	
	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc]
								initWithString:@"<DeleteReceiptAction xmlns:i='http://www.w3.org/2001/XMLSchema-instance'>"];
	
	[bodyXML appendString:[NSString stringWithFormat:@"<ReceiptImageId>%@</ReceiptImageId>",self.receiptImageId]];
	[bodyXML appendString:@"</DeleteReceiptAction>"];
	return bodyXML;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.receiptImageId = parameterBag[@"ReceiptImageId"];
	
	NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/DeleteReceipt",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	
	return msg;
}

#pragma mark -
#pragma mark Parsing
-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	[self parseXMLFileAtData:data];
}


//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)data 
{		
	dataParser = [[NSXMLParser alloc] initWithData:data];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	self.currentElement = elementName;
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if ([currentElement isEqualToString:@"Status"]) 
	{
		self.status = string;
	}
}



@end
