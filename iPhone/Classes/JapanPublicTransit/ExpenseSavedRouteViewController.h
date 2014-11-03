//
//  ExpenseSavedRouteViewController.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "AbstractExpenseViewController.h"
#import "FancyDatePickerView.h"
#import "RouteView.h"

@interface ExpenseSavedRouteViewController : AbstractExpenseViewController <UIScrollViewDelegate, UITableViewDataSource, UITableViewDelegate>

@property (weak, nonatomic) IBOutlet RouteView *routeView;

@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;
@property (weak, nonatomic) IBOutlet UITextField *dateTextView;
@property (weak, nonatomic) IBOutlet UIView *formTableBar;
@property (weak, nonatomic) IBOutlet UITableView *formTable;

@end
