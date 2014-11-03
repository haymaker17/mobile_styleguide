//
//  WeakReference.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 11/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "WeakReference.h"


@implementation WeakReference
@synthesize ref;

+(WeakReference*) toObject:(NSObject*)object
{
	__autoreleasing WeakReference* weakRef = [WeakReference alloc];
	weakRef.ref = object;
	return weakRef;
}

@end
