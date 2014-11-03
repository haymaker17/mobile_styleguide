//
//  ViewStateHelper.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "ViewStateHelper.h"
#import "Config.h"

@implementation ViewStateHelper

@synthesize waitingStateView, negDataView, baseViewState, loadingStateView;
@synthesize delegate = _delegate;

const int MODAL_LOADING_VIEW_Y_OFFSET_IPHONE = 20;
const int MODAL_LOADING_VIEW_Y_OFFSET_IPAD = 40;

-(void) hideViewsForState:(int)toState
{
    switch (toState)
    {
        case VIEW_STATE_NORMAL:
        {
            if (!loadingStateView.hidden) {
                [self hideLoadingViewMaintainState];
            }
            
            if (!waitingStateView.hidden) {
                [self hideWaitingViewMaintainState];
            }
            
            if (!negDataView.hidden) {
                [self hideNegativeViewMaintainState];
            }
        }
            break;
        case VIEW_STATE_LOADING:
        {
            if (!waitingStateView.hidden) {
                [self hideWaitingViewMaintainState];
            }
            
            if (!negDataView.hidden) {
                [self hideNegativeViewMaintainState];
            }
        }
            break;
        case VIEW_STATE_WAITING:
        {
            if (!loadingStateView.hidden) {
                [self hideLoadingViewMaintainState];
            }
            
            if (!negDataView.hidden) {
                [self hideNegativeViewMaintainState];
            }
        }
            break;
        case VIEW_STATE_OFFLINE:
        case VIEW_STATE_NEGATIVE:
        {
            if (!loadingStateView.hidden) {
                [self hideLoadingViewMaintainState];
            }
            
            if (!waitingStateView.hidden) {
                [self hideWaitingViewMaintainState];
            }
        }
            break;
        default:
            break;
    }
}

-(void) hideLoadingViewMaintainState
{
    [loadingStateView setHidden:YES];
    [[self.delegate viewController].view sendSubviewToBack:loadingStateView];
    if ([[self.delegate viewController].view isKindOfClass:[UITableView class]])
    {
        [(UITableView *)[self.delegate viewController].view setSeparatorStyle:UITableViewCellSeparatorStyleSingleLine];
    }
}

-(void) hideWaitingViewMaintainState
{
    [waitingStateView setHidden:YES];
    [[self.delegate viewController].view sendSubviewToBack:waitingStateView];
}

-(void) hideNegativeViewMaintainState
{
    [negDataView setHidden:YES];
    [[self.delegate viewController].view sendSubviewToBack:negDataView];
}

#pragma public API
-(id) initWithDelegate:(id<ViewStateDelegate, NoDataMasterViewDelegate>)del
{
    if (self = [super init])
    {
        baseViewState = VIEW_STATE_NORMAL;
        self.delegate = del;
    }
    return self;
}

#pragma mark -
#pragma mark Offline Bar

-(void)makeOfflineBar
{
    baseViewState = VIEW_STATE_OFFLINE;
    [self hideViewsForState:VIEW_STATE_OFFLINE];
    
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 30, 100, 30)];
	[lbl setTextColor:[UIColor orangeColor]];
	[lbl setText:[Localizer getLocalizedText:@"Offline"]];
	[lbl setFont:[UIFont boldSystemFontOfSize:17.0f]];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setTextAlignment:NSTextAlignmentCenter];
	[lbl setShadowColor:[UIColor colorWithWhite:0.0f alpha:0.5f]];
	[lbl setShadowOffset:CGSizeMake(0.0f, -1.0f)];
	UIBarButtonItem *customBarItem = [[UIBarButtonItem alloc] initWithCustomView:lbl];
	
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	
	NSArray *toolbarItems = @[flexibleSpace,customBarItem, flexibleSpace];
	[[self.delegate viewController] setToolbarItems:toolbarItems animated:YES];

}

