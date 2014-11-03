//
//  MyToolbar.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "MyToolbar.h"


@implementation MyToolbar

- (void)drawRect:(CGRect)rect 
{
    // Warning: Hardcoded for portrait size
    UIImage *image = [UIImage imageNamed:@"Icon.png"];
    [image drawInRect:rect];
}

@end
