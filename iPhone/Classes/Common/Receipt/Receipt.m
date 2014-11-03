//
//  Receipt.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//
//  Receipt object contains all things associated with a receipt: receipt id, image, filecache path, url.

#import "Receipt.h"

@implementation Receipt
@synthesize receiptId, localReceiptId, url, cachedFilePath, cacheId, receiptImg, pdfData, dataType, useHttpPost;

-(BOOL) isPDF
{
    return dataType != nil && [dataType caseInsensitiveCompare:@"pdf"] == NSOrderedSame;
}

-(BOOL) hasReceipt
{
    return receiptImg != nil || [self isPDF] || [receiptId lengthIgnoreWhitespace] || [cacheId lengthIgnoreWhitespace];
}

-(BOOL) isLoaded
{
    return receiptImg != nil || pdfData != nil;
}

- (NSString*)fileCacheKey
{
    NSString *rcptId = self.receiptId;
    if (![rcptId length])
        rcptId = self.cacheId;
    
    return rcptId;
}

@end
