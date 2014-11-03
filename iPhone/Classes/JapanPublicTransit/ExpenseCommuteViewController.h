//
//  ExpenseCommuteViewController.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/20/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "AbstractExpenseViewController.h"

@interface ExpenseCommuteViewController : AbstractExpenseViewController <UITableViewDataSource, UITableViewDelegate>

@property (weak, nonatomic) IBOutlet UITableView *formTable;

@property (weak, nonatomic) UIBarButtonItem *doneButton;

@end
