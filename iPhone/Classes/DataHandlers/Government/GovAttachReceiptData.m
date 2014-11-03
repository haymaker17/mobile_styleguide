//
//  GovAttachReceiptData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/27/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//
/*
 <AttachTMReceiptRequest>
 <ccExpId>20121004000922236437.00</ccExpId>
 <receiptId>00E4CC3A439932F18BE58513DA6DBB02</receiptId>
 </AttachTMReceiptRequest>
 
 Attach to a Document. Get the docName and docType from the document list.
 <AttachTMReceiptRequest>
 <docName>ErnestTest</docName>
 <docType>LVCH</docType>
 <receiptId>00E4CC3A439932F18BE58513DA6DBB02</receiptId>
 </AttachTMReceiptRequest>
 
 Attach to an Expense within a Document. Get the docName, docType and expId from the document list.
 <AttachTMReceiptRequest>
 <docName>ErnestTest</docName>
 <docType>LVCH</docType>
 <expId>20121004000922236437.00</expId>
 <receiptId>00E4CC3A439932F18BE58513DA6DBB02</receiptId>
 </AttachTMReceiptRequest>*/

#import "GovAttachReceiptData.h"

@implementation GovAttachReceiptData
@synthesize status, receiptId, expId, docName, docType;

-(NSString *)getMsgIdKey
{
	return GOV_ATTACH_RECEIPT;
}


-(NSString *)makeXMLBody
{//knows how to make a post
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<AttachTMReceiptRequest>"];
    if ([self.docName length] && [self.docType length])
    {
        [bodyXML appendString:[NSString stringWithFormat:@"<docName>%@</docName>", [NSString stringByEncodingXmlEntities:self.docName]]];
        [bodyXML appendString:[NSString stringWithFormat:@"<docType>%@</docType>", [NSString stringByEncodingXmlEntities:self.docType]]];
    
        if ([self.expId length])
            [bodyXML appendString:[NSString stringWithFormat:@"<expId>%@</expId>", [NSString stringByEncodingXmlEntities:self.expId]]];
    }
    else
    {
        [bodyXML appendString:[NSString stringWithFormat:@"<ccExpId>%@</ccExpId>", [NSString stringByEncodingXmlEntities:self.expId]]];
    }
    
    [bodyXML appendString:[NSString stringWithFormat:@"<receiptId>%@</receiptId>", [NSString stringByEncodingXmlEntities:self.receiptId]]];
    [bodyXML appendString:@"</AttachTMReceiptRequest>"];
	return bodyXML;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.docType = [parameterBag objectForKey:@"DOC_TYPE"];
    self.docName = [parameterBag objectForKey:@"DOC_NAME"];
    self.receiptId = [parameterBag objectForKey:@"RECEIPT_ID"];
    self.expId = [parameterBag objectForKey:@"EXP_ID"];
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/AttachTMReceipt",[ExSystem sharedInstance].entitySettings.uri];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	
	return msg;
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
	if ([elementName isEqualToString:@"result"])
	{//alloc the trip instance
        self.status = [[ActionStatus alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    if ([currentElement isEqualToString:@"error"])
	{
		[self.status setStatus:buildString];
	}
}

@end
