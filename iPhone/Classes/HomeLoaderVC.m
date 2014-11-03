//
//  HomeLoaderVC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 11/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXClient.h"
#import "HomeLoaderVC.h"
#import "TabBarViewController.h"
#import "MessageCenterViewController.h"
#import "SettingsViewController.h"
#import "MoreMenuViewController.h"
#import "LoginViewController.h"
#import "ChangingImageViewController.h"
#import "RXMLElement.h"
#import "TravelRequestFactory.h"
#import "MobileTourViewController.h"

@interface HomeLoaderVC ()

// To hold a reference to TestDriveStoryboard
@property (nonatomic, strong) UIViewController *LGCtrl;

@property (strong, nonatomic) IBOutlet TransparentViewUnderMoreMenu *transparentView;
@property (strong, nonatomic) UINavigationController    *moreMenuCtrlNav;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coMoreMenuLea;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coTransparentLea;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coMoreMenuTop;
@property (weak, nonatomic) MobileTourViewController *mobileTourVC;

// Used to programmatically adjust the height of the rotating image
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coRotatingImageHeight;

@property (strong, nonatomic) TabBarViewController *tabBarVC;
// Used to hide the TabBar
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coTabBarHeight;

@end

@implementation HomeLoaderVC

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
 
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateTabBar) name:NotificationOnLoginSuccess object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(showMobileTours) name:NotificationOnFirstTimeLogin object:nil];

    [self rotateHomeCollectionViewToInterfaceOrientation:self.interfaceOrientation];

    if ([Config isGov])
    {
        [self setupNavBarForGov];
    }
    else
    {
        [self setupNavBar];
    }
    
    if ([UIScreenEdgePanGestureRecognizer class])
    {
        UIScreenEdgePanGestureRecognizer *leftEdgePan = [[UIScreenEdgePanGestureRecognizer alloc] initWithTarget:self action:@selector(showMoreMenu:)];
        leftEdgePan.edges = UIRectEdgeLeft;
        [self.view setUserInteractionEnabled:YES]; // TODO: is really needed
        [self.view addGestureRecognizer:leftEdgePan];
    }
    
    [[MessageCenterManager sharedInstance] addListener:self];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

    // This makes sure we're rotated correctly when we come back from another viewcontroller
    [self willRotateToInterfaceOrientation:self.interfaceOrientation duration:0.2];
    [self adjustMoreMenuPosition];

    [self.navigationController setToolbarHidden:YES animated:NO];
}


/**
 Need to move the more menu based on OS version
 */
- (void)adjustMoreMenuPosition
{
    if ([ExSystem is7Plus]) {
        self.coMoreMenuTop.constant = 64.0;
    } else {
        self.coMoreMenuTop.constant = 0;

        // MOB-16540
        // HACK!!  I cannot figure out where we're setting the status bar on iOS6...
        // By the time we hit later screens this constant no longer needs to be set to -20.
        static dispatch_once_t onceToken;
        dispatch_once(&onceToken, ^{
            self.coMoreMenuTop.constant = -20;
        });
    }
}

/**
 Handles rotation
 */
- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
    if ([UIDevice isPad]) {

        // MOB-17649 the hack for rotation can leave the frame in a wierd size
        if (UIDeviceOrientationIsPortrait(self.interfaceOrientation)){
            self.view.frame = CGRectMake(0, 0, 768, 1024);
        } else {
            self.view.frame = CGRectMake(0, 0, 1024, 768);
        }

        // this resizes the containers on rotation
        [self rotateHomeCollectionViewToInterfaceOrientation:toInterfaceOrientation];

        // this forces the iPadHome9VC to rotate
        [self.iPadHome9VC willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
        [self.goviPadHome9VC willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
        
        // this forces the tab bar to rotate
        [self.tabBarVC willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:NotificationOnLoginSuccess object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:NotificationOnFirstTimeLogin object:nil];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender{
    //TODO: In the future, we should not even need to check for device.
    //Consolidate both path into one by using one protocal
    if ([UIDevice isPad])
    {
        if ([segue.identifier isEqualToString:@"LOADCONTENT"])
        {
            self.iPadHome9VC = segue.destinationViewController;
        }
        else if ([segue.identifier isEqualToString:@"LOADGOVCONTENT"])
        {
            self.goviPadHome9VC = segue.destinationViewController;
        }
        else if ([segue.identifier isEqualToString:@"LOADTABBAR"])
        {
            self.tabBarVC = segue.destinationViewController;
            __weak iPadHome9VC *home =  self.iPadHome9VC;
            [self.tabBarVC setSelectOption:^(NSDictionary *option) {
                if ([[option objectForKey:@"Expense"] isEqualToString:@"YES"]) {
                    [home btnQuickExpensePressed:sender];
                }
                else if ([[option objectForKey:@"Receipt"] isEqualToString:@"YES"]){
                    [home cameraPressed:sender];
                }
                else if ([[option objectForKey:@"Book"] isEqualToString:@"YES"]){
                    [home bookingsActionPressed:sender];
                }
                else if ([[option objectForKey:@"Mileage"] isEqualToString:@"YES"]){
                    [home btnCarMileagePressed:sender];
                }
            }];
        }
        else if ([segue.identifier isEqualToString:@"LOADGOVTABBAR"])
        {
            self.tabBarVC = segue.destinationViewController;
            __weak GoviPadHome9VC *home =  self.goviPadHome9VC;
            [self.tabBarVC setSelectOption:^(NSDictionary *option) {
                if ([[option objectForKey:@"Expense"] isEqualToString:@"YES"]) {
					[home btnQEpressed:sender];
                }
                else if ([[option objectForKey:@"Receipt"] isEqualToString:@"YES"]){
                }
                else if ([[option objectForKey:@"Book"] isEqualToString:@"YES"]){
					[home btnBookTravelPressed:sender];
                }
                else if ([[option objectForKey:@"Mileage"] isEqualToString:@"YES"]){
                }
            }];
        }
        else if ([segue.identifier isEqualToString:@"MoreMenuEmbedSegue"])
        {
            self.moreMenuCtrlNav  = segue.destinationViewController;
            if ([self.moreMenuCtrlNav.viewControllers[0] isKindOfClass:[MoreMenuViewController class]])
            {
                MoreMenuViewController *ctrl = self.moreMenuCtrlNav.viewControllers[0]; // good dev whould check to make sure it is a MoreMenuViewController
                ctrl.ipadHome = self.iPadHome9VC;
                [ctrl setTapHome:^(void){
                  [self setMoreMenuHidden:YES animated:NO];
                }];
                [self setMoreMenuHidden:YES animated:NO];
            }
        }
    }
    else
    {
        if ([segue.identifier isEqualToString:@"LOADCONTENT"])
        {
            self.home9VC = segue.destinationViewController;
        }
        else if ([segue.identifier isEqualToString:@"LOADGOVCONTENT"])
        {
            self.govHome9VC = segue.destinationViewController;
        }
        else if ([segue.identifier isEqualToString:@"LOADTABBAR"])
        {
            self.tabBarVC = segue.destinationViewController;
            __weak Home9VC *home =  self.home9VC;
            [self.tabBarVC setSelectOption:^(NSDictionary *option) {
                if ([[option objectForKey:@"Expense"] isEqualToString:@"YES"]) {
                    [home buttonQuickExpensePressed:sender];
                }
                else if ([[option objectForKey:@"Receipt"] isEqualToString:@"YES"]){
                    [home cameraPressed:sender];
                }
                else if ([[option objectForKey:@"Book"] isEqualToString:@"YES"]){
                    [home bookingsActionPressed:sender];
                }
                else if ([[option objectForKey:@"Mileage"] isEqualToString:@"YES"]){
                    [home btnCarMileagePressed:sender];
                }
            }];
        }
        else if ([segue.identifier isEqualToString:@"LOADGOVTABBAR"])
        {
            self.tabBarVC = segue.destinationViewController;
            __weak GovHome9VC *home = self.govHome9VC;
            [self.tabBarVC setSelectOption:^(NSDictionary *option)
            {
                if ([[option objectForKey:@"Expense"] isEqualToString:@"YES"]) {
                    [home buttonQuickExpensePressed:sender];
                }
                else if ([[option objectForKey:@"Receipt"] isEqualToString:@"YES"]){
                
                }
                else if ([[option objectForKey:@"Book"] isEqualToString:@"YES"]){
                    [home bookingsActionPressed:sender];
                }
                else if ([[option objectForKey:@"Mileage"] isEqualToString:@"YES"]){
                
                }
            }];
        }
    }
}

#pragma mark -
#pragma mark Toolbar/Nav bar action delegates
- (void)setupNavBarForGov
{
    UIImageView *img = nil;

    if ([ExSystem is7Plus])
        img = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"Logo_header"]];
    else
        img = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"concur_logo_titlebar"]];
    self.title = [Localizer getLocalizedText:@"Home"];

    self.navigationItem.titleView = img;
    self.navigationController.navigationBar.alpha = 0.9f;

    SEL settingsSelector = @selector(showSettingsMenu:);
    if ([ExSystem is7Plus])
    {
        self.navigationItem.leftBarButtonItem = [self getNavBarButtonWithImage:@"ic_menu_settings" withSelector:settingsSelector];
    }
    else
    {
        self.navigationItem.leftBarButtonItem = [self getNavBarButtonWithImage:@"icon_menu_settings" withSelector:settingsSelector];
    }
}

