//
//  MobileExpenseList.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/22/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//
// Description
//  This class is the xml parser for the expense list.
//  Gets the list of expenses and writes them to coredata.
//  entity.key is either pctkey or cctkey or mekey so that there is a unique key always
//
// MOB-13688 - Added support for personal card transactions
//
/*
 Sample xml for personal Card transactions
 <PersonalCards>
    <PersonalCard>
    <AccountNumberLastFour>1111</AccountNumberLastFour>
    <CardName>Test Creditcard Special Case - Credit Card - My Account</CardName>
    <CrnCode>USD</CrnCode>
    <PcaKey>nxdvCR2hkDfgUmNefa1sOxiE</PcaKey>
    <Transactions>
        <PersonalCardTransaction>
            <Amount>5.00000000</Amount>
            <Category>Uncategorized</Category>
            <DatePosted>2013-06-03T00:00:00</DatePosted>
            <Description>Moja vec 	01-02-03-04-05-	06-07-08-09-10-11-12-13-</Description>
            <ExpKey>UNDEF</ExpKey>
            <ExpName>Undefined</ExpName>
            <PctKey>n08tf$s$s7zr4sizTTgiso5mv0</PctKey>
            <Status>UN</Status>
        </PersonalCardTransaction>
    </Transactions>
 </PersonalCard>
 </PersonalCards>

 */
#import "MobileExpenseList.h"
#import "MobileEntryManager.h"
#import "DataConstants.h"


@interface MobileExpenseList ()
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, strong) NSMutableDictionary       *obsoleteEntries;
@property (nonatomic, strong) NSMutableArray			*entryKeys;
@property (strong,nonatomic) MobileEntryManager *mobileEntryManager;

@end


@implementation MobileExpenseList

@synthesize currentElement, path, buildString, obsoleteEntries, entryKeys, entity;
@synthesize managedObjectContext = _managedObjectContext;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData
{
	NSXMLParser* dataParser = [[NSXMLParser alloc] initWithData:webData];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:YES];
	[dataParser setShouldReportNamespacePrefixes:YES];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	
	[self flushData];
	[self parseXMLFileAtData:data];
}

