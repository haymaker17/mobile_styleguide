//
//  ReceiptStoreReceipt.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/8/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface ReceiptStoreReceipt : NSObject {
	NSString * fileName;
	NSString * fileType;
	NSString * imageDate;
	NSString * imageOrigin;
	NSString * receiptImageId;
	NSString * imageUrl;
	NSString * thumbUrl;
	UIImage	 * thumbImage;
	UIImage	 * fullScreenImage;
    NSString * commentTag;
}

@property (nonatomic,strong) NSString * fileName;
@property (nonatomic,strong) NSString * fileType;
@property (nonatomic,strong) NSString * imageDate;
@property (nonatomic,strong) NSString * imageOrigin;
@property (nonatomic,strong) NSString * receiptImageId;
@property (nonatomic,strong) NSString * imageUrl;
@property (nonatomic,strong) NSString * thumbUrl;
@property (nonatomic,strong) UIImage  * thumbImage;
@property (nonatomic,strong) UIImage  * fullScreenImage;
@property (nonatomic,strong) NSData   * pdfData;
@property (nonatomic,strong) NSString * commentTag;
@end
