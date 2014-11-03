//
//  ReserveHotel.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReserveHotel.h"
#import "HotelReservationRequest.h"
#import "HotelReservationResponse.h"
#import "FormatUtils.h"
#import "DataConstants.h"
#import "Config.h"

@implementation ReserveHotel


@synthesize currentElement;
@synthesize pathRoot;
@synthesize path;
@synthesize hotelReservationRequest;
@synthesize hotelReservationResponse, buildString;


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
//	NSString* responseString = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
	//NSLog(@"Response body for reserving a room:\n%@", responseString);
	[self parseXMLFileAtData:data];
}



- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.hotelReservationRequest = (HotelReservationRequest*)parameterBag[@"HOTEL_RESERVATION_REQUEST"];
	self.pathRoot =[ExSystem sharedInstance].entitySettings.uri;
	self.path = [NSString stringWithFormat:@"%@/mobile/Hotel/Reserve",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:RESERVE_HOTEL State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag Options:NO_RETRY];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
    msg.timeoutInterval = 90.0;
	return msg;
}


-(NSString *)makeXMLBody:(NSMutableDictionary *)pBag
{//knows how to make a post

    NSString *customFields = pBag[@"TRAVEL_CUSTOM_FIELDS"];
    
	NSString* sellSource = @"";
	if (hotelReservationRequest.sellSource != nil)
	{
		sellSource = hotelReservationRequest.sellSource;
	}
	
	NSString* tripId = @"";
	if (hotelReservationRequest.tripKey != nil)
	{
		tripId = hotelReservationRequest.tripKey;
	}
	
	NSString *violationJustification = (hotelReservationRequest.violationJustification != nil ? hotelReservationRequest.violationJustification : @"");
	NSString *violationCode = (hotelReservationRequest.violationCode != nil ? hotelReservationRequest.violationCode : @"");
	
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<SellCriteria>"];
	[bodyXML appendString:@"<BicCode>%@</BicCode>"];
	[bodyXML appendString:@"<CcId>%@</CcId>"];
	[bodyXML appendString:@"<ChainCode>%@</ChainCode>"];

    if (customFields != nil) 
        [bodyXML appendString:customFields];

    NSString *existingTANumber = pBag[@"EXISTING_TA_NUMBER"];
    NSString *perdiemLocationID = pBag[@"PER_DIEM_LOCATION_ID"];
    if ([existingTANumber length])
        [bodyXML appendString:[NSString stringWithFormat:@"<ExistingTANumber>%@</ExistingTANumber>", [NSString stringByEncodingXmlEntities:existingTANumber]]];
    if ([perdiemLocationID length])
        [bodyXML appendString:[NSString stringWithFormat:@"<PerdiemLocationID>%@</PerdiemLocationID>", [NSString stringByEncodingXmlEntities:perdiemLocationID]]];

	[bodyXML appendString:@"<PropertyId>%@</PropertyId>"];
	[bodyXML appendString:@"<PropertyName>%@</PropertyName>"];
    if (hotelReservationRequest.isUsingTravelPointsAgainstViolations)
        [bodyXML appendString:@"<RedeemTravelPoints>true</RedeemTravelPoints>"];
	[bodyXML appendString:@"<SellSource>%@</SellSource>"];
	[bodyXML appendString:@"<TripId>%@</TripId>"];
    
    NSMutableString *USGovtPerDiemLocation = nil;
    if ([Config isGov])
    {
        NSString *perDiemLocation = [pBag objectForKey:@"PER_DIEM_LOCATION"];
        NSString *perDiemLocState = [pBag objectForKey:@"GOV_PER_DIEM_LOC_STATE"];
        NSString *perDiemLocZipCode = [pBag objectForKey:@"GOV_PER_DIEM_LOC_ZIP"];
        NSString *perDiemLocCountry = [pBag objectForKey:@"GOV_PER_DIEM_COUNTRY"];
        
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
    if (USGovtPerDiemLocation != nil)
        [bodyXML appendString:USGovtPerDiemLocation];
    
	[bodyXML appendString:@"<ViolationCode>%@</ViolationCode>"];
	[bodyXML appendString:@"<ViolationJustification>%@</ViolationJustification>"];
	[bodyXML appendString:@"</SellCriteria>"];
	
	__autoreleasing NSString* formattedBodyXml = [NSString stringWithFormat:bodyXML,
								  hotelReservationRequest.bicCode,
								  hotelReservationRequest.creditCardId,
								  hotelReservationRequest.hotelChainCode,
								  hotelReservationRequest.propertyId,
								  hotelReservationRequest.propertyName,
								  sellSource,
								  tripId,
                                  violationCode,
								  [FormatUtils makeXMLSafe:violationJustification]
								  ];
	
	
	//NSLog(@"Request body for reserving a room:\n%@", formattedBodyXml);
	

	return formattedBodyXml;
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	// no op
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	//NSString * errorString = [NSString stringWithFormat:@"Unable to get hotel reservation results (Error code %i )", [parseError code]];
	//NSLog(@"error parsing XML: %@", errorString);
	
	// TODO: handle error
	//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error loading content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	//	[errorAlert show];
	//	[errorAlert release];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	self.currentElement = elementName;
    self.buildString = [[NSMutableString alloc] initWithString:@""];
	if ([elementName isEqualToString:@"SellResponse"])
	{
		self.hotelReservationResponse = [[HotelReservationResponse alloc] init]; 
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	// no op
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if (hotelReservationResponse != nil)
	{
        [buildString appendString:string];
		if ([currentElement isEqualToString:@"ErrorMessage"])
		{
			hotelReservationResponse.errorMessage = buildString;
		}
		else if ([currentElement isEqualToString:@"Status"])
		{
			hotelReservationResponse.status = buildString;
		}
		else if ([currentElement isEqualToString:@"RecordLocator"])
		{
			hotelReservationResponse.recordLocator = buildString;
		}
        else if ([currentElement isEqualToString:@"AuthorizationNumber"])
        {
            hotelReservationResponse.authorizationNumber = buildString;
        }
        else if ([currentElement isEqualToString:@"ItinLocator"])
        {
            hotelReservationResponse.itinLocator = buildString;
        }
	}
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}


@end

