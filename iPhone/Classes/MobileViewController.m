//
//  MobileViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/10/09.
//  Copyright 2009 Concur. All rights reserved.
//

// Mobileviewcontroller with viewstatehelper

#import "MobileViewController.h"
#import "ExSystem.h"
#import "FindMe.h"
#import "MCLogging.h"
#import "MsgHandler.h"
#import "UnifiedImagePicker.h"
#import "ConcurMobileAppDelegate.h"
#import "ExSystem.h"
#import "MsgControl.h"

#import "ReportDetailViewController_iPad.h"
#import "iPadHomeVC.h"

#import "UploadQueue.h"
#import "Config.h"
#import "ViewStateHelper.h"

NSString * const MVC_CONNECTION_PROGRESS_MESSAGE = @"MVC_CONNECTION_PROGRESS_MESSAGE";

@interface MobileViewController ()

// Helper functions

-(BOOL) neediPadWaitViews;
@end

@implementation MobileViewController
@synthesize IDKey;
@synthesize cameFrom;
@synthesize pickerPopOver;
@synthesize actionPopOver;
@synthesize negDataView;

@synthesize baseViewState;
@synthesize uploadView;

NSString * const VIEW_DISPLAY_TYPE_NAVI = @"VIEW_DISPLAY_TYPE_NAVI";
NSString * const VIEW_DISPLAY_TYPE_REGULAR = @"VIEW_DISPLAY_TYPE_REGULAR";
NSString * const VIEW_DISPLAY_TYPE_MODAL = @"VIEW_DISPLAY_TYPE_MODAL";


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
        self.viewStateHelper = [[ViewStateHelper alloc] initWithDelegate:self];
    }
    return self;
}

// In storyboard, this is used to intialize things
- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
        // Custom initialization
        self.viewStateHelper = [[ViewStateHelper alloc] initWithDelegate:self];
    }
    return self;
}


#pragma mark Utility Methods
-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_REGULAR;
}

-(NSString *)getViewIDKey
{
	return IDKey;
}

#pragma mark - ExMsgRespondDelegate Method

-(void) didProcessMessage:(Msg *)msg
{
    if ([msg didConnectionFail])
        [self respondToConnectionFailure:msg];
    else
        [self respondToFoundData:msg];
}

-(void) respondToConnectionFailure:(Msg* )msg
{
    // Check if the VC is able to handle timeouts itself thru a selector
    if ([self respondsToSelector:@selector(handleTimeOut)])
    {
        [self performSelector:@selector(handleTimeOut) withObject:nil];
    }
    else
    {
        // otherwise use default timeout handling
        if ([self isWaitViewShowing])
        {
            [self hideWaitView];
    // Login Time out error should not show offline message at the same time
    // This is trigered by a connection failuar (mostly time out, or wrong request address) to the server
            if(![ExSystem connectedToNetwork])
            {
                MobileAlertView *alert = [[MobileAlertView alloc]
                                          initWithTitle:[Localizer getLocalizedText:@"Offline"]
                                          message:[Localizer getLocalizedText:@"Operation Not Supported Offline"]
                                          delegate:nil
                                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                          otherButtonTitles:nil];
                [alert show];
            }
        }
        
        if ([self isLoadingViewShowing])
        {
            [self hideLoadingView];
            [self showOfflineView:self];
        }
    }

}

-(void) respondToFoundData:(Msg *)msg
{
    NSLog(@"Mobile View Controller implementation of RespondToFoundData is Empty");
}

-(void)setParentReturnValues:(NSMutableDictionary *)returnValues
{
	
}

- (void)applicationDidEnterBackground
{
    // Any clean up that your view controller needs to do when the application enters the background
    // should go here.  Note: not all mobile view controllers will receive this message.  Please see
    // ConcurMobileAppDelegate::applicationDidEnterBackground to see which controllers will receive it.
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"MVC::applicationDidEnterBackground: %@", [self class]] Level:MC_LOG_DEBU];
    
    self.actionPopOver = nil;
}

#pragma mark
#pragma mark View methods
-(void) viewDidAppear:(BOOL)animated
{
    // Comment out all the MCLogging as UIViewController+logging category will take care of it. 
//	[[MCLogging getInstance] log:[NSString stringWithFormat:@"MVC::viewDidAppear: %@", [self class]] Level:MC_LOG_DEBU];
	[super viewDidAppear:animated];
}

