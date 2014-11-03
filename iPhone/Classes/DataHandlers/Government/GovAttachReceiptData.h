//
//  GovAttachReceiptData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/27/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ActionStatus.h"

@interface GovAttachReceiptData : MsgResponderCommon
{
    NSString            *receiptId;
    NSString            *expId; // ccExpId, if docName is null
    NSString            *docName;
    NSString            *docType;
    
    ActionStatus        *status;
}

@property (strong, nonatomic) NSString          *receiptId;
@property (strong, nonatomic) NSString          *expId;
@property (strong, nonatomic) NSString          *docName;
@property (strong, nonatomic) NSString          *docType;
@property (strong, nonatomic) ActionStatus      *status;

@end
