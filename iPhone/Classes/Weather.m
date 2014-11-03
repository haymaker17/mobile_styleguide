//
//  weather.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/30/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "Weather.h"
#import "MobileAlertView.h"

@implementation Weather

@synthesize imgTemp;
@synthesize temperature;
@synthesize condition;
@synthesize imgWeather;
@synthesize path, descript, buildString, aForecast, nextDay, nextNextDay, conditionCode;


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	////NSLog(@"Weather::respondToXMLData");
	
	[self parseXMLFileAtData:data];
}


//-(void) init:(MsgControl *)msgControl mainRootViewController:(RootViewController *)mainRootVC ParameterBag:(NSMutableDictionary *)parameterBag
//{	
//	////NSLog(@"WOEID=%@", mainRootVC.findMe.woeid);
//	self.path = [NSString stringWithFormat:@"http://weather.yahooapis.com/forecastrss?w=%@&u=f", mainRootVC.findMe.woeid];
//	//	self.msg = [Msg alloc];
//	//	[self.msg init:@"WEATHER" State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
//}


- (void)parseXMLFileAtURL:(NSString *)URL 
{
	//you must then convert the path to a proper NSURL or it won't work
	NSURL *xmlURL = [NSURL URLWithString:URL];
	dataParser = [[NSXMLParser alloc] initWithContentsOfURL:xmlURL];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}


//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	dataParser = [[NSXMLParser alloc] initWithData:webData];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	////NSLog(@"found file and started parsing");
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
//	NSString * errorString = [NSString stringWithFormat:@"Unable to download weather feed from web site (Error code %i )", [parseError code]];
	//NSLog(@"weather, error parsing XML: %@", errorString);
	
//	UIAlertView * errorAlert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Error loading content"]  message:errorString delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"]  otherButtonTitles:nil];
//	[errorAlert show];
//	[errorAlert release];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	//NSLog(@"found this element: %@", elementName);
	currentElement = [elementName copy];
	////NSLog(@"weather didStartElement::elementName %@", elementName);
	
	
	self.buildString = [[NSMutableString alloc] init];
	
	if ([elementName isEqualToString:@"channel"]) {
		// clear out our story item caches...
		channel = [[NSMutableDictionary alloc] init];
		self.condition = [[NSMutableString alloc] init];
		self.conditionCode = [[NSMutableString alloc] init];
		temp = [[NSMutableString alloc] init];
	}
	
	if ( [elementName isEqualToString:@"yweather:forecast"]) 
	{
		if(aForecast == nil)
			self.aForecast = [[NSMutableArray alloc] initWithObjects:nil];
		
		//NSLog(@"attributes size = %d", [attributeDict count]);
		NSMutableDictionary	*dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		for(NSString * key in attributeDict)
		{
			NSString *val = attributeDict[key];
			//NSLog(@"forecastKey = %@, forecastVal = %@", key, val);
			dict[key] = val;
		}
		[aForecast addObject:dict];
		
		if([aForecast count] == 1)
			nextDay = dict[@"day"];
		else 
			nextNextDay = dict[@"day"];
		
	}
	else if ( [elementName isEqualToString:@"yweather:condition"]) 
	{
		NSString *thisCondition = attributeDict[@"text"]; 
		if (thisCondition)
			condition = thisCondition; 
		
		NSString *thisCode = attributeDict[@"code"]; 
		 
		if (thisCode)
		{
			self.conditionCode = thisCode;
			////NSLog(@"Weather Conditions Retrieved!");	
			
			//NSRange rng = [conditionCode rangeOfString: @"Rain"];
			if (conditionCode != nil)
			{
				int code = [conditionCode intValue];
				if (code == 11)
				{
					imgWeather = [UIImage imageNamed:@"weather_light_rain2.png"];
					
				}
				else if (code == 12)
				{
					imgWeather = [UIImage imageNamed:@"weather_showers2.png"];
					
				}
				else if ((code >= 13 && code <= 16) || (code >= 41 && code <= 43))
				{
					imgWeather = [UIImage imageNamed:@"weather_snow2.png"];
					
				}
				else if ((code >= 18 && code <= 24))
				{
					imgWeather = [UIImage imageNamed:@"weather_fog.png"];
					
				}
				else if (code == 26 || code == 28 || code == 30)
				{
					imgWeather = [UIImage imageNamed:@"weather_cloudy2.png"];
					
				}
				else if (code == 27 || code == 27)
				{
					imgWeather = [UIImage imageNamed:@"weather_cloudy_night.png"];
					
				}
				else if (code == 31 || code == 33)
				{
					imgWeather = [UIImage imageNamed:@"weather_clear_night.png"];
					
				}
				else if (code == 32 || code == 34)
				{
					imgWeather = [UIImage imageNamed:@"weather_clear2.png"];
					
				}
				imgTemp.image = imgWeather;
				////NSLog(@"Weather Image made");
			}
			
		}
		
		NSString *thisTemp = attributeDict[@"temp"]; 
		if (thisTemp)
		{
			temp = thisTemp;
			////NSLog(@"Weather Temperature Retrieved!");
			NSString *s = [temp stringByAppendingString:@"\xC2\xB0"];
			s = [NSString stringWithFormat:@"%@F", s];
			temperature = s;
		}
		return;
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	if ([elementName isEqualToString:@"channel"]) {
		item[@"condition"] = condition;
		item[@"conditionCode"] = conditionCode;
		item[@"temp"] = temp;
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	//NSLog(@"Weather found [%@] characters: %@", currentElement, string);
	//NSLog(@"string len=%d", [string length]);
	// save the characters for the current item...
	////NSLog(@"weather currentElement: %@, and string:%@", currentElement, string);
	if ([currentElement isEqualToString:@"LocalityName"])// && [thisId isEqualToString:@"p1"]) 
	{
		NSComparisonResult compareResult;
		compareResult = [thisId compare: @"p1"];
		////NSLog(compareResult);
		if (compareResult == NSOrderedSame)
		{
			//NSLog(@"The Same");
			address = string;
			//NSLog(@"thisId is %@", thisId);
			//NSLog(@"LocalityName is %@", address);
		}
		else {
			//NSLog(@"Not the same and string is %@", string);
		}
	}
	else if ([currentElement isEqualToString:@"description"])
	{
		[buildString appendString:string];
		self.descript = buildString;
	}
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}


-(NSString *) getForecastTomorrow
{
	if([aForecast count] < 1)
		return nil;
	
	NSMutableDictionary *dict = aForecast[0];
	NSString *s = [NSString stringWithFormat:@"%@ H: %@\u02DAF L: %@\u02DAF", dict[@"text"], dict[@"high"], dict[@"low"]];
	
	return s;
	
}


-(NSString *) getForecastNextNext
{
	if([aForecast count] < 2)
		return nil;
	
	NSMutableDictionary *dict = aForecast[1];
	NSString *s = [NSString stringWithFormat:@"%@ H: %@\u02DAF L: %@\u02DAF", dict[@"text"], dict[@"high"], dict[@"low"]];
	
	return s;
	
}


@end
