//
//  GovStampTMDocumentData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovStampTMDocumentData.h"
#import "EntityGovDocument.h"
#import "GovDocumentManager.h"
#import "GovDocumentsData.h"

@implementation GovStampTMDocumentData
@synthesize docName, docType, sigkey, stampName, travid, status, currentDoc;
@synthesize comments, reasonCode, returnTo;
@synthesize managedObjectContext=__managedObjectContext;

//
//<StampTMDocumentRequest>
//<docName>ernest exception</docName>
//<docType>GVJ</docType>
//<sigkey>outtask1</sigkey>
//<stampName>PREPARED</stampName>
//<travid>14085142</travid>
//</StampTMDocumentRequest>

-(NSString *)getMsgIdKey
{
	return GOV_STAMP_TM_DOCUMENTS;
}


-(NSString *)makeXMLBody
{//knows how to make a post
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<StampTMDocumentRequest>"];
    if ([self.comments length])
    {
        [bodyXML appendString:[NSString stringWithFormat:@"<comments>%@</comments>", [NSString stringByEncodingXmlEntities:self.comments]]];
    }
    
	[bodyXML appendString:@"<docName>%@</docName>"];
	[bodyXML appendString:@"<docType>%@</docType>"];

    if ([ self.reasonCode length])
    {
        [bodyXML appendString:[NSString stringWithFormat:@"<reasonCode>%@</reasonCode>", [NSString stringByEncodingXmlEntities:self.reasonCode]]];
    }
    if ([self.returnTo length])
    {
        [bodyXML appendString:[NSString stringWithFormat:@"<returnTo>%@</returnTo>", [NSString stringByEncodingXmlEntities:self.returnTo]]];
    }
        
	[bodyXML appendString:@"<sigkey>%@</sigkey>"];
	[bodyXML appendString:@"<stampName>%@</stampName>"];
	[bodyXML appendString:@"<travid>%@</travid>"];

    [bodyXML appendString:@"</StampTMDocumentRequest>"];
    
	NSString* formattedBodyXml = nil;
	
	formattedBodyXml = [NSString stringWithFormat:bodyXML,
						[NSString stringByEncodingXmlEntities:self.docName], [NSString stringByEncodingXmlEntities:self.docType], [NSString stringByEncodingXmlEntities:self.sigkey], [NSString stringByEncodingXmlEntities:self.stampName], [NSString stringByEncodingXmlEntities:self.travid]];
	
//	NSLog(@"%@", formattedBodyXml);
	return formattedBodyXml;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.docType = [parameterBag objectForKey:@"DOC_TYPE"];
    self.docName = [parameterBag objectForKey:@"DOC_NAME"];
    self.sigkey = [parameterBag objectForKey:@"SIG_KEY"];
    self.stampName = [parameterBag objectForKey:@"STAMP_NAME"];
    self.travid = [parameterBag objectForKey:@"TRAVELER_ID"];
    
    self.reasonCode = [parameterBag objectForKey:@"REASON_CODE"];
    self.comments = [parameterBag objectForKey:@"COMMENTS"];
    self.returnTo = [parameterBag objectForKey:@"RETURN_TO"];
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/StampTMDocument",[ExSystem sharedInstance].entitySettings.uri];
    
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
    [self saveContext];
    __managedObjectContext = nil;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
	if ([elementName isEqualToString:@"ActionStatus"])
	{//alloc the trip instance
        self.status = [[ActionStatus alloc] init];
	}
    else if ([elementName isEqualToString:@"Document"])
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
    if ([currentElement isEqualToString:@"Status"])
	{
		[self.status setStatus:buildString];
	}
    else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		[self.status setErrMsg:buildString];
	}
    else
    {
        [GovDocumentsData fillDocListInfo:self.currentDoc withName:currentElement withValue:buildString];
    }
}

@end
