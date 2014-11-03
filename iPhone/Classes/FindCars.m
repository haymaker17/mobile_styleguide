//
//  FindCars.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "FindCars.h"
#import "CarSearchCriteria.h"
#import "CarShop.h"
#import "CarResult.h"
#import "CarChain.h"
#import "CarDescription.h"
#import "CarLocation.h"
#import "HotelViolation.h"
#import "ExtendedHour.h"
#import "DateTimeConverter.h"

@implementation FindCars


@synthesize currentElement;
@synthesize path;
@synthesize criteria;
@synthesize carShop;
@synthesize currentCarResult;
@synthesize currentCarChain;
@synthesize currentCarDescription;
@synthesize currentCarLocation;
@synthesize currentCarViolation;


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	[self parseXMLFileAtData:data];
}


- (Msg*) newMsg: (NSMutableDictionary *)parameterBag
{

	self.criteria = parameterBag[@"CAR_SEARCH_CRITERIA"];
	self.path = [NSString stringWithFormat:@"%@/mobile/Car/GetCarList", [ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:FIND_CARS State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	return msg;
}


-(NSString *)makeDateString:(NSDate*)date
{
	NSDateFormatter* dateFormatter = [[NSDateFormatter alloc] init];
	// specify timezone
    [dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateFormat:@"yyyy-MM-dd"];
	NSString *dateOnlyString = [dateFormatter stringFromDate:date];
	
	NSString *dateString = [NSString stringWithFormat:@"%@T00:00:00", dateOnlyString];
	return dateString;
}


-(NSString *)makeHourString:(NSDate*)date
{
	NSCalendar *calendar = [NSCalendar currentCalendar];
	calendar.timeZone = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
	NSDateComponents *components = [calendar components:(NSHourCalendarUnit) fromDate:date];
	NSString *hourString = [NSString stringWithFormat:@"%i", components.hour];
	return hourString;
}


-(NSString *)makeXMLBody
{//knows how to make a post
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<CarShop>"];
	[bodyXML appendString:@"<CarType>%@</CarType>"];
	[bodyXML appendString:@"<DropOffDate>%@</DropOffDate>"];
	[bodyXML appendString:@"<DropOffHour>%@</DropOffHour>"];
    if ([criteria.dropoffLocationResult.iataCode length])
        [bodyXML appendFormat:@"<DropOffIATA>%@</DropOffIATA>",criteria.dropoffLocationResult.iataCode];
	[bodyXML appendString:@"<DropOffLatitude>%@</DropOffLatitude>"];
	[bodyXML appendString:@"<DropOffLongitude>%@</DropOffLongitude>"];
	[bodyXML appendString:@"<IsOffAirport>%@</IsOffAirport>"];
	[bodyXML appendString:@"<PickUpDate>%@</PickUpDate>"];
	[bodyXML appendString:@"<PickUpHour>%@</PickUpHour>"];
    if ([criteria.pickupLocationResult.iataCode length])
        [bodyXML appendFormat:@"<PickUpIATA>%@</PickUpIATA>",criteria.pickupLocationResult.iataCode];
	[bodyXML appendString:@"<PickUpLatitude>%@</PickUpLatitude>"];
	[bodyXML appendString:@"<PickUpLongitude>%@</PickUpLongitude>"];
	[bodyXML appendString:@"<Smoking>%@</Smoking>"];
	[bodyXML appendString:@"</CarShop>"];
	
	NSString* carType = (criteria.carTypeCodes)[criteria.carTypeIndex];
	NSString* pickupLatitude = criteria.pickupLocationResult.latitude;
	NSString* pickupLongitude = criteria.pickupLocationResult.longitude;
	NSString* dropoffLatitude = criteria.dropoffLocationResult.latitude;
	NSString* dropoffLongitude = criteria.dropoffLocationResult.longitude;
	NSString* pickupDate = [self makeDateString:criteria.pickupDate];
//	NSLog(@"pickupDate=%@", pickupDate);
//	NSLog(@"criteria.pickupDate=%@", [criteria.pickupDate description]);
	NSString* pickupHour = [NSString stringWithFormat:@"%i", [ExtendedHour getHourFromExtendedHour:criteria.pickupExtendedHour]];
	NSString* dropoffDate = [self makeDateString:criteria.dropoffDate];
	NSString* dropoffHour = [NSString stringWithFormat:@"%i", [ExtendedHour getHourFromExtendedHour:criteria.dropoffExtendedHour]];
	NSString* smoking = (criteria.smokingPreferenceCodes)[criteria.smokingIndex];
    NSString *offAirport = @"true";
    if(criteria.isOffAirport == NO)
        offAirport = @"false";
	
	NSString* formattedBodyXml = [NSString stringWithFormat:bodyXML,
								  carType,
								  dropoffDate,
								  dropoffHour,
								  dropoffLatitude,
								  dropoffLongitude,
								  offAirport, // IsOffAirport
								  pickupDate,
								  pickupHour,
								  pickupLatitude,
								  pickupLongitude,
								  smoking
								  ];
	
	
	return formattedBodyXml;
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	self.carShop = [[CarShop alloc] init];	// Retain count = 2

}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	NSString * errorString = [NSString stringWithFormat:@"Unable to get car search results (Error code %i )", [parseError code]];
	NSLog(@"error parsing XML: %@", errorString);
	
	// TODO: handle error
	//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error loading content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	//	[errorAlert show];
	//	[errorAlert release];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	self.currentElement = elementName;
	
	if ([elementName isEqualToString:@"CarChoice"])
	{
		self.currentCarResult = [[CarResult alloc] init];	// Retain count = 2

	}
	else if ([elementName isEqualToString:@"CarChain"])
	{
		self.currentCarChain = [[CarChain alloc] init];	// Retain count = 2

	}
	else if ([elementName isEqualToString:@"CarDescription"])
	{
		self.currentCarDescription = [[CarDescription alloc] init];	// Retain count = 2

	}
	else if ([elementName isEqualToString:@"CarLocation"])
	{
		self.currentCarLocation = [[CarLocation alloc] init];	// Retain count = 2

	}
	else if ([elementName isEqualToString:@"Violation"])
	{
		self.currentCarViolation = [[HotelViolation alloc] init];	// Retain count = 2

	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	if ([elementName isEqualToString:@"CarChoice"])
	{
		[carShop.carResults addObject:currentCarResult];
		self.currentCarResult = nil;
	}
	else if ([elementName isEqualToString:@"CarChain"])
	{
		if (currentCarChain.code != nil)
			(carShop.carChains)[currentCarChain.code] = currentCarChain;
		self.currentCarChain = nil;
	}
	else if ([elementName isEqualToString:@"CarDescription"])
	{
		(carShop.carDescriptions)[currentCarDescription.carCode] = currentCarDescription;
		self.currentCarDescription = nil;
	}
	else if ([elementName isEqualToString:@"CarLocation"])
	{
        //NSLog(@"currentCarLocation.iataCode %@-%@", currentCarLocation.iataCode, currentCarLocation.chainCode);
		(carShop.carLocations)[[NSString stringWithFormat:@"%@-%@", currentCarLocation.iataCode, currentCarLocation.chainCode]] = currentCarLocation;
		self.currentCarLocation = nil;
	}
	else if ([elementName isEqualToString:@"Violation"])
	{
		if (currentCarResult != nil)
			[currentCarResult.violations addObject:currentCarViolation];
		self.currentCarViolation = nil;
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if (currentCarResult != nil)
	{
		if ([currentElement isEqualToString:@"CarId"])
		{
			currentCarResult.carId = string;
		}
		else if ([currentElement isEqualToString:@"DailyRate"])
		{
			currentCarResult.dailyRate = [string doubleValue];
		}
		else if ([currentElement isEqualToString:@"TotalRate"])
		{
			currentCarResult.totalRate = [string doubleValue];
		}
		else if ([currentElement isEqualToString:@"FreeMiles"])
		{
			currentCarResult.freeMiles = string;
		}
		else if ([currentElement isEqualToString:@"GdsName"])
		{
			currentCarResult.gdsName = string;
		}
		else if ([currentElement isEqualToString:@"CarType"])
		{
			currentCarResult.carType = string;
		}
        else if ([currentElement isEqualToString:@"ChoiceId"])
		{
			currentCarResult.choiceId = string;
		}
		else if ([currentElement isEqualToString:@"ChainCode"])
		{
			currentCarResult.chainCode = [string lowercaseString];
		}
        else if ([currentElement isEqualToString:@"Currency"])
		{
			currentCarResult.currencyCode = string;
		}
		else if ([currentElement isEqualToString:@"ImageUri"])
		{
			currentCarResult.imageUri = [NSString stringWithFormat:@"%@%@", [ExSystem sharedInstance].entitySettings.uri, string];
		}
        else if([currentElement isEqualToString:@"SendCreditCard"])
        {
            currentCarResult.sendCreditCard = [string boolValue];
        }
        else if([currentElement isEqualToString:@"MaxEnforcementLevel"])
        {
            currentCarResult.maxEnforcementLevel = @([string intValue]);
        }
		else if ([currentElement isEqualToString:@"Code"])
		{
			if (currentCarViolation != nil)
				currentCarViolation.code = string;
		}
		else if ([currentElement isEqualToString:@"Message"])
		{
			if (currentCarViolation != nil)
				currentCarViolation.message = string;
		}
        else if ([currentElement isEqualToString:@"EnforcementLevel"])
        {
			if (currentCarViolation != nil)
				currentCarViolation.enforcementLevel = string;            
        }
        else if ([currentElement isEqualToString:@"ViolationType"])
        {
			if (currentCarViolation != nil)
				currentCarViolation.violationType = string;
        }
	}
	else if (currentCarChain != nil)
	{
		if ([currentElement isEqualToString:@"ChainCode"])
		{
			currentCarChain.code = [string lowercaseString];
		}
		else if ([currentElement isEqualToString:@"ChainName"])
		{
			currentCarChain.name = string;
		}
		else if ([currentElement isEqualToString:@"ImageUri"])
		{
			currentCarChain.imageUri = [NSString stringWithFormat:@"%@%@", [ExSystem sharedInstance].entitySettings.uri, string];
		}
	}
	else if (currentCarDescription != nil)
	{
		if ([currentElement isEqualToString:@"CarAC"])
		{
			currentCarDescription.carAC = string;
		}
		else if ([currentElement isEqualToString:@"CarBody"])
		{
			currentCarDescription.carBody = string;
		}
		else if ([currentElement isEqualToString:@"CarClass"])
		{
			currentCarDescription.carClass = string;
		}
		else if ([currentElement isEqualToString:@"CarCode"])
		{
			currentCarDescription.carCode = string;
		}
		else if ([currentElement isEqualToString:@"CarFuel"])
		{
			currentCarDescription.carFuel = string;
		}
		else if ([currentElement isEqualToString:@"CarTrans"])
		{
			currentCarDescription.carTrans = string;
		}
	}
	else if (currentCarLocation != nil)
	{
		if ([currentElement isEqualToString:@"Address1"])
		{
			currentCarLocation.address1 = string;
		}
		else if ([currentElement isEqualToString:@"Address2"])
		{
			currentCarLocation.address2 = string;
		}
		else if ([currentElement isEqualToString:@"ChainCode"])
		{
			currentCarLocation.chainCode = string;
		}
		else if ([currentElement isEqualToString:@"CountryCode"])
		{
			currentCarLocation.countryCode = string;
		}
		else if ([currentElement isEqualToString:@"IataCode"])
		{
			currentCarLocation.iataCode = string;
		}
		else if ([currentElement isEqualToString:@"Latitude"])
		{
			currentCarLocation.latitude = [string doubleValue];
		}
		else if ([currentElement isEqualToString:@"Longitude"])
		{
			currentCarLocation.longitude = [string doubleValue];
		}
		else if ([currentElement isEqualToString:@"LocationCategory"])
		{
			currentCarLocation.locationCategory = string;
		}
		else if ([currentElement isEqualToString:@"LocationName"])
		{
			currentCarLocation.locationName = string;
		}
		else if ([currentElement isEqualToString:@"PhoneNumber"])
		{
            //NSLog(@"phone %@", string);
			currentCarLocation.phoneNumber = string;
		}
        else if ([currentElement isEqualToString:@"MoOpen"])
        {
            currentCarLocation.moOpen = string;
        }
        else if ([currentElement isEqualToString:@"MoClose"])
        {
            currentCarLocation.moClose = string;
        }
        else if ([currentElement isEqualToString:@"TuOpen"])
        {
            currentCarLocation.tuOpen = string;
        }
        else if ([currentElement isEqualToString:@"TuClose"])
        {
            currentCarLocation.tuClose = string;
        }
        else if ([currentElement isEqualToString:@"WeOpen"])
        {
            currentCarLocation.weOpen = string;
        }
        else if ([currentElement isEqualToString:@"WeClose"])
        {
            currentCarLocation.weClose = string;
        }
        else if ([currentElement isEqualToString:@"ThOpen"])
        {
            currentCarLocation.thOpen = string;
        }
        else if ([currentElement isEqualToString:@"ThClose"])
        {
            currentCarLocation.thClose = string;
        }
        else if ([currentElement isEqualToString:@"FrOpen"])
        {
            currentCarLocation.frOpen = string;
        }
        else if ([currentElement isEqualToString:@"FrClose"])
        {
            currentCarLocation.frClose = string;
        }
        else if ([currentElement isEqualToString:@"SaOpen"])
        {
            currentCarLocation.saOpen = string;
        }
        else if ([currentElement isEqualToString:@"SaClose"])
        {
            currentCarLocation.saClose = string;
        }
        else if ([currentElement isEqualToString:@"SuOpen"])
        {
            currentCarLocation.suOpen = string;
        }
        else if ([currentElement isEqualToString:@"SuClose"])
        {
            currentCarLocation.suClose = string;
        }
		else if ([currentElement isEqualToString:@"State"])
		{
			currentCarLocation.state = string;
		}
	}
	else
	{
		if ([currentElement isEqualToString:@"PickupIATA"])
		{
			carShop.pickupIata = string;
		}
		else if ([currentElement isEqualToString:@"PickupDateTime"])
		{
//			NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init];
//			// specify timezone
//			[dateFormatter setTimeZone:[NSTimeZone localTimeZone]];// timeZoneWithAbbreviation:@"GMT"]];
//			// Localizing date
//			[dateFormatter setLocale:[NSLocale currentLocale]];
//			
//			[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
//			//NSLog(@"string = %@",string);
//			NSDate *dt = [dateFormatter dateFromString:string];
//			//NSLog(@"dt = %@", [dt description]);
			carShop.pickupDate = [DateTimeFormatter getNSDateFromMWSDateString:string];
		}
		else if ([currentElement isEqualToString:@"DropoffIATA"])
		{
			carShop.dropoffIata = string;
		}
		else if ([currentElement isEqualToString:@"DropoffDateTime"])
		{
//			NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init];
//			// specify timezone
//			[dateFormatter setTimeZone:[NSTimeZone localTimeZone]];// timeZoneWithAbbreviation:@"GMT"]];
//			// Localizing date
//			[dateFormatter setLocale:[NSLocale currentLocale]];
//			
//			[dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
//			NSDate *dt = [dateFormatter dateFromString:string];
			carShop.dropoffDate = [DateTimeFormatter getNSDateFromMWSDateString:string];
		}
	}
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	[carShop didPopulate];
	
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}



@end