- (void)setupNavBar
{
    UIImageView *img = nil;
    
    if ([ExSystem is7Plus])
        img = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"Logo_header"]];
    else
        img = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"concur_logo_titlebar"]];
    self.title = [Localizer getLocalizedText:@"Home"];
    
    self.navigationItem.titleView = img;
    self.navigationController.navigationBar.alpha = 0.9f;
    // Wire up Moremenu and messageCenter actions
    
    SEL moreMenuSelector = @selector(showMoreMenu:);
    SEL messageCenterSelector = @selector(showMessageCenter:);
    // Add More menu and Message center buttons.
    
    if ([ExSystem is7Plus])
    {
        self.navigationItem.leftBarButtonItem = [self getNavBarButtonWithImage:@"menu_drawer" withSelector:moreMenuSelector];
        [self setMessageCenterIcon];
    }
    else
    {
        self.navigationItem.leftBarButtonItem = [self getNavBarButtonWithImage:@"icon_menu" withSelector:moreMenuSelector];
        self.navigationItem.rightBarButtonItem = [self getNavBarButtonWithImage:@"icon_messagecenter_iOS6" withSelector:messageCenterSelector];
    }
}

- (void)setMessageCenterIcon {
    SEL messageCenterSelector = @selector(showMessageCenter:);
    
    if ([[MessageCenterManager sharedInstance] numMessagesForType:MessageTypeUnread] > 0) {
        self.navigationItem.rightBarButtonItem = [self getNavBarButtonWithImage:@"icon_messagecenter_badged" withSelector:messageCenterSelector];
    } else {
        if ([ExSystem is7Plus]) {
            self.navigationItem.rightBarButtonItem = [self getNavBarButtonWithImage:@"icon_messagecenter_iOS7" withSelector:messageCenterSelector];
        } else  {
            // the new blue icon is very hard to read on iOS6.  Need to switch to something else.
            self.navigationItem.rightBarButtonItem = [self getNavBarButtonWithImage:@"icon_messagecenter_iOS6" withSelector:messageCenterSelector];
        }
    }
}

// Nav bar buttons
-(UIBarButtonItem *)getNavBarButtonWithImage:(NSString *)imgName withSelector:(SEL)selectorName
{
    UIButton* mbtn =[UIButton buttonWithType:UIButtonTypeCustom];
    UIImage* mImage = [UIImage imageNamed:imgName];
    [mbtn addTarget:self action:selectorName forControlEvents:UIControlEventTouchUpInside];
    [mbtn setImage:mImage forState:UIControlStateNormal];
    mbtn.frame = CGRectMake(0, 0, mImage.size.width, mImage.size.height);
    UIBarButtonItem *menuButton = [[UIBarButtonItem alloc]initWithCustomView:mbtn];
    
    return menuButton;
    
}

