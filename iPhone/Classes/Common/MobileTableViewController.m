//
//  MobileTableViewController.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/8/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//
//  
//

#import "MobileTableViewController.h"
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

//NSString * const MVC_CONNECTION_PROGRESS_MESSAGE = @"MVC_CONNECTION_PROGRESS_MESSAGE";

@interface MobileTableViewController ()
// Pull down refresh
-(void) initPullDownRefresh;

@end

@implementation MobileTableViewController

@synthesize IDKey;
@synthesize cameFrom;
@synthesize pickerPopOver;
@synthesize actionPopOver;
@synthesize uploadView;
@synthesize viewStateHelper;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
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
    if ([self isWaitViewShowing])
    {
        [self hideWaitView];
// Login Time out error should not show offline message at the same time
// This is trigered by a connection failuar (mostly time out) to the server
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

-(void) respondToFoundData:(Msg *)msg
{
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
    // Comment out all the MCLogging as UIViewController+logging category will take care of it.
//	[[MCLogging getInstance] log:[NSString stringWithFormat:@"MVC::viewDidDisappear: %@", [self class]] Level:MC_LOG_DEBU];
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
    // Comment out all the MCLogging as UIViewController+logging category will take care of it.
//	[[MCLogging getInstance] log:[NSString stringWithFormat:@"MVC::viewDidLoad: %@", [self class]] Level:MC_LOG_DEBU];
    [super viewDidLoad];
    
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
    
    // need to reset the top on iOS7 to be like iOS6
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }

    UITableView *tblv = self.tableView;
    // Show soft gray background for grouped table
    if (tblv.style == UITableViewStyleGrouped)
    {
        tblv.backgroundColor = [UIColor colorWithRed:0.882871 green:0.887548 blue:0.892861 alpha:1];
        tblv.backgroundView = nil;
    }
    // TODO - set line separator color
    
    [self initPullDownRefresh];
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

-(void)makeOfflineBarWithLastUpdateMsg:(NSString*) lastUpdatedStr
{
    [self.viewStateHelper makeOfflineBarWithLastUpdateMsg:lastUpdatedStr];
}

//-(UIView *)makeOfflineHeader
//{
//    offlineBackground = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 480, 20)];
//    [offlineBackground setBackgroundColor:[UIColor redColor]];
//    offlineText = [[UILabel alloc] init];
//    offlineText.text = [Localizer getLocalizedText:@"Offline"];
//   [offlineBackground addSubview:offlineText];
//   [offlineBackground bringSubviewToFront:offlineText];

//    return offlineBackground;
//}

-(UploadBannerView *)makeUploadView
{
    if (self.uploadView == nil)
        self.uploadView = [UploadBannerView getUploadView];
    
    //self.uploadView.frame = CGRectMake(0, 0, uploadView.frame.size.width, uploadView.frame.size.height);
    int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
    [self.uploadView setBannerText:itemNum];
    
    return self.uploadView;
}

#pragma mark Filter View Controllers that do not use iPhone XIB for loading/wait views
-(BOOL)neediPadWaitViews
{
    if ([self isKindOfClass:[ReportDetailViewController_iPad class]] || [self isKindOfClass:[iPadHomeVC class]])
    {
        return TRUE;
    }
    else
    {
        return FALSE;
    }
}

#pragma mark ViewStateDelegate method
-(UIViewController*) viewController
{
    return self;
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

#pragma mark Offline view
-(void) showOfflineView:(MobileTableViewController*)callingViewController
{
    [self.viewStateHelper showOfflineView:callingViewController];
}

-(void) hideOfflineView
{
    [self.viewStateHelper hideOfflineView];
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

#pragma mark PullDownRefresh
-(BOOL) enablePullDownRefresh
{
    if ([ExSystem is6Plus])
        return [ExSystem connectedToNetwork];
    return NO;
}

// Return whether the task is completed
-(BOOL) refreshView:(UIRefreshControl*) refresh
{
    // To be implemented by subclass
    return YES;
}

-(void) initPullDownRefresh
{
    if ([self enablePullDownRefresh])
    {
        UIRefreshControl *refresh = [[UIRefreshControl alloc] init];
        
        NSString *title = [Localizer getLocalizedText:@"Pull to Refresh"];
        refresh.attributedTitle = [[NSAttributedString alloc] initWithString:title];
        
        [refresh addTarget:self action:@selector(refreshViewImpl:) forControlEvents:UIControlEventValueChanged];
        
        self.refreshControl = refresh;
        
        // MOB-16947 'Pull to Refresh' string is overlapping the spinner dialogue.
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.refreshControl beginRefreshing];
            [self.refreshControl endRefreshing];
        });
    }
}

-(void) doneOffline
{
    self.refreshControl.attributedTitle = [[NSAttributedString alloc] initWithString:[Localizer getLocalizedText:@"Offline"]];
    
    [self.refreshControl endRefreshing];
}

-(void) refreshViewImpl:(UIRefreshControl*)refresh
{
    if (![ExSystem connectedToNetwork])
    {
        [self doneOffline];
        return;
    }
    NSString *title = [Localizer getLocalizedText:@"Refreshing Data"];
    NSString *titleStr = [title stringByAppendingString:@"..."];
    refresh.attributedTitle = [[NSAttributedString alloc] initWithString:titleStr];
    // custom refresh logic would be placed here...
    
    BOOL taskCompleted = [self refreshView:refresh];
    if (taskCompleted)
        [self doneRefreshing];

}

-(void) doneRefreshing
{
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    
    [formatter setDateFormat:@"MMM d, h:mm a"];
    NSString *title = [Localizer getLocalizedText:@"Last updated on"];
    
    NSString *lastUpdated = [NSString stringWithFormat:@"%@ %@", title, [formatter stringFromDate:[NSDate date]]];
    
    self.refreshControl.attributedTitle = [[NSAttributedString alloc] initWithString:lastUpdated];
    
    [self.refreshControl endRefreshing];
}

-(BOOL) isRefreshing
{
    return ([self enablePullDownRefresh] ? self.refreshControl.isRefreshing : NO);
}

@end
