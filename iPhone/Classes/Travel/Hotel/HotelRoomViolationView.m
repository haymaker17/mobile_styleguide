//
//  HotelRoomViolationView.m
//  ConcurMobile
//
//  Created by ernest cho on 10/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelRoomViolationView.h"
#import "CTETriangleBadge.h"
#import "HotelViolationReasonTableViewController.h"

typedef void(^NextViewControllerBlock)(UIViewController *nextViewController);
typedef void(^UpdateActiveFieldBlock)(UIView *activeField);

/**
 *  This class is programmatically instantiated by HotelRoomReserveView
 */
@interface HotelRoomViolationView()

@property (nonatomic, readwrite, weak) IBOutlet CTETriangleBadge *badge;
@property (nonatomic, readwrite, weak) IBOutlet UITextView *violationDescription;
@property (nonatomic, readwrite, weak) IBOutlet UITextView *violationMessage;

@property (nonatomic, readwrite, weak) IBOutlet UILabel *reason;
@property (nonatomic, readwrite, weak) IBOutlet UITextView *justification;

// violation data
@property (nonatomic, readwrite, strong) CTEHotelViolation *violation;

// reasons provided by the user
@property (nonatomic, readonly, strong) NSString *defaultJustificationText;
@property (nonatomic, readwrite, strong) NSString *violationReasonCode;

@property (nonatomic, readonly, copy) NextViewControllerBlock nextViewControllerBlock;
@property (nonatomic, readonly, copy) UpdateActiveFieldBlock updateActiveFieldBlock;

@end

@implementation HotelRoomViolationView

- (void)setHotelViolation:(CTEHotelViolation *)violation nextViewControllerBlock:(void (^)(UIViewController *nextViewController))nextViewControllerBlock updateActiveField:(void (^)(UIView *activeField))updateActiveField
{
    _violation = violation;
    _nextViewControllerBlock = nextViewControllerBlock;
    _updateActiveFieldBlock = updateActiveField;

    [self handleBadge];
    [self.violationMessage setText:self.violation.message];

    [self.reason setText:@"Select a reason"];

    _defaultJustificationText = @"Enter a justification";
    [self.justification setText:self.defaultJustificationText];

    // set a UITextViewDelegate to update the default text and set the active text field
    [self.justification setDelegate:self];
}

- (void)textViewDidBeginEditing:(UITextView *)textView
{
    // update the default text
    if ([textView.text isEqualToString:self.defaultJustificationText]) {
        [textView setText:@""];
    }

    // update the active field
    if (self.updateActiveFieldBlock) {
        self.updateActiveFieldBlock(textView);
    }
}

- (void)textViewDidEndEditing:(UITextView *)textView
{
    // for some reason the stupid button won't allow the user to click done without entering anything, so this might be unnecessary
    if (textView.text.length == 0) {
        [textView setText:self.defaultJustificationText];
    }
}

// hides the keyboard when a new line character is detected in the text
// This is a little hacky.  Apple prefers if you simply don't hide the keyboard or give it Next button functionality.
- (BOOL)textView:(UITextView *)txtView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text {
    if([text rangeOfCharacterFromSet:[NSCharacterSet newlineCharacterSet]].location == NSNotFound) {
        return YES;
    }

    [txtView resignFirstResponder];
    return NO;
}

- (void)handleBadge
{
    switch (self.violation.enforcementLevel) {
        case CTEHotelBookingAllowed:
            [self.badge switchToGreen];
            break;
        case CTEHotelBookingAllowedWithViolationCode:
            [self.badge switchToYellow];
            break;
        case CTEHotelBookingAllowedWithViolationCodeAndApproval:
            [self.badge switchToRed];
            break;
        case CTEHotelBookingNotAllowed:
            NSLog(@"Error! We should block rooms with CTEHotelBookingNotAllowed at the room list level!");
            break;
    }
}

- (IBAction)openHotelViolationReasons
{
    if (self.nextViewControllerBlock) {
        HotelViolationReasonTableViewController *viewController = [[HotelViolationReasonTableViewController alloc] initWithReason:self.violationReasonCode completion:^(NSString *selectedReasonCode, NSString *selectedReasonDescription) {
            _violationReasonCode = selectedReasonCode;
            [self.reason setText:selectedReasonDescription];
        }];
        self.nextViewControllerBlock(viewController);
    }
}

// Bundles up the violation with the user provided reason
- (CTEHotelViolationReason *)violationReason
{
    CTEHotelViolationReason *tmp;
    if (self.violationReasonCode) {

        // ignore the default justification text
        NSString *justificationText = self.justification.text;
        if ([self.defaultJustificationText isEqualToString:justificationText]) {
            justificationText = @"";
        }

        tmp = [[CTEHotelViolationReason alloc] initWithViolationId:self.violation.violationId reasonCode:self.violationReasonCode justification:justificationText];
    }

    return tmp;
}

@end
