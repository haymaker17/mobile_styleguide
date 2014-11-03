//
//  UploadQueue.h
//  ConcurMobile
//
//  Created by charlottef on 10/30/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UploadQueueDelegate.h"
#import "UploadQueueVCDelegate.h"
#import "UploadableItemDelegate.h"

@class UploadQueueHelper;

typedef enum uploadQueueState
{
    UploadQueueStateIdle,
    UploadQueueStateUploading,
    UploadQueueStateCancelling
 } UploadQueueState;

@interface UploadQueue : NSObject <UploadableItemDelegate, UploadQueueDelegate>
{
    UploadQueueState state;
    
    NSMutableArray  *queuedItems;
    int             currentItemIndex;
    int             lastItemIndex;
    
    int             totalVisibleItemsToUpload;
    
    UploadQueueHelper *helper;
}

+(UploadQueue*) sharedInstance;

@property (weak, nonatomic) id<UploadQueueDelegate>	delegate;
@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;

@property (nonatomic, assign) UploadQueueState state;

@property (nonatomic, strong) NSMutableArray *queuedItems;
@property (nonatomic, assign) int currentItemIndex;
@property (nonatomic, assign) int lastItemIndex;

@property (nonatomic, assign) int totalVisibleItemsToUpload;

@property (nonatomic, strong) UploadQueueHelper *helper;

@property (weak, nonatomic) id<UploadQueueVCDelegate> uploadQueueVCDelegatedelegate;

-(void) startUpload;
-(void) cancelUpload;

+(EntityUploadQueueItem*) queueItemWithId:(NSString*)entityInstanceId entityTypeName:(NSString*)entityTypeName creationDate:(NSDate*)creationDate;
+(void) dequeue:(EntityUploadQueueItem*)item;

-(BOOL) isUpdatingAfterUpload;
-(void) didUpdateAfterUpload;

-(void) onApplicationDidEnterBackground;

-(int) visibleQueuedItemCount;
-(NSFetchRequest*) makeFetchRequestForVisibleQueuedItemsInContext:(NSManagedObjectContext*)context;

-(void) log;

@end
