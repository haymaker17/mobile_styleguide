//
//  GoGoPurchaseConfirmationViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 11/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AnalyticsManager.h"
#import "GoGoPurchaseConfirmationViewController.h"

@interface GoGoPurchaseConfirmationViewController ()

@end

@implementation GoGoPurchaseConfirmationViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        self.title = @"Thank You!";
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self.navigationItem setHidesBackButton:YES];
    
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithTitle:@"Done"
                                                                   style:UIBarButtonItemStyleDone
                                                                  target:self
                                                                  action:@selector(didSelectDone:)];
    
    [self.navigationItem setRightBarButtonItem:doneButton];
    
    self.confirmationNoticeLabel.text = [NSString stringWithFormat:@"An email confirmation has been sent to %@.",
                                         [ExSystem sharedInstance].userName];
    
    MessageCenterManager *messageCenterManager = [MessageCenterManager sharedInstance];
    
    MessageCenterMessage *message = [messageCenterManager messageAtIndex:0];
    
    self.confirmationCodeLabel.text = message.stringExtra;
    
    [self.confirmationCodeLabel sizeToFit];
    
    CGPoint center = CGPointMake(self.view.center.x, self.confirmationCodeLabel.center.y);
    self.confirmationCodeLabel.center = center;
}

#pragma mark - Responders

- (void)didSelectDone:(id)sender {
    NSMutableArray *controllerStack = [self.navigationController.viewControllers mutableCopy];
    
    [controllerStack removeLastObject];
    [controllerStack removeLastObject];
    
    [self.navigationController setViewControllers:controllerStack animated:YES];
}

- (IBAction)tapToCopy:(UITapGestureRecognizer *)sender {
    [sender.view becomeFirstResponder];
    
    UIMenuController *menuController = [UIMenuController sharedMenuController];
    [menuController setTargetRect:sender.view.frame inView:sender.view.superview];
    [menuController setMenuVisible:YES animated:YES];
    
    /* Old bouncy stuff. Yuck.
     *
     UIPasteboard *pb = [UIPasteboard generalPasteboard];
     [pb setString:self.confirmationCodeLabel.text];
     
     [UIView animateWithDuration:0.1 delay:0 options:nil animations:^{
     self.confirmationCodeLabel.transform = CGAffineTransformMakeScale(1.2f, 1.2f);
     } completion:^(BOOL finished) {
     [UIView animateWithDuration:0.2 animations:^{
     self.confirmationCodeLabel.transform = CGAffineTransformMakeScale(1.0f, 1.0f);
     }];
     }];
     */
}

@end
