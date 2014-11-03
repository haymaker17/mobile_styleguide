//
//  UIView+FindAndResignFirstResponder.m
//  ConcurMobile
//
//  Created by Sally Yan on 7/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "UIView+FindAndResignFirstResponder.h"

@implementation UIView (FindAndResignFirstResponder)
- (BOOL)findAndResignFirstResponder
{
    if (self.isFirstResponder) {
        [self resignFirstResponder];
        return YES;
    }
    for (UIView *subView in self.subviews) {
        if ([subView findAndResignFirstResponder])
            return YES;
    }
    return NO;
}
@end
