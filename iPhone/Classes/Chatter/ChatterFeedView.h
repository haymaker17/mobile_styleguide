//
//  ChatterView.h
//  ConcurMobile
//
//  Created by ernest cho on 6/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ChatterFeedView : UIView <UITableViewDelegate, UITableViewDataSource>

- (void)setItemId:(NSString *)itemId;

// ChatterFeed lets the view know it needs to update with this method
// If we end up with more ChatterFeed clients this will need to move to a protocol
- (void)updateChatterView;

@end
