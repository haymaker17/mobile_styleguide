//
//  GovDocInfoFromTripLocatorData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 2/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovDocInfoFromTripLocatorData.h"
#import "GovDocumentsData.h"

@implementation GovDocInfoFromTripLocatorData
@synthesize tanum, travid, tripLocator, currentDoc;
@synthesize managedObjectContext=__managedObjectContext;
@synthesize currentDocType, currentDocName;

-(NSString *)getMsgIdKey
{
	return GOV_DOC_INFO_FROM_TRIP_LOCATOR;
}

-(NSString *)makeXMLBody
{//knows how to make a post
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<TMTripLocatorRequest>"];
	[bodyXML appendString:@"<tanum>%@</tanum>"];
	[bodyXML appendString:@"<travid>%@</travid>"];
	[bodyXML appendString:@"<tripLocator>%@</tripLocator>"];
    
    [bodyXML appendString:@"</TMTripLocatorRequest>"];
    
	NSString* formattedBodyXml = nil;
	
	formattedBodyXml = [NSString stringWithFormat:bodyXML,
						[NSString stringByEncodingXmlEntities:self.tanum], [NSString stringByEncodingXmlEntities:self.travid], [NSString stringByEncodingXmlEntities:self.tripLocator]];
	
	return formattedBodyXml;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.tanum = [parameterBag objectForKey:@"TA_NUM"];
    self.tripLocator = [parameterBag objectForKey:@"TRIP_LOCATOR"];
    self.travid = [parameterBag objectForKey:@"TRAVELER_ID"];

    if (self.travid == nil)
        self.travid = @"";
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/GetTMDocInfoFromTripLocator",[ExSystem sharedInstance].entitySettings.uri];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	
	return msg;
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


- (void)parserDidStartDocument:(NSXMLParser *)parser
{
    [super parserDidStartDocument:parser];
}


-(void) parserDidEndDocument:(NSXMLParser *)parser
{
    [super parserDidEndDocument:parser];
    // Update the document in core data
    self.currentDoc.needsStamping = [NSNumber numberWithBool:YES];
    self.currentDoc.authForVch = [NSNumber numberWithBool:NO];
    // MOB-12353 the currentDoc core data object cannot be passed to main thread, hence store the key variables in member string variables.
    self.currentDocName = self.currentDoc.docName;
    self.currentDocType = self.currentDoc.docType;
    
    [self saveContext];
    __managedObjectContext = nil;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
    if ([elementName isEqualToString:@"Document"])
    {
        self.currentDoc = [NSEntityDescription insertNewObjectForEntityForName:@"EntityGovDocument" inManagedObjectContext:self.managedObjectContext];
    }
}
//<?xml version="1.0"?>
//<ApproveResponse xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
//<ApproveResponseRow>
//<ErrorFlag>NO</ErrorFlag>
//<ErrorDesc/>
//</ApproveResponseRow>
//</ApproveResponse>
- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    [GovDocumentsData fillDocListInfo:self.currentDoc withName:currentElement withValue:buildString];
}

@end
