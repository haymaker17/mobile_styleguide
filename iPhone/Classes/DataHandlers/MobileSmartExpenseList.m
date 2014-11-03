//
//  MobileSmartExpenseList.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 9/30/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MobileSmartExpenseList.h"
#import "ExpenseTypesManager.h"

@interface MobileSmartExpenseList ()

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) EntityMobileEntry			*entity;
@property (nonatomic, strong) NSMutableString			*buildString;

@property (nonatomic, copy) NSString			*isInElement;
@property BOOL isInOOP;
@property BOOL isPersonalCard;
@property BOOL isCorporateCard;
@property BOOL isReceiptCapture;
@property BOOL isEreceipt;
@property (strong, nonatomic) NSManagedObjectContext    *managedObjectContext;
@property (nonatomic, strong) NSMutableDictionary       *obsoleteEntries;
@property (nonatomic, strong) NSMutableArray			*entryKeys;
@property (strong,nonatomic) MobileEntryManager         *mobileEntryManager;
@property (strong,nonatomic) ExpenseTypesManager        *expenseTypesManager;

@end


@implementation MobileSmartExpenseList
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
        self.entryKeys = [[NSMutableArray alloc] initWithObjects:nil];
        self.isInElement = @"NO";
        self.currentElement = @"";
        self.isInOOP = NO;
        self.isPersonalCard = NO;
        self.isCorporateCard = NO;
        self.isReceiptCapture = NO;
        self.isEreceipt = NO;
        
    }
    return self;
}


-(NSString *)getMsgIdKey
{
    return @"MOBILE_SMART_EXPENSE_LIST_DATA";
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/Expense/v1.0/smartexpenses?reset=Y", [ExSystem sharedInstance].entitySettings.uri];
    Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:self.path MessageResponder:self ParameterBag:parameterBag];
    [msg setHeader:[ExSystem sharedInstance].sessionID];
    [msg setContentType:@"application/xml"];
    [msg setMethod:@"GET"];
    msg.oauth2AccessToken = [ExSystem sharedInstance].concurAccessToken;

    return msg;
}


