//
//  UploadableItem.h
//  ConcurMobile
//
//  Created by charlottef on 10/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityUploadQueueItem.h"

@protocol UploadableItemDelegate; // Forward declaration to prevent compiler error about circular imports.

@protocol UploadableItem <NSObject>

@required

#pragma mark - Upload Method
-(void) uploadItemWithUUID:(NSString*)uuid isPdfReceipt:(BOOL)isPdfReceipt delegate:(id<UploadableItemDelegate>)delegate;

#pragma mark - Notification Methods

@optional
-(void) didUploadRequiredItemWithUUID:(NSString*)uuid details:(NSDictionary*)details;

@end
