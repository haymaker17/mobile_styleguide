//
//  PaymentVendor.h
//  ConcurMobile
//
//  Created by yiwen on 9/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface PaymentVendor : NSObject {
	NSString *venKey, *vendorName;
	NSString *address1, *city, *ctryCode, *ctryName, *postalCode, *state;
	NSString *creationDate;
	NSString *venCode, *vendorAddrCode;
}

@property (strong, nonatomic) NSString *venKey;
@property (strong, nonatomic) NSString *vendorName;
@property (strong, nonatomic) NSString *address1;
@property (strong, nonatomic) NSString *city;
@property (strong, nonatomic) NSString *ctryCode;
@property (strong, nonatomic) NSString *ctryName;
@property (strong, nonatomic) NSString *postalCode;
@property (strong, nonatomic) NSString *state;
@property (strong, nonatomic) NSString *creationDate;
@property (strong, nonatomic) NSString *venCode;
@property (strong, nonatomic) NSString *vendorAddrCode;

- (id)initWithCoder:(NSCoder *)coder;
- (void)encodeWithCoder:(NSCoder *)coder;


@end
