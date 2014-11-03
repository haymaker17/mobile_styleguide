//
//  CarVendorDescriptor.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface CarVendorDescriptor : NSObject
{
	NSString		*vendorName;
	NSMutableArray	*cars;
}

@property (nonatomic, strong) NSString			*vendorName;
@property (nonatomic, strong) NSMutableArray	*cars;

@end
