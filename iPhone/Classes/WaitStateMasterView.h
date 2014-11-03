//
//  WaitStateMasterView.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 5/10/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface WaitStateMasterView : UIView {
}

@property (nonatomic, assign) BOOL showProgress;
@property (nonatomic, strong) IBOutlet UILabel *waitLabel;
@property (nonatomic, strong) IBOutlet UIActivityIndicatorView *spinner;
@property (nonatomic, strong) IBOutlet UIProgressView *progressIndicator;

- (WaitStateMasterView*)initWithLoadingViewNib;
- (WaitStateMasterView*)initWithWaitViewNib;
- (WaitStateMasterView*)initWithWaitViewPadNib;

- (void)startSpinner;
- (void)stopSpinner;
- (void)setWaitLabelText:(NSString*)text;
- (void)resetLayout;

@end
