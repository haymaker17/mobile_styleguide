//
//  ReceiptManager.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 5/23/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReceiptManager.h"
#import "ReceiptCache.h"
#import "ImageUtil.h"

static ReceiptManager *sharedInstance;

@interface ReceiptManager (Private)
-(NSArray *) fetchAll:(NSString *) name;
-(int)currentCacheCount;
-(NSManagedObject *) fetchLast;
@end

@implementation ReceiptManager
@synthesize entityName;
@synthesize context = _context;

+(ReceiptManager*)sharedInstance
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
				sharedInstance = [[ReceiptManager alloc] init];
			}
		}
		return sharedInstance;
	}
}


-(ReceiptManager*)init
{
    self = [super init];
	if (self)
	{
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        self.context = [ad managedObjectContext];
        self.entityName = @"EntityReceipt";
	}
    
	return self;
}


-(EntityReceipt *) makeNew
{
    // If Cache limit exceeded, drop the oldest(last) receipt and add this new one
    int cacheCount = [self currentCacheCount];
    while (cacheCount >= RECEIPT_CACHE_LIMIT) 
    {
        EntityReceipt *entity = (EntityReceipt*)[self fetchLast];
        // MOB-13007 Delete file from cache
        if (![ entity.fullscreenImageLocalPath length])
        {
            // Delete from ReceiptCache -  delete fullsize receipts only
            [[ReceiptCache sharedInstance] deleteFilesMatchingId:entity.imageID];
            // This will delete both ThumbNail and fullsize receipts
            //[[ReceiptCache sharedInstance] deleteReceiptsMatchingId:entity.imageID];
        }
        else if ([entity.imageID length])
        {
            [ImageUtil killImageFromDocumentsFolder:[NSString stringWithFormat:@"%@.png",entity.imageID]];
        }
        else
        {
            // Delete using the local file path
            [ImageUtil killImageFromDocumentsFolder:entity.fullscreenImageLocalPath];
        }
        [self deleteObj:entity];
        cacheCount = [self currentCacheCount];
    }
    
    return [NSEntityDescription insertNewObjectForEntityForName:self.entityName inManagedObjectContext:self.context];
}

-(void) clearAll
{
    NSArray *aReceiptManagerData = [self fetchAll:entityName];
    
    for(EntityReceipt *entity in aReceiptManagerData)
    {
        [self deleteObj:entity];
    }
}

-(NSManagedObject *) fetchReceipt:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(key = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return nil;
}


-(void) saveReceipt
{
    NSError *error;
    if (![_context save:&error]) {
        NSLog(@"Whoops, couldn't save receipt: %@", [error localizedDescription]);
    }
    else
    {
        NSLog(@"receiptCacheCount is now: %d", [self currentCacheCount]);
    }
}

#pragma mark Common functions

-(int)currentCacheCount
{
    NSArray *cachedReceipts = [self fetchAll:self.entityName];
    if (cachedReceipts != nil) {
        return (int)[cachedReceipts count];
    }
    else
    {
        return 0;
    }
}

-(NSArray *) fetchAll:(NSString *) name
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:name inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
    
}


-(NSManagedObject *) fetchLast
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:self.entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return nil;
}

-(BOOL) hasAnyReceipt
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return YES;
    else
        return NO;
}

-(void) deleteObj:(NSManagedObject *)obj
{
    [_context deleteObject:obj];
    NSError *error;
    if (![_context save:&error]) {
        NSLog(@"Whoops, couldn't delete object: %@", [error localizedDescription]);
    }
    else
    {
        NSLog(@"receiptCacheCount is now: %d", [self currentCacheCount]);
    }
}

@end
