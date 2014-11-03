//
//  UploadQueue.m
//  ConcurMobile
//
//  Created by charlottef on 10/30/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "UploadQueue.h"
#import "EntityUploadQueueItem.h"
#import "UploadQueueItemManager.h"
#import "UploadableItemMap.h"
#import "UploadQueueItemDescriptor.h"
#import "UploadQueueHelper.h"
#import "UploadQueueAlertView.h"

@implementation UploadQueue

@synthesize delegate = _delegate;
@synthesize managedObjectContext = _managedObjectContext;
@synthesize state, queuedItems, currentItemIndex, lastItemIndex, totalVisibleItemsToUpload, helper;

static UploadQueue *sharedInstance;

+(UploadQueue*)sharedInstance
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
				sharedInstance = [[UploadQueue alloc] init];
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
        self.managedObjectContext = [ad managedObjectContext];
        
        [self reset];
	}
	return self;
}

#pragma mark - Upload Methods
-(void) startUpload
{
    [[MCLogging getInstance] log:@"UploadQueue::startUpload" Level:MC_LOG_DEBU];
    [Flurry logEvent:@"Offline: Upload" timed:YES];
    
    if (self.state != UploadQueueStateIdle)
    {
        [[MCLogging getInstance] log:@"UploadQueue::startUpload force quitting previous upload" Level:MC_LOG_DEBU];
        [self forceQuit];
    }
    
    [self writeToFlurry];
    
    self.state = UploadQueueStateUploading;
    
    //[self log];

    // Create descriptors of items in the queue (in uploading order)
    NSArray *items = [self fetchQueuedItemsForTopLevelItemsOnly:NO];
    self.queuedItems = [NSMutableArray arrayWithCapacity:(items.count > 0 ? items.count : 1)];
    for (EntityUploadQueueItem *item in items)
    {
        UploadQueueItemDescriptor *itemDesc = [[UploadQueueItemDescriptor alloc] init];
        itemDesc.uuid = item.uuid;
        itemDesc.itemNumber = totalVisibleItemsToUpload;
        [queuedItems addObject:itemDesc];
        
        if (item.relRequiredBy == nil || item.relRequiredBy.count == 0)
            totalVisibleItemsToUpload++;
    }

    self.currentItemIndex = 0;
    self.lastItemIndex = queuedItems.count - 1;

    if (self.delegate != nil)
        [self.delegate willUploadQueue];
    
    if (queuedItems.count == 0)
    {
        [self didUploadAllItems];
        // MOB-11276
        [Flurry endTimedEvent:@"Offline: Upload" withParameters:nil];
    }
    else
    {
        UploadQueueAlertView *alertView = [[UploadQueueAlertView alloc] initForUpload];
        [alertView willUploadItemNumber:0 totalItems:self.totalVisibleItemsToUpload];
        [alertView show];
        [self uploadNextItem];
    }
}

-(void) uploadNextItem
{
    EntityUploadQueueItem *item = nil;
    UploadQueueItemDescriptor *currentItemDesc = nil;
    
    //
    // Beginning at the current item, walk through the array of UUIDs
    // until we find a matching queued item in core data.
    //
    // Note: it is possible that the UUID will not match an item in the queue
    // if the item has already been removed from the queue.  It could have been
    // removed a number of ways, including the completion of a prior attempt
    // to upload it.
    //
    while (currentItemIndex <= lastItemIndex)
    {
        currentItemDesc = queuedItems[currentItemIndex];
        item = [[UploadQueueItemManager sharedInstance] fetchByUUID:currentItemDesc.uuid];
        
        if (item != nil)
            break;
        
        currentItemIndex++;
    }
    
    if (item != nil)
    {
        if (self.delegate != nil)
        {
            [self.delegate willUploadItemNumber:currentItemDesc.itemNumber totalItems:self.totalVisibleItemsToUpload];
        }
        
        id<UploadableItem> uploadableItem = [UploadableItemMap uploadableItemForEntityInstanceId:item.entityInstanceId entityTypeName:item.entityTypeName inContext:self.managedObjectContext];
        
        if (uploadableItem == nil)
            [self didFailToUploadItemWithUUID:item.uuid];
        else
            [uploadableItem uploadItemWithUUID:item.uuid delegate:self];
    }
    else
    {
        [self didUploadAllItems];
    }
}

-(void) didUploadAllItems
{
    [self.uploadQueueVCDelegatedelegate didDismissUploadQueueVC];
    [self updateAfterUpload];
}

#pragma mark - Update Methods

