//
//  ViewStateHelper.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "NoDataMasterView.h"
#import "WaitStateMasterView.h"
#import "ViewStateDelegate.h"

@interface ViewStateHelper : NSObject
{
    int                     baseViewState;

    NoDataMasterView        *negDataView;
    WaitStateMasterView     *waitingStateView;
    WaitStateMasterView     *loadingStateView;
    
    id<ViewStateDelegate, NoDataMasterViewDelegate>   __weak _delegate;
}

@property (strong, nonatomic) NoDataMasterView *negDataView;
@property (strong, nonatomic) WaitStateMasterView *waitingStateView;
@property (strong, nonatomic) WaitStateMasterView *loadingStateView;
@property (assign, readonly) int baseViewState;
@property (weak, nonatomic) id<ViewStateDelegate> delegate;

-(id) initWithDelegate:(id<ViewStateDelegate, NoDataMasterViewDelegate>)del;
-(void)makeOfflineBar;
-(void)makeOfflineBarWithLastUpdateMsg:(NSString*) lastUpdatedStr;
-(void) showOfflineView:(UIViewController<NoDataMasterViewDelegate>*)callingViewController;
-(void) hideOfflineView;

-(void) showLoadingView;
-(void) showLoadingViewWithText:(NSString *)waitText;
-(void) hideLoadingView;
-(BOOL) isLoadingViewShowing;

-(void) showWaitView;
-(void) showWaitViewWithText:(NSString *)waitText;
-(void) showWaitViewWithProgress:(BOOL)enableProgressView withText:(NSString*) waitText;
-(void) hideWaitView;
-(void) setWaitViewProgress:(float)progress;
-(BOOL) isWaitViewShowing;

-(void)showNoDataView:(UIViewController<NoDataMasterViewDelegate>*)callingViewController;
- (void)hideNoDataView;
- (void)showNoDataView:(UIViewController<NoDataMasterViewDelegate>*)callingViewController asSubviewOfView:(UIView*)parentView;
- (void)showNewNoDataView:(UIViewController<NoDataMasterViewDelegate>*)callingViewController;

@end
