//
//  MobileEntryManager.m
//  ConcurMobile
//
//  Created by charlottef on 10/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
// 
//  Change log
//      Updated on 4/22 by Pavan for MOB-12986.
//      This class is wrapper class to manage the EntityMobileEntry coredata object

#import "MobileEntryManager.h"

static MobileEntryManager *sharedInstance;

@implementation MobileEntryManager


+(MobileEntryManager*)sharedInstance
{
	if (sharedInstance != nil)
	{
		return sharedInstance;
	}
	else
	{
		@synchronized (self)
		{
			if (sharedInstance == nil)
			{
				sharedInstance = [[MobileEntryManager alloc] init];
			}
		}
		return sharedInstance;
	}
}


-(id)init
{
    self = [super init];
	if (self)
	{
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        self.context = [ad managedObjectContext];
    }
    
	return self;
}


/**
 Init the manager with a given context
 @param incontext - private context if initiated from a xml parser
 @return new instance of object
 */

-(instancetype)initWithContext:(NSManagedObjectContext*)inContext
{
    self = [super init];
	if (self)
	{
        self.context = inContext;
	}
    
	return self;
}


#pragma mark - Creation and deletion

-(EntityMobileEntry *) makeNew
{
    return ((EntityMobileEntry *)[super makeNew:@"EntityMobileEntry"]);
}

-(void) deleteByLocalId:(NSString*)localId 
{
    EntityMobileEntry *mobileEntry = [self fetchByLocalId:localId];
    if (mobileEntry != nil)
        [mobileEntry.managedObjectContext deleteObject:mobileEntry];
}


-(EntityMobileEntry *) makeNewInContext:(NSManagedObjectContext*)context
{
    return [NSEntityDescription insertNewObjectForEntityForName:@"EntityMobileEntry" inManagedObjectContext:context];
}

-(void) deleteByKey:(NSString*)key
{
    EntityMobileEntry *mobileEntry = [self fetchByKey:key ];
    if (mobileEntry != nil)
        [mobileEntry.managedObjectContext deleteObject:mobileEntry];
}

-(void) deleteBySmartExpenseId:(NSString*)smartExpenseId
{
    EntityMobileEntry *mobileEntry = [self fetchBySmartExpenseId:smartExpenseId ];
    if (mobileEntry != nil)
        [mobileEntry.managedObjectContext deleteObject:mobileEntry];
}


-(void) deleteBypctKey:(NSString*)key
{
    EntityMobileEntry *mobileEntry = [self fetchBypctKey:key ];
    if (mobileEntry != nil)
        [mobileEntry.managedObjectContext deleteObject:mobileEntry];
}

-(void) deleteBycctKey:(NSString*)key
{
    EntityMobileEntry *mobileEntry = [self fetchBycctKey:key ];
    if (mobileEntry != nil)
        [mobileEntry.managedObjectContext deleteObject:mobileEntry];
}

-(void) deleteByrcKey:(NSString*)key
{
    EntityMobileEntry *mobileEntry = [self fetchByrcKey:key ];
    if (mobileEntry != nil)
        [mobileEntry.managedObjectContext deleteObject:mobileEntry];
}

-(void) deleteByEreceiptID:(NSString*)key
{
    EntityMobileEntry *mobileEntry = [self fetchByEreceiptID:key];
    if (mobileEntry != nil)
        [mobileEntry.managedObjectContext deleteObject:mobileEntry];
}

-(void) clearAll
{
    NSArray *medata = [self fetchAll:@"EntityMobileEntry"];
    
    for(EntityMobileEntry *entity in medata)
    {
        [self deleteObj:entity];
    }
}

#pragma mark - Fetch methods

-(EntityMobileEntry *) fetchOrMake:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(key = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return [self makeNew];
}


-(NSArray*) fetchAll
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context];
    [fetchRequest setEntity:entity];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

-(EntityMobileEntry *) fetchByKey:(NSString*)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(key = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    
    return nil;
}


-(EntityMobileEntry *) fetchBySmartExpenseId:(NSString*)smartExpenseId
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(smartExpenseId = %@)", smartExpenseId];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    
    return nil;
}



-(EntityMobileEntry *) fetchBypctKey:(NSString*)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(pctKey = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    
    return nil;
}

-(EntityMobileEntry *) fetchBycctKey:(NSString*)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(cctKey = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    
    return nil;
}

-(EntityMobileEntry *) fetchByrcKey:(NSString*)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(rcKey = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    
    return nil;
}

-(EntityMobileEntry *) fetchByEreceiptID:(NSString*)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(ereceiptId = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    
    return nil;
}

-(EntityMobileEntry *) fetchByLocalId:(NSString*)localId 
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(localId = %@)", localId];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    
    return nil;
}

