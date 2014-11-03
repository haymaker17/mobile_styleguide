//
//  GovPerDiemRateData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovPerDiemRateData.h"
#import "DateTimeFormatter.h"
#import "FormatUtils.h"

@implementation GovPerDiemRateData
@synthesize location, stateOrCountryCode, effectiveDate, expirationDate, crnCode;
@synthesize currentPerDiemRate;


-(NSString *)getMsgIdKey
{
	return GOV_PER_DIEM_RATE;
}


-(NSString *)makeXMLBody
{//knows how to make a post
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<GetPerDiemRateRequest>"];
    /*<currency>USD</currency>
    <effDate>01282013</effDate>
    <expDate>01292013</expDate>
    <location>RICHLAND</location>
    <stateOrCountryCode>WA</stateOrCountryCode>*/
    
	[bodyXML appendString:@"<currency>%@</currency>"];
	[bodyXML appendString:@"<effDate>%@</effDate>"];
	[bodyXML appendString:@"<expDate>%@</expDate>"];
	[bodyXML appendString:@"<location>%@</location>"];
	[bodyXML appendString:@"<stateOrCountryCode>%@</stateOrCountryCode>"];
    
    
    [bodyXML appendString:@"</GetPerDiemRateRequest>"];
    
	NSString* formattedBodyXml = nil;
	NSString *effDateStr = [DateTimeFormatter formatDate:self.effectiveDate Format:@"MMddyyyy"  TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    NSString *expDateStr = [DateTimeFormatter formatDate:self.expirationDate Format:@"MMddyyyy"  TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	formattedBodyXml = [NSString stringWithFormat:bodyXML,
						self.crnCode, [NSString stringByEncodingXmlEntities:effDateStr], [NSString stringByEncodingXmlEntities:expDateStr], [NSString stringByEncodingXmlEntities:self.location], [NSString stringByEncodingXmlEntities:self.stateOrCountryCode]];
	
	return formattedBodyXml;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    self.location = [parameterBag objectForKey:@"LOCATION"];
    self.effectiveDate = [parameterBag objectForKey:@"EFFECTIVE_DATE"];
    self.expirationDate = [parameterBag objectForKey:@"EXPIRATEION_DATE"];
    self.stateOrCountryCode = [parameterBag objectForKey:@"STATE_CTRY_CODE"];
    self.crnCode = @"USD";
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/GetPerDiemRate",[ExSystem sharedInstance].entitySettings.uri];
    
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
	
	if ([elementName isEqualToString:@"GetPerdiemRateResponseRow"])
	{
		self.currentPerDiemRate = [[GovPerDiemRate alloc] init];
	}
    
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if ([currentElement isEqualToString:@"locate"])
    {
		[self.currentPerDiemRate setLocation:buildString];
    }
    else if ([currentElement isEqualToString:@"locst"])
    {
		[self.currentPerDiemRate setLocState:buildString];
    }
    else if ([currentElement isEqualToString:@"currency"])
    {
		[self.currentPerDiemRate setCrnCode:buildString];
    }
    else if ([currentElement isEqualToString:@"effdate"])
	{
		[self.currentPerDiemRate setEffectiveDate:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"expdate"])
	{
		[self.currentPerDiemRate setExpirationDate:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"ldgrate"])
	{
        [self.currentPerDiemRate setLdgRate:[FormatUtils decimalNumberFromServerString:buildString]];
    }
    else if ([currentElement isEqualToString:@"mierate"])
	{
        [self.currentPerDiemRate setMieRate:[FormatUtils decimalNumberFromServerString:buildString]];
    }
    else if ([currentElement isEqualToString:@"TabRow"])
	{
        [self.currentPerDiemRate setPerDiemId:buildString];
    }
}


/*<locate>RICHLAND</locate>
 <locst>WA</locst>
 <effdate>2012-10-01</effdate>
 <snl-start>1992-01-01</snl-start>
 <snl-end>1992-12-31</snl-end>
 <ldgrate>93.00</ldgrate>
 <mierate>46.00</mierate>
 <expdate>2049-12-31</expdate>
 <snl-name/>
 <comment/>
 <ftnote-rate>0.00</ftnote-rate>
 <incid-amt>5.00</incid-amt>
 <custom-rt-org/>
 <currency>USD</currency>
 <extra-char1/>
 <extra-char2/>
 <extra-dec1>0.00</extra-dec1>
 <extra-dec2>0.00</extra-dec2>
 <extra-date1 xsi:nil="true"/>
 <TabRow>0x00000000000dcac7</TabRow>*/
@end
