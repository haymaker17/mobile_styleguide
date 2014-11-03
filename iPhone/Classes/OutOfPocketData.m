//
//  OutOfPocketData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/19/10.
//  Copyright 2010 Concur. All rights reserved.
//
// TODO: delete comments once refactoring is done and finally delete this file
//
#import "OutOfPocketData.h"
#import "MCLogging.h"
#import "CCardTransaction.h"
#import "SmartExpenseManager.h"
#import "DataConstants.h"
#import "DateTimeFormatter.h"
//#import "EntityMobileEntryExtension.h"
//#import "EntityMobileCorpCardEntryExtension.h"
//#import "EntityMobilePersonalCardEntryExtension.h"

@interface OutOfPocketData ()
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@end

@implementation OutOfPocketData
@synthesize path, currentElement, obsoleteOopes, oopes, oope, oopKeys, buildString;
@synthesize pCards, pCard;
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
        isInElement = @"NO";
        currentElement = @"";
        oopes = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];	
	
        oopKeys = [[NSMutableArray alloc] initWithObjects:nil];
        pCards = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return OOPES_DATA;
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
	self.oopes = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	self.oopKeys = [[NSMutableArray alloc] initWithObjects:nil];
	self.pCards = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	isInOOP = FALSE;
	isInCard = FALSE;
	isInCorpCard = FALSE;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{

	self.currentElement = elementName;

	isInElement = @"YES";
	
	self.buildString = [[NSMutableString alloc] init];
	
	if ([elementName isEqualToString:@"MobileEntry"])
	{
		if (isInOOP) 
		{
			oope = [[OOPEntry alloc] init];
		}
	}
	else if ([elementName isEqualToString:@"CorporateCardTransaction"])
	{
		
		oope = [[CCardTransaction alloc] init];
	}
	else if ([elementName isEqualToString:@"PersonalCardTransaction"])
	{
		oope = [[PCardTransaction alloc] init];
	}	
	else if ([elementName isEqualToString:@"PersonalCard"])
	{
		pCard = [[PersonalCardData alloc] init];
	}	
	else if ([elementName isEqualToString:@"Entries"])
	{
		isInOOP = TRUE;
	}
	else if ([elementName isEqualToString:@"PersonalCards"])
	{
		isInCard = TRUE;
	}
	else if ([elementName isEqualToString:@"CorporateCardTransactions"])
	{
		isInCorpCard = TRUE;
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if (([elementName isEqualToString:@"MobileEntry"] && isInOOP)
		|| [elementName isEqualToString:@"CorporateCardTransaction"]
		|| [elementName isEqualToString:@"PersonalCardTransaction"])
	{
		NSString* idKey = [oope getKey];
		//[[MCLogging getInstance] log:[NSString stringWithFormat:@"Add object with id - %@", idKey] Level:MC_LOG_DEBU];
		oopes[idKey] = oope;
		[oopKeys addObject:idKey];
        
        if (self.obsoleteOopes != nil)
        {
            //NSLog(@"OB: Found %@ ", idKey);
            [self.obsoleteOopes removeObjectForKey:idKey];
        }
		
		if (isInCorpCard)
		{
			// Fix the location
			CCardTransaction* cct = (CCardTransaction*) oope;
			if (cct.locationName == nil)
			{
				cct.locationName = [cct getMerchantLocationName];
			}
			
			if (cct.vendorName == nil)
			{
				cct.vendorName = cct.merchantName;
			}
        //MOB-12986: Commenting out unwanted code delete later
//            EntityMobileCorpCardEntry *corpCard = [EntityMobileCorpCardEntry fetchByKey:cct.cctKey inContext:self.managedObjectContext];
//            if (corpCard == nil)
//                corpCard = [EntityMobileCorpCardEntry makeNewInContext:self.managedObjectContext];
//            
//            corpCard.key = cct.cctKey;
//            corpCard.expKey = cct.expKey;
//            corpCard.expName = cct.expName;
//            corpCard.locationName = cct.locationName;
//            corpCard.vendorName = cct.vendorName;
//            corpCard.crnCode = cct.crnCode;
//            corpCard.receiptImageId = cct.receiptImageId;
//            corpCard.comment = cct.comment;
//            corpCard.transactionDate = cct.tranDate;
//            corpCard.transactionAmount = [NSDecimalNumber decimalNumberWithDecimal:[[NSNumber numberWithDouble:cct.tranAmount] decimalValue]];
//            corpCard.smartExpenseMeKey = cct.smartExpenseMeKey;
//            corpCard.transactionDescription = cct.description;
//            corpCard.cardName = cct.cardTypeName;
		}
		else if (isInCard)
		{
			PCardTransaction* pct = (PCardTransaction*) oope;
			pct.crnCode = pCard.crnCode;
			pct.cardName = pCard.cardName;
			pct.pcaKey = pCard.pcaKey;
			if (pct.vendorName == nil)
				pct.vendorName = pct.description;

//            EntityMobilePersonalCardEntry *personalCard = [EntityMobilePersonalCardEntry fetchByKey:pct.pctKey inContext:self.managedObjectContext];
//            if (personalCard == nil)
//                personalCard = [EntityMobilePersonalCardEntry makeNewInContext:self.managedObjectContext];
//
//            personalCard.key = pct.pctKey;
//            personalCard.expKey= pct.expKey;
//            personalCard.expName = pct.expName;
//            personalCard.locationName = pct.locationName;
//            personalCard.vendorName = pct.vendorName;
//            personalCard.crnCode = pct.crnCode;
//            personalCard.receiptImageId = pct.receiptImageId;
//            personalCard.comment = pct.comment;
//            personalCard.transactionDate = pct.tranDate;
//            personalCard.transactionAmount = [NSDecimalNumber decimalNumberWithDecimal:[[NSNumber numberWithDouble:pct.tranAmount] decimalValue]];
//            personalCard.smartExpenseMeKey = pct.smartExpenseMeKey;
//            personalCard.transactionDescription = pct.description;
//            personalCard.cardName = pct.cardName;
//            personalCard.pcaKey = pct.pcaKey;
		}
        else // Not a card
        {
//            EntityMobileEntry *mobileEntry = [EntityMobileEntry fetchByKey:oope.meKey inContext:self.managedObjectContext];
//            if (mobileEntry == nil)
//                mobileEntry = [EntityMobileEntry makeNewInContext:self.managedObjectContext];
//            
//            mobileEntry.key = oope.meKey;
//            mobileEntry.expKey= oope.expKey;
//            mobileEntry.expName = oope.expName;
//            mobileEntry.locationName = oope.locationName;
//            mobileEntry.vendorName = oope.vendorName;
//            mobileEntry.crnCode = oope.crnCode;
//            mobileEntry.receiptImageId = oope.receiptImageId;
//            mobileEntry.comment = oope.comment;
//            mobileEntry.transactionDate = oope.tranDate;
//            mobileEntry.transactionAmount = [NSDecimalNumber decimalNumberWithDecimal:[[NSNumber numberWithDouble:oope.tranAmount] decimalValue]];
        }
	}
	else if ([elementName isEqualToString:@"PersonalCard"])
	{
		pCards[pCard.pcaKey] = pCard;
	}
	else if ([elementName isEqualToString:@"DoingBusinessAs"])
	{
		// For corp card, Let's not override the vendor name from mobile entry 
		if (oope.vendorName == nil && buildString != nil && buildString.length > 0)
			[oope setVendorName:buildString]; 
	}
	else if ([elementName isEqualToString:@"Entries"])
	{
		isInOOP = FALSE;
	}
	else if ([elementName isEqualToString:@"PersonalCards"])
	{
		isInCard = FALSE;
	}
	else if ([elementName isEqualToString:@"CorporateCardTransactions"])
	{
		isInCorpCard = FALSE;
	}
	
}



- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[buildString appendString:string];
	
	if (isInOOP || isInCorpCard || isInCard)
	{
		if ([currentElement isEqualToString:@"MobileEntry"])
		{
			
		}
		else if ((!isInCard && [currentElement isEqualToString:@"CrnCode"])||[currentElement isEqualToString:@"TransactionCrnCode"])
		{
			[oope setCrnCode:buildString];
		}
		else if ([currentElement isEqualToString:@"ExpKey"])
		{
			[oope setExpKey:buildString];
		}
		else if ([currentElement isEqualToString:@"ExpName"])
		{
			[oope setExpName:buildString];
		}
		else if ([currentElement isEqualToString:@"MeKey"])
		{
			[oope setMeKey:buildString];
		}
		else if ([currentElement isEqualToString:@"TransactionAmount"] || [currentElement isEqualToString:@"Amount"])
		{
			[oope setTranAmount:[string doubleValue]]; 
		}
		else if ([currentElement isEqualToString:@"TransactionDate"] || [currentElement isEqualToString:@"DatePosted"])
		{
			NSDate *dt = [DateTimeFormatter getLocalDate:buildString];
			[oope setTranDate:dt]; 
		}
		else if ([currentElement isEqualToString:@"Comment"])
		{
			[oope setComment:buildString];
		}
		else if ([currentElement isEqualToString:@"LocationName"])
		{
			
			[oope setLocationName:buildString]; 
		}
		else if ([currentElement isEqualToString:@"VendorName"])
		{
			
			[oope setVendorName:buildString]; 
		}
		else if ([currentElement isEqualToString:@"DoingBusinessAs"])
		{
			//
		}
		else if ([currentElement isEqualToString:@"HasReceiptImage"])
		{
			[oope setHasReceipt:buildString]; 
		}
        else if ([currentElement isEqualToString:@"ReceiptImageId"])
		{
			[oope setReceiptImageId:buildString]; 
		}
		else if ([currentElement isEqualToString:@"CctKey"])
		{
			[oope setCctKey:buildString]; 
		}
		else if ([currentElement isEqualToString:@"PctKey"])
		{
			[oope setPctKey:buildString]; 
		}
		else if (isInCard)
		{
			// Personal card specific stuff
			PCardTransaction* pct = (PCardTransaction*) oope;

			if ([currentElement isEqualToString:@"Category"])
			{
				[pct setCategory:buildString];
			}
			else if ([currentElement isEqualToString:@"Description"])
			{
				
				[pct setDescription:buildString];
			}
			else if ([currentElement isEqualToString:@"Status"])
			{
				[pct setTranStatus:buildString];
			}
			// Personal Card account data
			else if ([currentElement isEqualToString:@"AccountNumberLastFour"])
			{
				[pCard setAccountNumberLastFour:buildString];
			}
			else if ([currentElement isEqualToString:@"CardName"])
			{
				[pCard setCardName:buildString];
			}
			else if ([currentElement isEqualToString:@"CrnCode"])
			{
				[pCard setCrnCode:buildString];				
			}
			else if ([currentElement isEqualToString:@"PcaKey"])
			{
				[pCard setPcaKey:buildString];
			}
			
		}
		// Corporate Card specific field
		else if (isInCorpCard)
		{
			CCardTransaction* cct = (CCardTransaction*) oope;
			if ([currentElement isEqualToString:@"MerchantCity"])
			{
				
				[cct setMerchantCity:buildString];
			}
			else if ([currentElement isEqualToString:@"MerchantState"])
			{
				[cct setMerchantState:buildString];
			}
			else if ([currentElement isEqualToString:@"MerchantCtryCode"])
			{
				[cct setMerchantCtryCode:buildString];
			}
			else if ([currentElement isEqualToString:@"Description"])
			{
				
				[cct setDescription:buildString];
			}
			else if ([currentElement isEqualToString:@"CardTypeName"])
			{
				
				[cct setCardTypeName:buildString];
			}
			else if ([currentElement isEqualToString:@"CardTypeCode"])
			{
				[cct setCardTypeCode:buildString];
			}
			else if ([currentElement isEqualToString:@"HasRichData"])
			{
				[cct setHasRichData:buildString];
			}
			else if ([currentElement isEqualToString:@"MerchantName"])
			{
				
				[cct setMerchantName:buildString];
			}
		}
	}

	if (isInCorpCard || isInCard)
	{
		if ([currentElement isEqualToString:@"SmartExpense"])
		{
			CardTransaction* ct = (CardTransaction*) oope;
			[ct setSmartExpenseMeKey:buildString];
		}
	}
}

