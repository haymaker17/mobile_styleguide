//
//  PaymentVendor.m
//  ConcurMobile
//
//  Created by yiwen on 9/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "PaymentVendor.h"


@implementation PaymentVendor
@synthesize	venKey, vendorName;
@synthesize address1, city, ctryCode, ctryName, postalCode, state;
@synthesize creationDate, venCode, vendorAddrCode;



#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
	[coder encodeObject:venKey	forKey:@"venKey"];
	[coder encodeObject:vendorName	forKey:@"vendorName"];
	[coder encodeObject:address1	forKey:@"address1"];
	[coder encodeObject:city	forKey:@"city"];
	[coder encodeObject:ctryCode	forKey:@"ctryCode"];
	[coder encodeObject:ctryName	forKey:@"ctryName"];
	[coder encodeObject:postalCode	forKey:@"postalCode"];
	[coder encodeObject:state	forKey:@"state"];
	[coder encodeObject:creationDate	forKey:@"creationDate"];
}

- (id)initWithCoder:(NSCoder *)coder {
	self.venKey = [coder decodeObjectForKey:@"venKey"];
	self.vendorName = [coder decodeObjectForKey:@"vendorName"];
	self.address1 = [coder decodeObjectForKey:@"address1"];
	self.city = [coder decodeObjectForKey:@"city"];
	self.ctryCode = [coder decodeObjectForKey:@"ctryCode"];
	self.ctryName = [coder decodeObjectForKey:@"ctryName"];
	self.postalCode = [coder decodeObjectForKey:@"postalCode"];
	self.state = [coder decodeObjectForKey:@"state"];
	self.creationDate = [coder decodeObjectForKey:@"creationDate"];
	
    return self;
}

@end
