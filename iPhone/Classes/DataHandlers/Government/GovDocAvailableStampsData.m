//
//  GovDocAvailableStampsData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/20/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovDocAvailableStampsData.h"
#import "FormatUtils.h"

@interface GovDocAvailableStampsData(Private)
-(void)fillStampInfo:(NSString *)string;
-(void)fillReturnToInfo:(NSString *)string;
-(void)fillDocInfo:(NSString *)string;
@end

@implementation GovDocAvailableStampsData
@synthesize travelerId, docType, docName, availStamps, stampInfo, returnToInfo;

-(NSString *)getMsgIdKey
{
	return GOV_DOC_AVAIL_STAMPS;
}


-(NSString *)makeXMLBody
{
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<TMDocRequest>"];
	[bodyXML appendString:@"<docName>%@</docName>"];
	[bodyXML appendString:@"<docType>%@</docType>"];
	[bodyXML appendString:@"<travid>%@</travid>"];
	[bodyXML appendString:@"</TMDocRequest>"];
	
	
	NSString* formattedBodyXml = nil;
	
	formattedBodyXml = [NSString stringWithFormat:bodyXML,
						[NSString stringByEncodingXmlEntities:self.docName], self.docType, self.travelerId];
	
	return formattedBodyXml;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.travelerId = [parameterBag objectForKey:@"TRAVELER_ID"];
    self.docName = [parameterBag objectForKey:@"DOC_NAME"];
    self.docType = [parameterBag objectForKey:@"DOC_TYPE"];
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/GetTMDocAvailableStamps",[ExSystem sharedInstance].entitySettings.uri];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	
	return msg;
}

/*
 <dsStamps>
 <Mtt-document>
 <docname>DT09262NWK</docname>
 <doctype>AUTH</doctype>
 <travid>55492</travid>
 <user_id>55492</user_id>
 <sig_required>false</sig_required>
 </Mtt-document>
 <Mtt-stamps>
 <stamp>APPROVED</stamp>
 <default_stamp>true</default_stamp>
 <returnto_required>false</returnto_required>
 </Mtt-stamps>
 <Mtt-stamps><stamp>AUDIT FAIL</stamp><default_stamp>false</default_stamp><returnto_required>false</returnto_required></Mtt-stamps>
 ...
 <Mtt-returnto>
 <returnto_id>444001</returnto_id>
 <returnto_ssn>444001</returnto_ssn>
 <returnto_name>Mirro, Rober</returnto_name>
 </Mtt-returnto>
 <Mtt-returnto>*/

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"Mtt-stamps"])
	{
        inStampInfo = YES;
		self.stampInfo = [[GovDocStampInfo alloc] init];
	}
	else if ([elementName isEqualToString:@"Mtt-returnto"])
	{
		inReturnToInfo = YES;
        self.returnToInfo = [[GovDocReturnToInfo alloc] init];
	}
	else if ([elementName isEqualToString:@"Mtt-document"])
	{
        self.availStamps = [[GovDocAvailableStamps alloc] init];
	}
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    if ([elementName isEqualToString:@"Mtt-returnto"])
	{
		inReturnToInfo = NO;
        if (self.availStamps != nil && self.returnToInfo != nil)
        {
            [self.availStamps.returnToInfoList addObject:self.returnToInfo];
        }
        self.returnToInfo = nil;
	}
    else if ([elementName isEqualToString:@"Mtt-stamps"])
	{
		inStampInfo = NO;
        if (self.availStamps != nil && self.stampInfo != nil)
        {
            [self.availStamps.stampInfoList setObject:self.stampInfo forKey:self.stampInfo.stampName];
        }
        self.stampInfo = nil;
	}
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if (inStampInfo)
	{
        [self fillStampInfo:buildString];
    }
    else if (inReturnToInfo)
    {
        [self fillReturnToInfo:buildString];
    }
    else
        [self fillDocInfo:buildString];
}

#pragma mark Entry Nodes
-(void)fillStampInfo:(NSString *)string
{
    if ([currentElement isEqualToString:@"stamp"])
    {
		[self.stampInfo setStampName:buildString];
    }
    else if ([currentElement isEqualToString:@"default_stamp"])
    {
		[self.stampInfo setIsDefault:@([buildString boolValue])];
    }
    else if ([currentElement isEqualToString:@"returnto_required"])
    {
		[self.stampInfo setReturnToRequired:@([buildString boolValue])];
    }
}

-(void)fillReturnToInfo:(NSString *)string
{
    if ([currentElement isEqualToString:@"returnto_id"])
    {
		[self.returnToInfo setReturnToId:buildString];
    }
    else if ([currentElement isEqualToString:@"returnto_name"])
    {
		[self.returnToInfo setReturnToName:buildString];
    }
}

-(void)fillDocInfo:(NSString *)string
{
    if ([currentElement isEqualToString:@"travid"])
    {
		[self.availStamps setTravelerId:buildString];
    }
    else if ([currentElement isEqualToString:@"docname"])
    {
		[self.availStamps setDocName:buildString];
    }
    else if ([currentElement isEqualToString:@"sig_required"])
    {
		[self.availStamps setSigRequired:@([buildString boolValue])];
    }
    else if ([currentElement isEqualToString:@"doctype"])
    {
		[self.availStamps setDocType:buildString];
    }
}
@end
