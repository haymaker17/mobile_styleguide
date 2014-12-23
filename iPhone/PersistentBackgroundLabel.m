//
//  PersistentBackgroundLabel.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 02/12/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "PersistentBackgroundLabel.h"

@implementation PersistentBackgroundLabel

// MOB-21732 If the user taps on a hotel result cell, the background color on Preferred label was disappearing
// This sub-class prevents this by overriding normal access to the backgroundColor setting

- (void)setPersistentBackgroundColor:(UIColor*)color {
    super.backgroundColor = color;
}

- (void)setBackgroundColor:(UIColor *)color {
    // do nothing - background color never changes
}

@end
