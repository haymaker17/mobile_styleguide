//
//  ItemizedToolbarHelper.h
//  ConcurMobile
//
//  Created by yiwen on 5/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntryData.h"

@interface ItemizedToolbarHelper : NSObject 
{
	NSString				*itemizedAmount, *remainingAmount;
    
}

@property (strong, nonatomic) NSString					*itemizedAmount;
@property (strong, nonatomic) NSString					*remainingAmount;

// Init data
- (void)setSeedData:(EntryData*)thisEntry;
-(void) calculateRemainingAmounts:(EntryData*)parentEntry;

-(void)setupToolbar:(MobileViewController*) vc;

@end