-(NSArray*) fetchMobileEntriesReferencingLocalReceiptImageId:(NSString*)localReceiptImageId
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(localReceiptImageId = %@)", localReceiptImageId];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

-(NSArray*) fetchAllPersonalCardMobileEntries
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(pctKey != nil and cardName != nil)"];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

-(NSArray*) fetchAllPersonalCardNames
{
    NSArray *aFetch = [self fetchAllPersonalCardMobileEntries];
    // return unique card names from the collections
    NSArray *cardNames = [aFetch  valueForKeyPath:@"@distinctUnionOfObjects.cardName"];
    
//    for( EntityMobileEntry* mobileEntry in aFetch )
//    {
//        NSString *cardName = mobileEntry.cardName;
//        if (cardName != nil && cardName.length > 0)
//            [cardNames addObject:cardName];
//    }

    return cardNames;

}


-(NSArray*) fetchAllCorporateCardMobileEntries
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(cctKey != nil)"];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

-(NSArray*) fetchAllExpenseItEntries
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(rcKey != nil)"];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

+(BOOL)isCorporateCardTransaction:(EntityMobileEntry*)mobileEntry
{
    BOOL isCorporateCardTransaction = mobileEntry.cctKey != nil;
    return isCorporateCardTransaction;
}

+(BOOL)isCardTransaction:(EntityMobileEntry*)mobileEntry
{
    BOOL isOfflineCardTransaction = false;
    if( true == [self isCorporateCardTransaction:mobileEntry] ||
        true == [self isPersonalCardTransaction:mobileEntry])
    {
        isOfflineCardTransaction = true;
    }
    return isOfflineCardTransaction;
}

+(BOOL)isPersonalCardTransaction:(EntityMobileEntry*)mobileEntry
{
    BOOL isPersonalCardTransaction = mobileEntry.pctKey != nil;
    return isPersonalCardTransaction;
}

+(BOOL)isReceiptCapture:(EntityMobileEntry*)mobileEntry
{
    BOOL isReceiptCapture = mobileEntry.rcKey != nil;
    return isReceiptCapture;
}

+(BOOL)isEreceipt:(EntityMobileEntry*)mobileEntry
{
    BOOL isEreceipt = mobileEntry.ereceiptId != nil;
    return isEreceipt;
}

+(BOOL)isSmartMatched:(EntityMobileEntry*)mobileEntry
{
    //
    // Follow android team's logic for smart matching, will add expense smart matching type later.
    if(mobileEntry == nil)
        return NO;
    
    BOOL isSmartMatched = NO;
    
    BOOL hasEReceiptId = mobileEntry.ereceiptId != nil;
    BOOL hasCctKey = mobileEntry.cctKey != nil;
    BOOL hasPctKey = mobileEntry.pctKey != nil;
    BOOL hasRcKey = mobileEntry.rcKey != nil;
    BOOL hasMeKey = mobileEntry.key != nil;
    
    if(hasEReceiptId){
        isSmartMatched = (hasCctKey || hasPctKey || hasRcKey || hasMeKey);
    }
    else if(hasRcKey){
        isSmartMatched = (hasCctKey || hasPctKey || hasMeKey);
    }
    else if(hasCctKey){
        isSmartMatched = (hasMeKey);
    }
    else if(hasPctKey){
        isSmartMatched = (hasMeKey);
    }
    else{
        //
        // Unknow type
    }
    
    return isSmartMatched;
}

+(BOOL)isSmartMatchedEReceipt:(EntityMobileEntry*)mobileEntry
{
    if(mobileEntry == nil || mobileEntry.ereceiptId == nil){
        return NO;
    }
    else{
        return (mobileEntry.cctKey != nil || mobileEntry.pctKey != nil || mobileEntry.rcKey != nil || mobileEntry.key != nil);
    }
}

+(BOOL)isCardAuthorizationTransaction:(EntityMobileEntry*)mobileEntry
{
    return [self isCorporateCardTransaction:mobileEntry] &&
        ([CCT_TYPE_AUTH isEqualToString: mobileEntry.cctType] ||
         [CCT_TYPE_PRE_AUTH isEqualToString:mobileEntry.cctType]);
}


+(NSString*) getKey:(EntityMobileEntry*)mobileEntry
{
	if (mobileEntry.cctKey != nil)
        return mobileEntry.cctKey;
    else if (mobileEntry.pctKey != nil)
        return mobileEntry.pctKey;
    else if (mobileEntry.rcKey != nil)
        return mobileEntry.rcKey;
    else if (mobileEntry.ereceiptId != nil)
        return mobileEntry.ereceiptId;
    else
        return mobileEntry.key;
}


@end
