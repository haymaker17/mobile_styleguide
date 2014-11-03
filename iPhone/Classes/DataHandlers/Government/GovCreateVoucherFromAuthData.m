//
//  GovCreateVoucherFromAuthData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/19/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovCreateVoucherFromAuthData.h"
#import "GovDocumentsData.h"

@interface GovCreateVoucherFromAuthData ()
@property (nonatomic, strong) EntityGovDocument* voucherListInfo;
@end

@implementation GovCreateVoucherFromAuthData

@synthesize docName, docType, returnDocName, returnDocType, gtmDocType, travid, status;
@synthesize voucherListInfo = _voucherListInfo;
@synthesize managedObjectContext=__managedObjectContext;

//<TMDocRequest>
//<docName>TA3652</docName>
//<docType>AUTH</docType>
//<travid>13767168</travid>
//</TMDocRequest>

-(NSString *)getMsgIdKey
{
	return GOV_CREATE_VOUCHER_FROM_AUTH;
}


-(NSString *)makeXMLBody
{//knows how to make a post
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<TMDocRequest>"];
	[bodyXML appendString:@"<docName>%@</docName>"];
	[bodyXML appendString:@"<docType>%@</docType>"];
	[bodyXML appendString:@"<travid>%@</travid>"];
    [bodyXML appendString:@"</TMDocRequest>"];
    
	NSString* formattedBodyXml = nil;
	
	formattedBodyXml = [NSString stringWithFormat:bodyXML,
						[NSString stringByEncodingXmlEntities:self.docName], [NSString stringByEncodingXmlEntities:self.docType], [NSString stringByEncodingXmlEntities:self.travid]];
	
    //	NSLog(@"%@", formattedBodyXml);
	return formattedBodyXml;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.docType = [parameterBag objectForKey:@"DOC_TYPE"];
    self.docName = [parameterBag objectForKey:@"DOC_NAME"];
    self.travid = [parameterBag objectForKey:@"TRAVELER_ID"];
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/CreateVoucherFromAuth",[ExSystem sharedInstance].entitySettings.uri];
    
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
    self.voucherListInfo.needsStamping = [NSNumber numberWithBool:YES];
    self.voucherListInfo.authForVch = [NSNumber numberWithBool:NO];
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
        self.voucherListInfo = [NSEntityDescription insertNewObjectForEntityForName:@"EntityGovDocument" inManagedObjectContext:self.managedObjectContext];
    }
}

-(void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    
    if([elementName isEqualToString:@"Document"])
    {
        self.returnDocName = self.voucherListInfo.docName;
        self.returnDocType = self.voucherListInfo.docType;
        self.gtmDocType = self.voucherListInfo.gtmDocType;
    }
}
//<?xml version="1.0"?>
//<DocumentList xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
//<Document>
//<TravelerName>Traveler, Lisa</TravelerName>
//<TravelerId>14288767</TravelerId><DocTypeLabel/><DocType>VCH</DocType><GtmDocType>VCH</GtmDocType>
//<DocName>TA4214</DocName><PurposeCode/><TripBeginDate>2012-11-01</TripBeginDate><TripEndDate>2012-11-30</TripEndDate>
//<TotalExpCost>5831.0</TotalExpCost><ApproveLabel/>
//<triplength>30</triplength>
//</Document>
//</DocumentList>
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
        [GovDocumentsData fillDocListInfo:self.voucherListInfo withName:currentElement withValue:buildString];
    }
}

@end