-(void)makeOfflineBarWithLastUpdateMsg:(NSString*) lastUpdatedStr
{
    baseViewState = VIEW_STATE_OFFLINE;
    [self hideViewsForState:VIEW_STATE_OFFLINE];
    
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 30, 100, 30)];
	[lbl setTextColor:[UIColor orangeColor]];
	[lbl setText:[Localizer getLocalizedText:@"Offline"]];
	[lbl setFont:[UIFont boldSystemFontOfSize:17.0f]];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setTextAlignment:NSTextAlignmentCenter];
	[lbl setShadowColor:[UIColor colorWithWhite:0.0f alpha:0.5f]];
	[lbl setShadowOffset:CGSizeMake(0.0f, -1.0f)];
	UIBarButtonItem *customBarItem = [[UIBarButtonItem alloc] initWithCustomView:lbl];
	
	int refreshDateWidth = 160;
	int refreshDateHeight = 30;
	int numberOfLines = 2;
	
	UIView *cv = [[UIView alloc] initWithFrame:CGRectMake(0, 0, refreshDateWidth, refreshDateHeight)];
    UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, refreshDateWidth, refreshDateHeight)];
	lblText.numberOfLines = numberOfLines;
	lblText.lineBreakMode = NSLineBreakByWordWrapping;
	lblText.textAlignment = NSTextAlignmentRight;
	lblText.text = lastUpdatedStr;
	[lblText setBackgroundColor:[UIColor clearColor]];
	[lblText setTextColor:[UIColor whiteColor]];
	[lblText setShadowColor:[UIColor blackColor]];
	[lblText setShadowOffset:CGSizeMake(0, -1)];
	[lblText setFont:[UIFont boldSystemFontOfSize:12.0f]];
	[cv addSubview:lblText];
	UIBarButtonItem *btnRefreshDate = [[UIBarButtonItem alloc] initWithCustomView:cv];
    
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	
	NSArray *toolbarItems = @[flexibleSpace,customBarItem, flexibleSpace, btnRefreshDate];
	[[self.delegate viewController] setToolbarItems:toolbarItems animated:YES];
}

#pragma mark Offline view
// Note: This offline view resembles the negative view, except, there is no action available. Please check the design spec before using it.
-(void)hideOfflineView
{
    if ([UIDevice isPad] && [self.delegate neediPadWaitViews])
    {
        return;
    }
    
    baseViewState = VIEW_STATE_NORMAL;
    
    [negDataView setHidden:YES];
    [[self.delegate viewController].view sendSubviewToBack:negDataView];
}

-(void)showOfflineView:(UIViewController<NoDataMasterViewDelegate>*)callingViewController
{
    if ([UIDevice isPad] && [self.delegate neediPadWaitViews])
    {
        return;
    }
    
    baseViewState = VIEW_STATE_OFFLINE;
    [self hideViewsForState:VIEW_STATE_OFFLINE];
    
    if (negDataView == nil)
    {
        self.negDataView = [[NoDataMasterView alloc] initWithNib];
        negDataView.delegate = callingViewController;
        
        negDataView.frame = [self.delegate viewController].view.frame;
        
        for (UIView *v in [callingViewController.view subviews])
        {
            if ([v isKindOfClass:[UITableView class]])
            {
                negDataView.frame = v.frame;
                break;
            }
        }
        
        [negDataView prepareSubviews];
        [[self.delegate viewController].view addSubview:negDataView];
    }
    else
    {
        negDataView.delegate = callingViewController;
        [negDataView setHidden:NO];
        [[self.delegate viewController].view bringSubviewToFront:negDataView];
    }
}

#pragma mark Loading view
-(void) adjustLoadingViewForSpecialCases
{
    //MOB-9533 & MOB-10196 Loading data gray area doesn't cover full screen
    if([self respondsToSelector:@selector(presentingViewController)])
    {
        if ([self.delegate viewController].presentingViewController != nil)
        {
            // Adjust Loading view frame if presented modally
            int offset = 0;
            
            if ([UIDevice isPad])
                offset = [self.delegate viewController].presentingViewController.view.frame.origin.y;
            else
                offset = MODAL_LOADING_VIEW_Y_OFFSET_IPHONE;
            
            int y = loadingStateView.frame.origin.y + offset;
            int height = loadingStateView.frame.size.height - offset;
            //MOB-10678 loading aire missed by 20 pts. TravelRequest loading page
            if (y == 20 && ![UIDevice isPad])
            {
                y = loadingStateView.frame.origin.y;
                height = loadingStateView.frame.size.height;
            }
            
            loadingStateView.frame = CGRectMake(loadingStateView.frame.origin.x, y, loadingStateView.frame.size.width, height);
        }
    }
}

