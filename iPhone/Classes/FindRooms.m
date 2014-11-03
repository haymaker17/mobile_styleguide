//
//  FindRooms.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "FindRooms.h"
#import "RoomResult.h"
#import "HotelFee.h"
#import "HotelDetail.h"
#import "HotelViolation.h"
#import "HotelSearch.h"
#import "hotelResult.h"
#import "FormatUtils.h"

@implementation FindRooms

@synthesize currentElement;
@synthesize path;
@synthesize buildString;
@synthesize currentRoomResult;
@synthesize currentHotelFee;
@synthesize currentHotelDetail;
@synthesize currentHotelViolation;
@synthesize hotelSearch, hotelBooking, hotelDetail, hotelFee, hotelRoom, hotelViolation;

-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
    
    if ([self.hotelBooking isFault])
    {
        [[MCLogging getInstance] log:@"### HotelBooking fault" Level:MC_LOG_DEBU];
        return;
    }
//    NSString *xml = [[NSString alloc] initWithData:data encoding:NSStringEncodingConversionAllowLossy];
//    NSLog(@"FindRooms XML = %@", xml);
//    [xml release];
	[self parseXMLFileAtData:data];
}


- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.hotelSearch = (HotelSearch*)parameterBag[@"HOTEL_SEARCH"];
    self.hotelBooking = parameterBag[@"HOTEL_BOOKING"];
    [[HotelBookingManager sharedInstance] deleteRooms:hotelBooking];
    
	NSString* propertyId = [FormatUtils makeXMLSafe:hotelBooking.propertyId]; // [FormatUtils makeXMLSafe:hotelSearch.selectedHotel.propertyId];
	self.path = [NSString stringWithFormat:@"%@/mobile/Hotel/Details/%@",[ExSystem sharedInstance].entitySettings.uri, propertyId];
	
	Msg* msg = [[Msg alloc] initWithData:FIND_HOTEL_ROOMS State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setMethod:@"GET"];
	return msg;
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
    //hotelSearch.selectedHotel =  [[[HotelResult alloc] init] autorelease];
    hotelSearch.selectedHotelIndex = nil;
	hotelSearch.selectedHotel.detail = [[HotelInfo alloc] init];	// Retain count = 2

}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	NSString * errorString = [NSString stringWithFormat:@"Unable to get room results (Error code %i )", [parseError code]];
	NSLog(@"error parsing XML: %@", errorString);
	
	// TODO: handle error
	//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error loading content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	//	[errorAlert show];
	//	[errorAlert release];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	self.buildString = [[NSMutableString alloc] init];	// Retain count = 2
	
	self.currentElement = elementName;
	
	if ([elementName isEqualToString:@"Room"])
	{
//		self.currentRoomResult = [[RoomResult alloc] init];	// Retain count = 2
//		[self.currentRoomResult release];	// Retain count = 1
        self.hotelRoom = [[HotelBookingManager sharedInstance] makeNewRoom];
	}
	else if ([elementName isEqualToString:@"Fee"])
	{
//		self.currentHotelFee = [[HotelFee alloc] init];	// Retain count = 2
//		[self.currentHotelFee release];	// Retain count = 1
        self.hotelFee = [[HotelBookingManager sharedInstance] makeNewFee];
	}
	else if ([elementName isEqualToString:@"Detail"])
	{
//		self.currentHotelDetail = [[HotelDetail alloc] init];	// Retain count = 2
//		[self.currentHotelDetail release];	// Retain count = 1
        self.hotelDetail = [[HotelBookingManager sharedInstance] makeNewDetail];
	}
	else if ([elementName isEqualToString:@"Violation"])
	{
//		self.currentHotelViolation = [[HotelViolation alloc] init];	// Retain count = 2
//		[self.currentHotelViolation release];	// Retain count = 1
        self.hotelViolation = [[HotelBookingManager sharedInstance] makeNewViolation];
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    if ([self.hotelBooking isFault])
    {   // MOB-8068 stop parsing, if enity object no longer valid, this message is orphaned.
        [[MCLogging getInstance] log:@"### HotelBooking fault - abort parsing" Level:MC_LOG_WARN];
        [parser abortParsing];
        return;
    }
    
//	if (currentRoomResult != nil)
//	{
//		if (currentHotelViolation != nil)
//		{
			if ([elementName isEqualToString:@"Violation"])
			{
                self.hotelViolation.relHotelRoom = self.hotelRoom;
                
//				[currentRoomResult.violations addObject:currentHotelViolation];
//				currentHotelViolation = nil;
			}
//		}
//		else
//		{
			if ([elementName isEqualToString:@"Room"])
			{
//				[hotelSearch.selectedHotel.detail.roomResults addObject:currentRoomResult];
//				currentRoomResult = nil;
                self.hotelRoom.relHotelBooking = self.hotelBooking;
                [[HotelBookingManager sharedInstance] saveIt:self.hotelBooking];
			}
//		}
//	}
//	else if (currentHotelFee != nil)
//	{
		if ([elementName isEqualToString:@"Fee"])
		{
//			[hotelSearch.selectedHotel.detail.hotelFees addObject:currentHotelFee];
//			currentHotelFee = nil;
            self.hotelFee.relHotelBooking = self.hotelBooking;
		}
//	}
//	else if (currentHotelDetail != nil)
//	{
		if ([elementName isEqualToString:@"Detail"])
		{
			NSCharacterSet *wsCharSet = [NSCharacterSet whitespaceCharacterSet];
			NSString* trimmedName = [hotelDetail.name stringByTrimmingCharactersInSet:wsCharSet];
			NSString* trimmedDescription = [hotelDetail.descript stringByTrimmingCharactersInSet:wsCharSet];
			
			NSCharacterSet *extCharSet = [NSCharacterSet characterSetWithCharactersInString:@" \r\n\t"];
			trimmedName = [trimmedName stringByTrimmingCharactersInSet:extCharSet];
			trimmedDescription = [trimmedDescription stringByTrimmingCharactersInSet:extCharSet];
			
			hotelDetail.name = trimmedName;
			hotelDetail.descript = trimmedDescription;
            self.hotelDetail.relHotelBooking = self.hotelBooking;
            [[HotelBookingManager sharedInstance] saveIt:hotelBooking];
//			[hotelSearch.selectedHotel.detail.hotelDetails addObject:currentHotelDetail];
//			currentHotelDetail = nil;
		}
//	}
        if ([elementName isEqualToString:@"HotelBenchmark"])
        {
            [[HotelBookingManager sharedInstance] saveIt:self.hotelBooking];
        }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [buildString appendString:string];
    
        if ([currentElement isEqualToString:@"Currency"])
        {
            self.hotelBooking.benchmarkCurrency = buildString;
        }
        else if ([currentElement isEqualToString:@"Price"])
        {
            self.hotelBooking.benchmarkPrice = @([buildString doubleValue]);
        }
        else if ([currentElement isEqualToString:@"GdsRateErrorCode"])
        {
            if ([buildString isEqualToString:@"PropertyNotAvailable"])
                self.hotelBooking.isSoldOut = @YES;
            else if ([buildString length])
                self.hotelBooking.isNoRates = @YES;
            [[HotelBookingManager sharedInstance] saveIt:self.hotelBooking];
        }
    
//	if (currentRoomResult != nil)
//	{
//		if (currentHotelViolation != nil)
//		{
			if ([currentElement isEqualToString:@"Message"])
			{
				hotelViolation.message = buildString;
//				currentHotelViolation.message = buildString;
			}
			else if ([currentElement isEqualToString:@"Code"])
			{
				hotelViolation.code = buildString;
//				currentHotelViolation.code = buildString;
			}
            else if ([currentElement isEqualToString:@"EnforcementLevel"])
            {
                hotelViolation.enforcementLevel = @([buildString intValue]);
            }
			else if ([currentElement isEqualToString:@"ViolationType"])
			{
				hotelViolation.violationType = buildString;
			}
//		}
//		else
//		{
			if ([currentElement isEqualToString:@"CrnCode"])
			{
                hotelRoom.crnCode = buildString;
//				currentRoomResult.currencyCode = string;
			}
			else if ([currentElement isEqualToString:@"Rate"])
			{
                hotelRoom.rate = buildString;
//				currentRoomResult.rate = string;
			}
			else if ([currentElement isEqualToString:@"Summary"])
			{
                hotelRoom.summary = buildString;
//				currentRoomResult.summary = string;
			}
			else if ([currentElement isEqualToString:@"BicCode"])
			{
                hotelRoom.bicCode = buildString;
//				currentRoomResult.bicCode = string;
			}
            else if ([currentElement isEqualToString:@"CanRedeemTravelPointsAgainstViolations"])
			{
                hotelRoom.canUseTravelPoints = @([buildString boolValue]);
			}
            else if ([currentElement isEqualToString:@"TravelPoints"])
			{
                hotelRoom.travelPoints = @([buildString intValue]);
			}
            else if ([currentElement isEqualToString:@"DepositRequired"])
			{
                hotelRoom.depositRequired = @([buildString boolValue]);
			}
            else if ([currentElement isEqualToString:@"ChoiceId"])
			{
                hotelRoom.choiceId = buildString;
			}
            else if ([currentElement isEqualToString:@"GdsName"])
			{
                hotelRoom.gdsName = buildString;
			}
			else if ([currentElement isEqualToString:@"SellSource"])
			{
                hotelRoom.sellSource  = buildString;
//				[buildString appendString:string];
//				currentRoomResult.sellSource = buildString;
			}
            else if ([currentElement isEqualToString:@"MaxEnforcementLevel"])
            {
                hotelRoom.maxEnforcementLevel = @([buildString intValue]);
            }
//		}
//	}
//	else if (currentHotelFee != nil)
//	{
		if ([currentElement isEqualToString:@"FeeType"])
		{
			hotelFee.type = buildString;
//			currentHotelFee.name = buildString;
		}
		else if ([currentElement isEqualToString:@"FeeDetails"])
		{
            hotelFee.details = buildString;
//			[buildString appendString:string];
//			currentHotelFee.description = buildString;
		}
//	}
//	else if (currentHotelDetail != nil)
//	{
		if ([currentElement isEqualToString:@"Name"])
		{
//			[buildString appendString:string];
            hotelDetail.name = buildString;
//			currentHotelDetail.name = buildString;
		}
		else if ([currentElement isEqualToString:@"Text"])
		{
			hotelDetail.descript = buildString;
//			currentHotelDetail.description = buildString;
		}
//	}
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}



@end

