//
//  TripItCacheData.m
//  ConcurMobile
//
//  Created by  on 3/22/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "TripItCacheData.h"

@implementation TripItCacheData

@synthesize emailAddressList,currentEmailAddress,loginIdOfLinkedAccount,tripId,tripItOAuthRequestToken;

#pragma mark - Lifecycle Methods
-(id)init
{
    self = [super init];
	if (self)
    {
        self.emailAddressList = [NSMutableArray arrayWithObjects:nil];
        self.currentEmailAddress = nil;
    }
	return self;
}


#pragma mark - Helpers
-(NSArray*) getPreVerifiedUserIds
{
    if (emailAddressList != nil && [emailAddressList count] > 0)
    {
        NSMutableArray* list = [NSMutableArray array];
        for (TripItEmailAddress* tripItEmailAddress in emailAddressList)
        {
            [list addObject:tripItEmailAddress.emailAddress];
        }
        return list;
    }
    return nil;
}

#pragma mark - Message Creation

- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
//    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSString* cacheKey = parameterBag[@"CACHE_KEY"];

	self.path = [NSString stringWithFormat:@"%@/mobile/mobilesession/gettripitcachedata/%@",[ExSystem sharedInstance].entitySettings.uri, cacheKey];
	
	Msg* msg = [[Msg alloc] initWithData:GET_TRIPIT_CACHE_DATA State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setMethod:@"GET"];
	return msg;
}

#pragma mark - Parsing Methods
//<ActionStatus xmlns="http://schemas.datacontract.org/2004/07/Snowbird" xmlns:i="http://www.w3.org/2001/XMLSchema-instance"><Status>FAILURE</Status></ActionStatus>
//<TripItCacheResult xmlns:i="http://www.w3.org/2001/XMLSchema-instance"><TripItEmailAddresses><TripItEmailAddress><EmailAddress>my@mycompany.com</EmailAddress><MatchesLoginId>false</MatchesLoginId></TripItEmailAddress></TripItEmailAddresses></TripItCacheResult>
- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
    [super parserDidStartDocument:parser];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];

	if ([elementName isEqualToString:@"TripItEmailAddress"])
	{
        self.currentEmailAddress = [[TripItEmailAddress alloc] init];
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    
	if ([elementName isEqualToString:@"TripItEmailAddress"])
	{
        [self.emailAddressList addObject:self.currentEmailAddress];
        self.currentEmailAddress = nil;
   }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
 	if (currentEmailAddress != nil)
	{
		if ([currentElement isEqualToString:@"EmailAddress"])
			currentEmailAddress.emailAddress = string;
        else if ([currentElement isEqualToString:@"MatchesLoginId"])
            currentEmailAddress.emailAddressMatchesLoginId = [string isEqualToString:@"true"];
	}
    else if ([currentElement isEqualToString:@"LoginIdOfLinkedAccount"])
        self.loginIdOfLinkedAccount = string;
    else if ([currentElement isEqualToString:@"TripId"])
        self.tripId = string;
    else if ([currentElement isEqualToString:@"TripItOAuthRequestToken"])
        self.tripItOAuthRequestToken = string;
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
    [super parserDidEndDocument:parser];
    /*
	if (userConfig != nil)
    {
        [userConfig sortCreditCardsByUsage];
		[UserConfig setSingleton:userConfig];
    }
    */
}

@end

@implementation TripItEmailAddress

@synthesize emailAddress, emailAddressMatchesLoginId;

#pragma mark - Lifecycle Methods
@end