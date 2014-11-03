//
//  MoreMenuSectionHeader.m
//  ConcurMobile
//
//  Created by ernest cho on 3/12/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>
#import "MoreMenuSectionHeader.h"

@implementation MoreMenuSectionHeader

@synthesize title;

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        self = [[NSBundle mainBundle] loadNibNamed:@"MoreMenuSectionHeader" owner:nil options:nil][0];
        
        // add a gradient
        CAGradientLayer *gradient = [CAGradientLayer layer];
        gradient.frame = frame;
        gradient.colors = @[(id)[[UIColor grayColor] CGColor], (id)[[UIColor darkGrayColor] CGColor]];
        [self.layer insertSublayer:gradient atIndex:0];
    }
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
