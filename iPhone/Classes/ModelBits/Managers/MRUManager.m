//
//  MRUManager.m
//  ConcurMobile
//
//  Created by yiwen on 9/23/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "MRUManager.h"

static MRUManager *sharedInstance;

@implementation MRUManager

+(MRUManager*)sharedInstance
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
				sharedInstance = [[MRUManager alloc] init];
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

-(void) saveExpenseType:(ExpenseTypeData *)expenseType
{
    // mru_expenseType
}

-(NSArray *) getExpenseTypes
{
    NSMutableArray *expenseTypes = [[NSMutableArray alloc] init];

    return expenseTypes;
}

// currency is a simple abstraction of the standard save list item
-(void) saveCurrency:(ListItem *)listItem
{
    [self saveListItem:listItem withType:@"TransactionCurrencyName"];
}

-(NSArray *) getCurrencies
{
    return [self getListItemsOfType:@"TransactionCurrencyName"];
}

-(ListItem *) getLastUsedCurrency
{
    return [self getLastUsedListItem:[self getCurrencies]];

}

// There are two location field.iDs.  The old code kept both saved under mru_locationpicker.
// We'll do the same for backwards compatibility.
-(void) saveLocation:(ListItem *)listItem
{
    // when likey is nil, the user has selected the NONE location.  Don't save that to the MRU
    if (listItem != nil && listItem.liKey != nil) {
        [self saveListItem:listItem withType:@"mru_locationpicker"];
    }
}

-(NSArray *) getLocations
{
    return [self getListItemsOfType:@"mru_locationpicker"];
}

-(ListItem *)getLastUsedLocation
{
    return [self getLastUsedListItem:[self getLocations]];
}

-(ListItem *) getLastUsedListItem:(NSArray *)listItems
{
    if (listItems != nil && [listItems count] > 0 ) // if there is mru, use it
    {
        return (ListItem *)listItems[0];
    }
    return nil;
}

-(void) saveListItem:(ListItem *)listItem withType:(NSString *)listItemType
{
    NSInteger   keyVal = [listItem.liKey integerValue];

    NSData *extra = [self convertDictionaryToData:listItem.fields];

    [[MRUManager sharedInstance] addMRUForType:listItemType value:listItem.liName key:&keyVal code:listItem.liCode extra:extra];
}

-(NSArray *) getListItemsOfType:(NSString *)listItemType
{
    NSMutableArray *listItems = [[NSMutableArray alloc] init];

    NSArray *entities = [[MRUManager sharedInstance] getMRUsByType:listItemType];
    for (int i=0; i<entities.count; i++) {
        EntityMRU *eMru = (EntityMRU *)entities[i]; // first one is the latest one
        ListItem *listItem = [[ListItem alloc] init];
        listItem.liName = eMru.value;
        listItem.liKey = eMru.key == nil ? nil : [NSString stringWithFormat:@"%d", [eMru.key intValue]];
        listItem.liCode = eMru.code;
        listItem.fields = [self convertDataToDictionary:eMru.extra];

        [listItems addObject:listItem];
    }

    return listItems;
}

-(NSData *) convertDictionaryToData:(NSDictionary *)dictionary
{
    if (!dictionary) {
        return nil;
    }

    NSMutableData *data = [[NSMutableData alloc] init];
    NSKeyedArchiver *archiver = [[NSKeyedArchiver alloc] initForWritingWithMutableData:data];
    [archiver encodeObject:dictionary forKey:@"ListItem"];
    [archiver finishEncoding];
    return data;
}

-(NSMutableDictionary*) convertDataToDictionary:(NSData *)data
{
    NSKeyedUnarchiver *unarchiver = [[NSKeyedUnarchiver alloc] initForReadingWithData:data];
    NSMutableDictionary *dictionary = [unarchiver decodeObjectForKey:@"ListItem"];
    [unarchiver finishDecoding];
    return dictionary;
}


-(EntityMRU *) makeNew
{
    return ((EntityMRU *)[super makeNew:@"EntityMRU"]);
}

-(void) clearAll
{
    NSArray *aHomeData = [self fetchAll:@"EntityMRU"];
    
    for(EntityMRU *entity in aHomeData)
    {
        [self deleteObj:entity];
    }
}

-(NSArray *) getMRUsByType:(NSString *)tType
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMRU" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(type = %@)", tType];
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"lastUsedDate" ascending:NO];
    [fetchRequest setSortDescriptors:@[sort]];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

// MIB : MOB-13462  - show upto 10 MRU
#define MAX_MRU_COUNT 10

-(EntityMRU*)addMRUForType:(NSString*)tType value:(NSString*)tVal key:(NSInteger*)tKey code:(NSString*)tCode
{
    return [self addMRUForType:tType value:tVal key:tKey code:tCode extra:nil];
}

-(EntityMRU*)addMRUForType:(NSString*)tType value:(NSString*)tVal key:(NSInteger*)tKey code:(NSString*)tCode extra:(NSData*)extra
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMRU" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(type = %@)", tType];
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"lastUsedDate" ascending:NO];
    [fetchRequest setSortDescriptors:@[sort]];

    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    // Let's see if we can find an existing MRU for this value
    EntityMRU* result = nil;
    for (EntityMRU* mru in aFetch)
    {
        // handle issues around the Apple locale returning "US Dollar" versus our "US, Dollar"
        // Really hope this doesn't have bad side effects. :/
        BOOL valueIsEqual = NO;
        if ([tType isEqualToString:@"TransactionCurrencyName"]) {
            NSString *altValue = [tVal stringByReplacingOccurrencesOfString:@"," withString:@""];
            if ([[mru.value stringByReplacingOccurrencesOfString:@"," withString:@""] isEqualToString:altValue]) {
                valueIsEqual = YES;
            }
        }

        if ([mru.value isEqualToString:tVal]) {
            valueIsEqual = YES;
        }

        // MOB-11717 ignore likey if value is NSNotFound.
        if ((valueIsEqual && [tVal length]) || (tKey != nil && *tKey!=NSNotFound && [mru.key intValue] == *tKey))
        {
            result = mru;
            // MOB-11735 : if new liKey is sent then update the same
            if(result.key ==nil)
                result.key = (*tKey == NSNotFound || *tKey == 0 )? nil : @(*tKey);
            break;
        }
    }
    
    if(result == nil && aFetch != nil && [aFetch count] >= MAX_MRU_COUNT)
    {
        EntityMRU* oldestMRU = aFetch[[aFetch count]-1];
        [self deleteObj:oldestMRU];
    }
    
    if (result==nil)
    {
        result = (EntityMRU*)[self makeNew:@"EntityMRU"];
        // MOB-11717 : Do not set key if its key is NSNotFound
        result.key = (*tKey == NSNotFound || *tKey == 0 )? nil : @(*tKey);
        result.code = tCode;
        result.value = tVal;
        result.type = tType;
        if (extra != nil) {
            result.extra = extra;
        }
    }
    
    result.lastUsedDate = [NSDate date];
    [self saveIt:result];
    return result;
}

//MOB-8451
-(NSArray *) deleteMRUsByType:(NSString *)tType
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityMRU" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(type = %@)", tType];
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"lastUsedDate" ascending:NO];
    [fetchRequest setSortDescriptors:@[sort]];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    for(EntityMRU *entry in aFetch)
    {
        // Delete all these cache entries
        [self deleteObj:entry];
        
    }
    return aFetch;
}


@end
