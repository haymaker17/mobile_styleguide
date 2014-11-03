//
//  ChatterFeed.h
//  ConcurMobile
//
//  Created by ernest cho on 6/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ChatterFeedView.h"
#import "ChatterFeedPost.h"

@interface ChatterFeed : NSObject <ExMsgRespondDelegate>

- (id)initWithView:(ChatterFeedView *)view withItemId:(NSString *)itemId;

- (void)requestSalesForceChatterFeed;
- (int)numberOfChatterPostsInFeed;
- (ChatterFeedPost *)chatterPostAtIndex:(int)index;

@end
