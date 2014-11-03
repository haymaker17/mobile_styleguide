//
//  UploadableMobileEntry.h
//  ConcurMobile
//
//  Created by charlottef on 10/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UploadableItem.h"
#import "EntityMobileEntry.h"
#import "ExMsgRespondDelegate.h"

@interface UploadableMobileEntry : NSObject <UploadableItem, ExMsgRespondDelegate>
{
    NSString    *localId;
    NSString    *localReceiptImageId;
    EntityMobileEntry *meEntry;
    
    NSManagedObjectContext *managedObjectContext;
}

@property (nonatomic, weak) id<UploadableItemDelegate> uploadableItemDelegate;
@property (nonatomic, strong) NSString* localId;
@property (nonatomic, strong) NSString* localReceiptImageId;
@property (nonatomic, strong) EntityMobileEntry* meEntry;
@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;

-(id) initWithEntityInstanceId:(NSString*)entityInstanceId inContext:(NSManagedObjectContext*)context;

+(void) didDequeueEntityInstanceId:(NSString*)entityInstanceId inContext:(NSManagedObjectContext*)context;

@end
