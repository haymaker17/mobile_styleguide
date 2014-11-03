//
//  OpportunitesData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "OpportunitesData.h"
#import "SalesForceCOLAManager.h"


@interface OpportunitesData (Private)
- (void)saveContext;

@end

@implementation OpportunitesData
@synthesize city, opp;
@synthesize managedObjectContext=__managedObjectContext;

/*<SalesForceOpportunity>
 <AccountAddress>150 Chestnut Street, Toronto, Ontario L4B 1Y3, Canada</AccountAddress>
 <AccountCity>Toronto, Ontario</AccountCity>
 <AccountName>Global Media</AccountName>
 <ContactName>Geoff Minor</ContactName>
 <OpportunityAmount>500000.0</OpportunityAmount>
 <OpportunityName>salesforce.com - 5000 Widgets</OpportunityName>
 </SalesForceOpportunity>*/




-(NSString *)getMsgIdKey
{
	return SALES_OPPORTUNITIES_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.city = [parameterBag objectForKey:@"CITY"];
    
    self.path = [NSString stringWithFormat:@"%@/mobile/SalesForce/GetOpportunities",[ExSystem sharedInstance].entitySettings.uri];
    if ([self.city length])
    {
        NSString *cityEncoded = (__bridge_transfer NSString *)CFURLCreateStringByAddingPercentEscapes(
                                                                               NULL,
                                                                               (CFStringRef)city,
                                                                               NULL,
                                                                               (CFStringRef)@" !*'();:@&=+$,/?%#[]",
                                                                               kCFStringEncodingUTF8 );
        self.path = [NSString stringWithFormat:@"%@/%@", self.path, cityEncoded];
    }
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

-(void) flushData
{
    if ([self.city length])
        [[SalesForceCOLAManager sharedInstance] deleteOpportunitiesByCity:self.city withContext:[self managedObjectContext]];
}



- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
    [super parserDidStartDocument:parser];
}

-(void) parserDidEndDocument:(NSXMLParser *)parser
{
    [super parserDidEndDocument:parser];
    [self saveContext];
    __managedObjectContext = nil;
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
    
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
	if ([elementName isEqualToString:@"SalesForceOpportunity"])
	{//alloc the trip instance
        self.opp = [NSEntityDescription insertNewObjectForEntityForName:@"EntitySalesOpportunity" inManagedObjectContext:[self managedObjectContext]];
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
	if ([elementName isEqualToString:@"SalesForceOpportunity"]) 
	{
		if (opp != nil)
		{
            NSError *error;
            if (![[self managedObjectContext] save:&error])
                NSLog(@"Whoops, couldn't save object: %@", [error localizedDescription]);    
		}
        self.opp = nil;
	}
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    if ([currentElement isEqualToString:@"AccountAddress"])
	{
		[opp setAccountAddress:buildString];
	}
    else if ([currentElement isEqualToString:@"AccountCity"])
	{
		[opp setAccountCity:buildString];
	}
    else if ([currentElement isEqualToString:@"AccountName"])
	{
		[opp setAccountName:buildString];
	}
    else if ([currentElement isEqualToString:@"ContactName"])
	{
		[opp setContactName:buildString];
	}
//    else if ([currentElement isEqualToString:@"ContactId"])
    // TODO - create ContactImageUrl in EntitySalesOpportunity
    else if ([currentElement isEqualToString:@"ContactImageUrl"])
	{
		[opp setContactId:buildString];
	}
    else if ([currentElement isEqualToString:@"OpportunityName"])
	{
		[opp setOpportunityName:buildString];
	}
    else if ([currentElement isEqualToString:@"OpportunityId"])
	{
		[opp setOpportunityId:buildString];
	}
    else if ([currentElement isEqualToString:@"OpportunityAmount"])
	{
        NSDecimalNumber * num = [NSDecimalNumber decimalNumberWithString:buildString];
		[opp setOpportunityAmount:num];
	}
}

#pragma mark - Context
/**
 Returns the managed object context for the application.
 If the context doesn't already exist, it is created and bound to the persistent store coordinator for the application.
 */
- (NSManagedObjectContext *)managedObjectContext
{
    if (__managedObjectContext != nil)
    {
        return __managedObjectContext;
    }
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSPersistentStoreCoordinator *coordinator = [ad persistentStoreCoordinator];
    if (coordinator != nil)
    {
        __managedObjectContext = [[NSManagedObjectContext alloc] init];
        [__managedObjectContext setPersistentStoreCoordinator:coordinator];
    }
    return __managedObjectContext;
}

- (void)saveContext
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = __managedObjectContext;
    if (managedObjectContext != nil)
    {
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
        {
            /*
             Replace this implementation with code to handle the error appropriately.
             
             abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. If it is not possible to recover from the error, display an alert panel that instructs the user to quit the application by pressing the Home button.
             */
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
        } 
    }
}
@end