-(UIBarButtonItem *)getCustomBarButton:(NSString *)imgName withSelector:(SEL)selectorName withBorder:(UIColor *)bordercolor
{
    UIButton *button = nil;
    UIBarButtonItem *barButton = nil;
    UIImage *toolbarimage = [UIImage imageNamed: @"action_bar_cell"];
    
    button = [UIButton buttonWithType:UIButtonTypeCustom];
    [button setImage:[UIImage imageNamed:imgName] forState:UIControlStateNormal];
    button.bounds = CGRectMake(0,0,self.navigationController.toolbar.frame.size.width/2.8, toolbarimage.size.height + 5);
    
    // MOB-14537 iOS 7 needs the background color set, it defaults to transparent.
    button.backgroundColor = UIColor.groupTableViewBackgroundColor;
    [button addTarget:self action:selectorName forControlEvents:UIControlEventTouchUpInside];
    
    if(bordercolor!=nil)
    {
        // Set border
        [[button layer] setBorderWidth:1.0f];
        [[button layer] setBorderColor:bordercolor.CGColor];
    }
    if(selectorName == nil)
    {
        button.userInteractionEnabled = NO;
    }
    barButton = [[UIBarButtonItem alloc] initWithCustomView:button];
    
    return barButton;
}

#pragma mark - Menu functions
-(void)showMoreMenu:(id)sender
{
    if ([sender isKindOfClass:[UIGestureRecognizer class]]) {
        if ([(UIGestureRecognizer*)sender state] != UIGestureRecognizerStateEnded) {
            // UIGestureRecognizer call at least 3 times: 1 stateStarted statedChanged stateEnded
            // we only care about state ended
            return;
        }
    }
    
    if ([UIDevice isPad]){
        if (self.coMoreMenuLea.constant ==0) { // 0 means visible
            [self setMoreMenuHidden:YES animated:YES];
        } else {
            [self setMoreMenuHidden:NO animated:YES];
        }
        __weak HomeLoaderVC *weakSelf = self;
        [self.transparentView setDismiss:^(void){
            [weakSelf setMoreMenuHidden:YES animated:YES];
        }];
    } else {
        MoreMenuViewController *moreMenuVC = [[MoreMenuViewController alloc] init];
        UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:moreMenuVC];
        [self presentViewController:navi withDirection:@"fromLeft"];
    }
}

#pragma mark - show mobile tour
-(void) showMobileTours
{
    //only show if the user has Expense related or Tavel Related features
    if ( [[ExSystem sharedInstance] isExpenseRelated ]|| [[ExSystem sharedInstance] isTravelRelated]) {
        if ([UIDevice isPhone]){
            self.mobileTourVC = [[UIStoryboard storyboardWithName:@"MobileTour" bundle:nil] instantiateInitialViewController];
        } else {
            self.mobileTourVC = [[UIStoryboard storyboardWithName:@"MobileTour_iPad" bundle:nil] instantiateInitialViewController];
        }
        
        [self.navigationController addChildViewController:self.mobileTourVC];
        [self.navigationController.view addSubview:self.mobileTourVC.view];
        [self.mobileTourVC didMoveToParentViewController:self.navigationController];
        
        [self.mobileTourVC setOnDismissTapped:^{
            [self.mobileTourVC willMoveToParentViewController:nil];
            [self.mobileTourVC.view removeFromSuperview];
            [self.mobileTourVC removeFromParentViewController];
        }];
        
        if ([UIDevice isPad]) {
            // MOB-17649 our view stack is messed up so rotation notifications don't reach everyone
            self.mobileTourVC.home = self;
            [self.mobileTourVC didRotate:nil];
        }
        
        
        [[NSUserDefaults standardUserDefaults] setObject:@(YES) forKey:@"NotFirstTimeLogin"];
    }
}

