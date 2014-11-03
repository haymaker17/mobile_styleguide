//
//  Weather.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/30/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "XMLBase.h"
#import "MsgResponder.h"
#import "ExSystem.h" 

#import "MsgControl.h"
#import "Msg.h"

@interface Weather : NSObject <NSXMLParserDelegate>
{
	UIActivityIndicatorView * activityIndicator;
	NSXMLParser				* dataParser;
	NSMutableArray			* stories;
	
	// a temporary item; added to the "stories" array one at a time, and cleared for the next one
	NSMutableDictionary		* item;
	NSMutableDictionary		* channel;
	
	// it parses through the document, from top to bottom...
	// we collect and cache each sub-element value, and then save each item to our array.
	// we use these to track each current item, until it's ready to be added to the "stories" array
	NSString				* currentElement, *conditionCode;
	NSString                *descript, *condition, *temp, *thisId, *address, *temperature;
	NSMutableString			*buildString;
	
	UIImageView				*imgTemp;
	UIImage					*imgWeather;
	
	NSString				*path;
	
	NSString				*pubDate, *latPos, *longPos, *title, *woeid, *imgYahoo, *currentCondition, *nextDay, *nextNextDay;
	NSMutableArray			*aForecast;
}

@property (nonatomic, strong) UIImageView *imgTemp;
@property (nonatomic, strong) NSString *temperature;
@property (nonatomic, strong) NSString *condition;
@property (nonatomic, strong) NSString *conditionCode;
@property (nonatomic, strong) UIImage *imgWeather;
@property (nonatomic, strong) NSString *path;
@property (nonatomic, strong) NSString *descript;
@property (nonatomic, strong) NSMutableString *buildString;
@property (nonatomic, strong) NSMutableArray			*aForecast;

@property (nonatomic, strong) NSString *nextDay;
@property (nonatomic, strong) NSString *nextNextDay;

- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
- (void)parseXMLFileAtData:(NSData *)webData;

-(NSString *) getForecastTomorrow;
-(NSString *) getForecastNextNext;
@end

