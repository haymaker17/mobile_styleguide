//
//  Receipt.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Receipt : NSObject
{
    NSString        *receiptId;       // png
    NSString        *localReceiptId;  // local id of receipt when queued for offline (not on server yet)
    NSString        *url;             // for pdf
    NSString        *cachedFilePath;  // png, or PDF file
    NSString        *cacheId;         // key for EntityReceipt
    UIImage         *receiptImg;
    NSData          *pdfData;
    NSString        *dataType;
    BOOL            useHttpPost;
}

@property (nonatomic, retain) NSString                  *receiptId;
@property (nonatomic, retain) NSString                  *localReceiptId;
@property (nonatomic, retain) NSString                  *url;
@property (nonatomic, retain) NSString                  *cachedFilePath;
@property (nonatomic, retain) NSString                  *cacheId;
@property (nonatomic, retain) UIImage                   *receiptImg;
@property (nonatomic, retain) NSData                    *pdfData;
@property (nonatomic, retain) NSString                  *dataType;
@property BOOL                                          useHttpPost;

-(BOOL) isPDF;
-(BOOL) hasReceipt;
-(BOOL) isLoaded;
- (NSString*)fileCacheKey;

@end
