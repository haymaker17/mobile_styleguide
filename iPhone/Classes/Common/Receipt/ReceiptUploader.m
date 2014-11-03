//
//  ReceiptUploader.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//
//  A light weight receipt uploader

#import "ReceiptUploader.h"
#import "ImageUtil.h"
#import "UploadReceiptData.h"
#import "ReceiptCache.h"
#import "Config.h"
#import "WaitViewController.h"

@interface ReceiptUploader ()
{
    BOOL   isUploadPdf;
}
@end

@implementation ReceiptUploader

@synthesize delegate = _delegate;
@synthesize receiptImage, receiptImageId, pdfData;

-(void)setSeedData:(id<ReceiptUploaderDelegate>) del withImage:(UIImage*) img
{
    self.delegate = del;
    self.receiptImageId = nil;
    self.receiptImage = img;
}

-(void)setSeedDataPdf:(id<ReceiptUploaderDelegate>) del withPdf:(NSData*) data
{
    self.delegate = del;
    self.receiptImageId = nil;
    self.pdfData = data;
}

-(NSData *) prepareImageDataFromReceiptImage:(UIImage*)receiptImg
{
    // MOB-10877 For iOS 6 only, receiptImage.imageOrientation is 3 - UIImageOrientationRight.
    if ([ExSystem is6Plus])
    {
        UIImage* correctedImage = nil;
        if(UIImageOrientationRight == [receiptImage imageOrientation])
        {
            correctedImage = [ImageUtil fixOrientation:receiptImage];
            if (correctedImage != nil)
                receiptImg = correctedImage;
        }
    }
    
    NSData *imgData = nil;
	if (receiptImage != nil)
	{
        imgData = UIImageJPEGRepresentation(receiptImg, 0.9f);
        //        imgData = nil;//###MOB-9416##test only
    }
    
    return imgData;
}

-(void)startUpload
{
    imageData = [self prepareImageDataFromReceiptImage:self.receiptImage];
    if (imageData == nil)
    {   // MOB-9416 validate image before uploading
        [self.delegate failedToPrepareImageData]; // memory issue
    }
    else
    {
        NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/SaveReceipt/%@", [ExSystem sharedInstance].entitySettings.uri, @"mobile"];
        NSMutableDictionary *pBag  = nil;
        pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:path,@"URL",imageData,@"IMAGE",nil];
        [[ExSystem sharedInstance].msgControl createMsg:UPLOAD_IMAGE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

-(void)startUploadPdf
{
    if (self.pdfData == nil)
    {   
        [self.delegate failedToPrepareImageData]; 
    }
    
    isUploadPdf = YES;
    NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/SaveReceipt/%@", [ExSystem sharedInstance].entitySettings.uri, @"mobile"];
    NSMutableDictionary *pBag  = nil;
    pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:path,@"URL",self.pdfData,@"PDF",nil];
    [[ExSystem sharedInstance].msgControl createMsg:UPLOAD_IMAGE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

#pragma mark -
#pragma mark Handle Messages
-(void) didProcessMessage:(Msg *)msg
{
	if ([msg.idKey isEqualToString:UPLOAD_IMAGE_DATA])
	{
		UploadReceiptData *receiptData = (UploadReceiptData*)msg.responder;
        if (msg.errBody == nil && [receiptData.returnStatus isEqualToString:@"SUCCESS"])
        {
            ReceiptCache *receiptCache = [ReceiptCache sharedInstance];
            if (isUploadPdf)
            {
                [receiptCache cacheFullSizeReceiptData:self.pdfData dataType:@"pdf" receiptId:receiptData.receiptImageId];
                // MOB - 13241 The imaging server needs 4+ second delay between pdf upload to receipt store and add receiptId to report.
                // The delay is not needed for regular image receipts.
                [self performSelector:@selector(attachReceiptToReport:) withObject:receiptData afterDelay:0.0f];
            }
            else
            {
                [receiptCache cacheFullSizeReceiptData:imageData dataType:@"jpeg" receiptId:receiptData.receiptImageId];
                [self.delegate receiptUploadSucceeded:receiptData.receiptImageId];
            }
        }
        else
        {
            NSString *status = msg.errBody;
            if (status == nil)
                status = receiptData.returnStatus;
            
            NSDictionary *dict = @{@"Failure": @"Failed to upload receipt image"};
            [Flurry logEvent:@"Receipts: Failure" withParameters:dict];
    
            // MOB-9653 handle imaging not configured error
            [self.delegate failedToUploadImage:status]; // e.g. @"Imaging Configuration Not Available."
        }
        [WaitViewController hideAnimated:YES withCompletionBlock:nil];
	}
}

-(void) attachReceiptToReport:(UploadReceiptData*) receiptData
{
    [self.delegate receiptUploadSucceeded:receiptData.receiptImageId];
}

@end
