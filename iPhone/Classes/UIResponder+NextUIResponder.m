//
//  UIResponder+NextResponder.m
//  ConcurAuth
//
//  Created by Wanny Morellato on 11/11/13.
//  Copyright (c) 2013 Wanny Morellato. All rights reserved.
//

#import "UIResponder+NextUIResponder.h"
#import <objc/runtime.h>

static char NextUIResponderKey;

@implementation UIResponder (NextUIResponder)

- (UIResponder*) nextUIResponder {
    return objc_getAssociatedObject(self, &NextUIResponderKey);
}

- (void) setNextUIResponder:(UIResponder *)nextUIResponder{
    objc_setAssociatedObject(self, &NextUIResponderKey, nextUIResponder, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (BOOL)canBecomeFirstUIResponder{
    if ([self canBecomeFirstResponder]) {
        return YES;
    } else {
        if ([self isKindOfClass:[UIControl class]]) {
            return [(UIButton*)self allControlEvents] & UIControlEventTouchUpInside;
        }
    }
    return NO;
}

- (BOOL)becomeFirstUIResponder{
    if ([self becomeFirstResponder]) {
        return YES;
    } else {
        if ([self isKindOfClass:[UIButton class]]) {
            if ([(UIButton*)self allControlEvents] & UIControlEventTouchUpInside) {
                [(UIButton*)self sendActionsForControlEvents:UIControlEventTouchUpInside];
                return YES;
            }
        }
    }
    return NO;
}

@end
