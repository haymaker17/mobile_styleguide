//
//  AirSell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/15/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "AirSell.h"
#import "Config.h"

@interface AirSell()
@property (nonatomic, strong) NSMutableString *buildString;
@end

@implementation AirSell
/*
 <AirSellCriteria>
 <CcId>487</CcId>
 <FareId>569aa2de-abc2-423a-b3de-e23eb8d609c7_1_1023</FareId>
 <TripName>No matter where you go, there you are</TripName>
 </AirSellCriteria>
 */

@synthesize path, currentElement, items, obj, keys;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	self.items = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    self.keys = [[NSMutableArray alloc] initWithObjects:nil];
	
	dataParser = [[NSXMLParser alloc] initWithData:webData];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	[self flushData];
	[self parseXMLFileAtData:data];
}

-(id)init
{
	self = [super init];
    if (self)
    {
        isInElement = @"NO";
        currentElement = @"";
        [self flushData];
    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return AIR_SELL;
}

-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post
	/*
     <AirSellCriteria>
     <CcId>487</CcId>
     <FareId>569aa2de-abc2-423a-b3de-e23eb8d609c7_1_1023</FareId>
     <TripName>No matter where you go, there you are</TripName>
     </AirSellCriteria>
     */
    
    NSString *customFields = parameterBag[@"TRAVEL_CUSTOM_FIELDS"];
	NSString *fareId = parameterBag[@"FARE_ID"];
	NSString *tripName = parameterBag[@"TRIP_NAME"];
	
	NSString *ccId = parameterBag[@"CREDIT_CARD_ID"];
	NSString *programId = parameterBag[@"PROGRAM_ID"];
    
    NSString *violationCode = parameterBag[@"VIOLATION_CODE"];
    NSString *violationJustification = parameterBag[@"VIOLATION_JUSTIFICATION"];

    NSMutableString *USGovtPerDiemLocation = nil;

    if ([Config isGov])
    {
        NSString *perDiemLocation = [parameterBag objectForKey:@"PER_DIEM_LOCATION"];
        NSString *perDiemLocState = [parameterBag objectForKey:@"GOV_PER_DIEM_LOC_STATE"];
        NSString *perDiemLocZipCode = [parameterBag objectForKey:@"GOV_PER_DIEM_LOC_ZIP"];
        NSString *perDiemLocCountry = [parameterBag objectForKey:@"GOV_PER_DIEM_COUNTRY"];
        
        if (perDiemLocCountry != nil && perDiemLocation != nil)
        {
            USGovtPerDiemLocation = [[NSMutableString alloc] initWithString:@"<USGovtPerDiemLocation>"];
            [USGovtPerDiemLocation appendString:[NSString stringWithFormat:@"<Country>%@</Country>", [NSString stringByEncodingXmlEntities:perDiemLocCountry]]];
            [USGovtPerDiemLocation appendString:[NSString stringWithFormat:@"<Name>%@</Name>", [NSString stringByEncodingXmlEntities:perDiemLocation]]];
            if (perDiemLocState != nil)
                [USGovtPerDiemLocation appendString:[NSString stringWithFormat:@"<State>%@</State>", [NSString stringByEncodingXmlEntities:perDiemLocState]]];
            else
                [USGovtPerDiemLocation appendString:@"<State/>"];
            
            if (perDiemLocZipCode != nil)
                [USGovtPerDiemLocation appendString:[NSString stringWithFormat:@"<ZipCode>%@</ZipCode>", [NSString stringByEncodingXmlEntities:perDiemLocZipCode]]];
            else
                [USGovtPerDiemLocation appendString:@"<ZipCode/>"];
            [USGovtPerDiemLocation appendString:@"</USGovtPerDiemLocation>"];
        }
    }
    
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<AirSellCriteria>"];
	[bodyXML appendString:@"<CcId>%@</CcId>"];

    // Travel custom fields
    if (customFields != nil) 
        [bodyXML appendString:customFields];
    
    if (parameterBag[@"CREDIT_CARD_CVV"] != nil)
        [bodyXML appendFormat:@"<CvvNumber>%@</CvvNumber>",[NSString stringByEncodingXmlEntities:parameterBag[@"CREDIT_CARD_CVV"]]];

    NSString *existingTANumber = parameterBag[@"EXISTING_TA_NUMBER"];
    NSString *perdiemLocationID = parameterBag[@"PER_DIEM_LOCATION_ID"];
    if ([existingTANumber length])
        [bodyXML appendString:[NSString stringWithFormat:@"<ExistingTANumber>%@</ExistingTANumber>", [NSString stringByEncodingXmlEntities:existingTANumber]]];

	[bodyXML appendString:@"<FareId>%@</FareId>"];
    
    if (parameterBag[@"FLIGHT_OPTIONS"] != nil) // <FlightOptionsSelected></FlightOptionsSelected>
        [bodyXML appendString:parameterBag[@"FLIGHT_OPTIONS"]];

    if ([perdiemLocationID length])
        [bodyXML appendString:[NSString stringWithFormat:@"<PerdiemLocationID>%@</PerdiemLocationID>", [NSString stringByEncodingXmlEntities:perdiemLocationID]]];

    if (programId != nil)
        [bodyXML appendString:[NSString stringWithFormat:@"<ProgramId>%@</ProgramId>", [NSString stringByEncodingXmlEntities:programId]]];
    
    if ([parameterBag[@"REDEEM_POINTS"] boolValue])
        [bodyXML appendString:@"<RedeemTravelPoints>true</RedeemTravelPoints>"];
    
	[bodyXML appendString:@"<TripName>%@</TripName>"];
    
    if (USGovtPerDiemLocation != nil)
        [bodyXML appendString:USGovtPerDiemLocation];
    
    if (violationCode != nil)
        [bodyXML appendString:[NSString stringWithFormat:@"<ViolationCode>%@</ViolationCode>", [NSString stringByEncodingXmlEntities:violationCode]]];

    if (violationJustification != nil)
        [bodyXML appendString:[NSString stringWithFormat:@"<ViolationJustification>%@</ViolationJustification>", [NSString stringByEncodingXmlEntities:violationJustification]]];
    
	[bodyXML appendString:@"</AirSellCriteria>"];
	
	
	NSString* formattedBodyXml = nil;
	
	formattedBodyXml = [NSString stringWithFormat:bodyXML,
						[NSString stringByEncodingXmlEntities:ccId], [NSString stringByEncodingXmlEntities:fareId], [NSString stringByEncodingXmlEntities:tripName]];
    
	return formattedBodyXml;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	//NSString *vendorCode = @"2V"; //amtrak?
	self.path = [NSString stringWithFormat:@"%@/Mobile/Air/SellV2",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
    if ([parameterBag[@"HAS_FLIGHT_OPTIONS"] isEqualToString:@"YES"]) // AirSell with FlightOptions take longer to respond
        msg.timeoutInterval = 120.0;
	
	return msg;
}




-(void) flushData
{
	
}



- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	self.currentElement = elementName;
	self.buildString = [[NSMutableString alloc] init];
    isInElement = @"YES";
    	
	if ([elementName isEqualToString:@"AirSellResponse"])
	{		
		self.obj = [[AmtrakSellData alloc] init];
	}
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"AirSellResponse"])
	{
		if (obj.tripLocator != nil) 
		{
			items[obj.tripLocator] = obj;
			[keys addObject:obj.tripLocator];
		}
	}
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [self.buildString appendString:string];
	//NSLog(@"element = %@, string = %@", currentElement, string);
	if ([currentElement isEqualToString:@"Status"])
	{
		[obj setSellStatus:string];
	}
	else if ([currentElement isEqualToString:@"RecordLocator"])
	{
		[obj setTripLocator:string];
	}
    else if ([currentElement isEqualToString:@"AuthorizationNumber"])
    {
        obj.authorizationNumber = string;
    }
    else if ([currentElement isEqualToString:@"ItinLocator"])
    {
        obj.itinLocator = string;
    }
    else if ([currentElement isEqualToString:@"ErrorMessage"])
    {
        obj.errorMessage = [self.buildString copy];
    }
   
}


- (void)parserDidEndDocument:(NSXMLParser *)parser
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}


@end
