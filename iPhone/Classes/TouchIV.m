//
//  TouchScroller.m
//  ConcurMobile
//
//  Created by Paul Kramer on 9/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TouchIV.h"
#import "iPadImageViewerVC.h"

@implementation TouchIV
@synthesize parentVC;



#pragma mark -
#pragma mark Touch Methods

#define kMinimumPinchDelta 15
CGFloat distanceBetweenPoints2 (CGPoint first, CGPoint second) {
	CGFloat deltaX = second.x - first.x;
	CGFloat deltaY = second.y - first.y;
	return sqrt(deltaX*deltaX + deltaY*deltaY );
};

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    if ([touches count] == 2) {
        NSArray *twoTouches = [touches allObjects];
        UITouch *first = twoTouches[0];
        UITouch *second = twoTouches[1];
        initialDistance = distanceBetweenPoints2(
                                                [first locationInView:self], 
                                                [second locationInView:self]);
    }
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event {
	
	if ([touches count] == 2) {
		NSArray *twoTouches = [touches allObjects];
		UITouch *first = twoTouches[0];
		UITouch *second = twoTouches[1];
		CGFloat currentDistance = distanceBetweenPoints2(
														[first locationInView:self],
														[second locationInView:self]);
		
		if (initialDistance == 0)
			initialDistance = currentDistance; 
		else if (currentDistance - initialDistance > kMinimumPinchDelta) {
			//label.text = @"Outward Pinch";
			[self performSelector:@selector(callParentExpandBack:) 
					   withObject:nil 
					   afterDelay:1.6f];
		}
		else if (initialDistance - currentDistance > kMinimumPinchDelta) {
			//label.text = @"Inward Pinch";
			[self performSelector:@selector(callParentExpandBack:) 
					   withObject:nil 
					   afterDelay:1.6f];
		}
	}
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    initialDistance = 0;
}

-(void)callParentExpandBack:(id)sender
{
	[parentVC switchToGridView:nil];
}
@end
