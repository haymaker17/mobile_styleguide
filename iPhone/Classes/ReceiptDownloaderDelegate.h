//
//  ReceiptDownloaderDelegate.h
//  ConcurMobile
//
//  Created by charlottef on 11/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol ReceiptDownloaderDelegate <NSObject>

-(void) didDownloadReceiptId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url;

-(void) didFailToDownloadReceiptId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url;

@end
