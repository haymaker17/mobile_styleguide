//
//  CXCopyableLabel.m
//  ConcurMobile
//
//  Created by Richard Puckett on 1/28/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CXCopyableLabel.h"

@implementation CXCopyableLabel

- (BOOL)canBecomeFirstResponder {
    return YES;
}

- (BOOL)canPerformAction:(SEL)action
              withSender:(id)sender {
    
    NSLog(@"%@", NSStringFromSelector(action));
    
    if (action == @selector(copy:)) {
        self.backgroundColor = [UIColor colorWithRed:1 green:1 blue:1 alpha:.3];
        return YES;
    }
    
    return NO;
}

#pragma mark - UIResponderStandardEditActions

- (void)copy:(id)sender {
    self.backgroundColor = [UIColor clearColor];
    
    [[UIPasteboard generalPasteboard] setString:self.text];
}

@end