-(void) updateAfterUpload
{
    BOOL wasUploadCancelled = (lastItemIndex < queuedItems.count - 1);
    
    if (self.delegate != nil)
        [self.delegate willUpdateAfterUpload:wasUploadCancelled];

    self.helper.queue = nil;
    // do not refresh with a network call.
    //self.helper = [[UploadQueueHelper alloc] initWithQueue:self];
    //[self.helper updateAfterUpl ad];
    [self didUpdateAfterUpload];
}

-(void) didUpdateAfterUpload
{
    BOOL wasUploadCancelled = (lastItemIndex < queuedItems.count - 1);

    if (self.delegate != nil)
    {
        [self.delegate didUpdateAfterUpload:wasUploadCancelled];
        [self.delegate didUploadQueue:wasUploadCancelled];
    }
    
    [self reset];
}

-(BOOL) isUpdatingAfterUpload
{
    return (self.helper != nil);
}

#pragma mark - Cancellation Methods

-(void) cancelUpload
{
    // Cancellation is an *asynchronous* operation.  When this method is called, we mark the item that is currently being uploaded as the last one that needs uploading; this will prevent further uploading.  After the last item has been uploaded, the updating will begin.  Updating entails fetching the quick expense list, receipt store list, etc.  It is  necessary to fetch these lists after uploading so that they will contain all the items that were successfully uploaded.  Since the fetching of these lists is asynchronous, cancellation is asynchronous.
    
    if (self.state == UploadQueueStateUploading)
    {
        self.state = UploadQueueStateCancelling;
        
        // Starting with the current item
        int index = self.currentItemIndex;
        
        // Walk through the items until we find one that is not required by another queued item.  This is the index of the last item that we will upload due to the cancellation.
        // For example, if the current item is a receipt, and we discover that it is required by another item (e.g., a quick expense), then we will not stop at the receipt.  We will keep going until we find an item, say a quick expense, that is not required by anything else.  That will be the last item uploaded due to the cancellation.
        // The intent is to avoid having non-uploaded items dependent upon uploaded items.  For example, we do not want a queued quick expense being dependent on non-queued receipt.
        
        while (index < self.lastItemIndex) // Purposely using the < operator rather than the <= operator.  There is no need to check anymore if we reach the last item.  And we don't want to overshoot the lastItemIndex or we will have an invalid index.
        {
            UploadQueueItemDescriptor *itemDesc = queuedItems[index];
            EntityUploadQueueItem *item = [[UploadQueueItemManager sharedInstance] fetchByUUID:itemDesc.uuid];
            
            if (item == nil || item.relRequiredBy == nil || item.relRequiredBy.count == 0)
                break; // We the item at which we can stop uploading
            
            index++;
        }
        
        self.lastItemIndex = index;
    }
    // MOB-11276
    NSDictionary *dict = @{@"How many uploaded": [NSString stringWithFormat:@"%i", self.currentItemIndex]};
    [Flurry logEvent:@"Offline: Upload Cancel" withParameters:dict];
    
}

#pragma mark - Private Helper Methods

-(void) reset
{
    self.queuedItems = [[NSMutableArray alloc] init];
    self.currentItemIndex = 0;
    self.lastItemIndex = 0;
    self.totalVisibleItemsToUpload = 0;
    self.state = UploadQueueStateIdle;
    
    if (self.helper != nil)
        self.helper.queue = nil;
    
    self.helper = nil;
}

-(void) forceQuit
{
    [[MCLogging getInstance] log:@"UploadQueue::forceQuit" Level:MC_LOG_DEBU];
    
    if (self.state != UploadQueueStateIdle)
    {
        [self.delegate didForceQuitUpload];
    }
    
    [self reset];
}

#pragma mark - App State Notifications

-(void) onApplicationDidEnterBackground
{
    if (self.state != UploadQueueStateIdle)
    {
        [self forceQuit];
    }
}

#pragma mark - Notification Methods
+(void) postQueueCountChangeNotification
{
    int newCount = [[UploadQueue sharedInstance] visibleQueuedItemCount];
    NSDictionary *userDict = @{@"count": @(newCount)};
    [[NSNotificationCenter defaultCenter] postNotificationName:@"UploadQueueCountChanged" object:self userInfo:userDict];
}

#pragma mark - Queue Methods
+(EntityUploadQueueItem*) queueItemWithId:(NSString*)entityInstanceId entityTypeName:(NSString*)entityTypeName creationDate:(NSDate*)creationDate
{
    EntityUploadQueueItem *queuedItem = [[UploadQueueItemManager sharedInstance] queueItemId:entityInstanceId entityTypeName:entityTypeName creationDate:creationDate];
    
    [self postQueueCountChangeNotification];
    
    return queuedItem;
}

