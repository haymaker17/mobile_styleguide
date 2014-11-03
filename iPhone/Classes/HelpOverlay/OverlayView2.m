//
//  OverlayView2.m
//  ConcurMobile
//
//  Created by ernest cho on 10/29/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "OverlayView2.h"
#import "HelpOverlayStatusList.h"

// this UILabel or UIImageView should be used as the dismiss button
#define DISMISS_LABEL_TAG 1

// this UILabel should be localized via the Concur Localizer
#define LOCALIZE_LABEL_TAG 2

// These UILabels are used for Fusion 2014
#define BUTTON_NO_THANKS_TAG 3
#define BUTTON_YES_TAG 4

@interface OverlayView2()
@property (nonatomic, readwrite, strong) NSString *overlayName;

// HACK! I can't seem to figure out how to set this up in Interface Builder...
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coIPadLowerLeft;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coIPadLowerRight;

@property (weak, nonatomic) IBOutlet UIView *leftItem;
@property (weak, nonatomic) IBOutlet UIView *rightItem;

// Flurry timer
@property (nonatomic, readwrite, strong) NSDate *flurryTimer;

@end

@implementation OverlayView2

/**
 These should all be built in HelpOverlayFactory

 @param nibName, cannot be nil
 */
- (id)initWithNibNamed:(NSString *)nibName
{
    self = [super init];
    if (self && nibName) {
        self = [[NSBundle mainBundle] loadNibNamed:nibName owner:nil options:nil][0];
        self.overlayName = nibName;

        [self fixIPadHomeConstraints];
        [self localizeUILabelsInView:self];

        self.flurryTimer = [NSDate date];
    }
    return self;
}

/**
 Sets up a static mapping of overlay xib names to the flurry event name.
 */
- (NSString *)flurryNameForOverlay:(NSString *)overlay
{
    NSDictionary *map = @{@"iPhoneHomeOverlay" : @"Home Screen",
                          @"iPhoneExpenseListOverlay" : @"Expense Screen",
                          @"iPhoneReceiptListOverlay" : @"Receipts Screen",
                          @"iPhoneReportListOverlay" : @"Reports",
                          @"iPhoneReportDetailOverlay" : @"Report Details",
                          @"iPhoneApprovalListOverlay" : @"Approvals",
                          @"iPhoneApprovalDetailOverlay" : @"Approve Report",
                          @"iPadHomeOverlay" : @"Home Screen",
                          @"iPadExpenseListOverlay" : @"Expense Screen",
                          @"iPadReceiptListOverlay" : @"Receipts Screen",
                          @"iPadReportListOverlay" : @"Reports",
                          @"iPadReportDetailOverlay" : @"Report Details",
                          @"iPadApprovalListOverlay" : @"Approvals",
                          @"iPadApprovalDetailOverlay" : @"Approve Report"};

    return [map objectForKey:overlay];
}

/**
 Temporary workaround for iPad Test drive overlay.
 
 I was unable to figure out how to set constraints to a percentage of screen in Interface Builder.
 */
- (void)fixIPadHomeConstraints
{
    if (self.coIPadLowerLeft && self.coIPadLowerRight && self.leftItem && self.rightItem) {
        [self removeConstraint:self.coIPadLowerLeft];
        [self removeConstraint:self.coIPadLowerRight];

        NSLayoutConstraint *left = [NSLayoutConstraint constraintWithItem:self.leftItem attribute:NSLayoutAttributeCenterX relatedBy:0 toItem:self attribute:NSLayoutAttributeRight multiplier:.25 constant:0];
        NSLayoutConstraint *right = [NSLayoutConstraint constraintWithItem:self.rightItem attribute:NSLayoutAttributeCenterX relatedBy:0 toItem:self attribute:NSLayoutAttributeRight multiplier:.75 constant:0];
        [self addConstraints:@[left,right]];
    }
}

/**
 Localizes tagged UILabels in the given view.
 Recursively searches for UILabels and tries to localize the text.
 
 Set the UILabel tag = 2
 */
- (void)localizeUILabelsInView:(UIView *)view
{
    if (view.tag == LOCALIZE_LABEL_TAG && [view isKindOfClass:[UILabel class]]) {
        UILabel *tmp = (UILabel *)view;
        tmp.text = [Localizer getLocalizedText:tmp.text];
    }

    for (UIView *subview in view.subviews) {
        [self localizeUILabelsInView:subview];
    }
}

/**
 Close this overlay via a UILabel or UIImageView.
 This is an override that's why it's named touchesBegan.

 Set the UILabel or UIImageView tag = 1
 */
- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    UITouch *touch = [touches anyObject];
    if (touch.view.tag == DISMISS_LABEL_TAG)
    {
        [self closeOverlay];
    }
    else if (touch.view.tag == BUTTON_NO_THANKS_TAG)
    {
        // Replace with Fusion14 itin screen
        //[self closeOverlay];
    }
    else if (touch.view.tag == BUTTON_YES_TAG)
    {
        // Replace with Fusion14 Hotel Search result screen
        [self.delegate buttonYesClicked];
        [self closeOverlay];
    }
}

/**
 Close this overlay via a UIButton.
 
 Connect this IBAction to the UIButton in IB.
 */
- (IBAction)closeOverlay
{
    [self logFlurryEvent];

    // disable the overlay when it's closed
    [[HelpOverlayStatusList sharedList] disableOverlay:self.overlayName];
    [self removeWithEffect: self];
}

/**
 Logs the time spent viewing the test drive overlay.
 */
- (void)logFlurryEvent
{
    NSInteger timeInSeconds = -round([self.flurryTimer timeIntervalSinceNow]);

    NSString *flurryName = [self flurryNameForOverlay:self.overlayName];
    NSString *time = @"error";
    if (timeInSeconds >= 0) {
        time = @"0-3";
    }
    if (timeInSeconds > 3) {
        time = @"3-10";
    }
    if (timeInSeconds > 10) {
        time = @"10+";
    }

    NSDictionary *dict = @{@"Seconds on Overlay":time};
    [Flurry logEvent:flurryName withParameters:dict];
}

/**
 Animate the overlay removal. Looks nicer.
 This could be static and public, but seems out of place to do that.

 @param viewToRemove, should always be self.
 */
- (void)removeWithEffect:(UIView *)viewToRemove
{
    [UIView beginAnimations:@"removeWithEffect" context:nil];
    [UIView setAnimationDuration:0.25f];

    viewToRemove.frame = self.frame;
    viewToRemove.alpha = 0.0f;
    [UIView commitAnimations];
    [viewToRemove performSelector:@selector(removeFromSuperview) withObject:nil afterDelay:0.25f];
}

@end
