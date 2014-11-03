//
//  UniversalTourViewController.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/24/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "UniversalTourVC_ipad.h"
#import "SigninRegisterVC_ipad.h"

#import "AnalyticsTracker.h"

@interface UniversalTourVC_ipad ()

@end

@implementation UniversalTourVC_ipad

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    [self willAnimateRotationToInterfaceOrientation:[UIApplication sharedApplication].statusBarOrientation duration:0];
    
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(didTapOnTableView:)];
    [self.view addGestureRecognizer:tap];
    
    [AnalyticsTracker initializeScreenName:@"Learn More"];
}

-(void)viewWillAppear:(BOOL)animated
{
    [self.navigationController setNavigationBarHidden:NO animated:NO];
    [self setupNavBar];
    NSUserDefaults* standardUserDefaults = [NSUserDefaults standardUserDefaults];
//    NSString* isTourScreenShown = (NSString*)[standardUserDefaults objectForKey:@"isUniversalTourScreenShown"];
// MOB-16229 - Updated flow as per Loc Design This is the fasted way to change flow  without making breaking changes
//    Right way is to change the storyboard flow and update the flow.
        [standardUserDefaults setObject:@"YES" forKey:@"isUniversalTourScreenShown"];
        [standardUserDefaults synchronize];

    
    if (!self.didSegueFromOtherScreen)  //if this is not the first time and if the tour was already shown then skip showing this class
    {   
        
        [self performSegueWithIdentifier:@"ShowSignIn" sender:self];
    }

}

-(void)viewWillDisappear:(BOOL)animated
{
//    [self.navigationController setNavigationBarHidden:YES animated:NO];
    self.navigationItem.hidesBackButton = NO;
}

- (void)viewDidDisappear:(BOOL)animated
{
    [AnalyticsTracker resetScreenName];
    [super viewDidDisappear:animated];
}

- (void)setupNavBar
{
    
 // Removed the concur logo and added the title as Per Loc's suggestion 
     self.title  = @"Welcome";
    
    // if showing for the first time then show next button after that always show back button.
    // if showing the Learn more then show back button and hide next button
    // if showing learn more hide next button.
    if(![ConcurMobileAppDelegate isUniversalTourScreenShown])
        self.navigationItem.hidesBackButton = YES;
    else
    {
        self.navigationItem.rightBarButtonItem = nil;
    }
    
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)didTapOnTableView:(id)sender {
    // if this is the first time tapped then dismiss the screen every other time just pop
    // if there is right button is displayed then it means this is first time.
    if(!self.didSegueFromOtherScreen)
        [self performSegueWithIdentifier:@"ShowSignIn" sender:self];
    else
        [self.navigationController popViewControllerAnimated:YES];
    
}

- (NSUInteger)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskAll;
}


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
  //  Return YES for supported orientations
    return  YES;
        
}


- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration{
    
    if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
        self.coLogoAreaTop.constant = 80;
        self.coLogoAreaLea.constant = 50;
        self.coLogoAreaWidth.constant = 430;
        self.coFormAreaTop.constant = 80;
        self.coFormAreaLea.constant = 525;
    } else if (UIInterfaceOrientationIsPortrait(toInterfaceOrientation)) {
        self.coLogoAreaTop.constant = 100;
        self.coLogoAreaLea.constant = 145;
        self.coLogoAreaWidth.constant = 450;
        
        self.coFormAreaTop.constant = 380;
        self.coFormAreaLea.constant = 145;
    }
    [self.view setNeedsUpdateConstraints];
    [UIView animateWithDuration:duration animations:^{
        [self.view layoutIfNeeded];
    }];
}

// In a story board-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if ([segue.identifier isEqualToString:@"ShowSignIn"])
    {
        SigninRegisterVC_ipad *nextVC = (SigninRegisterVC_ipad *)segue.destinationViewController;
        [nextVC setOnCloseTestDriveTapped:^{
            if(self.onCloseTestDriveTapped)
                self.onCloseTestDriveTapped();
        }];
        //Add flurry event
        [Flurry logEvent:[NSString stringWithFormat:@"%@,%@", fCatStartup,fNameTestDriveClick] ];
    }
    
}

@end
