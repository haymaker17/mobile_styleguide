//
//  GovTANumber.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovTANumber : NSObject
@property (nonatomic, strong) NSString      *docName;
@property (nonatomic, strong) NSString      *docType;
@property (nonatomic, strong) NSString      *tANumber;
@property (nonatomic, strong) NSString      *tAType;
@property (nonatomic, strong) NSString      *purposeCode;
@property (nonatomic, strong) NSDate        *tripBeginDate;
@property (nonatomic, strong) NSDate        *tripEndDate;
@property (nonatomic, strong) NSString      *tALabel;
@property (nonatomic, strong) NSString      *pdmLocation;

@end
