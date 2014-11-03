//
//  GovLocation.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovLocation : NSObject
{
    NSString            *location;
    NSString            *locState;
    NSString            *linkLocation;
    NSString            *linkState;
    NSString            *locZip;
    NSString            *county;
    NSDate              *effectiveDate;
    NSDate              *expirationDate;
    BOOL                isUSContiguous;
}

@property (nonatomic, strong) NSString              *location;
@property (nonatomic, strong) NSString              *locState;
@property (nonatomic, strong) NSString              *linkLocation;
@property (nonatomic, strong) NSString              *linkState;
@property (nonatomic, strong) NSString              *locZip;
@property (nonatomic, strong) NSString              *county;
@property (nonatomic, strong) NSDate                *effectiveDate;
@property (nonatomic, strong) NSDate                *expirationDate;
@property BOOL                                      isUSContiguous;

@end