- (void)setMoreMenuHidden:(BOOL)hidden animated:(BOOL)animated{
    if (hidden) {
        if (animated) {
            self.coMoreMenuLea.constant = -320;
            //[self.transparentView setAlpha:1.0];
            [UIView animateWithDuration:0.35 animations:^{
                [self.view layoutIfNeeded];
                [self.transparentView setAlpha:0.0];
            } completion:^(BOOL finished) {
                [self.transparentView setAlpha:0.6];  // just making sure that we leave things to 1.0
                [self.transparentView setHidden:hidden];
            }];
        }else {
            self.coMoreMenuLea.constant = -320;
            [self.transparentView setHidden:hidden];
            [self.view layoutIfNeeded];
        }
    } else{
        if (animated) {
            self.coMoreMenuLea.constant = 0;
            [self.transparentView setHidden:hidden];
            [self.transparentView setAlpha:0.0];
            [UIView animateWithDuration:0.35 animations:^{
                [self.view layoutIfNeeded];
                [self.transparentView setAlpha:0.6];
            } completion:^(BOOL finished) {
                [self.transparentView setAlpha:0.6]; // just making sure that we leave things to 1.0
            }];
        }else {
            self.coMoreMenuLea.constant = 0;
            [self.transparentView setHidden:hidden];
            [self.view layoutIfNeeded];
        }

    }
}


-(void)showMessageCenter:(id)sender
{
    MessageCenterViewController *nextController = [[MessageCenterViewController alloc] init];
    
    if( [UIDevice isPad] )
    {
        UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:nextController];
        navi.modalPresentationStyle = UIModalPresentationFormSheet;
        
        [self presentViewController:navi animated:YES completion:nil];
    }
    else // iPhone
    {
        [self.navigationController pushViewController:nextController animated:YES];
    }
}

-(IBAction)showSettingsMenu:(id)sender
{
    SettingsViewController *vc = [[SettingsViewController alloc] init];
    UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:vc];
    localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
    [self presentViewController:localNavigationController animated:YES completion:nil];
}

#pragma mark -
#pragma mark Utility functions
- (void) presentViewController:(UIViewController *)viewController withDirection: (NSString *) direction {
    
    [CATransaction begin];
    
    CATransition *transition = [CATransition animation];
    transition.type = kCATransitionMoveIn;
    transition.subtype = direction;
    transition.duration = 0.25f;
    transition.fillMode = kCAFillModeForwards;
    transition.removedOnCompletion = YES;
    
    [[UIApplication sharedApplication].keyWindow.layer addAnimation:transition forKey:@"transition"];
    [[UIApplication sharedApplication] beginIgnoringInteractionEvents];
    [CATransaction setCompletionBlock: ^ {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(transition.duration * NSEC_PER_SEC)), dispatch_get_main_queue(), ^ {
            [[UIApplication sharedApplication] endIgnoringInteractionEvents];
        });
    }];
    
    [self presentViewController:viewController animated:NO completion:nil];
    
    [CATransaction commit];
}

#pragma mark - call corresponding homeVC methods

-(void) showManualLoginView
{
    if (self.iPadHome9VC)
    {
        [self.iPadHome9VC showManualLoginView];
    }
    else if(self.home9VC)
    {
        [self.home9VC showManualLoginView];
    }
    else if (self.goviPadHome9VC)
    {
        [self.goviPadHome9VC showManualLoginView];
    }
    else if (self.govHome9VC)
    {
        [self.govHome9VC showManualLoginView];
    }
}

/**
 Updates Home with login info
 */
-(void) doPostLoginInitialization
{
    if (self.iPadHome9VC)
    {
        [self.iPadHome9VC doPostLoginInitialization];
    }
    else if(self.home9VC)
    {
        [self.home9VC doPostLoginInitialization];
    }
    // update tabbar after login
    [self updateTabBar];
}

-(void)removeTestDriveStoryBoard
{
    if (self.iPadHome9VC)
    {
        [self.iPadHome9VC removeTestDriveStoryBoard];
    }
    else if(self.home9VC)
    {
        [self.home9VC removeTestDriveStoryBoard];
    }
}

