//
//  DownloadSystemConfig.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "DownloadSystemConfig.h"
#import "Msg.h"
#import "SystemConfig.h"
#import "ViolationReason.h"
#import "OfficeLocationResult.h"


@implementation DownloadSystemConfig


@synthesize currentElement;
@synthesize path;
@synthesize buildString;
@synthesize systemConfig;
@synthesize parsingHotelReasons;
@synthesize parsingCarReasons;
@synthesize parsingAirReasons;
@synthesize currentReason;
@synthesize currentOffice;
@synthesize checkboxDefault;
@synthesize nonRefundableMsg;
@synthesize isRefundableInfo;
@synthesize showCheckbox;

-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	[self parseXMLFileAtData:data];
}


- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.path = [NSString stringWithFormat:@"%@/mobile/Config/SystemConfig",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:DOWNLOAD_SYSTEM_CONFIG State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
    [msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	return msg;
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	self.systemConfig = [[SystemConfig alloc] init];  // Retain count = 2

}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	//NSString * errorString = [NSString stringWithFormat:@"Unable to get user config (Error code %i )", [parseError code]];
	//NSLog(@"error parsing XML: %@", errorString);
	
	// TODO: handle error
	//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error loading content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	//	[errorAlert show];
	//	[errorAlert release];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	self.buildString = [[NSMutableString alloc] init];  // Retain count = 2

	
	self.currentElement = elementName;
	
    if ([elementName isEqualToString:@"AirReasons"])
	{
		self.parsingAirReasons = YES;
	}
	else if ([elementName isEqualToString:@"HotelReasons"])
	{
		self.parsingHotelReasons = YES;
	}
	else if ([elementName isEqualToString:@"CarReasons"])
	{
		self.parsingCarReasons = YES;
	}
	else if ([elementName isEqualToString:@"ReasonCode"])
	{
		self.currentReason = [[ViolationReason alloc] init];  // Retain count = 2
	}
	else if ([elementName isEqualToString:@"OfficeChoice"])
	{
		self.currentOffice = [[OfficeLocationResult alloc] init];  // Retain count = 2
	}
    else if ([elementName isEqualToString:@"RefundableInfo"])
    {
        self.isRefundableInfo = YES;
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    if ([elementName isEqualToString:@"AirReasons"])
	{
		self.parsingAirReasons = NO;
	}
	else if ([elementName isEqualToString:@"HotelReasons"])
	{
		self.parsingHotelReasons = NO;
	}
	else if ([elementName isEqualToString:@"CarReasons"])
	{
		self.parsingCarReasons = NO;
	}
	else if ([elementName isEqualToString:@"ReasonCode"])
	{
		if (parsingHotelReasons)
		{
			(systemConfig.hotelViolationReasons)[currentReason.code] = currentReason;
			self.currentReason = nil;
		}
		else if (parsingCarReasons)
		{
			(systemConfig.carViolationReasons)[currentReason.code] = currentReason;
			self.currentReason = nil;
		}
        else if (parsingAirReasons)
        {
            (systemConfig.airViolationReasons)[currentReason.code] = currentReason;
			self.currentReason = nil;
        }
	}
	else if ([elementName isEqualToString:@"OfficeChoice"])
	{
		// MOB-12548 : show street address in location search
		NSString *officeLocation = (currentOffice.streetAddress == nil ? @"" : currentOffice.streetAddress);
		
        if (currentOffice.city != nil)
        {
            if([officeLocation lengthIgnoreWhitespace])
            {
                officeLocation = [officeLocation stringByAppendingFormat:@", "];
            }
            
			officeLocation = [officeLocation stringByAppendingFormat:@"%@", currentOffice.city];
		}

		if (currentOffice.state != nil)
        {
            //for : MOB-10387
            if([officeLocation lengthIgnoreWhitespace])
            {
                officeLocation = [officeLocation stringByAppendingFormat:@", "];
            }

			officeLocation = [officeLocation stringByAppendingFormat:@"%@", currentOffice.state];
		}
		
        if (currentOffice.country != nil)
        {
            if([officeLocation lengthIgnoreWhitespace])
            {
                officeLocation = [officeLocation stringByAppendingFormat:@", "];
            }

			officeLocation = [officeLocation stringByAppendingFormat:@"%@", currentOffice.country];
		}
		currentOffice.location = officeLocation;
		[systemConfig.officeLocations addObject:currentOffice];
		self.currentOffice = nil;
	}
    else if ([elementName isEqualToString:@"CheckboxDefault"])
    {
        self.checkboxDefault = ([[buildString lowercaseString] isEqualToString:@"true"])? YES: NO;
        systemConfig.checkboxDefault = checkboxDefault;
    }
    else if ([elementName isEqualToString:@"RefundableInfo"])
    {
        self.isRefundableInfo = NO;
    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    /*MOB-5538
    The SystemConfig parser of XML was set up to use a mutable string, but it was never implemented.  This bug should've never happened ever since we figured out that the XML parser will make multiple foundchar calls when it comes across extended characters.*/
    [buildString appendString:string];
    
	if (parsingHotelReasons || parsingCarReasons || parsingAirReasons)
	{
		if ([currentElement isEqualToString:@"Description"])
		{
			currentReason.description = buildString;
		}
		else if ([currentElement isEqualToString:@"Id"])
		{
			currentReason.code = buildString;
		}
	}
	else if (currentOffice != nil)
	{
		if ([currentElement isEqualToString:@"Lat"])
		{
			currentOffice.latitude = buildString;
		}
		else if ([currentElement isEqualToString:@"Lon"])
		{
			currentOffice.longitude = buildString;
		}
		else if ([currentElement isEqualToString:@"City"])
		{
			currentOffice.city = buildString;
		}
		else if ([currentElement isEqualToString:@"State"])
		{
			currentOffice.state = buildString;
		}
		else if ([currentElement isEqualToString:@"Country"])
		{
			currentOffice.country = buildString;
		}
        else if ([currentElement isEqualToString:@"Address"])
		{
			currentOffice.streetAddress = buildString;
		}
	}
    else if ([currentElement isEqualToString:@"Message"])
    {
        if (isRefundableInfo == YES)
            self.nonRefundableMsg = buildString;
    }
    else if ([currentElement isEqualToString:@"ShowCheckbox"])
    {
        self.showCheckbox = ([[buildString lowercaseString] isEqualToString:@"true"])? YES: NO;
        systemConfig.showCheckbox = showCheckbox;
    }
    else if ([currentElement isEqualToString:@"RuleViolationExplanationRequired"])
    {
        systemConfig.ruleViolationExplanationRequired = ([[buildString lowercaseString] isEqualToString:@"true"])? YES: NO;
    }
}

- (void)parserDidEndDocument:(NSXMLParser *)parser
{
	if (systemConfig != nil)
		[SystemConfig setSingleton:systemConfig];
}


@end