-(void) flushData
{
    self.isInOOP = NO;
    self.isPersonalCard = NO;
    self.isCorporateCard = NO;
    self.isReceiptCapture = NO;
    self.isEreceipt = NO;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
    
    self.currentElement = elementName;
    
    self.isInElement = @"YES";
    
    self.buildString = [[NSMutableString alloc] init];
    
    
    if ([elementName isEqualToString:@"SmartExpense"])
    {
        self.isInOOP = YES;
        self.entity = [self.mobileEntryManager makeNew];
    }
    
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    self.isInElement = @"NO";
    
    if ([elementName isEqualToString:@"SmartExpense"])
    {
    	// MOB-21331
        // Follow Android team's logic, get expName by using expKey because server return wrong expName sometimes
        // If the e-receipt type can't be found in the expense type. show undefined
        NSString *lookUpExpName = [self.expenseTypesManager getExpNameByExpKey:self.entity.expKey];
        if([lookUpExpName lengthIgnoreWhitespace]){
            [self.entity setExpName:lookUpExpName];
        }
        
        // set vendor and location if its card transaction so VC's can display
        if(self.isCorporateCard)
        {
            if (self.entity.locationName == nil)
            {
                self.entity.locationName = [self getMerchantLocationName:self.entity];
            }
            
            if (self.entity.vendorName == nil)
            {
                self.entity.vendorName = self.entity.merchantName;
            }
            self.isCorporateCard = NO;
        }
        if(self.isPersonalCard)
        {
            if (self.entity.vendorName == nil)
                self.entity.vendorName = self.entity.transactionDescription;
            self.isPersonalCard = NO;
        }
        if(self.isReceiptCapture)
        {
             self.isReceiptCapture = NO;
        }
        // TODO : Handle more ereceipt stuff here
        if (self.isEreceipt)
        {
            if(self.entity.expName == nil)
            {
                self.entity.expName = @"Undefined";
            }
            
            if (self.entity.eReceiptImageId == nil)
            {
                self.entity.eReceiptImageId = @"HACK e-receipt Image ID";   //MOB-21302 For some e-receipt items they do not return e-receipt ID.
                [self.entity setHasReceipt:@"YES" ];
            }
            self.isEreceipt = NO;
        }

        self.isInOOP = NO;
        // MOB-13731
//        self.entity = [self.mobileEntryManager   makeNew];
    }
    
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
    //NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [self.buildString appendString:string];
    
    // TODO : not handled tags
    
    /*
     <EstimatedAmount>0</EstimatedAmount>
     <ExchangeRate>1</ExchangeRate>
     <EReceiptImageId>gWgKsFXlUtdKIhwYErFGTSDopnjoSb$p0cJiyJMHzFHIyVWJUCR$puYi2EpxqNuoHQtOA</EReceiptImageId>
     <EreceiptId>gWgOoG39cs6H59AbVfgkqD$sP70fWwwi2kkyJ5</EreceiptId>
     <EreceiptSource>Enterprise</EreceiptSource>
     <EreceiptType>Car</EreceiptType>
     MobileReceiptImageId -- ReceiptImageId
     VendorDescription -- VendorName
     
     == What are these tags == 
     <TransactionGroup>Smart Expenses</TransactionGroup>
     <TravelCompanyCode>ZI</TravelCompanyCode>
     <TripId>gWmaeqdNRHmPqA2OI0DTq9N7cvr8xzgOYGw</TripId>
     <TripName>E-Receipts</TripName>
     <VendorCode>ZI</VendorCode>
     <VendorDescription>Avis</VendorDescription>
     
     */
    if (self.isInOOP)
    {
        if ((!self.isPersonalCard && [self.currentElement isEqualToString:@"CrnCode"])||[self.currentElement isEqualToString:@"TransactionCrnCode"])
        {
            self.entity.crnCode = self.buildString;
        }
        else if ([self.currentElement isEqualToString:@"ExpKey"])
        {
            [self.entity setExpKey:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"ExpName"])
        {
            [self.entity setExpName:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"MeKey"])
        {
            // Write MeKey to key if its a non-card transaction
            [self.entity setKey:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"TransactionAmount"] || [self.currentElement isEqualToString:@"Amount"])
        {
            [self.entity setTransactionAmount:[NSDecimalNumber decimalNumberWithString:string]];
        }
        else if ([self.currentElement isEqualToString:@"TransactionDate"] || [self.currentElement isEqualToString:@"DatePosted"])
        {
            NSDate *dt = [CCDateUtilities formatDateToNSDateYYYYMMddTHHmmss:self.buildString];
            [self.entity  setTransactionDate:dt];
        }
        else if ([self.currentElement isEqualToString:@"Comment"])
        {
            [self.entity  setComment:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"LocName"])
        {
            [self.entity  setLocationName:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"VendorDescription"])
        {
            [self.entity  setVendorName:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"DoingBusinessAs"])
        {
            [self.entity setDoingBusinessAs:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"ReceiptImageId"])
        {
            [self.entity  setReceiptImageId:self.buildString];
            [self.entity setHasReceipt:@"YES" ];
        }
        else if ([self.currentElement isEqualToString:@"MobileReceiptImageId"])
        {
            [self.entity  setMobileReceiptImageId:self.buildString];
            [self.entity setHasReceipt:@"YES" ];
        }
        else if ([self.currentElement isEqualToString:@"EReceiptImageId"])
        {
            [self.entity  setEReceiptImageId:self.buildString];
            [self.entity setHasReceipt:@"YES" ];
        }

        else if ([self.currentElement isEqualToString:@"CctReceiptImageId"])
        {
            [self.entity  setCctReceiptImageId:self.buildString];
            [self.entity setHasReceipt:@"YES" ];
        }
        // eReceipt id
        else if ([self.currentElement isEqualToString:@"EreceiptId"])
        {
            [self.entity  setEreceiptId:self.buildString];
            self.isEreceipt = YES;
        }

        else if ([self.currentElement isEqualToString:@"EreceiptSource"])
        {
            [self.entity  setEreceiptSource:self.buildString];
        }

        else if ([self.currentElement isEqualToString:@"EreceiptType"])
        {
            [self.entity  setEreceiptType:self.buildString];
        }
        
        else if ([self.currentElement isEqualToString:@"StatKey"])
        {
            [self.entity setStatKey:self.buildString];
        }

        else if ([self.currentElement isEqualToString:@"CctKey"])
        {
            // set cctKey to key if its a corpcard
            [self.entity  setCctKey:self.buildString];
            self.isCorporateCard = YES;
            
        }
        else if ([self.currentElement isEqualToString:@"PctKey"])
        {
            // set cctKey to key if its a corpcard
            [self.entity  setPctKey:self.buildString];
            self.isPersonalCard = YES;
            
        }
        else if ([self.currentElement isEqualToString:@"Category"])
        {
            [self.entity setCategory:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"Description"])
        {
            [self.entity setTransactionDescription:self.buildString];
        }

        else if ([self.currentElement isEqualToString:@"CardTypeName"])
        {
            
            [self.entity setCardTypeName:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"HasRichData"])
        {
            [self.entity setHasRichData:self.buildString];
        }
        
        // Personal Card account data
        else if ([self.currentElement isEqualToString:@"AccountNumberLastFour"])
        {
            [self.entity setAccountNumberLastFour:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"CardName"])
        {
            [self.entity setCardName:self.buildString];
        }
        //MOB-13731
        else if (self.isPersonalCard && [self.currentElement isEqualToString:@"CrnCode"])
        {
            [self.entity setCrnCode:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"PcaKey"])
        {
            [self.entity setPcaKey:self.buildString];
        }
        // Corporate Card specific field
        else if ([self.currentElement isEqualToString:@"MerchantCity"])
        {
            
            [self.entity setMerchantCity:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"MerchantState"])
        {
            [self.entity setMerchantState:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"MerchantCtryCode"])
        {
            [self.entity setMerchantCtryCode:self.buildString];
        }
        
        else if ([self.currentElement isEqualToString:@"CardTypeCode"])
        {
            [self.entity setCardTypeCode:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"MerchantName"])
        {
            
            [self.entity setMerchantName:self.buildString];
        }
        
        else if ([self.currentElement isEqualToString:@"SmartExpense"])
        {
            [self.entity setSmartExpenseMeKey:self.buildString];
        }
        // MOB-13615 AMEX Pre-Auth fields
        else if ([self.currentElement isEqualToString:@"CctType"])
        {
            [self.entity setCctType:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"AuthorizationRefNo"])
        {
            [self.entity setAuthorizationRefNo:self.buildString];
        }
        else if ([self.currentElement isEqualToString:@"RcKey"])
        {
            [self.entity setRcKey:self.buildString];
            self.isReceiptCapture = YES;
        }
        else if ([self.currentElement isEqualToString:@"SmartExpenseId"])
        {
            [self.entity setSmartExpenseId:self.buildString];
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
        // Create the mobile entry type manager.
        self.expenseTypesManager = [ExpenseTypesManager sharedInstance];
        
        NSArray *entriesInCoreData = [self.mobileEntryManager fetchAll];
        if (entriesInCoreData != nil && entriesInCoreData.count > 0)
        {
            self.obsoleteEntries = [[NSMutableDictionary alloc] initWithCapacity:entriesInCoreData.count];
            for (EntityMobileEntry* oop in entriesInCoreData)
            {
                if ([MobileEntryManager getKey:oop] != nil)
                {
                    if([MobileEntryManager isCorporateCardTransaction:oop])
                        [self.mobileEntryManager deleteBycctKey:oop.cctKey ];
                    else if([MobileEntryManager isPersonalCardTransaction:oop])
                        [self.mobileEntryManager deleteBypctKey:oop.pctKey ];
                    else if ([MobileEntryManager isReceiptCapture:oop])
                        [self.mobileEntryManager deleteByrcKey:oop.rcKey ];
                    else if ([MobileEntryManager isEreceipt:oop])
                        [self.mobileEntryManager deleteByEreceiptID:oop.ereceiptId];
                    else
                        [self.mobileEntryManager deleteBySmartExpenseId:oop.smartExpenseId ];
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
