//
//  HelpOverlayFactory.h
//  ConcurMobile
//
//  Created by ernest cho on 10/29/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "OverlayView2.h"

@interface HelpOverlayFactory : NSObject

// iPhone Test drive overlays
+ (bool)addiPhoneHomeOverlayToView:(UIView *)view;
+ (bool)addiPhoneExpenseListOverlayToView:(UIView *)view;
+ (bool)addiPhoneReceiptListOverlayToView:(UIView *)view;
+ (bool)addiPhoneReportListOverlayToView:(UIView *)view;
+ (bool)addiPhoneReportDetailOverlayToView:(UIView *)view;
+ (bool)addiPhoneApprovalListOverlayToView:(UIView *)view;
+ (bool)addiPhoneApprovalDetailOverlayToView:(UIView *)view;

// iPad Test drive overlays
+ (bool)addiPadHomeOverlayToView:(UIView *)view;
+ (bool)addiPadExpenseListOverlayToView:(UIView *)view;
+ (bool)addiPadReceiptListOverlayToView:(UIView *)view;
+ (bool)addiPadReportListOverlayToView:(UIView *)view;
+ (bool)addiPadReportDetailOverlayToView:(UIView *)view;
+ (bool)addiPadApprovalListOverlayToView:(UIView *)view;
+ (bool)addiPadApprovalDetailOverlayToView:(UIView *)view;

+ (bool)addExampleTestDriveOverlayToView:(UIView *)view;

@end
