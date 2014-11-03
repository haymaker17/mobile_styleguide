//
//  DeliveryData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface DeliveryData : NSObject {
	NSString	*fee, *name, *type;
}

@property (strong, nonatomic) NSString	*fee;
@property (strong, nonatomic) NSString	*name;
@property (strong, nonatomic) NSString	*type;
@end
