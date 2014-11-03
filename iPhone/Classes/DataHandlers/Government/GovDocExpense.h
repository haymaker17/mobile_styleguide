//
//  GovDocExpense.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovDocExpense : NSObject
{
    NSString            *expId;
    NSDate              *expDate;
    NSString            *expenseDesc;
    NSDecimalNumber     *amount;
    NSString            *paymentMethod;
    NSNumber            *reimbursable;
    NSNumber            *exception;
    NSString            *exceptionCmt;
    NSString            *expenseCategory;
    NSString            *imageId;
}

@property (nonatomic, strong) NSString              *expId;
@property (nonatomic, strong) NSString              *expenseDesc;
@property (nonatomic, strong) NSDecimalNumber       *amount;
@property (nonatomic, strong) NSDate                *expDate;
@property (nonatomic, strong) NSString              *paymentMethod;
@property (nonatomic, strong) NSString              *exceptionCmt;
@property (nonatomic, strong) NSNumber              *reimbursable;
@property (nonatomic, strong) NSNumber              *exception;
@property (nonatomic, strong) NSString              *expenseCategory;
@property (nonatomic, strong) NSString              *imageId;
@end
