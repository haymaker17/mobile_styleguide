//
//  CarChain.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface CarChain : NSObject
{
	NSString	*code;
	NSString	*name;
	NSString	*imageUri;
}

@property (nonatomic, strong) NSString	*code;
@property (nonatomic, strong) NSString	*name;
@property (nonatomic, strong) NSString	*imageUri;

@end
