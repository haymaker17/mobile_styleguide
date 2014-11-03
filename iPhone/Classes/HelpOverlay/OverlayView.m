//
//  OverlayView.m
//  ConcurMobile
//
//  Created by ernest cho on 2/21/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "OverlayView.h"

// dismiss label is tagged for UI events
#define DISMISS_LABEL_TAG 1

@implementation OverlayView
@synthesize dismissText, menuHelpText, messageCenterText, travelHelpText, cameraHelpText, quickExpenseHelpText, menuHelpImage, messageCenterImage, travelHelpImage, cameraHelpImage, quickExpenseHelpImage;

- (id)initForIPad
{
    self = [super init];
    if (self) {
        // load most of the setup from the nib
        self = [[NSBundle mainBundle] loadNibNamed:@"OverlayViewIPad" owner:nil options:nil][0];
        
        // set the screen size since I can't figure out how to do it in Interface Builder
        if ([ExSystem isLandscape]) {
            self.frame = CGRectMake(0, 0, 1024, 768);
        }
        
        // we don't use Interface Builder for localization
        dismissText.text = [Localizer getLocalizedText:@"Dismiss tip overlay"];
        menuHelpText.text = [Localizer getLocalizedText:@"Access other areas of the app"];
        messageCenterText.text = [Localizer getLocalizedText:@"Message Center"];

            travelHelpText.text = nil;
            travelHelpImage.alpha = 0.0;
        //}
        
        // hide the camera if there's no receiptstore?
        if ([[ExSystem sharedInstance] hasReceiptStore]) {
            cameraHelpText.text = [Localizer getLocalizedText:@"Receipt Capture"];
        } else {
            cameraHelpText.text = nil;
            cameraHelpImage.alpha = 0.0;
        }
            quickExpenseHelpText.text = nil;
            quickExpenseHelpImage.alpha = 0.0;
        //}
    }
    return self;
}

- (id)init
{
    self = [super init];
    if (self) {
        // load most of the setup from the nib
        self = [[NSBundle mainBundle] loadNibNamed:@"OverlayView" owner:nil options:nil][0];
        
        // set the screen size since I can't figure out how to do it in Interface Builder
        if ([ExSystem is5]) {
            self.frame = CGRectMake(0, 0, 320, 568);
        }
        
        // we don't use Interface Builder for localization
        dismissText.text = [Localizer getLocalizedText:@"Dismiss tip overlay"];
        menuHelpText.text = [Localizer getLocalizedText:@"Access other areas of the app"];
        messageCenterText.text = [Localizer getLocalizedText:@"Message Center"];
        
        if ([[ExSystem sharedInstance] hasTravelBooking] && ![[ExSystem sharedInstance] isTravelOnly]) {
            travelHelpText.text = [Localizer getLocalizedText:@"Book Travel"];
        } else {
            travelHelpText.text = nil;
            travelHelpImage.alpha = 0.0;
        }
        
        // hide the camera if there's no receiptstore?
        if ([[ExSystem sharedInstance] hasReceiptStore]) {
            cameraHelpText.text = [Localizer getLocalizedText:@"Receipt Capture"];
        } else {
            cameraHelpText.text = nil;
            cameraHelpImage.alpha = 0.0;
        }
        
        if ([[ExSystem sharedInstance] isExpenseRelated]) {
            quickExpenseHelpText.text = [Localizer getLocalizedText:@"Quick Expense"];
        } else {
            quickExpenseHelpText.text = nil;
            quickExpenseHelpImage.alpha = 0.0;
        }
    }
    return self;
}

// in order to pass touch events to the dismissText UILabel
- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    UITouch *touch = [touches anyObject];
    if (touch.view.tag == DISMISS_LABEL_TAG) {
        // MOB-12665 removed what's new so overlay has to set this when dismissed
        [ExSystem sharedInstance].sys.showWhatsNew = NO;
        [[ExSystem sharedInstance] saveSystem];
        
        [self removeWithEffect: self];
    }
}

// animate the overlay removal. looks nicer
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