//- (void)parserDidStartDocument:(NSXMLParser *)parser
//{
//    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//    NSPersistentStoreCoordinator *coordinator = [ad persistentStoreCoordinator];
//    if (coordinator != nil)
//    {
//        self.managedObjectContext = [[NSManagedObjectContext alloc] init];
//        [self.managedObjectContext setPersistentStoreCoordinator:coordinator];
//        
//        // Add observer
//        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//        [[NSNotificationCenter defaultCenter] addObserver:ad
//                                                 selector:@selector(processNotification:)
//                                                     name:NSManagedObjectContextDidSaveNotification
//                                                   object:self.managedObjectContext];
//
//        // Get keys of all items in core data BEFORE we put in the new ones.
//        NSArray *oopesInCoreData = [EntityMobileEntry fetchAllInContext:self.managedObjectContext];
//        if (oopesInCoreData != nil && oopesInCoreData.count > 0)
//        {
//            self.obsoleteOopes = [[NSMutableDictionary alloc] initWithCapacity:oopesInCoreData.count];
//            for (EntityMobileEntry* oop in oopesInCoreData)
//            {
//                if (oop.key != nil)
//                {
//                    //NSLog(@"OB: Added %@ to dictionary", oop.key);
//                    [self.obsoleteOopes setObject:oop.key forKey:oop.key];
//                }
//            }
//        }
//    }
//}

