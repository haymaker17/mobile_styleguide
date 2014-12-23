//
//  CTEOCRExpense.h
//  ConcurSDK
//
//  Created by Sally Yan on 11/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEError.h"
#import "CTENetworking.h"
#import "CTEOCRExpenseDetails.h"

@interface CTEOCRExpense : NSObject

-(id)initWithReceiptImageID:(NSString *)receiptImageID;
-(void)startOCRWithSuccess:(void(^)(CTEOCRExpenseDetails *expenseDetails)) success failure:(void (^)(CTEError *error))failure;

@end
