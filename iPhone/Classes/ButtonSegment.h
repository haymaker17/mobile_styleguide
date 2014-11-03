//
//  ButtonSegment.h
//  ConcurMobile
//
//  Created by Paul Kramer on 9/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SegmentData.h"

@interface ButtonSegment : UIButton {
	EntitySegment	*segment;
	UIScrollView	*scroller;
	UIView			*parentView, *dayView;
}

@property (strong, nonatomic) EntitySegment		*segment;
@property (strong, nonatomic) UIScrollView		*scroller;
@property (strong, nonatomic) UIView			*parentView;
@property (strong, nonatomic) UIView			*dayView;

@end