-(void) showPasswordRestScreen
{
    if (self.iPadHome9VC)
    {
        [self.iPadHome9VC showPasswordRestScreen];
    }
    else if(self.home9VC)
    {
        [self.home9VC showPasswordRestScreen];
    }

}
// The applicationdidfinishlaunch no longer initiates the home9 or ipadhome9 as rootviewcontroler
// It always loads homeloader as root so homloader has a helper class to return rootvc. 
// Rootvc is not actually the rootviewcontroller of the application
// Rootvc is rather a utility object so return the same from ipad or iphone home.

-(UIViewController *)getRootviewController
{
    if([UIDevice isPad])
    {
        if ([Config isGov])
            return self.goviPadHome9VC.rootVC;
        else
            return self.iPadHome9VC.rootVC;
    }
    else
    {
        if ([Config isGov])
            return self.govHome9VC.rootVC;
        else
            return self.home9VC.rootVC;
    }
}

-(UIViewController *)getHomeVC
{
    if([UIDevice isPad])
    {
        if ([Config isGov])
            return self.goviPadHome9VC;
        else
            return self.iPadHome9VC;
    }
    else
    {
        if ([Config isGov])
            return self.govHome9VC;
        else
            return self.home9VC;
    }
}


-(void) willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [super willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
    [self rotateHomeCollectionViewToInterfaceOrientation:toInterfaceOrientation];
}

/**
 This programmatically adjusts the height of the rotating image bar
 */
- (void)rotateHomeCollectionViewToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
 
    // Hack to change the container size in home storyboard
    if(![UIDevice isPad])
    {
       // Apply this change only for iPhone
       // if this is iphone 5 then show more of rotating view.
        if([ExSystem is5])
        {
            self.coRotatingImageHeight.constant = 120;
        }
        else
        {
            self.coRotatingImageHeight.constant = 85;
        }
        return;
    }
    
    if (UIInterfaceOrientationIsPortrait(toInterfaceOrientation)) {
        if ([ExSystem is7Plus]) {
            self.coRotatingImageHeight.constant = 264;
        } else {
            // need to compensate for the top nav bar, status bar and tab bar size differences
            self.coRotatingImageHeight.constant = 187;
        }
    } else {
        if ([ExSystem is7Plus]) {
            self.coRotatingImageHeight.constant = 306;
        } else {
        	// need to compensate for the top nav bar, status bar and tab bar size differences
            self.coRotatingImageHeight.constant = 229;
        }
    }
}

#pragma mark -
#pragma mark Tabbar appearance functions

/**
 Updates tabbar appearance based on user roles
 */
- (void)updateTabBar
{
    if ([[ExSystem sharedInstance] isTravelOnly] || [[ExSystem sharedInstance] isApprovalOnlyUser] || [[ExSystem sharedInstance] isTravelAndApprovalOnlyUser])
    {
        [self hideTabBar];
    }
    else
    {
        [self showTabBar];
    }
}

/**
 This programmatically hides the tab bar
 */
- (void)hideTabBar
{
    self.coTabBarHeight.constant = 0;
}

/**
 This programmatically shows the tab bar
 */
- (void)showTabBar
{
    if ([UIDevice isPad])
    {
        if ([ExSystem is7Plus]) {
            self.coTabBarHeight.constant = 85;
        } else {
            // appears the view height is larger on iOS6, need to compensate
            self.coTabBarHeight.constant = 100;
        }
    } else {
        self.coTabBarHeight.constant = 73;
    }
}

- (void)messageCenter:(MessageCenterManager *)manager didAddMessage:(MessageCenterMessage *)message {
    [self setMessageCenterIcon];
}

- (void)messageCenter:(MessageCenterManager *)manager didRemoveMessage:(MessageCenterMessage *)message {
    [self setMessageCenterIcon];
}

- (void)messageCenter:(MessageCenterManager *)manager didChangeMessageType:(MessageCenterMessage *)message {
    [self setMessageCenterIcon];
}

@end
