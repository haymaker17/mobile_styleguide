//
//  ReceiptStoreListData.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/8/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReceiptStoreListData.h"
#import "DataConstants.h"
#import "EntityReceiptInfoExtension.h"

@interface ReceiptStoreListData ()
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@end

@implementation ReceiptStoreListData
@synthesize receiptInfo,receiptObjects,dataParser,currentElement,path,buildString,status,obsoleteReceipts;
@synthesize managedObjectContext = _managedObjectContext;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	if (receiptObjects != nil) 
	{
		self.receiptObjects = nil;
	}
	self.receiptObjects = [[NSMutableArray alloc] initWithObjects:nil];
	
	dataParser = [[NSXMLParser alloc] initWithData:webData];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	[self parseXMLFileAtData:data];
}

-(id)init
{
    self = [super init];
	if(self)
	{
		self.currentElement = @"";
		self.path = nil;
		self.receiptInfo = nil;
	}
	return self;
}


-(NSString *)getMsgIdKey
{
	return RECEIPT_STORE_RECEIPTS;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	NSString *filterMobileExpense = parameterBag[@"FILTER_MOBILE_EXPENSE"];
	
	self.path = [NSString stringWithFormat:@"%@/Mobile/Expense/GetReceiptImageUrlsV2/%@",[ExSystem sharedInstance].entitySettings.uri,filterMobileExpense];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}


- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSPersistentStoreCoordinator *coordinator = [ad persistentStoreCoordinator];
    if (coordinator != nil)
    {
        self.managedObjectContext = [[NSManagedObjectContext alloc] init];
        [self.managedObjectContext setPersistentStoreCoordinator:coordinator];
        
        // Add observer
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        [[NSNotificationCenter defaultCenter] addObserver:ad
                                                 selector:@selector(processNotification:)
                                                     name:NSManagedObjectContextDidSaveNotification
                                                   object:self.managedObjectContext];
        
        // Get keys of all items in core data BEFORE we put in the new ones.
        NSArray *receiptsInCoreData = [EntityReceiptInfo fetchAllInContext:self.managedObjectContext];
        if (receiptsInCoreData != nil && receiptsInCoreData.count > 0)
        {
            self.obsoleteReceipts = [[NSMutableDictionary alloc] initWithCapacity:receiptsInCoreData.count];
            for (EntityReceiptInfo* receiptInCoreData in receiptsInCoreData)
            {
                if (receiptInCoreData.receiptId != nil)
                {
                    (self.obsoleteReceipts)[receiptInCoreData.receiptId] = receiptInCoreData.receiptId;
                }
            }
        }
    }
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	self.buildString = [[NSMutableString alloc] init];
	
	self.currentElement = elementName;
	
	if ([elementName isEqualToString:@"ReceiptInfo"])
	{		
		self.receiptInfo = [[ReceiptStoreReceipt alloc] init];
	}
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	if ([elementName isEqualToString:@"ReceiptInfo"])
	{
		[receiptObjects addObject:receiptInfo];
        
       [EntityReceiptInfo updateOrCreateFromReceiptStoreReceipt:receiptInfo inContext:self.managedObjectContext];
	}
    
    if (receiptInfo.receiptImageId != nil && self.obsoleteReceipts != nil)
    {
        [self.obsoleteReceipts removeObjectForKey:receiptInfo.receiptImageId];
    }
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	if ([currentElement isEqualToString:@"FileName"])
	{
		[buildString appendString:string];
		[receiptInfo setFileName:buildString];
	}
	else if ([currentElement isEqualToString:@"FileType"])
	{
		[buildString appendString:string];
		[receiptInfo setFileType:buildString];
	}
	else if ([currentElement isEqualToString:@"ImageDate"])
	{
		[buildString appendString:string];
		[receiptInfo setImageDate:buildString];
	}
	else if ([currentElement isEqualToString:@"ImageOrigin"])
	{
		[buildString appendString:string];
		[receiptInfo setImageOrigin:buildString];
	}
	else if ([currentElement isEqualToString:@"ImageUrl"])
	{
		[buildString appendString:string];
		[receiptInfo setImageUrl:buildString];
	}
	else if ([currentElement isEqualToString:@"ReceiptImageId"])
	{
		[buildString appendString:string];
		[receiptInfo setReceiptImageId:buildString];
	}
	else if ([currentElement isEqualToString:@"ThumbUrl"])
	{
		[buildString appendString:string];
		[receiptInfo setThumbUrl:buildString];
	}
	else if ([currentElement isEqualToString:@"Status"])
	{
		[buildString appendString:string];
		[self setStatus:buildString];
	}
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
    // Delete any old receipts that are no longer in the list.
    if (self.obsoleteReceipts != nil)
    {
        for (NSString *obsoleteReceiptId in self.obsoleteReceipts)
        {
            //NSLog(@"OB: Deleting %@ from core data", obsoleteReceiptId);
            [EntityReceiptInfo deleteByImageId:obsoleteReceiptId inContext:self.managedObjectContext];
        }
    }

    NSError *error = nil;
    if (self.managedObjectContext != nil)
    {
        if ([self.managedObjectContext hasChanges] && ![self.managedObjectContext save:&error])
        {
            NSLog(@"Unresolved error in ReceiptStoreListData::parserDidEndDocument %@, %@", error, [error userInfo]);
            abort();
        }
        
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        [[NSNotificationCenter defaultCenter] removeObserver:ad name:NSManagedObjectContextDidSaveNotification object:self.managedObjectContext];
    }
}




@end
