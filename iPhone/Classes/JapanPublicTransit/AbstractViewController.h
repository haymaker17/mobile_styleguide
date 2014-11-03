//
//  AbstractViewController.h
//  ConcurMobile
//
//  Created by Richard Puckett on 10/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ReportActionDelegate.h"
#import "ReportData.h"
#import "RouteExpense.h"

@interface AbstractViewController : UIViewController <ReportActionDelegate, UIActionSheetDelegate>

@property (strong, nonatomic) ReportData *chosenReport;
@property (strong, nonatomic) RouteExpense *routeExpense;

- (BOOL)canSaveFavorite;

- (void)gotoIndex;
- (void)gotoIndexToTab:(NSInteger)tabIndex;
- (void)gotoReport;

- (BOOL)isFormComplete;

- (void)notifyUserOfRouteAdded;
- (void)notifyUserOfError:(NSError *)error;

- (void)onSaveTapped:(id)sender;

- (void)saveAsExpense;

- (void)selectReport;

@end
