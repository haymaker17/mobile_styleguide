//
//  DownloadUserConfig.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "DownloadUserConfig.h"
#import "CreditCard.h"
#import "CarType.h"
#import "UserConfig.h"
#import "AffinityProgram.h"
#import "AttendeeType.h"
#import "Policy.h"
#import "ExpenseConfirmation.h"

@interface DownloadUserConfig()
@property (nonatomic) BOOL inTravelPointsConfig;
@end

@implementation DownloadUserConfig

@synthesize currentCarType;
@synthesize userConfig;
@synthesize allowFor;
@synthesize defaultFor;
@synthesize curClassOfServices;
@synthesize curAttendeeType, curExpensePolicy, curExpenseConfirmation;
@synthesize curYodleePaymentType;

-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	[self parseXMLFileAtData:data];
}


- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.path = [NSString stringWithFormat:@"%@/mobile/Config/UserConfigV2",[ExSystem sharedInstance].entitySettings.uri];
	
	Msg* msg = [[Msg alloc] initWithData:DOWNLOAD_USER_CONFIG State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setMethod:@"GET"];
	return msg;
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
    [super parserDidStartDocument:parser];
	self.userConfig = [[UserConfig alloc] init];  
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	
    if ([elementName isEqualToString:@"AttendeeType"])
    {
        self.curAttendeeType = [[AttendeeType alloc] init];
    }
    else if ([elementName isEqualToString:@"Policy"])
    {
        self.curExpensePolicy = [[Policy alloc] init];
    }
    else if ([elementName isEqualToString:@"ExpenseConfirmation"])
    {
        self.curExpenseConfirmation = [[ExpenseConfirmation alloc] init];
    }
    else if ([elementName isEqualToString:@"AllowedAirClassesOfService"])
    {
        self.curClassOfServices = [[NSString alloc] init];
    }
    else if ([elementName isEqualToString:@"CarType"])
    {
        self.currentCarType = [[CarType alloc] init];
    }
    else if ([elementName isEqualToString:@"YodleePaymentTypes"])
    {
        inYodleePaymentTypes = YES;
    }
    else if ([elementName isEqualToString:@"TravelPointsConfig"])
    {
        self.inTravelPointsConfig = YES;
    }
    else if (inYodleePaymentTypes && [elementName isEqualToString:@"ListItem"])
    {
        self.curYodleePaymentType = [[ListItem alloc] init];
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];

    if (currentCarType != nil)
    {
        if ([elementName isEqualToString:@"CarType"]) {
            [userConfig.allowedCarTypes addObject:currentCarType];
            currentCarType = nil;
        }
    }
    else if ([elementName isEqualToString:@"TravelPointsConfig"])
    {
        self.inTravelPointsConfig = NO;
    }
    else if (curClassOfServices != nil && [elementName isEqualToString:@"AllowedAirClassesOfService"])
    {
        [DownloadUserConfig populateArray:userConfig.classOfServices FromSpaceDelimitedList:curClassOfServices];
        self.curClassOfServices = nil;
    }
    else if (curAttendeeType != nil && [elementName isEqualToString:@"AttendeeType"])
    {
        (userConfig.attendeeTypes)[self.curAttendeeType.atnTypeKey] = curAttendeeType;
        curAttendeeType = nil;
    }
    else if (curExpensePolicy != nil && [elementName isEqualToString:@"Policy"])
    {
        (userConfig.expensePolicies)[self.curExpensePolicy.polKey] = curExpensePolicy;
        curExpensePolicy = nil;
    }
    else if (curExpenseConfirmation != nil && [elementName isEqualToString:@"ExpenseConfirmation"])
    {
        (userConfig.expenseConfirmations)[curExpenseConfirmation.confirmationKey] = curExpenseConfirmation;
        curExpenseConfirmation = nil;
    }
    else if (inYodleePaymentTypes && curYodleePaymentType != nil && [elementName isEqualToString:@"ListItem"])
    {
        [userConfig.yodleePaymentTypes addObject:curYodleePaymentType];
        curYodleePaymentType = nil;
    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if(currentCarType != nil)
    {
        if ([currentElement isEqualToString:@"Description"])
        {
            currentCarType.carTypeName = buildString;
        }
        else if ([currentElement isEqualToString:@"Code"])
        {
            currentCarType.carTypeCode = buildString;
        }
    }
    else if (self.inTravelPointsConfig)
    {
        self.userConfig.travelPointsConfig[self.currentElement] = buildString;
    }
    else if([currentElement isEqualToString:@"AllowedAirClassesOfService"])
    {
        self.curClassOfServices = buildString;
    }
    else if (curAttendeeType != nil)
    {
        NSString* propName = [AttendeeType getXmlToPropertyMap][currentElement];
        if (propName != nil)
        {
            [self.curAttendeeType setValue:buildString forKey:propName];
        }
    }
    else if (curExpensePolicy != nil)
    {
        NSString* propName = [Policy getXmlToPropertyMap][currentElement];
        if (propName != nil)
        {
            [self.curExpensePolicy setValue:buildString forKey:propName];
        }        
    }
    else if (curExpenseConfirmation != nil)
    {
        NSString* propName = [ExpenseConfirmation getXmlToPropertyMap][currentElement];
        if (propName != nil)
        {
            [self.curExpenseConfirmation setValue:buildString forKey:propName];
        }        
    }
    else if (curYodleePaymentType != nil)
    {
        if ([self.currentElement isEqualToString:@"Key"])
            curYodleePaymentType.liKey = buildString;
        else if ([self.currentElement isEqualToString:@"Text"])
            curYodleePaymentType.liName = buildString;
    }
    else if ([currentElement isEqualToString:@"ShowGDSNameInSearchResults"])
    {
        self.userConfig.showGDSNameInSearchResults = ([buildString boolValue]);
    }
}

- (void)parserDidEndDocument:(NSXMLParser *)parser
{
    [super parserDidEndDocument:parser];
	if (userConfig != nil)
    {
		[UserConfig setSingleton:userConfig];
    }
}

#pragma -
#pragma Helpers
+(void)populateDictionary:(NSMutableDictionary*)dict FromCommaDelimitedList:(NSString*)list
{
    if (list != nil && [list length] > 0)
    {
        NSArray* arrayOfUntrimmedItems = [list componentsSeparatedByString:@","];
        for (NSString* untrimmedItem in arrayOfUntrimmedItems)
        {
            NSString* trimmedItem = [untrimmedItem stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
            dict[trimmedItem] = trimmedItem;
        }
    }
}

+(void)populateArray:(NSMutableArray *)allowedItems FromSpaceDelimitedList:(NSString *)listOfAllowedClasses
{
    if (listOfAllowedClasses != nil && [listOfAllowedClasses length] > 0)
    {
        NSArray *arrayOfUntrimmedItems = [listOfAllowedClasses componentsSeparatedByCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];

        for (int i =0 ; i < [arrayOfUntrimmedItems count]; i++)
        {
            NSString *trimmedItem = [arrayOfUntrimmedItems[i] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
            [allowedItems addObject:trimmedItem];
        }
        //MOB-10508 hide the ability of using one class upgrade "mixed class" on mobile air booking.
        [allowedItems removeObject:@"OneClassUpgrade"];
    }
}

@end
