//
//  ReceiptDownloader.h
//  ConcurMobile
//
//  Created by charlottef on 11/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReceiptDownloaderDelegate.h"
#import "ExMsgRespondDelegate.h"

@interface ReceiptDownloader : NSObject <ExMsgRespondDelegate>
{
    id<ReceiptDownloaderDelegate> __weak _delegate;
    
    NSString    *receiptId;
    NSString    *url;
    NSString    *dataType;
    BOOL        thumbNail;
    NSDate      *creationDate;
    BOOL        useHttpPost;
}

@property (weak, nonatomic) id<ReceiptDownloaderDelegate> delegate;

@property (strong, nonatomic) NSString  *receiptId;
@property (strong, nonatomic) NSString  *url;
@property (strong, nonatomic) NSString  *dataType;
@property (assign, nonatomic) BOOL      thumbNail;
@property (strong, nonatomic) NSDate    *creationDate;
@property BOOL                          userHttpPost;

// Return value indicates whether the download will be attempted
+(BOOL) downloadReceiptForId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url delegate:(id<ReceiptDownloaderDelegate>)delegate;
+(BOOL) downloadReceiptForId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url delegate:(id<ReceiptDownloaderDelegate>)delegate useHttpPost:(BOOL)usePost;

@end
