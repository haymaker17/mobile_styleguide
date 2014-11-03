//
//  ListDataBase.m
//  ConcurMobile
//
//  Created by yiwen on 8/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ListDataBase.h"


@implementation ListDataBase
@synthesize objDict, keys, isInList;

-(id)init
{
	self = [super init];
    if (self)
    {
        objDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        keys = [[NSMutableArray alloc] initWithObjects:nil];
        isInList = NO;
    }
	return self;
}

-(void) flushData
{
	// Recreate data holder to avoid race condition in iOS4
	self.objDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];	
	self.keys = [[NSMutableArray alloc] initWithObjects:nil];

	isInList = NO;
}


@end
