//
//  XCTest+GCovFlush.m
//  TestOutCoverage
//
//  Created by LeRoy Mattingly on 9/12/13.
//  Copyright (c) 2013 OpenSource. All rights reserved.
//

#import "XCTest+GCovFlush.h"
#import <objc/runtime.h>

#if CODE_COVERAGE

@implementation XCTest (GCovFlush)

+ (void)load
{
    Method original, swizzled;
    
    original = class_getInstanceMethod(self, @selector(tearDown));
    swizzled = class_getInstanceMethod(self, @selector(_swizzledTearDown));
    method_exchangeImplementations(original, swizzled);
}

- (void)_swizzledTearDown
{
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    
    if ([ad respondsToSelector:@selector(flushCodeCoverageData)]) {
        [ad flushCodeCoverageData];
    }
    
    [self _swizzledTearDown];
}

@end
#endif