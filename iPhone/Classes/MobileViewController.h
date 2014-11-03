//
//  MobileViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/10/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ExMsgRespondDelegate.h"
#import "UtilityMethods.h"
#import "NoDataMasterView.h"
#import "WaitStateMasterView.h"
#import "UploadBannerView.h"
#import "UIDevice+Additions.h"
#import "ViewStateHelper.h"

@class UnifiedImagePicker;
@class RootViewController;
@class Msg;
@class ExSystem;

#if !defined(kIconSize)
#define		kIconSize			19
#endif

extern NSString * const MVC_CONNECTION_PROGRESS_MESSAGE;

typedef enum BaseViewState{
	VIEW_STATE_NORMAL = 0,      // Default view state
	VIEW_STATE_NEGATIVE = 1,    // Negative state
	VIEW_STATE_LOADING = 2,     // Loading state: waiting for a GET response (blocks just the table view but not the navigation controls)
    VIEW_STATE_WAITING = 3,     // Waiting state: waiting for a POST response (blocks user actions)
    VIEW_STATE_OFFLINE = 4      // Offline state
} BaseViewState;


@interface MobileViewController : UIViewController <ExMsgRespondDelegate, NoDataMasterViewDelegate, ViewStateDelegate>
{
	NSString				*IDKey, *cameFrom;
	UIPopoverController		*pickerPopOver;
    UIActionSheet           *actionPopOver;  // For iPad orientation switch or view unload
    int                     baseViewState;

    UploadBannerView        *uploadView;
    
    @private
    NoDataMasterView        *negDataView;
    WaitStateMasterView     *waitingStateView;
    WaitStateMasterView     *loadingStateView;
}

extern NSString * const VIEW_DISPLAY_TYPE_NAVI;
extern NSString * const VIEW_DISPLAY_TYPE_REGULAR;
extern NSString * const VIEW_DISPLAY_TYPE_MODAL;

@property (strong, nonatomic) NSString *IDKey;
@property (strong, nonatomic) NSString *cameFrom;

@property (strong, nonatomic) UploadBannerView *uploadView;

@property (strong, nonatomic) ViewStateHelper *viewStateHelper;

@property (strong, nonatomic) NoDataMasterView *negDataView;
//@property (strong, nonatomic) WaitStateMasterView *waitingStateView;
//@property (strong, nonatomic) WaitStateMasterView *loadingStateView;
@property (assign, readonly) int baseViewState;

@property (nonatomic, strong) UIPopoverController *pickerPopOver;
@property (nonatomic, strong) UIActionSheet       *actionPopOver;

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
- (void)showNoDataView:(MobileViewController*)callingViewController;
- (void)hideNoDataView;
- (void)showNoDataView:(MobileViewController*)callingViewController asSubviewOfView:(UIView*)parentView;

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
-(void) showOfflineView:(MobileViewController*)callingViewController;
-(void) hideOfflineView;

#pragma mark - Table configuration
-(void) configureTables;
-(UIColor*) getTableBackgroundColor;

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

-(void)afterChoiceToRateApp;

// these methods should not be called unless the app is configured for apple demo
// these methods are duplicated between MobileViewController and MobileTableViewController with the forward looking plan to consolidate the classes
// at that time, only one set of these duplicated methods will be needed -- AJC 2013-04-30
-(void)showDemoMessageBox;
-(void)showDemoMessageBoxWithDelegate:(id<UIAlertViewDelegate>)alertViewDelegate;

@end
