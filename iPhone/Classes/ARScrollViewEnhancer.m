//
//  ARScrollViewEnhancer.m
//  SnowJob
//
//  Created by Paul Kramer on 1/1/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import "ARScrollViewEnhancer.h"


@implementation ARScrollViewEnhancer

#pragma mark -
#pragma mark Construction & Destruction

- (void)dealloc {
	[_scrollView release];
    [super dealloc];
}

#pragma mark -
#pragma mark UIView methods

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
	if ([self pointInside:point withEvent:event]) {
		return _scrollView;
	}
	return nil;
}
@end
