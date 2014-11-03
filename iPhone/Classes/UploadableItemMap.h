//
//  UploadableItemMap.h
//  ConcurMobile
//
//  Created by charlottef on 10/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityUploadQueueItem.h"
#import "UploadableItem.h"

@interface UploadableItemMap : NSObject
{
}

+(id<UploadableItem>) uploadableItemForEntityInstanceId:(NSString*)entityInstanceId entityTypeName:(NSString*)entityTypeName inContext:(NSManagedObjectContext*)context;

+(void) didDequeueEntityInstanceId:(NSString*)entityInstanceId entityTypeName:(NSString*)entityTypeName inContext:(NSManagedObjectContext*)context;

@end
