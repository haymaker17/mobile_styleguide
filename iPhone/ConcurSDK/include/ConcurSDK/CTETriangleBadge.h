//
//  CTETriangleBadge.h
//  Badges
//
//  Created by ernest cho on 9/16/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CTETriangleBadge : UIView

- (void)switchToGreen;
- (void)switchToYellow;
- (void)switchToOrange;
- (void)switchToRed;

- (void)updateBadgeColor:(UIColor *)color;

@end
