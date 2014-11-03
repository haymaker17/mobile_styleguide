//
//  CardTransaction.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 9/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "OOPEntry.h"

@interface CardTransaction : OOPEntry
{
	NSString*	smartExpenseMeKey;
}

@property (nonatomic, strong) NSString*	smartExpenseMeKey;

@end
