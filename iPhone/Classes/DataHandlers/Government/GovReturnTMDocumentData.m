//
//  GovReturnTMDocumentData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovReturnTMDocumentData.h"
#import "EntityGovDocument.h"
#import "GovDocumentManager.h"

@implementation GovReturnTMDocumentData
@synthesize comments, docName, docType, reasonCode, sigkey, travid, status;
//
//<ReturnTMDocumentRequest>
//<comments>test comment</comments>
//<docName>TA3645</docName>
//<docType>AUTH</docType>
//<reasonCode>Ernest1</reasonCode>
//<sigkey>outtask1</sigkey>
//<travid>14085142</travid>
//</ReturnTMDocumentRequest>

-(NSString *)getMsgIdKey
{
	return GOV_STAMP_TM_DOCUMENTS;
}


-(NSString *)makeXMLBody
{//knows how to make a post
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<ReturnTMDocumentRequest>"];
	[bodyXML appendString:@"<comments>%@</comments>"];
	[bodyXML appendString:@"<docName>%@</docName>"];
	[bodyXML appendString:@"<docType>%@</docType>"];
	[bodyXML appendString:@"<reasonCode>%@</reasonCode>"];
	[bodyXML appendString:@"<sigkey>%@</sigkey>"];
	[bodyXML appendString:@"<travid>%@</travid>"];
    [bodyXML appendString:@"</ReturnTMDocumentRequest>"];
    
	NSString* formattedBodyXml = nil;
	
	formattedBodyXml = [NSString stringWithFormat:bodyXML, [NSString stringByEncodingXmlEntities:self.comments],
						[NSString stringByEncodingXmlEntities:self.docName], [NSString stringByEncodingXmlEntities:self.docType],  [NSString stringByEncodingXmlEntities:self.reasonCode], [NSString stringByEncodingXmlEntities:self.sigkey],[NSString stringByEncodingXmlEntities:self.travid]];
	
    //	NSLog(@"%@", formattedBodyXml);
	return formattedBodyXml;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.comments = [parameterBag objectForKey:@"COMMENTS"];
    self.docType = [parameterBag objectForKey:@"DOC_TYPE"];
    self.docName = [parameterBag objectForKey:@"DOC_NAME"];
    self.sigkey = [parameterBag objectForKey:@"SIG_KEY"];
    self.reasonCode = [parameterBag objectForKey:@"REASON_CODE"];
    self.travid = [parameterBag objectForKey:@"TRAVELER_ID"];
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/ReturnTMDocument",[ExSystem sharedInstance].entitySettings.uri];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	
	return msg;
}


- (void)parserDidStartDocument:(NSXMLParser *)parser
{
    [super parserDidStartDocument:parser];
}


-(void) parserDidEndDocument:(NSXMLParser *)parser
{
    [super parserDidEndDocument:parser];
    // TODO - update the document in core data
    //EntityGovDocument * currentDoc = [
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
	if ([elementName isEqualToString:@"ReturnToTravResponseRow"])
	{//alloc the trip instance
        self.status = [[ActionStatus alloc] init];
	}
}
//<?xml version="1.0"?>
//<ApproveResponse xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
//<ApproveResponseRow>
//<ErrorFlag>NO</ErrorFlag>
//<ErrorDesc/>
//</ApproveResponseRow>
//</ApproveResponse>
- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    if ([currentElement isEqualToString:@"ErrorFlag"])
	{
		[self.status setStatus:buildString];
	}
    else if ([currentElement isEqualToString:@"ErrorDesc"])
	{
		[self.status setErrMsg:buildString];
	}
}

@end
