//
//  UploadQueueItemManager.m
//  ConcurMobile
//
//  Created by charlottef on 10/30/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "UploadQueueItemManager.h"
#import "PostMsgInfo.h"

@implementation UploadQueueItemManager

static UploadQueueItemManager *sharedInstance;

+(UploadQueueItemManager*)sharedInstance
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
				sharedInstance = [[UploadQueueItemManager alloc] init];
			}
		}
		return sharedInstance;
	}
}

-(EntityUploadQueueItem*) queueItemId:(NSString*)entityInstanceId entityTypeName:(NSString*)entityTypeName creationDate:(NSDate*)creationDate
{
    EntityUploadQueueItem *item = [self makeNew];
    item.creationDate = creationDate;
    item.uuid = [PostMsgInfo getUUID]; // Creates a UUID;
    item.entityTypeName = entityTypeName;
    item.entityInstanceId = entityInstanceId;
    item.loginId = [ExSystem sharedInstance].userName;
    [self saveIt:item];
    
    return item;
}

+(void) dequeueItem:(EntityUploadQueueItem*)item withContext:(NSManagedObjectContext*)managedObjectContext
{
    [managedObjectContext deleteObject:item];
    NSError *error;
    if (![managedObjectContext save:&error]) {
        NSLog(@"Whoops, couldn't delete object: %@", [error localizedDescription]);
    }
}

-(EntityUploadQueueItem *) makeNew
{
    return ((EntityUploadQueueItem *)[super makeNew:@"EntityUploadQueueItem"]);
}

-(void) deleteObj:(NSManagedObject *)obj
{
    NSManagedObjectContext *context = obj.managedObjectContext;
    [context deleteObject:obj];
    
    NSError *error = nil;
    if (![context save:&error])
        NSLog(@"Whoops, couldn't delete object: %@", [error localizedDescription]);
}

-(EntityUploadQueueItem *) fetchByUUID:(NSString*)uuid
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityUploadQueueItem" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(uuid = %@)", uuid];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    
    return nil;
}

-(EntityUploadQueueItem*) fetchByEntityInstanceId:(NSString*)entityInstanceId entityTypeName:(NSString*)entityTypeName
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityUploadQueueItem" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(entityInstanceId = %@ AND entityTypeName = %@)", entityInstanceId, entityTypeName];
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    
    return nil;
}

-(BOOL) isQueuedEntityInstanceId:(NSString*)entityInstanceId entityTypeName:(NSString*)entityTypeName
{
    return (nil != [self fetchByEntityInstanceId:entityInstanceId entityTypeName:entityTypeName]);
}

@end
