//
//  TripItExpenseTripData.m
//  ConcurMobile
//
//  Created by  on 4/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "TripItExpenseTripData.h"

@implementation TripItExpenseTripData

@synthesize rptKey, actionStatus;

-(NSString *)getMsgIdKey
{
	return EXPENSE_TRIPIT_TRIP;
}

#pragma mark - Lifecycle Methods
-(id)init
{
	if (self = [super init])
    {
        self.rptKey = nil;
    }
	return self;
}


#pragma mark - Message Creation

- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.path = [NSString stringWithFormat:@"%@/mobile/tripit/ExpenseTripItTrip",[ExSystem sharedInstance].entitySettings.uri];
    
    BOOL isExpenseByItinLocator = (parameterBag[@"ItinLocatorId"] != nil?YES:NO);
    if(isExpenseByItinLocator)
        self.path = [NSString stringWithFormat:@"%@/mobile/itinerary/ExpenseTripByItinLocator",[ExSystem sharedInstance].entitySettings.uri];
    
	Msg* msg = [[Msg alloc] initWithData:EXPENSE_TRIPIT_TRIP State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"application/xml"];
    [msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setMethod:@"POST"];
    
    if(!isExpenseByItinLocator)
        [msg setBody:[self makeXMLBody:parameterBag]];
    else {
        [msg setBody:[self makeItinLocatorXMLBody:parameterBag[@"ItinLocatorId"]]];
    }
    return msg;
}

-(NSString *)makeItinLocatorXMLBody:(NSString*)locatorId
{    
    NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<ExpenseTrip>"];
	[bodyXML appendString:@"<TripId>%@</TripId>"];
	[bodyXML appendString:@"</ExpenseTrip>"];
	
	NSString* formattedBodyXml = [NSString stringWithFormat:bodyXML, locatorId];
	
	
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"TripItExpenseTripData::makeXMLBody: body is %@", formattedBodyXml] Level:MC_LOG_DEBU];
    
	return formattedBodyXml;
}

-(NSString *)makeXMLBody:(NSMutableDictionary*)pBag
{
    int tripId = [pBag[@"TRIPIT_TRIPID"] intValue];
    
    //<ExpenseTripMobileParams xmlns:i="http://www.w3.org/2001/XMLSchema-instance"><TripId>123</TripId></ExpenseTripMobileParams>
    
    NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<ExpenseTripParams>"];
	[bodyXML appendString:@"<TripId>%i</TripId>"];
	[bodyXML appendString:@"</ExpenseTripParams>"];
	
	NSString* formattedBodyXml = [NSString stringWithFormat:bodyXML,
								  tripId
								  ];
	
	
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"TripItExpenseTripData::makeXMLBody: body is %@", formattedBodyXml] Level:MC_LOG_DEBU];
    
	return formattedBodyXml;
}

#pragma mark - Parsing Methods

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.actionStatus = [[ActionStatus alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
	if ([currentElement isEqualToString:@"Status"])
	{
		self.actionStatus.status = buildString;
	}	
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.actionStatus.errMsg = buildString;
	}
    else if ([currentElement isEqualToString:@"RptKey"])
    {
        self.rptKey = buildString;
    }
}

@end