-(id)init
{
    self = [super init];
	if (self)
    {
        entryKeys = [[NSMutableArray alloc] initWithObjects:nil];
        isInElement = @"NO";
        currentElement = @"";
        isInOOP = NO;
        isInCard = NO;
        isInCorpCard = NO;
        isInReceiptCapture = NO;

    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return @"ME_LIST_DATA";
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
    
	self.path = [NSString stringWithFormat:@"%@/Mobile/Expense/GetAllExpenses", [ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}


-(void) flushData
{
	isInOOP = NO;
	isInCard = NO;
	isInCorpCard = NO;
    isInReceiptCapture = NO;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
    
	self.currentElement = elementName;
    
	isInElement = @"YES";
	
	self.buildString = [[NSMutableString alloc] init];
	

    if ([elementName isEqualToString:@"Entries"])
    {
        isInOOP = YES;
    }
    else if ([elementName isEqualToString:@"PersonalCards"])
    {
        isInCard = YES;
    }
    else if ([elementName isEqualToString:@"CorporateCardTransactions"])
    {
        isInCorpCard = YES;
    }
    else if ([elementName isEqualToString:@"ReceiptCaptures"])
    {
        isInReceiptCapture = YES;
    }
    
	//MOB-13656
    if(isInOOP && [elementName isEqualToString:@"MobileEntry"] )
    {
        self.entity = [self.mobileEntryManager  makeNew];
    }
    else if ([elementName isEqualToString:@"CorporateCardTransaction"] || [elementName isEqualToString:@"PersonalCardTransaction"])
    {
        self.entity = [self.mobileEntryManager  makeNew];
    }
    else if (isInReceiptCapture && [elementName isEqualToString:@"ReceiptCapture"])
    {
        self.entity = [self.mobileEntryManager  makeNew];
    }
    
    // Set isInOOP / isInCard / isInCorpCard here.
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
    if (([elementName isEqualToString:@"MobileEntry"] )
		|| [elementName isEqualToString:@"CorporateCardTransaction"]
		|| [elementName isEqualToString:@"PersonalCardTransaction"] || [elementName isEqualToString:@"ReceiptCapture"])
	{
        // set vendor and location if its card transaction so VC's can display 
        if(isInCorpCard)
        {
            if (entity.locationName == nil)
            {
                entity.locationName = [self getMerchantLocationName:entity];
            }
            
            if (entity.vendorName == nil)
            {
                entity.vendorName = entity.merchantName;
            }
        }
        if(isInCard)
        {
            if (entity.vendorName == nil)
                entity.vendorName = entity.transactionDescription;
        }
        //Delete the keys that are saved.
        // MOB-13678 - handle case when dict is allocated but there are no entries 
        if (self.obsoleteEntries != nil && [self.obsoleteEntries count] > 0)
        {
            //NSLog(@"OB: Found %@ ", idKey);
            [self.obsoleteEntries removeObjectForKey:entity.key];
        }

    }
    
    if ([elementName isEqualToString:@"Entries"])
    {
        isInOOP = NO;
       // MOB-13731
        self.entity = [self.mobileEntryManager   makeNew];
    }
    else if ([elementName isEqualToString:@"PersonalCards"])
    {
        isInCard = NO;
    }
    else if ([elementName isEqualToString:@"CorporateCardTransactions"])
    {
        isInCorpCard = NO;
    }
    else if ([elementName isEqualToString:@"ReceiptCaptures"])
    {
        isInReceiptCapture = NO;
    }

}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[buildString appendString:string];
	
	if (isInOOP || isInCorpCard || isInCard || isInReceiptCapture)
	{
		if ([currentElement isEqualToString:@"MobileEntry"])
		{
			
		}
		else if ((!isInCard && [currentElement isEqualToString:@"CrnCode"])||[currentElement isEqualToString:@"TransactionCrnCode"])
		{
			self.entity.crnCode = buildString;
		}
		else if ([currentElement isEqualToString:@"ExpKey"])
		{
			[self.entity setExpKey:buildString];
		}
		else if ([currentElement isEqualToString:@"ExpName"])
		{
			[self.entity setExpName:buildString];
		}
		else if ([currentElement isEqualToString:@"MeKey"])
		{
            // Write MeKey to key if its a non-card transaction
			[self.entity setKey:buildString];
		}
		else if ([currentElement isEqualToString:@"TransactionAmount"] || [currentElement isEqualToString:@"Amount"])
		{
			[self.entity setTransactionAmount:[NSDecimalNumber decimalNumberWithString:string]];
		}
		else if ([currentElement isEqualToString:@"TransactionDate"] || [currentElement isEqualToString:@"DatePosted"])
		{
            NSDate *dt = [CCDateUtilities formatDateToNSDateYYYYMMddTHHmmss:buildString];
			[self.entity  setTransactionDate:dt];
		}
		else if ([currentElement isEqualToString:@"Comment"])
		{
			[self.entity  setComment:buildString];
		}
		else if ([currentElement isEqualToString:@"LocationName"])
		{
			[self.entity  setLocationName:buildString];
		}
		else if ([currentElement isEqualToString:@"VendorName"])
		{
			[self.entity  setVendorName:buildString];
		}
		else if ([currentElement isEqualToString:@"DoingBusinessAs"])
		{
			[self.entity setDoingBusinessAs:buildString];
		}
		else if ([currentElement isEqualToString:@"HasReceiptImage"])
		{            
			[self.entity  setHasReceipt:buildString ];
		}
        else if ([currentElement isEqualToString:@"ReceiptImageId"])
		{
			[self.entity  setReceiptImageId:buildString];
		}
		else if ([currentElement isEqualToString:@"CctKey"])
		{
            // set cctKey to key if its a corpcard
			[self.entity  setCctKey:buildString];
   
		}
		else if ([currentElement isEqualToString:@"PctKey"])
		{
            // set cctKey to key if its a corpcard
			[self.entity  setPctKey:buildString];
   
		}
		else if ([currentElement isEqualToString:@"Category"])
        {
            [self.entity setCategory:buildString];
        }
        else if ([currentElement isEqualToString:@"Description"])
        {
            [self.entity setTransactionDescription:buildString];
        }
        // TODO : Check if this is required ?? 
//        else if ([currentElement isEqualToString:@"Status"])
//        {
//            [self.entity setTranactionStatus:buildString];
//        }
        else if ([currentElement isEqualToString:@"CardTypeName"])
        {

            [self.entity setCardTypeName:buildString];
        }
        else if ([currentElement isEqualToString:@"HasRichData"])
        {
            [self.entity setHasRichData:buildString];
        }

        // Personal Card account data
        else if ([currentElement isEqualToString:@"AccountNumberLastFour"])
        {
            [self.entity setAccountNumberLastFour:buildString];
        }
        else if ([currentElement isEqualToString:@"CardName"])
        {
            [self.entity setCardName:buildString];
        }
        //MOB-13731
        else if (isInCard && [currentElement isEqualToString:@"CrnCode"])
        {
            [self.entity setCrnCode:buildString];
        }
        else if ([currentElement isEqualToString:@"PcaKey"])
        {
            [self.entity setPcaKey:buildString];
        }
        // Corporate Card specific field
		else if ([currentElement isEqualToString:@"MerchantCity"])
        {
            
            [self.entity setMerchantCity:buildString];
        }
        else if ([currentElement isEqualToString:@"MerchantState"])
        {
            [self.entity setMerchantState:buildString];
        }
        else if ([currentElement isEqualToString:@"MerchantCtryCode"])
        {
            [self.entity setMerchantCtryCode:buildString];
        }

        else if ([currentElement isEqualToString:@"CardTypeCode"])
        {
            [self.entity setCardTypeCode:buildString];
        }
        else if ([currentElement isEqualToString:@"MerchantName"])
        {
            
            [self.entity setMerchantName:buildString];
        }
        
        else if ([currentElement isEqualToString:@"SmartExpense"])
		{
			[self.entity setSmartExpenseMeKey:buildString];
		}
        // MOB-13615 AMEX Pre-Auth fields
        else if ([currentElement isEqualToString:@"CctType"])
        {
            [self.entity setCctType:buildString];
        }
        else if ([currentElement isEqualToString:@"AuthorizationRefNo"])
        {
            [self.entity setAuthorizationRefNo:buildString];
        }
        else if ([currentElement isEqualToString:@"RcKey"])
        {
            [self.entity setRcKey:buildString];
        }
        else if ([currentElement isEqualToString:@"SmartExpenseId"])
        {
            [self.entity setSmartExpenseId:buildString];
        }
	}
}

-(NSString*) getMerchantLocationName:(EntityMobileEntry *)meEntry
{
	if (meEntry.merchantCity == nil)
	{
		if (meEntry.merchantState == nil)
			return meEntry.merchantCtryCode;
        
		if (meEntry.merchantCtryCode == nil)
			return meEntry.merchantState;
		
		return [NSString stringWithFormat:@"%@, %@", meEntry.merchantState, meEntry.merchantCtryCode];
	}
	else {
		if (meEntry.merchantState == nil)
		{
			if (meEntry.merchantCtryCode == nil)
				return meEntry.merchantCity;
			
			return [NSString stringWithFormat:@"%@, , %@", meEntry.merchantCity, meEntry.merchantCtryCode];
		}
		
		if (meEntry.merchantCtryCode == nil)
			return [NSString stringWithFormat:@"%@, %@", meEntry.merchantCity, meEntry.merchantState];
		
		return [NSString stringWithFormat:@"%@, %@, %@", meEntry.merchantCity, meEntry.merchantState, meEntry.merchantCtryCode];
	}
    
}

- (void)parserDidStartDocument:(NSXMLParser *)parser
{
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSPersistentStoreCoordinator *coordinator = [ad persistentStoreCoordinator];
    if (coordinator != nil)
    {
        self.managedObjectContext = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSPrivateQueueConcurrencyType];
        [self.managedObjectContext setPersistentStoreCoordinator:coordinator];
        
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        // suppress notifications to any thread until updates are complete.
        [[NSNotificationCenter defaultCenter] removeObserver:ad name:NSManagedObjectContextDidSaveNotification object:self.managedObjectContext];
        [[NSNotificationCenter defaultCenter] removeObserver:ad name:NSManagedObjectContextObjectsDidChangeNotification object:self.managedObjectContext];
        
        // Create the mobileEntry manager with new private context
        self.mobileEntryManager = [[MobileEntryManager alloc] initWithContext:self.managedObjectContext];
        
        NSArray *entriesInCoreData = [self.mobileEntryManager fetchAll];
        if (entriesInCoreData != nil && entriesInCoreData.count > 0)
        {
            self.obsoleteEntries = [[NSMutableDictionary alloc] initWithCapacity:entriesInCoreData.count];
            for (EntityMobileEntry* oop in entriesInCoreData)
            {
                //Do not delete offline entries, if key is nil then its offline entry
                // MOB-13680 - entry.key always stores mekey only
                //if (oop.key != nil || oop.cctKey != nil || oop.pctKey != nil)
                if ([MobileEntryManager getKey:oop] != nil)
                {
                    if([MobileEntryManager isCorporateCardTransaction:oop])
                        [self.mobileEntryManager deleteBycctKey:oop.cctKey ];
                    else if([MobileEntryManager isPersonalCardTransaction:oop])
                        [self.mobileEntryManager deleteBypctKey:oop.pctKey ];
                    else if ([MobileEntryManager isReceiptCapture:oop])
                        [self.mobileEntryManager deleteByrcKey:oop.rcKey ];
                    else
                        [self.mobileEntryManager deleteByKey:oop.key ];
                    // Delete all keys
                }
            }
        }
    }
}

- (void)parserDidEndDocument:(NSXMLParser *)parser
{
    // TODO : Do we need to keep track of smart expense manage ? 
	// [[SmartExpenseManager getInstance] setCurrentExpenses:oopes];
    
    //MOB-13693 - Not required anymore. We delete and add all the expenese at parserDidStartDocument.
//    // Delete any old expenses that are no longer in the list.
//    
//    if (self.obsoleteEntries != nil)
//    {
//        for (NSString *keyOfObsoleteOope in self.obsoleteEntries)
//        {
//            //NSLog(@"OB: Deleting %@ from core data", keyOfObsoleteOope);
//            [self.mobileEntryManager  deleteByKey:keyOfObsoleteOope ];
//        }
//    }
    
    NSError *error = nil;
    if (self.managedObjectContext != nil)
    {
        // Add observer so save operation notification is sent out to all listeners
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        [[NSNotificationCenter defaultCenter] addObserver:ad
                                                 selector:@selector(processNotification:)
                                                     name:NSManagedObjectContextDidSaveNotification
                                                   object:self.managedObjectContext];

        if ([self.managedObjectContext hasChanges] && ![self.managedObjectContext save:&error])
        {
            NSLog(@"Unresolved error in MobileExpenseList::parserDidEndDocument %@, %@", error, [error userInfo]);
            abort();
        }
        
        // Stop observing changes after save is done. 
        [[NSNotificationCenter defaultCenter] removeObserver:ad name:NSManagedObjectContextDidSaveNotification object:self.managedObjectContext];

    }
}


//TODO : Implement these
-(void)saveToLocalCache:(NSString*) uId withCacheMeta:(CacheData*) cacheData
{
}

-(void)loadFromLocalCache:(NSString*) uId withCacheMeta:(CacheData*) cacheData
{
}
@end
