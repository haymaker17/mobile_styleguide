//
//  GovLocationsData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovLocationsData.h"
#import "DateTimeFormatter.h"

@implementation GovLocationsData
@synthesize locations, currentLoc, countryCode, city, stateCode, latitude, longitude, zipCode, range;

//<GetLocationsResponse xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
//<GetLocationsResponseRow>
//<locate>BREMERTON</locate>
//<locst>WA</locst>
//<passwd>P</passwd>
//<effdate>2010-10-01</effdate>
//<time-zone>9</time-zone>
//<expdate>2049-12-31</expdate>
//<linkloc>ALL PLACES NOT LIST</linkloc>
//<linkst>US</linkst>
//<comment/>
//<comploc>BREMERTON</comploc>
//<county>KITSAP</county>
//<currency/>
//<dos-code/>
//<conus>C</conus>
//<dod-ind>false</dod-ind>
//<countyid>0</countyid>
//<fips/>
//<custom-rt-org/>
//<zipcode>98312</zipcode>
//<lattitude>47.574600</lattitude>
//<longitude>-122.810580</longitude>



-(NSString *)getMsgIdKey
{
	return GOV_LOCATIONS;
}

-(NSString *)makeXMLBody
{
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<GetTMLocationsRequest>"];
//    <GetTMLocationsRequest>
//    <city>Seattle</city>
//    <countryCode>US</countryCode>
//    <latitude>47.6062095</latitude>
//    <longitude>-122.3320708</longitude>
//    <range>25</range>
//    <state>WA</state>
//    <! -- Optional:  <zip>98101</zip>  - ->
//    </GetTMLocationsRequest>

    [bodyXML appendString:@"<city>%@</city>"];
    [bodyXML appendString:@"<countryCode>%@</countryCode>"];
    [bodyXML appendString:@"<latitude>%@</latitude>"];
    [bodyXML appendString:@"<longitude>%@</longitude>"];
  	[bodyXML appendString:@"<range>25</range>"];
   	[bodyXML appendString:@"<state>%@</state>"];
    
    if ([self.zipCode length])
    {
        [bodyXML appendString:[NSString stringWithFormat:@"<zipCode>%@</zipCode>", self.zipCode]];
	}
    else
        [bodyXML appendString:@"<zipCode/>"];

    [bodyXML appendString:@"</GetTMLocationsRequest>"];
    
	NSString* formattedBodyXml = nil;
	formattedBodyXml = [NSString stringWithFormat:bodyXML,
                        [NSString stringByEncodingXmlEntities:self.city],
						[NSString stringByEncodingXmlEntities:self.countryCode],
                        [NSString stringByEncodingXmlEntities:self.latitude],
                        [NSString stringByEncodingXmlEntities:self.longitude],
                        [NSString stringByEncodingXmlEntities:self.stateCode]
                        ];
	
	return formattedBodyXml;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.longitude = [parameterBag objectForKey:@"LONGITUDE"];
    self.latitude = [parameterBag objectForKey:@"LATITUDE"];
    self.zipCode = [parameterBag objectForKey:@"ZIP_CODE"];
    self.countryCode = [parameterBag objectForKey:@"CTRY_CODE"];
    self.city = [parameterBag objectForKey:@"CITY"];
    self.stateCode = [parameterBag objectForKey:@"STATE_CODE"];
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/GetTMLocationsV2",[ExSystem sharedInstance].entitySettings.uri];
    
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
	
	if ([elementName isEqualToString:@"GetLocationsResponseRow"])
	{
		self.currentLoc = [[GovLocation alloc] init];
	}
    else if ([elementName isEqualToString:@"GetLocationsResponse"])
	{
        self.locations = [[NSMutableArray alloc] init];
	}
    
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    if ([elementName isEqualToString:@"GetLocationsResponseRow"])
	{
        [self.locations addObject:self.currentLoc];
        self.currentLoc = nil;
	}
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if ([currentElement isEqualToString:@"locate"])
    {
		[self.currentLoc setLocation:buildString];
    }
    else if ([currentElement isEqualToString:@"locst"])
    {
		[self.currentLoc setLocState:buildString];
    }
    else if ([currentElement isEqualToString:@"linkloc"])
    {
		[self.currentLoc setLinkLocation:buildString];
    }
    else if ([currentElement isEqualToString:@"linkst"])
    {
		[self.currentLoc setLinkState:buildString];
    }
    else if ([currentElement isEqualToString:@"county"])
    {
        [self.currentLoc setCounty:buildString];
    }
    else if ([currentElement isEqualToString:@"effdate"])
	{
		[self.currentLoc setEffectiveDate:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"expdate"])
	{
		[self.currentLoc setExpirationDate:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"zipcode"])
    {
        [self.currentLoc setLocZip:buildString];
    }
    else if ([currentElement isEqualToString:@"conus"])
    {
        if ([buildString isEqualToString:@"C"])
            [self.currentLoc setIsUSContiguous:YES];
        else if ([buildString isEqualToString:@"O"])
            [self.currentLoc setIsUSContiguous:NO];
    }
}
@end
