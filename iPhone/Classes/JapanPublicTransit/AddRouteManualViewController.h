//
//  AddRouteManualViewController.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/22/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AbstractViewController.h"
#import "AddRouteManualViewController.h"
#import "DCRoundSwitch.h"
#import "ReportActionDelegate.h"
#import "RouteExpense.h"

@interface AddRouteManualViewController : AbstractViewController <ReportActionDelegate, UIActionSheetDelegate, UITableViewDataSource, UITableViewDelegate>

@property (weak, nonatomic) IBOutlet UITableView *tableView;

@property (strong, nonatomic) NSString *rightButtonText;

@end
