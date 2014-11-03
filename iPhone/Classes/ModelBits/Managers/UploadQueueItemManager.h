//
//  UploadQueueItemManager.h
//  ConcurMobile
//
//  Created by charlottef on 10/30/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "BaseManager.h"
#import "EntityUploadQueueItem.h"

@interface UploadQueueItemManager : BaseManager
{
/*
 Notes about EntityUploadQueueItem
 
 The 'relRequires' attribute is a set which can be empty or can contain instances of EntityUploadQueueItem.  By adding EntityUploadQueueItems to this set, you are informing the upload queue that your item requires the upload of the items you placed in the relRequires set.
 
 After an item in your 'relRequires' set is uploaded, the UploadQueue will inform you by calling your didUploadRequiredItemWithUUID method (declared in the UploadableItem protocol).
 
 Note that as of November 2012, relRequires only affects notifications, as was just described, and which items will show up in the queue UI.  Items that are marked as being required by other items do not appear in the queue UI.  For example, a receipt that appears in the relRequires set of a quick expense will not appear in the UI.  Instead, only the quick exense will appear in the UI.
 
 In the future, the UploadQueue would ideally use the relRequires attribute to automatically determine upload order.  But as of November, 2012, that does not happen.  Therefore items are uploaded in the exact orer they are added to the queue.
*/
}

+(UploadQueueItemManager*) sharedInstance;

-(EntityUploadQueueItem*) queueItemId:(NSString*)entityInstanceId entityTypeName:(NSString*)entityTypeName creationDate:(NSDate*)creationDate;
+(void) dequeueItem:(EntityUploadQueueItem*)item withContext:(NSManagedObjectContext*)managedObjectContext;

-(EntityUploadQueueItem *) fetchByUUID:(NSString*)uuid;
-(EntityUploadQueueItem*) fetchByEntityInstanceId:(NSString*)entityInstanceId entityTypeName:(NSString*)entityTypeName;

-(BOOL) isQueuedEntityInstanceId:(NSString*)entityInstanceId entityTypeName:(NSString*)entityTypeName;

@end
