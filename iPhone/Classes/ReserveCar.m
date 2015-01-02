//
//  ReserveCar.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReserveCar.h"
#import "CarReservationRequest.h"
#import "CarReservationResponse.h"
#import "CarBookingTripData.h"
#import "Config.h"

@interface ReserveCar()
@property (nonatomic, strong) NSMutableString *buildString;
@end


@implementation ReserveCar


@synthesize currentElement;
@synthesize pathRoot;
@synthesize path;
@synthesize carReservationRequest;
@synthesize carReservationResponse;


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	[self parseXMLFileAtData:data];
}


- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.carReservationRequest = (CarReservationRequest*)parameterBag[@"CAR_RESERVATION_REQUEST"];
	self.pathRoot =[ExSystem sharedInstance].entitySettings.uri;
	self.path = [NSString stringWithFormat:@"%@/mobile/Car/BookCar",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:RESERVE_CAR State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag Options:NO_RETRY];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	return msg;
}


-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post
    
    NSString *customFields = parameterBag[@"TRAVEL_CUSTOM_FIELDS"];
	NSString *tripId = @"";
	NSString *tripLocator = @"";
	NSString *recordLocator = @"";
	if (carReservationRequest.carBookingTripData != nil)
	{
		tripId = carReservationRequest.carBookingTripData.tripKey;
		tripLocator = carReservationRequest.carBookingTripData.clientLocator;
		recordLocator = carReservationRequest.carBookingTripData.recordLocator;
	}
    
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
        else
        {
            USGovtPerDiemLocation = [[NSMutableString alloc] initWithString:@"<USGovtPerDiemLocation/>"];
        }
    }
    
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<CarSell>"];
	[bodyXML appendString:@"<CarId>%@</CarId>"];
	[bodyXML appendString:@"<CreditCardId>%@</CreditCardId>"];
    
    if (customFields != nil) 
        [bodyXML appendString:customFields];
    
    NSString *existingTANumber = parameterBag[@"EXISTING_TA_NUMBER"];
    NSString *perdiemLocationID = parameterBag[@"PER_DIEM_LOCATION_ID"];
    if ([existingTANumber length])
        [bodyXML appendString:[NSString stringWithFormat:@"<ExistingTANumber>%@</ExistingTANumber>", [NSString stringByEncodingXmlEntities:existingTANumber]]];
    if ([perdiemLocationID length])
        [bodyXML appendString:[NSString stringWithFormat:@"<PerdiemLocationID>%@</PerdiemLocationID>", [NSString stringByEncodingXmlEntities:perdiemLocationID]]];
    
	[bodyXML appendString:@"<RecordLocator>%@</RecordLocator>"];
	[bodyXML appendString:@"<TripId>%@</TripId>"];
	[bodyXML appendString:@"<TripLocator>%@</TripLocator>"];
    if (USGovtPerDiemLocation != nil)
        [bodyXML appendString:USGovtPerDiemLocation];
	if (carReservationRequest.violationReasonCode != nil && [carReservationRequest.violationReasonCode length] > 0)
		[bodyXML appendString:[NSString stringWithFormat:@"<ViolationCode>%@</ViolationCode>", carReservationRequest.violationReasonCode]];
	if (carReservationRequest.violationJustification != nil && [carReservationRequest.violationJustification length] > 0)
		[bodyXML appendString:[NSString stringWithFormat:@"<ViolationJustification>%@</ViolationJustification>", [NSString stringByEncodingXmlEntities:carReservationRequest.violationJustification]]];
	[bodyXML appendString:@"</CarSell>"];
	
	__autoreleasing NSString* formattedBodyXml = [NSString stringWithFormat:bodyXML,
								  carReservationRequest.carId,
								  carReservationRequest.creditCardId,
								  recordLocator,
								  tripId,
								  tripLocator];
    
    //carReservationRequest.carId
	
    //NSLog(@"formattedBodyXml=%@", formattedBodyXml);
	return formattedBodyXml;
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	// no op
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	NSString * errorString = [NSString stringWithFormat:@"Unable to get car reservation results (Error code %li )", (long)[parseError code]];
	NSLog(@"error parsing XML: %@", errorString);
	
	// TODO: handle error
	//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error loading content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	//	[errorAlert show];
	//	[errorAlert release];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	self.currentElement = elementName;
	if ([elementName isEqualToString:@"CarSellResponse"])
	{
		self.carReservationResponse = [[CarReservationResponse alloc] init];
	}
    self.buildString = [[NSMutableString alloc] init];
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	// no op
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if (carReservationResponse != nil)
	{
        [self.buildString appendString:string];
		if ([currentElement isEqualToString:@"Status"])
		{
			carReservationResponse.status = string;
		}
		else if ([currentElement isEqualToString:@"RecordLocator"])
		{
			carReservationResponse.recordLocator = string;
		}
        else if ([currentElement isEqualToString:@"AuthorizationNumber"])
        {
            carReservationResponse.authorizationNumber = string;
        }
		else if ([currentElement isEqualToString:@"ErrorMessage"])
		{
            NSString *currentBuildString = [self.buildString copy];
			NSRange rangeOfLeftBracket = [currentBuildString rangeOfString:@"["];
			NSString *messageOnly = (rangeOfLeftBracket.location == NSNotFound ? currentBuildString : [currentBuildString substringToIndex:rangeOfLeftBracket.location]);
			carReservationResponse.errorMessage = messageOnly;
		}
        else if ([currentElement isEqualToString:@"ItinLocator"])
        {
            carReservationResponse.itinLocator = string;
        }
	}
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}


@end

