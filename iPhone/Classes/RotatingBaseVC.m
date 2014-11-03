//
//  RotatingBaseVC.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RotatingBaseVC.h"
#import "UIView+FindAndResignFirstResponder.h"
#import "UIResponder+NextUIResponder.h"
#import "SettingsViewController.h"

@interface RotatingBaseVC ()

@end

@implementation RotatingBaseVC


- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    if([UIDevice isPad])
    {
        [self willRotateToInterfaceOrientation:[UIApplication sharedApplication].statusBarOrientation duration:0];
    }
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(viewWasTapped:)];
    [self.view addGestureRecognizer:tap];


}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

-(void)viewWillDisappear:(BOOL)animated
{
    [self.view endEditing:YES];
    [super viewWillDisappear:animated];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    if (self.coLogoAreaTop == nil && self.coLogoAreaLea  == nil  && self.coFormAreaTop  == nil  && self.coFormAreaLea  == nil ) {
        return;
    }

    if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
        self.coLogoAreaTop.constant = 80;
        self.coLogoAreaLea.constant = 30;
        self.coFormAreaTop.constant = 90;
        self.coFormAreaLea.constant = 520;
    } else if (UIInterfaceOrientationIsPortrait(toInterfaceOrientation)) {
        self.coLogoAreaTop.constant = 100;
        self.coLogoAreaLea.constant = 148;
        self.coFormAreaTop.constant = 350;
        self.coFormAreaLea.constant = 168;
    }
    [self.view setNeedsUpdateConstraints];
    [UIView animateWithDuration:duration animations:^{
        [self.view layoutIfNeeded];
    }];
//    
//	if ([ExSystem isLandscape])
//	{
//		[self resetForLandscape];
//	}
//	else
//	{
//		[self resetForPortrait];
//	}
}

//-(void)resetForLandscape
//{
//	if([UIDevice isPad])
//		[self layoutPad:YES];
//	else
//	{
//		[UIView beginAnimations:@"Fade" context:nil];
//		[UIView setAnimationDelegate:self];
//		[UIView setAnimationDidStopSelector:@selector(logoAnimationStart:)];
//		[UIView setAnimationDuration:.33];
//		[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
//		
//		[UIView commitAnimations];
//	}
//}
//
//-(void)resetForPortrait
//{
//
//	if([UIDevice isPad])
//		[self layoutPad:NO];
//	else
//	{
//		
//		[UIView beginAnimations:@"Fade" context:nil];
//		[UIView setAnimationDelegate:self];
//		[UIView setAnimationDidStopSelector:@selector(logoAnimationPortrait:)];
//		[UIView setAnimationDuration:.33];
//		[UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
//		[UIView commitAnimations];
//        
//	}
//}

//for the ipad, do all of it's layout here...
-(void)layoutPad:(BOOL)forceLandscape
{
         //TODO: Do we need this method ?? not sure what this does.
//    ivBackground.image = nil;
//    [tableList setBackgroundView:nil];
//    [tableList setBackgroundView:[[UIView alloc] init]];
//    [tableList setBackgroundColor:UIColor.clearColor];
}



/**
 Dismiss the keyboard
 */
- (void)viewWasTapped:(UITapGestureRecognizer*)gesture
{
//    [self.view findAndResignFirstResponder];
    [self.view endEditing:YES];
}

#pragma mark -
#pragma mark Text Field Methods
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    return[textField.nextUIResponder becomeFirstUIResponder];
}

- (IBAction)buttonSettingsPressed:(id)sender
{
    SettingsViewController *vc = [[SettingsViewController alloc] initBeforeUserLogin];
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:vc];
    navi.modalPresentationStyle = UIModalPresentationFormSheet;
    
    [self.navigationController presentViewController:navi animated:YES completion:nil];
}


@end
