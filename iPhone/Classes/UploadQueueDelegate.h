//
//  UploadQueueDelegate.h
//  ConcurMobile
//
//  Created by charlottef on 10/30/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol UploadQueueDelegate <NSObject>

#pragma mark - Queue Methods
-(void) willUploadQueue;
-(void) didUploadQueue:(BOOL)wasUploadCancelled;

#pragma mark - Item Methods
-(void) willUploadItemNumber:(int)itemNumber totalItems:(int)totalItems;
-(void) didUploadItemNumber:(int)itemNumber totalItems:(int)totalItems;
-(void) didFailToUploadItemNumber:(int)itemNumber totalItems:(int)totalItems;

#pragma mark - Update Methods
-(void) willUpdateAfterUpload:(BOOL)wasUploadCancelled;
-(void) didUpdateAfterUpload:(BOOL)wasUploadCancelled;

#pragma mark - Force Quit Methods
-(void) didForceQuitUpload;

@end
