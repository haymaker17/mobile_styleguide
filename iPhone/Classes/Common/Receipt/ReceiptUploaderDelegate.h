//
//  ReceiptUploaderDelegate.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol ReceiptUploaderDelegate <NSObject>

-(void) failedToPrepareImageData; // memory issue
-(void) failedToUploadImage:(NSString*) errorStatus; // e.g. Imaging not configured
-(void) receiptUploadSucceeded:(NSString*) receiptImageId;

@end
