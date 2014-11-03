//
//  MobileTableViewController.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/8/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ExMsgRespondDelegate.h"
#import "UploadBannerView.h"
#import "ViewStateHelper.h"
#import "UIDevice+Additions.h"

extern const int MODAL_LOADING_VIEW_Y_OFFSET_IPHONE;
extern const int MODAL_LOADING_VIEW_Y_OFFSET_IPAD;

@interface MobileTableViewController : UITableViewController <ExMsgRespondDelegate, NoDataMasterViewDelegate, ViewStateDelegate>
{
    NSString				*IDKey, *cameFrom;
	UIPopoverController		*pickerPopOver;
    UIActionSheet           *actionPopOver;  // For iPad orientation switch or view unload

    UploadBannerView        *uploadView; // TODO

    ViewStateHelper         *viewStateHelper;
}


@property (strong, nonatomic) NSString *IDKey;
@property (strong, nonatomic) NSString *cameFrom;

@property (strong, nonatomic) UploadBannerView *uploadView;

@property (strong, nonatomic) ViewStateHelper *viewStateHelper;

@property (nonatomic, strong) UIPopoverController *pickerPopOver;
@property (nonatomic, strong) UIActionSheet       *actionPopOver;
//@property (nonatomic, strong) IBOutlet UITableView         *tableView;

-(void) respondToConnectionFailure:(Msg* )msg;
-(void) respondToFoundData:(Msg *)msg;
-(NSString *) getViewDisplayType;
-(NSString *) getViewIDKey;
-(void) setParentReturnValues:(NSMutableDictionary *)returnValues;
-(void) makeOfflineBar;
//-(UIView *) makeOfflineHeader;
-(UploadBannerView *) makeUploadView;
-(void)makeOfflineBarWithLastUpdateMsg:(NSString*) lastUpdatedStr;

-(void) dismissPopovers;
-(NSMutableArray*) getPopovers;
-(void) applicationDidEnterBackground;

// To clean up actionsheet popup for iPad
-(void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex;

#pragma mark PullDownRefresh
// Overriden by subclass
-(BOOL) enablePullDownRefresh;
-(BOOL) refreshView:(UIRefreshControl*) refresh; // Return whether the refreshing task has been completed
-(void) doneRefreshing; // Called after async task completes to update refreshing control
-(BOOL) isRefreshing;

#pragma mark Negative state views
/*
 Negative view/ No data view: Shown when there is no data available
 The NoDataMaterView class contains 3 lookup methods that help select the icon image, button text & label text that you want to show depending on the viewIDKey. So, in your view controller, call [self showNoDataView:self]; and it will pick up the correct data to show. If either of the lookup methods do not contain the entry for a give viewIDKey, then that UI component (image view/ label/button) is hidden. If the view state is VIEW_STATE_OFFLINE, then the image view & action button are automatically hidden.
 
 How to use:
 
 If you see any view component missing, just go to NoDataMasterView's lookup methods and add the viewIDKey<->text/icon filename/action button title to the lookup methods:
 
 - (NSString*)lookUpTitle:(NSString*)key;
 - (NSString*)lookUpImageName:(NSString*)key;
 - (NSString*)lookUpButtonTitle:(NSString*)key;
 
 To set the action for the action button on the no data view, please override this method in your view controller:
 #pragma mark NoDataViewDelegate method
 -(void) actionOnNoData:(id)sender
 {
 }
 */
- (void)showNoDataView:(MobileTableViewController*)callingViewController;
- (void)hideNoDataView;
- (void)showNoDataView:(MobileTableViewController*)callingViewController asSubviewOfView:(UIView*)parentView;
- (void)showNewNoDataView:(UIViewController<NoDataMasterViewDelegate>*)callingViewController;

#pragma NoDataMasterViewDelegate methods
- (BOOL)adjustNoDataView:(NoDataMasterView*) negView;  // Return whether to hide toolbar
- (NSString *)titleForNoDataView;
- (NSString*) buttonTitleForNoDataView;
-(BOOL) canShowOfflineTitleForNoDataView;
-(void) actionOnNoData:(id)sender;
-(BOOL) canShowActionOnNoData;
- (NSString *)instructionForNoDataView;
/*
 Offline view:
 Is similar to the No Data view, but contains only a title label.
 */
-(void) showOfflineView:(MobileTableViewController*)callingViewController;
-(void) hideOfflineView;

#pragma mark Waiting state views
/*
 Is a blocking view that covers the entire screen (except for the status bar).
 Users cannot navigate away from this view until an action is complete.
 Typically used when an HTTP POST is in progress.
 
 In addition, showWaitViewWithProgress:withText shows a progress bar that tracks the progress of the POST. For example, tracking the progress of a receipt image upload.
 
 Note:The first time you use showWaitView/showWaitViewWithText, an instance of the loading view is created. If showWaitView/showWaitViewWithText is called again, then the view is made visible again. If you change the text param in showWaitViewWithText, then that is also set.
 */
-(void) showWaitView;
-(void) showWaitViewWithText:(NSString *)waitText;
-(void) showWaitViewWithProgress:(BOOL)enableProgressView withText:(NSString*) waitText;
-(void) hideWaitView;
-(void) setWaitViewProgress:(float)progress;
-(bool) isWaitViewShowing;
/*
 Is a non-blocking view that covers just the table view/view, but does not block any navigation items. So users can still navigate away from the view. Typically used while an HTTP GET is in progress.
 Note:
 The first time you use showLoadingView/showLoadingViewWithText, an instance of the loading view is created. If showLoadingView/showLoadingViewWithText is called again, then the view is made visible again. If you change the text param in showLoadingViewWithText, then that is also set.
 */
-(void) showLoadingView;
-(void) showLoadingViewWithText:(NSString *)waitText;
-(void) hideLoadingView;
-(bool) isLoadingViewShowing;

// these methods should not be called unless the app is configured for apple demo
// these methods are duplicated between MobileViewController and MobileTableViewController with the forward looking plan to consolidate the classes
// at that time, only one set of these duplicated methods will be needed -- AJC 2013-04-30
-(void)showDemoMessageBox;
-(void)showDemoMessageBoxWithDelegate:(id<UIAlertViewDelegate>)alertViewDelegate;

@end
