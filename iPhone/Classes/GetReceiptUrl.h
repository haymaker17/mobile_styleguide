//
//  GetReceiptUrl.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/13/11.
//  Copyright 2011 Concur. All rights reserved.
//  
//  This class creates a msg to fetch receipt url for a receipt store receipt.

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "Msg.h"

@interface GetReceiptUrl : MsgResponderCommon {
	NSString		*receiptUrl;
    NSString        *fileType;      // PDF
	NSString		*status;
}
@property (nonatomic, strong) NSString			*status;
@property (nonatomic, strong) NSString			*fileType;
@property (nonatomic,strong) NSString			*receiptUrl;

@end