#pragma mark - Dequeue Methods

+(void) dequeue:(EntityUploadQueueItem*)item
{
    /*
    // No longer automatically dequeuing required items as this is no longer desireable for the current
    // use cases as of Nov 28, 2012.  If dequeuing of required items is needed in the future, then uncomment
    // this code, but make it ask the item that is passed in as an argument to this function whether a
    // cascading dequeuing is needed.  That way, it will be up to individual items.
    //
    // First dequeue the items required by the item that was passed into this method
    //
    if (item.relRequires != nil)
    {
        NSArray* requiredItems = item.relRequires.allObjects;
        if (requiredItems != nil)
        {
            for (EntityUploadQueueItem *requiredItem in requiredItems)
                [UploadQueue dequeue:requiredItem];
        }
    }
    */

    NSString *entityInstanceId = item.entityInstanceId;
    NSString *entityTypeName = item.entityTypeName;
    NSManagedObjectContext *context = item.managedObjectContext;

    // The dequeue the item that was passed into this method
    //
    [UploadQueueItemManager dequeueItem:item withContext:context];
    [UploadableItemMap didDequeueEntityInstanceId:entityInstanceId entityTypeName:entityTypeName inContext:context];
    
    [self postQueueCountChangeNotification];
}

#pragma mark - UploadableItemDelegate Methods

-(void) didUploadItemWithUUID:(NSString*)uuid details:(NSDictionary*)details
{
    EntityUploadQueueItem *item = [[UploadQueueItemManager sharedInstance] fetchByUUID:uuid];

    // Notify the items that require this item
    if (item != nil)
    {
        NSSet *itemsToNotify = item.relRequiredBy;
        if (itemsToNotify != nil)
        {
            for (EntityUploadQueueItem *itemToNotify in itemsToNotify)
            {
                id<UploadableItem> uploadableItemToNotify = [UploadableItemMap uploadableItemForEntityInstanceId:itemToNotify.entityInstanceId entityTypeName:itemToNotify.entityTypeName inContext:self.managedObjectContext];
                [uploadableItemToNotify didUploadRequiredItemWithUUID:uuid details:details];
            }
        }
    }
    

    // Check if the item is the one we're currently processing
    UploadQueueItemDescriptor *currentItemDesc = nil;
    BOOL isCurrentItem = NO;
    
    if (self.state != UploadQueueStateIdle) //queuedItems is empty if we're in the idle state
    {
        currentItemDesc = queuedItems[currentItemIndex];
        isCurrentItem = [currentItemDesc.uuid isEqualToString:uuid];
    }
    
    if (isCurrentItem)
    {
        // If we're in the uploading state, then notify the delegate that this item was uploaded
        if (self.state == UploadQueueStateUploading)
        {
            if (self.delegate != nil)
            {
                [self.delegate didUploadItemNumber:currentItemDesc.itemNumber totalItems:self.totalVisibleItemsToUpload];
            }
        }
        
        self.currentItemIndex++;
    }
    
    // Delete the item from the upload queue in core data.
    if (item != nil)
        [UploadQueue dequeue:item];

    if (isCurrentItem)
    {
        // Upload the next item
        [self uploadNextItem];
    }
}

-(void) didFailToUploadItemWithUUID:(NSString*)uuid
{
    // Check if the item is the one we're currently processing
    UploadQueueItemDescriptor *currentItemDesc = nil;
    BOOL isCurrentItem = NO;
    
    if (self.state != UploadQueueStateIdle) //queuedItems is empty if we're in the idle state
    {
        currentItemDesc = queuedItems[currentItemIndex];
        isCurrentItem = [currentItemDesc.uuid isEqualToString:uuid];
    }
    
    if (isCurrentItem)
    {
        // If we're in the uploading state, then notify the delegate that this item was uploaded
        if (self.state == UploadQueueStateUploading)
        {
            if (self.delegate != nil)
            {
                [self.delegate didFailToUploadItemNumber:currentItemDesc.itemNumber totalItems:self.totalVisibleItemsToUpload];
            }
        }
        
        self.currentItemIndex++;
        
        // Upload the next item
        [self uploadNextItem];
    }
}

#pragma mark - Queued items fetch methods - filtered to those visible to user

-(int) visibleQueuedItemCount
{
    return [self fetchQueuedItemsForTopLevelItemsOnly:YES].count;
}