- (void)parserDidEndDocument:(NSXMLParser *)parser
{
	[[SmartExpenseManager getInstance] setCurrentExpenses:oopes];
    
    // Delete any old expenses that are no longer in the list.
    
//    if (self.obsoleteOopes != nil)
//    {
//        for (NSString *keyOfObsoleteOope in self.obsoleteOopes)
//        {
//            //NSLog(@"OB: Deleting %@ from core data", keyOfObsoleteOope);
//            [EntityMobileEntry deleteByKey:keyOfObsoleteOope inContext:self.managedObjectContext];
//        }
//    }
//    
//    NSError *error = nil;
//    if (self.managedObjectContext != nil)
//    {
//        if ([self.managedObjectContext hasChanges] && ![self.managedObjectContext save:&error])
//        {
//            NSLog(@"Unresolved error in OutOfPocketData::parserDidEndDocument %@, %@", error, [error userInfo]);
//            abort();
//        }
//        
//        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//        [[NSNotificationCenter defaultCenter] removeObserver:ad name:NSManagedObjectContextDidSaveNotification object:self.managedObjectContext];
//    }
}

-(void)saveToLocalCache:(NSString*) uId withCacheMeta:(CacheData*) cacheData
{
}

-(void)loadFromLocalCache:(NSString*) uId withCacheMeta:(CacheData*) cacheData
{
}

@end
