//
//  ReceiptUploader.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExMsgRespondDelegate.h"
#import "ReceiptUploaderDelegate.h"

@interface ReceiptUploader : NSObject <ExMsgRespondDelegate>
{
    UIImage                         *receiptImage;
    NSString                        *receiptImageId;
    NSData                          *imageData;
    id<ReceiptUploaderDelegate>     __weak _delegate;
}

@property (nonatomic, strong) UIImage                       *receiptImage;
@property (nonatomic, strong) NSString                      *receiptImageId;
@property (nonatomic, strong) NSData                        *pdfData;
@property (nonatomic, weak) id<ReceiptUploaderDelegate>		delegate;

-(void)setSeedData:(id<ReceiptUploaderDelegate>) del withImage:(UIImage*) receiptImage;
-(void)setSeedDataPdf:(id<ReceiptUploaderDelegate>) del withPdf:(NSData*) pdfData;
-(void)startUpload;
-(void)startUploadPdf;
@end