-(void) viewDidDisappear:(BOOL)animated
{
//    [[MCLogging getInstance] log:[NSString stringWithFormat:@"MVC::viewDidDisappear: %@", [self class]] Level:MC_LOG_DEBU];
    if ([UIDevice isPad] && self.actionPopOver != nil)
    {
        [self.actionPopOver dismissWithClickedButtonIndex:self.actionPopOver.cancelButtonIndex animated:NO];
        self.actionPopOver = nil;
    }
    // MOB-10761 removed for ARC migration. TODO - check w/ Charlotte
	//[[self retain] autorelease];
	[super viewDidDisappear:animated];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    
    if ([self respondsToSelector:@selector(edgesForExtendedLayout)])
    {
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }
    
    baseViewState = VIEW_STATE_NORMAL;
    if ([UIDevice isPad]) {
        if ([ExSystem is7Plus]) {
            [self.navigationController.navigationBar setTintColor:[UIColor concurBlueColor]];
            [self.navigationController.toolbar setTintColor:[UIColor concurBlueColor]];
        }
        else {       // iOS 6, did not chack for iOS6plus, because afraid of iOS7 included. only support iOS 7 and 6 at this point
            [self.navigationController.navigationBar setTintColor:[UIColor navBarTintColor_iPad]];
            [self.navigationController.toolbar setTintColor:[UIColor navBarTintColor_iPad]];
       }
    }
    else if ([UIDevice isPhone]) {
        if ([ExSystem is7Plus]) {
            [self.navigationController.navigationBar setTintColor:[UIColor concurBlueColor]];
            [self.navigationController.toolbar setTintColor:[UIColor concurBlueColor]];
        }
        else {       // iOS 6, did not chack for iOS6plus, because afraid of iOS7 included. only support iOS 7 and 6 at this point
            [self.navigationController.navigationBar setTintColor:[UIColor darkBlueConcur_iOS6]];
            [self.navigationController.toolbar setTintColor:[UIColor darkBlueConcur_iOS6]];
            [self.navigationController.navigationBar setAlpha:0.9f];
            [self.navigationController.toolbar setAlpha:0.9f];
        }
    }

//	[[MCLogging getInstance] log:[NSString stringWithFormat:@"MVC::viewDidLoad: %@", [self class]] Level:MC_LOG_DEBU];
    [super viewDidLoad];
    
    [self configureTables];
}


- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
//	[[MCLogging getInstance] log:[NSString stringWithFormat:@"MVC::viewDidUnload: %@", [self class]] Level:MC_LOG_DEBU];
    
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
}


- (void)dealloc
{
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"MVC::dealloc: %@", [self class]] Level:MC_LOG_DEBU];
	[MsgHandler cancelAllRequestsForDelegate:self];
}

-(void) dismissPopovers
{
}

-(NSMutableArray*) getPopovers
{
	__autoreleasing NSMutableArray* popovers = [[NSMutableArray alloc] initWithObjects:nil];
	
	if (pickerPopOver != nil)
		[popovers addObject: pickerPopOver];
	
	return popovers;
}

#pragma mark -
#pragma mark UIActionSheetDelegate method
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (self.actionPopOver != nil)
        self.actionPopOver = nil;
}

#pragma mark -
#pragma mark Offline Bar
-(void)makeOfflineBar
{
 [self.viewStateHelper makeOfflineBar];
}



-(UploadBannerView *)makeUploadView
{
    if (self.uploadView == nil)
        self.uploadView = [UploadBannerView getUploadView];
    
    //    self.uploadView.frame = CGRectMake(0, 0, uploadView.frame.size.width, uploadView.frame.size.height);
    if([UIDevice isPad])
    {
        [self.uploadView setFrame:CGRectMake(uploadView.frame.origin.x, uploadView.frame.origin.y, 540, uploadView.frame.size.height)];
        [self.uploadView.btn setFrame:CGRectMake(uploadView.btn.frame.origin.x, uploadView.btn.frame.origin.y, 531, uploadView.frame.size.height)];
    }
    int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
    [self.uploadView setBannerText:itemNum];
    
    return self.uploadView;
}

-(void)makeOfflineBarWithLastUpdateMsg:(NSString*) lastUpdatedStr
{
   [self.viewStateHelper makeOfflineBarWithLastUpdateMsg:lastUpdatedStr];
}



#pragma mark Wait view
-(void) showWaitView
{
    [self.viewStateHelper showWaitView];
}
-(void) showWaitViewWithText:(NSString *)waitText
{
    [self.viewStateHelper showWaitViewWithText:waitText];
}

-(void) showWaitViewWithProgress:(BOOL)enableProgressView withText:(NSString*) waitText
{
    [self.viewStateHelper showWaitViewWithProgress:enableProgressView withText:waitText];
}

-(void) hideWaitView
{
    [self.viewStateHelper hideWaitView];
}

-(void) setWaitViewProgress:(float)progress
{
    [self.viewStateHelper setWaitViewProgress:progress];
}

-(bool) isWaitViewShowing
{
    return [self.viewStateHelper isWaitViewShowing];
}

