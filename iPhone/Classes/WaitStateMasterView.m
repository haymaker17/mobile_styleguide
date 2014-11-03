//
//  WaitStateMasterView.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 5/10/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "WaitStateMasterView.h"

const int LEFT_PADDING_IPAD = 100;

@implementation WaitStateMasterView
@synthesize waitLabel, spinner, progressIndicator, showProgress;
- (WaitStateMasterView*)initWithWaitViewPadNib
{
    // Initialization code
    NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"WaitView_iPad" owner:self options:nil];
    
    WaitStateMasterView *viewFromXib = (WaitStateMasterView*)nib[0];
    self = viewFromXib;
    self.showProgress = NO;
    [self.progressIndicator setHidden:YES];
    
    return self;
}

- (WaitStateMasterView*)initWithWaitViewNib
{
    // Initialization code
    NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"WaitView" owner:self options:nil];
    
    WaitStateMasterView *viewFromXib = (WaitStateMasterView*)nib[0];
    self = viewFromXib;
    self.showProgress = NO;
    [self.progressIndicator setHidden:YES];
    
//    if ([UIDevice isPad]) {
//        // Align spinner & wait label to the center of the view for iPad
//        self.spinner.frame = CGRectMake(spinner.frame.origin.x + LEFT_PADDING_IPAD, spinner.frame.origin.y, spinner.frame.size.width, spinner.frame.size.height);
//        self.waitLabel.frame = CGRectMake(waitLabel.frame.origin.x + LEFT_PADDING_IPAD, waitLabel.frame.origin.y, waitLabel.frame.size.width, waitLabel.frame.size.height);
//    }
    return self;
}

- (WaitStateMasterView*)initWithLoadingViewNib
{
    // Initialization code
    NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"LoadingView" owner:self options:nil];
    
    WaitStateMasterView *viewFromXib = (WaitStateMasterView*)nib[0];
    self = viewFromXib;
    
//    if ([UIDevice isPad]) {
//        // Align spinner & wait label to the center of the view for iPad
//        self.spinner.frame = CGRectMake(spinner.frame.origin.x + LEFT_PADDING_IPAD, spinner.frame.origin.y, spinner.frame.size.width, spinner.frame.size.height);
//        self.waitLabel.frame = CGRectMake(waitLabel.frame.origin.x + LEFT_PADDING_IPAD, waitLabel.frame.origin.y, waitLabel.frame.size.width, waitLabel.frame.size.height);
//    }
    
    self.showProgress = NO; 
    [self.progressIndicator setHidden:YES];
    return self;
}

- (void)setWaitLabelText:(NSString*)text
{
    [waitLabel setText:text];
}

- (void)startSpinner
{
    [spinner startAnimating];
}

- (void)stopSpinner
{
    [spinner stopAnimating];
}

- (void)resetLayout
{
    if ([UIDevice isPad])
    {
        if ([ExSystem isLandscape])
        {
            self.frame = CGRectMake(0, 0, 1024.0, 768.0);
        }
        else
        {
            self.frame = CGRectMake(0, 0, 768.0, 1024.0);
        }
    }
}
@end