-(void) showLoadingView
{
    if ([UIDevice isPad] && [self.delegate neediPadWaitViews])
    {
        return;
    }
    
    if (loadingStateView != nil)
    {
        baseViewState = VIEW_STATE_LOADING;
        [self hideViewsForState:VIEW_STATE_LOADING];
        
        [self adjustLoadingViewForSpecialCases];
        
        [loadingStateView setHidden:NO];
        [[self.delegate viewController].view bringSubviewToFront:loadingStateView];
        [loadingStateView startSpinner];
    }
    else
    {
        [self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading Data"]];
    }
}

-(void)showLoadingViewWithText:(NSString*)text
{
    if ([UIDevice isPad] && [self.delegate neediPadWaitViews])
    {
        return;
    }
    
    baseViewState = VIEW_STATE_LOADING;
    [self hideViewsForState:VIEW_STATE_LOADING];
    
    if (loadingStateView == nil)
    {
        self.loadingStateView = [[WaitStateMasterView alloc] initWithLoadingViewNib];
        
        CGRect fr = [self.delegate viewController].view.frame;
        // if view has scrolled then the origin might change, so set the frame accordingly. 
        CGRect fr1 = [self.delegate viewController].view.bounds;
        loadingStateView.frame = CGRectMake(fr1.origin.x, fr1.origin.y, fr.size.width, fr.size.height);
        
        [self adjustLoadingViewForSpecialCases];
        [loadingStateView setWaitLabelText:text];
        [[self.delegate viewController].view addSubview:loadingStateView];
        if ([[self.delegate viewController].view isKindOfClass:[UITableView class]])
        {
            [(UITableView *)[self.delegate viewController].view setSeparatorStyle:UITableViewCellSeparatorStyleNone];
        }
        [[self.delegate viewController].view bringSubviewToFront:loadingStateView];
    }
    else
    {
        [self adjustLoadingViewForSpecialCases];
        // if view has scrolled then set the frame to the right place.
        CGRect fr = [self.delegate viewController].view.frame;
        CGRect fr1 = [self.delegate viewController].view.bounds;
        loadingStateView.frame = CGRectMake(fr1.origin.x, fr1.origin.y, fr.size.width, fr.size.height);
        [loadingStateView setHidden:NO];
        [[self.delegate viewController].view bringSubviewToFront:loadingStateView];
    }
    
    [loadingStateView startSpinner];
}

-(void)hideLoadingView
{
    if ([UIDevice isPad] && [self.delegate neediPadWaitViews])
    {
        return;
    }
    
    baseViewState = VIEW_STATE_NORMAL;
    
    [loadingStateView setHidden:YES];
    [[self.delegate viewController].view sendSubviewToBack:loadingStateView];
    if ([[self.delegate viewController].view isKindOfClass:[UITableView class]])
    {
        [(UITableView *)[self.delegate viewController].view setSeparatorStyle:UITableViewCellSeparatorStyleSingleLine];
    }
}

-(BOOL) isLoadingViewShowing
{
    return (loadingStateView != nil && !loadingStateView.hidden);
}

#pragma mark -
#pragma mark Connection Progress tracking Methods - used by the wait view
-(void) startListeningToConnectionProgressNotification
{
	NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
	[defaultCenter addObserver:self selector:@selector(receivedConnectionProgressNotification:) name:MVC_CONNECTION_PROGRESS_MESSAGE object:nil];
}

-(void) stopListeningToConnectionProgressNotification
{
	NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
	[defaultCenter removeObserver:self name:MVC_CONNECTION_PROGRESS_MESSAGE object:nil];
}

-(void) receivedConnectionProgressNotification:(NSNotification*)notification
{
	NSDictionary* userInfoDict = notification.userInfo;
	if (userInfoDict != nil)
	{
        int totalBytesWritten = [userInfoDict[@"TOTAL_WRITTEN_BYTES"] intValue];
        int totalBytesExpectedToWrite = [userInfoDict[@"TOTAL_BYTES_EXPECTED_TO_WRITE"] intValue];
        
        float progressValue = ((float)totalBytesWritten/(float)totalBytesExpectedToWrite);

        if ([Config isDevBuild]) {
            NSString* origWaitText = self.waitingStateView.waitLabel.text;
            NSRange r = [origWaitText rangeOfString:@", "];
            if (r.location != NSNotFound)
            {
                // should just cut off the string right at ",". if it is r.loation - 1,the string is cut off one letter before "," 
                origWaitText = [origWaitText substringToIndex:(r.location)];
            }
            origWaitText = [NSString stringWithFormat:@"%@, %d / %d bytes", origWaitText, totalBytesWritten, totalBytesExpectedToWrite];
            self.waitingStateView.waitLabel.text = origWaitText;
            [self setWaitViewProgress:progressValue];
        } else {
            [self setWaitViewProgress:progressValue];
        }
    }
}

#pragma mark Wait view
- (void)disableNavigationActions
{
    UINavigationController *navController = (UINavigationController*)[ConcurMobileAppDelegate getBaseNavigationController];
    
    [navController.topViewController.view setUserInteractionEnabled:NO];
    [navController.navigationBar setUserInteractionEnabled:NO];
    [navController.toolbar setUserInteractionEnabled:NO];
}

- (void)enableNavigationActions
{
    UINavigationController *navController = (UINavigationController*)[ConcurMobileAppDelegate getBaseNavigationController];
    
    [navController.topViewController.view setUserInteractionEnabled:YES];
    [navController.navigationBar setUserInteractionEnabled:YES];
    [navController.toolbar setUserInteractionEnabled:YES];
}

- (void)hideWaitView
{
    if ([UIDevice isPad])
    {
        [waitingStateView setHidden:YES];
        if ([self.delegate neediPadWaitViews]) {
            return;
        }
    }
    
    baseViewState = VIEW_STATE_NORMAL;
    
    [self enableNavigationActions];
    
    [waitingStateView setHidden:YES];
    [[self.delegate viewController].navigationController.view sendSubviewToBack:waitingStateView];
    
    if (waitingStateView.showProgress) {
        [self stopListeningToConnectionProgressNotification];
    }
}

- (void)showWaitView
{
    if ([UIDevice isPad] && [self.delegate neediPadWaitViews])
    {
        return;
    }
    
    [self disableNavigationActions];
    
    if (waitingStateView != nil)
    {
        baseViewState = VIEW_STATE_WAITING;
        [self hideViewsForState:VIEW_STATE_WAITING];
        
        [waitingStateView setHidden:NO];
        [[self.delegate viewController].navigationController.view bringSubviewToFront:waitingStateView];
        [waitingStateView startSpinner];
    }
    else
    {
        [self showWaitViewWithText:[Localizer getLocalizedText:@"Waiting"]];
    }
}

-(void) setWaitViewProgress:(float)progress
{
    [waitingStateView.progressIndicator setProgress:progress];
}

-(void)enableProgressBar:(BOOL)enableProgressView
{
    baseViewState = VIEW_STATE_WAITING;
    [self hideViewsForState:VIEW_STATE_WAITING];
    
    [waitingStateView setShowProgress:YES];
    [waitingStateView.progressIndicator setHidden:NO];
    [waitingStateView.progressIndicator setProgress:0];
    [self startListeningToConnectionProgressNotification];
}

-(void) showWaitViewWithProgress:(BOOL)enableProgressView withText:(NSString*) waitText
{
    [self showWaitViewWithText:waitText];
    [self enableProgressBar:enableProgressView];
}

-(void)showWaitViewWithText:(NSString*)text
{
    if ([UIDevice isPad] && [self.delegate neediPadWaitViews])
    {
        return;
    }
    
    baseViewState = VIEW_STATE_WAITING;
    [self hideViewsForState:VIEW_STATE_WAITING];
    
    [self disableNavigationActions];
    
    if([UIDevice isPad])
    {
        if (self.waitingStateView != nil)
        {   // MOB-7578 To prevent orphaned wait view, when it is called twice from iPad screens.
            [waitingStateView removeFromSuperview];
        }
        self.waitingStateView = nil;
    }
    if (waitingStateView == nil)
    {
        self.waitingStateView = [[WaitStateMasterView alloc] initWithWaitViewNib];
        
        if ([UIDevice isPad])
        {
            bool isInPadNavStack = false;
            UIViewController* padVC = [ConcurMobileAppDelegate findHomeVC];
            isInPadNavStack = ([self.delegate viewController].navigationController == padVC.navigationController);
            
            if (isInPadNavStack)
                [waitingStateView resetLayout];// nav.view is always in portrait on ipad, manual reset size
            else     //MOB-16174:Use bounds here since the view.frame will not update after rotation
                waitingStateView.frame = [self.delegate viewController].navigationController.view.bounds;
        }
        else
        {
            waitingStateView.frame = [self.delegate viewController].navigationController.view.frame; 
        }
        
        [waitingStateView setWaitLabelText:text];
        [[self.delegate viewController].navigationController.view addSubview:waitingStateView];
        [[self.delegate viewController].navigationController.view bringSubviewToFront:waitingStateView];
    }
    else
    {
        [waitingStateView setHidden:NO];
        [waitingStateView setWaitLabelText:text];
        [[self.delegate viewController].navigationController.view bringSubviewToFront:waitingStateView];
    }
    
    [waitingStateView startSpinner];
}

-(BOOL) isWaitViewShowing
{
    return (waitingStateView != nil && !waitingStateView.hidden);
}

#pragma mark Negative view
-(void)hideNoDataView
{
    if ([UIDevice isPad] && [self.delegate neediPadWaitViews])
    {
        return;
    }
    
    baseViewState = VIEW_STATE_NORMAL;
    
    [negDataView setHidden:YES];
    [[self.delegate viewController].view sendSubviewToBack:negDataView];
}

-(void)showNoDataView:(UIViewController<NoDataMasterViewDelegate>*)callingViewController
{
    if ([UIDevice isPad] && [self.delegate neediPadWaitViews])
    {
        return;
    }
    
    baseViewState = VIEW_STATE_NEGATIVE;
    [self hideViewsForState:VIEW_STATE_NEGATIVE];
    
    if (negDataView == nil)
    {
        self.negDataView = [[NoDataMasterView alloc] initWithNib];
        negDataView.delegate = callingViewController;
        
        CGRect fr = [self.delegate viewController].view.frame;
        negDataView.frame = CGRectMake(0, 0, fr.size.width, fr.size.height);

        [negDataView prepareSubviews];
        [[self.delegate viewController].view addSubview:negDataView];
        [[self.delegate viewController].view bringSubviewToFront:negDataView];
    }
    else
    {
        negDataView.delegate = callingViewController;
        [negDataView setHidden:NO];
        [[self.delegate viewController].view bringSubviewToFront:negDataView];
    }
}

- (void)showNoDataView:(MobileViewController*)callingViewController asSubviewOfView:(UIView*)parentView
{
    if ([UIDevice isPad] && [self.delegate neediPadWaitViews])
    {
        return;
    }
    
    [self showNoDataView:callingViewController];
    
    [negDataView removeFromSuperview];
    
    if (parentView != nil)
    {
        // MOB-5971 Adjust no data view size, in case the parent view is smaller than its size.
        CGFloat height = negDataView.frame.size.height > parentView.frame.size.height? parentView.frame.size.height:negDataView.frame.size.height;
        negDataView.frame = CGRectMake(parentView.frame.origin.x, parentView.frame.origin.x, negDataView.frame.size.width, /*negDataView.frame.size.*/height);
        [parentView addSubview:negDataView];
        [parentView bringSubviewToFront:negDataView];
    }
}

- (void)showNewNoDataView:(UIViewController<NoDataMasterViewDelegate>*)callingViewController
{
    // Although most of the time we just show and hide the same negative data view, this method handles the case where we need to destroy the old negative data view and then make a new one.  This is necessary for the expense/receipts combo view controller where sometimes the negative data view says there are no expenses and other time it says there are no receipts.
    [self destroyNoDataView];
    
    return [self showNoDataView:callingViewController];
}

- (void)destroyNoDataView
{
    if (self.negDataView != nil && self.negDataView.superview != nil)
        [self.negDataView removeFromSuperview];

    self.negDataView = nil;
}

@end