#pragma mark Loading view
-(void) showLoadingView
{
    [self.viewStateHelper showLoadingView];
}

-(void) showLoadingViewWithText:(NSString *)waitText
{
    [self.viewStateHelper showLoadingViewWithText:waitText];
}
-(void) hideLoadingView
{
    [self.viewStateHelper hideLoadingView];
}

-(bool) isLoadingViewShowing
{
    return [self.viewStateHelper isLoadingViewShowing];
}

#pragma mark NoDataView method
- (void)showNoDataView:(MobileTableViewController*)callingViewController
{
    [self.viewStateHelper showNoDataView:callingViewController];
}

- (void)hideNoDataView
{
    [self.viewStateHelper hideNoDataView];
}

- (void)showNoDataView:(MobileTableViewController*)callingViewController asSubviewOfView:(UIView*)parentView
{
    [self.viewStateHelper showNoDataView:callingViewController asSubviewOfView:parentView];
}

-(void)showNewNoDataView:(UIViewController<NoDataMasterViewDelegate>*)callingViewController
{
    [self.viewStateHelper showNewNoDataView:callingViewController];
}

#pragma mark NoDataViewDelegate method
-(int) baseViewState
{
    return self.viewStateHelper.baseViewState;
}

- (BOOL)adjustNoDataView:(NoDataMasterView*) negView
{
    // Return whether to hide toolbar
    return YES;
}

-(void) actionOnNoData:(id)sender
{
}

-(BOOL)canShowActionOnNoData
{
    return YES;
}

- (NSString*) buttonTitleForNoDataView
{
    return @"";
}

- (NSString*) titleForNoDataView
{
    return @"";
}

-(BOOL) canShowOfflineTitleForNoDataView
{
    return YES;
}

- (NSString *)instructionForNoDataView
{
    return @"";
}

#pragma mark iOS7 swipe to go back
- (void)disableSwipeToGoBack
{
    UINavigationController *navController = (UINavigationController*)[ConcurMobileAppDelegate getBaseNavigationController];
    [self disableSwipeToGoBack:navController];
}

- (void)enableSwipeToGoBack
{
    UINavigationController *navController = (UINavigationController*)[ConcurMobileAppDelegate getBaseNavigationController];
    [self enableSwipeToGoBack:navController];
}

- (void)disableSwipeToGoBack:(UINavigationController*)navController
{
    if([navController respondsToSelector:@selector(interactivePopGestureRecognizer)])
    {
        navController.interactivePopGestureRecognizer.enabled = NO;
    }
}

- (void)enableSwipeToGoBack:(UINavigationController*)navController
{
    if([navController respondsToSelector:@selector(interactivePopGestureRecognizer)])
    {
        navController.interactivePopGestureRecognizer.enabled = YES;
    }
}


#pragma mark Offline view
// Note: This offline view resembles the negative view, except, there is no action available. Please check the design spec before using it.
-(void)hideOfflineView
{
    [self.viewStateHelper hideOfflineView];
}

-(void)showOfflineView:(MobileViewController*)callingViewController
{
 [self.viewStateHelper showOfflineView:callingViewController];
}

#pragma mark - Table configuration
- (void) configureTables
{
    UIColor *tableBackgroundColor = [self getTableBackgroundColor];
    
	for(UIView *v in self.view.subviews)
	{
		if (v != nil && [v isKindOfClass:[UITableView class]])
		{
			UITableView *tblv = (UITableView*) v;
			
			// Show soft gray background for grouped table
			if (tblv.style == UITableViewStyleGrouped)
			{
                tblv.backgroundColor = tableBackgroundColor;
                tblv.backgroundView = nil;
            }
		}
	}
}

-(UIColor*) getTableBackgroundColor
{
    return [UIColor colorWithRed:0.882871 green:0.887548 blue:0.892861 alpha:1];
}


#pragma mark Filter View Controllers that do not use iPhone XIB for loading/wait views
// implement the delegate for viewstatehelper
-(BOOL)neediPadWaitViews
{
    // Return false by default.
    return FALSE;
}


#pragma mark Autorotate default method
// Override to allow orientations other than the default portrait orientation.
- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations.
    if ([UIDevice isPad] ||
        interfaceOrientation == UIInterfaceOrientationPortraitUpsideDown ||
        interfaceOrientation == UIInterfaceOrientationPortrait)
        return YES;
    return NO;
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	if ([UIDevice isPad] && self.actionPopOver != nil)
	{
		[self.actionPopOver dismissWithClickedButtonIndex:self.actionPopOver.cancelButtonIndex animated:NO];
        self.actionPopOver = nil;
	}
}

#pragma mark - Rate the application
-(void)afterChoiceToRateApp
{
    
}

#pragma mark ViewStateDelegate method
-(UIViewController*) viewController
{
    return self;
}


@end
