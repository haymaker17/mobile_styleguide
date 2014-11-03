//
//  UploadableItemDelegate.h
//  ConcurMobile
//
//  Created by charlottef on 10/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityUploadQueueItem.h"

@protocol UploadableItemDelegate <NSObject>

-(void) didUploadItemWithUUID:(NSString*)uuid details:(NSDictionary*)details;
-(void) didFailToUploadItemWithUUID:(NSString*)uuid;

@end
