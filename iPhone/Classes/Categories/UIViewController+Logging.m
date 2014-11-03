//
//  UIViewController+Logging.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/17/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "UIViewController+Logging.h"
#import  <objc/runtime.h>
#import "AnalyticsTracker.h"
#import "HotelViewController.h"
#import "HotelSearchResultsViewController.h"
#import "RoomListViewController.h"
#import "HotelBookingViewController.h"
#import "TripDetailsViewController.h"
#import "QuickExpensesReceiptStoreVC.h"

@implementation UIViewController (Logging)


void MethodSwizzle(Class c, SEL origSEL, SEL overrideSEL)
{
    Method origMethod = class_getInstanceMethod(c, origSEL);
    Method overrideMethod = class_getInstanceMethod(c, overrideSEL);
    
    if(class_addMethod(c, origSEL, method_getImplementation(overrideMethod), method_getTypeEncoding(overrideMethod)))
    {
        class_replaceMethod(c, overrideSEL, method_getImplementation(origMethod), method_getTypeEncoding(origMethod));
    }
    else
    {
        method_exchangeImplementations(origMethod, overrideMethod);
    }
}

+ (void)load {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        
        // When swizzling a class method, use the following:
        // Class class = object_getClass((id)self);
        // Swizzle all the viewcontroller methods with a simple log statment so that these log statements are useful while debugging.
        
        //
        // viewDidLoad
        MethodSwizzle(self, @selector(viewDidLoad), @selector(log_viewDidLoad));
        // viewWillAppear
        MethodSwizzle(self, @selector(viewWillAppear:), @selector(log_viewWillAppear:));
        //
        // viewDidAppear
        MethodSwizzle(self, @selector(viewDidAppear:), @selector(log_viewDidAppear:));
        //
        // viewWillDisappear
        MethodSwizzle(self, @selector(viewWillDisappear:), @selector(log_viewWillDisappear:));
        //
        // viewDidDisappear
        MethodSwizzle(self, @selector(viewDidDisappear:), @selector(log_viewDidDisappear:));
        //
        // viewDidUnload
        MethodSwizzle(self, @selector(viewDidUnload), @selector(log_viewDidUnload));

    });
}

#pragma mark - Method Swizzling

- (void)log_viewDidLoad {
    [self log_viewDidLoad];
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"::viewDidLoad:: %@", [self class]] Level:MC_LOG_DEBU];

}

- (void)log_viewWillAppear:(BOOL)animated {
    [self log_viewWillAppear:animated];
    // TODO : Add any google Analytics code here

  if ([self isKindOfClass:[HotelViewController class]]) {
        [AnalyticsTracker initializeScreenName:@"Hotel Criteria"];
    } else if ([self isKindOfClass:[HotelSearchResultsViewController class]]) {
        [AnalyticsTracker initializeScreenName:@"Hotel Listings"];
    } else if ([self isKindOfClass:[RoomListViewController class]]) {
        [AnalyticsTracker initializeScreenName:@"Hotel Rooms List"];
    } else if ([self isKindOfClass:[HotelBookingViewController class]]) {
        [AnalyticsTracker initializeScreenName:@"Hotel Booking"];
    } else if ([self isKindOfClass:[TripDetailsViewController class]]) {
        [AnalyticsTracker initializeScreenName:@"Trips Itinerary"];
    }
    else if ([self isKindOfClass:[QuickExpensesReceiptStoreVC class]]) {
        [AnalyticsTracker initializeScreenName:@"QuickExpenseReceiptStore"];
    } else {
        // Commenting out the default initialize screen name as it will override any custom initscreennames added in the other files. 
//        [AnalyticsTracker initializeScreenName:NSStringFromClass([self class])];
    }
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"::viewWillAppear:: %@", [self class]] Level:MC_LOG_DEBU];

}

- (void)log_viewDidAppear:(BOOL)animated {
    [self log_viewDidAppear:animated];
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"::viewDidAppear:: %@", [self class]] Level:MC_LOG_DEBU];

}

- (void)log_viewWillDisappear:(BOOL)animated {
    [self log_viewWillDisappear:animated];
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"::viewWillDisappear:: %@", [self class]] Level:MC_LOG_DEBU];
    [AnalyticsTracker resetScreenName];
}

- (void)log_viewDidDisappear:(BOOL)animated {
    [self log_viewDidDisappear:animated];
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"::viewDidDisappear:: %@", [self class]] Level:MC_LOG_DEBU];
    
}

- (void)log_viewDidUnload{
    [self log_viewDidUnload];
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"::viewDidUnload:: %@", [self class]] Level:MC_LOG_DEBU];
}


@end
