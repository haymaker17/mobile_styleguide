//
//  HomeDataProviderDelegate.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 10/30/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol HomeDataProviderDelegate <NSObject>

-(void) refreshComplete;

// need to update the badge counts
- (void)updateBadgeCounts;

@end
