//
//  ChooseStationViewController.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "CXRequest.h"

@interface ChooseStationViewController : UIViewController <UISearchBarDelegate, UITableViewDataSource, UITableViewDelegate>

@property (weak, nonatomic) IBOutlet UISearchBar *searchBar;
@property (weak, nonatomic) IBOutlet UITableView *tableView;

@property (strong, nonatomic) NSString *notificationName;
@property (strong, nonatomic) CXRequest *request;

@end
