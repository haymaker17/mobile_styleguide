//
//  ReceiptStoreReceipt.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/8/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReceiptStoreReceipt.h"


@implementation ReceiptStoreReceipt
@synthesize fileName,fileType,imageDate,imageOrigin,receiptImageId,imageUrl,thumbUrl,thumbImage,fullScreenImage,commentTag, pdfData;

-(ReceiptStoreReceipt*)init
{
    self = [super init];
	if(self)
	{
		self.fileName = nil;
		self.fileType = nil;
		self.imageDate = nil;
		self.imageOrigin = nil;
		self.imageUrl = nil;
		self.receiptImageId = nil;
		self.thumbUrl = nil;
		self.thumbImage = nil;
        self.pdfData = nil;
		self.fullScreenImage = nil;
        self.commentTag = nil;
	}
	return self;
}


@end
