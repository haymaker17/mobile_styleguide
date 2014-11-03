//
//  CTEBadge.h
//  ConcurMobile
//
//  Created by ernest cho on 10/31/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CTEBadge : UIView

- (void)updateBadgeCount:(NSNumber *)count;
- (void)updateBadgeColor:(UIColor *)color;

@end
