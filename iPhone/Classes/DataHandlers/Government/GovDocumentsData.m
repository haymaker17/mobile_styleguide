//
//  GovDocumentsData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//
//  Fetch new list of all TM documents associated to the user.
//  It preserves the existing needsStamping flag, and merges changes, i.e. updating existing,
//  deleting those no longer on the list and inserting new ones.

#import "GovDocumentsData.h"
#import "DateTimeFormatter.h"
#import "GovDocumentManager.h"

@implementation GovDocumentsData
@synthesize currentDoc, filter, existingDocs;
@synthesize managedObjectContext=__managedObjectContext;

-(NSString *)getMsgIdKey
{
	return GOV_DOCUMENTS;
}

-(void) flushData
{
    [super flushData];
    self.existingDocs = [[NSMutableDictionary alloc] init];
    __managedObjectContext = nil;
}

-(NSString*) serverEndPoint
{
    return @"GetAllTMDocuments";
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
//    GET Mobile/GovTravelManager/GetTMDocuments/{filter}
//    GET Mobile/GovTravelManager/GetAllTMDocuments/{filter}

    self.filter = [parameterBag objectForKey:@"FILTER"];
    if (![self.filter length])
        self.filter = GOV_DOC_TYPE_ALL;
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/%@/%@",[ExSystem sharedInstance].entitySettings.uri, [self serverEndPoint], self.filter];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

// No need to refresh core data if data from cache.
-(void) respondToXMLData:(NSData *)data withMsg:(Msg*)msg
{
    if (!msg.isCache)
        [super respondToXMLData:data withMsg:msg];
}

-(NSString*) keyForDoc:(EntityGovDocument*) doc
{
    return [NSString stringWithFormat:@"%@-%@", doc.travelerId, doc.docName];
}

- (void)parserDidStartDocument:(NSXMLParser *)parser
{
    [super parserDidStartDocument:parser];
    // Populate the existing list of all docs
    NSArray* allObj = [[GovDocumentManager sharedInstance] fetchDocumentsByDocType:GOV_DOC_TYPE_ALL withContext:[self managedObjectContext]];
    for (EntityGovDocument* doc in allObj)
    {
        [[self managedObjectContext] deleteObject:doc];
//        NSString *key = [self keyForDoc:doc];
//        [self.existingDocs setObject:doc forKey:key];
    }
}

- (void)cleanupAllRemainingObjects
{
    // Delete all not present
//    for (EntityGovDocument* doc in self.existingDocs.allValues)
//    {
//        [[GovDocumentManager sharedInstance] deleteObj:doc withContext:[self managedObjectContext]];
//    }
}

-(void) parserDidEndDocument:(NSXMLParser *)parser
{
    [super parserDidEndDocument:parser];
    [self cleanupAllRemainingObjects];
    
    [self saveContext];
    
    // remove observer
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    [[NSNotificationCenter defaultCenter] removeObserver:ad name:NSManagedObjectContextDidSaveNotification object:self.managedObjectContext];
    
    __managedObjectContext = nil;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
	if ([elementName isEqualToString:@"Document"])
	{//alloc the trip instance
        self.currentDoc = [NSEntityDescription insertNewObjectForEntityForName:@"EntityGovDocument" inManagedObjectContext:self.managedObjectContext];
	}
}

- (void)saveDocument
{
//    if (self.currentDoc != nil)
//    {
//        NSString *key = [self keyForDoc:self.currentDoc];
//        EntityGovDocument* existingDoc = [self.existingDocs objectForKey:key];
//        
//        if (existingDoc != nil)
//        {
//            // Copy over needsStamping flag to update existing and remove from existingDocs list
//            NSNumber *needsStamping = existingDoc.needsStamping;
//            [GovDocumentManager copyFrom:self.currentDoc to:existingDoc];
//            existingDoc.needsStamping = needsStamping;
//            
//            [self.existingDocs removeObjectForKey:key];
//            [[self managedObjectContext] deleteObject:self.currentDoc];
//        }        
//    }
    self.currentDoc = nil;
    
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
	if ([elementName isEqualToString:@"Document"])
	{
        [self saveDocument];
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    [GovDocumentsData fillDocListInfo:self.currentDoc withName:currentElement withValue:buildString];
}

+ (void) fillDocListInfo:(EntityGovDocument*) currentDoc withName:(NSString*) currentElement withValue:(NSString*)buildString
{
    if ([currentElement isEqualToString:@"TravelerName"])
	{
		[currentDoc setTravelerName:buildString];
	}
    else if ([currentElement isEqualToString:@"TravelerId"])
	{
		[currentDoc setTravelerId:buildString];
	}
    else if ([currentElement isEqualToString:@"DocTypeLabel"])
	{
		[currentDoc setDocTypeLabel:buildString];
	}
    else if ([currentElement isEqualToString:@"DocType"])
	{
		[currentDoc setDocType:buildString];
	}
    else if ([currentElement isEqualToString:@"GtmDocType"])
	{
		[currentDoc setGtmDocType:buildString];
	}
    else if ([currentElement isEqualToString:@"PurposeCode"])
	{
		[currentDoc setPurposeCode:buildString];
	}
    else if ([currentElement isEqualToString:@"DocName"])
	{
		[currentDoc setDocName:buildString];
	}
    else if ([currentElement isEqualToString:@"ApproveLabel"])
	{
		[currentDoc setApproveLabel:buildString];
	}
    else if ([currentElement isEqualToString:@"TripBeginDate"])
	{
		[currentDoc setTripBeginDate:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"TripEndDate"])
	{
		[currentDoc setTripEndDate:[DateTimeFormatter getNSDate:buildString Format:@"yyyy-MM-dd"]];
	}
    else if ([currentElement isEqualToString:@"TotalExpCost"])
	{
        NSDecimalNumber * num = [NSDecimalNumber decimalNumberWithString:buildString];
		[currentDoc setTotalExpCost:num];
	}
    else if ([currentElement isEqualToString:@"NeedsStamping"])
	{
		[currentDoc setNeedsStamping:[NSNumber numberWithBool:[@"true" isEqualToString:buildString]]];
	}
    else if ([currentElement isEqualToString:@"AuthForVch"])
	{
		[currentDoc setAuthForVch:[NSNumber numberWithBool:[@"true" isEqualToString:buildString]]];
	}
}

//<TripBeginDate>2012-02-02</TripBeginDate>
//@property (nonatomic, retain) NSString * travelerName;
//@property (nonatomic, retain) NSString * travelerId;
//@property (nonatomic, retain) NSString * docTypeLabel;
//@property (nonatomic, retain) NSString * docType;
//@property (nonatomic, retain) NSString * gtmDocType;
//@property (nonatomic, retain) NSString * purposeCode;
//@property (nonatomic, retain) NSString * docName;
//@property (nonatomic, retain) NSDate * tripBeginDate;
//@property (nonatomic, retain) NSDate * tripEndDate;
//@property (nonatomic, retain) NSDecimalNumber * totalExpCost;
//@property (nonatomic, retain) NSString * approveLabel;


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
        
        // Add observer
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        [[NSNotificationCenter defaultCenter] addObserver:ad selector:@selector(processNotification:) name:NSManagedObjectContextDidSaveNotification object:self.managedObjectContext];
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
