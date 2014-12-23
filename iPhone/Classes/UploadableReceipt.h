//
//  UploadableReceipt.h
//  ConcurMobile
//
//  Created by charlottef on 11/5/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UploadableItem.h"
#import "ExMsgRespondDelegate.h"

@interface UploadableReceipt : NSObject <UploadableItem, ExMsgRespondDelegate>
{
    NSString *localReceiptImageId;
}

@property (nonatomic, weak) id<UploadableItemDelegate> uploadableItemDelegate;
@property (nonatomic, strong) NSString* localReceiptImageId;


+(NSString*) filePathForLocalReceiptImageId:(NSString*)localReceiptImageId isPdfReceipt:(BOOL)isPdfReceipt;

+(UIImage*) imageForLocalReceiptImageId:(NSString*)localReceiptImageId;

-(id) initWithLocalReceiptImageId:(NSString*)localId;

+(void) didDequeueEntityInstanceId:(NSString*)entityInstanceId isPdfReceipt:(BOOL)isPdfReceipt;

+(NSData*) receiptDataForLocalReceiptImageId:(NSString*)localReceiptImageId;

@end
