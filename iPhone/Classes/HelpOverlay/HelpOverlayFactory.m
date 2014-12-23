//
//  HelpOverlayFactory.m
//  ConcurMobile
//
//  Created by ernest cho on 10/29/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "HelpOverlayFactory.h"
#import "HelpOverlayStatusList.h"
#import "Config.h"

@implementation HelpOverlayFactory

+ (bool)addiPhoneHomeOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPhoneHomeOverlay" toView:view];
}

+ (bool)addiPhoneExpenseListOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPhoneExpenseListOverlay" toView:view];
}

+ (bool)addiPhoneReceiptListOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPhoneReceiptListOverlay" toView:view];
}

+ (bool)addiPhoneReportListOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPhoneReportListOverlay" toView:view];
}

+ (bool)addiPhoneReportDetailOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPhoneReportDetailOverlay" toView:view];
}

+ (bool)addiPhoneApprovalListOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPhoneApprovalListOverlay" toView:view];
}

+ (bool)addiPhoneApprovalDetailOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPhoneApprovalDetailOverlay" toView:view];
}

+ (bool)addiPadHomeOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPadHomeOverlay" toView:view];
}

+ (bool)addiPadExpenseListOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPadExpenseListOverlay" toView:view];
}

+ (bool)addiPadReceiptListOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPadReceiptListOverlay" toView:view];
}

+ (bool)addiPadReportListOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPadReportListOverlay" toView:view];
}

+ (bool)addiPadReportDetailOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPadReportDetailOverlay" toView:view];
}

+ (bool)addiPadApprovalListOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPadApprovalListOverlay" toView:view];
}

+ (bool)addiPadApprovalDetailOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addTestDriveOverlay:@"iPadApprovalDetailOverlay" toView:view];
}

+ (bool)addiPhoneHomeReleaseNoteOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addOverlay:@"iPhoneHomeReleaseNoteOverlay" toView:view];
}

+ (bool)addiPadHomeReleaseNoteOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addOverlay:@"iPadHomeReleaseNoteOverlay" toView:view];
}

/**
 Adds example Test Drive overlay to the view
 */
+ (bool)addExampleTestDriveOverlayToView:(UIView *)view
{
    return [HelpOverlayFactory addOverlay:@"ExampleTestDriveOverlay" toView:view];
}

/**
 Checks if the view already has an Overlay on it.
 */
+ (BOOL)isCoveredByAnOverlay:(UIView *)view
{
    for (UIView *subView in [view subviews]) {
        if ([subView isKindOfClass:[OverlayView2 class]]) {
            return YES;
        }
    }
    return NO;
}

/**
 Adds an overlay to a view.  Checks overlay status.
 */
+ (bool)addOverlay:(NSString *)overlayName toView:(UIView *)view
{
    bool added = NO;
    
    HelpOverlayStatusList *list = [HelpOverlayStatusList sharedList];
    if ((![list isOverlayDisabled:overlayName] && ![HelpOverlayFactory isCoveredByAnOverlay:view]) || [Config isSprintDemoBuild])
    {
        OverlayView2 *overlay = [[OverlayView2 alloc] initWithNibNamed:overlayName];
        overlay.frame = view.bounds;
        [view addSubview:overlay];
        added = YES;
    }
    
    return added;
}

/**
 Adds an overlay to a view.  Checks both test drive status and overlay status.
 */
+ (bool)addTestDriveOverlay:(NSString *)overlayName toView:(UIView *)view
{
    if ([[ExSystem sharedInstance] isTestDrive]) {
        return [HelpOverlayFactory addOverlay:overlayName toView:view];
    }
    return NO;
}

@end