-(NSFetchRequest*) makeFetchRequestForVisibleQueuedItemsInContext:(NSManagedObjectContext*)context
{
    return [self makeFetchRequestForQueuedItemsInContext:context topLevelItemsOnly:YES];
}

#pragma mark - Queued items fetch methods - all queued items

//
// For consistency, the upload UI and upload backend should fetch queued items with this method
// to ensure a consistent, ordered result set.
//
-(NSFetchRequest*) makeFetchRequestForQueuedItemsInContext:(NSManagedObjectContext*)context topLevelItemsOnly:(BOOL)topLevelItemsOnly
{
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"EntityUploadQueueItem" inManagedObjectContext:context];
    
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    [request setEntity:entityDescription];
    
    NSMutableString *conditions = [NSMutableString stringWithFormat:@"((loginId == NULL) OR (loginId ==[c] '%@'))", [ExSystem sharedInstance].userName];// [c] denotes case-insensitivity
    
    if (topLevelItemsOnly == YES)
        [conditions appendString:@" AND (relRequiredBy.@count == 0)"];
    
    NSPredicate *predicate = [NSPredicate predicateWithFormat:conditions];
    [request setPredicate:predicate];

    // Descending order by creation date
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"creationDate" ascending:NO];
    [request setSortDescriptors:@[sortDescriptor]];
    
    return request;
}

- (NSArray*) fetchQueuedItemsForTopLevelItemsOnly:(BOOL)topLevelItemsOnly
{
    NSFetchRequest *fetchRequest = [self makeFetchRequestForQueuedItemsInContext:self.managedObjectContext topLevelItemsOnly:topLevelItemsOnly];
    
    NSError *error = nil;
    NSArray *queuedItemsArray = [self.managedObjectContext executeFetchRequest:fetchRequest error:&error];
    
    return queuedItemsArray;
}

-(void) writeToFlurry
{
    int totalReceipts = 0;
    int totalQuickExpenses = 0;
    int totalQuickExpensesWithReceipts = 0;
    
    NSArray *items = [self fetchQueuedItemsForTopLevelItemsOnly:NO]; // Gets all items in the queue, regardless of whether they are visible to the user
    for (EntityUploadQueueItem *item in items)
    {
        if ([item.entityTypeName isEqualToString:@"EntityMobileEntry"])
        {
            totalQuickExpenses++;
            
            if (item.relRequires != nil && item.relRequires.count > 0)
                totalQuickExpensesWithReceipts++;
        }
        else if ([item.entityTypeName isEqualToString:@"Receipt"])
            totalReceipts++;
    }
    
    //log to flurry
    NSDictionary *pBag = @{@"Queued Receipts": @(totalReceipts), @" Queued Mobile Entries": @(totalQuickExpenses), @"total Queued Mobile Entries with Receipts": @(totalQuickExpensesWithReceipts)};
    [Flurry logEvent:@"Offline: Queue View" withParameters:pBag];
}

-(void) log
{
    [[MCLogging getInstance] log:@"Upload Queue Items:" Level:MC_LOG_DEBU];

    NSArray *items = [self fetchQueuedItemsForTopLevelItemsOnly:NO];
    for (EntityUploadQueueItem *item in items)
    {
        [[MCLogging getInstance] log:@"    Item: " Level:MC_LOG_DEBU];
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"        uuid             %@", item.uuid] Level:MC_LOG_DEBU];
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"        entityTypeName   %@", item.entityTypeName] Level:MC_LOG_DEBU];
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"        entityInstanceId %@", item.entityInstanceId] Level:MC_LOG_DEBU];
        
        if (item.relRequires != nil && item.relRequires.count > 0)
        {
            [[MCLogging getInstance] log:@"        requires" Level:MC_LOG_DEBU];
            
            for (EntityUploadQueueItem *requiredItem in item.relRequires)
            {
                [[MCLogging getInstance] log:[NSString stringWithFormat:@"            uuid %@", requiredItem.uuid] Level:MC_LOG_DEBU];
            }
        }
        
        if (item.relRequiredBy != nil && item.relRequiredBy.count > 0)
        {
            [[MCLogging getInstance] log:@"        required by" Level:MC_LOG_DEBU];
            
            for (EntityUploadQueueItem *requiredByItem in item.relRequiredBy)
            {
                [[MCLogging getInstance] log:[NSString stringWithFormat:@"            uuid %@", requiredByItem.uuid] Level:MC_LOG_DEBU];
            }
        }
    }
}

@end
