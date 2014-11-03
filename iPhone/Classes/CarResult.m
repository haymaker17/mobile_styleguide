//
//  CarResult.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CarResult.h"


@implementation CarResult

-(id)init
{
	self = [super init];
    if (self)
    {
        self.violations = [[NSMutableArray alloc] init];
        self.sendCreditCard = TRUE; //allowed sending credit card info to prevent failure
    }
	return self;
}


@end
