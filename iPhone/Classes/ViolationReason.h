//
//  ViolationReason.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface ViolationReason : NSObject
{
	NSString	*code;
	NSString	*description;
    NSString    *violationType;
}

@property (nonatomic, strong) NSString	*code;
@property (nonatomic, strong) NSString	*description;
@property (nonatomic, strong) NSString	*violationType;

@end
