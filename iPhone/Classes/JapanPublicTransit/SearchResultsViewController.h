//
//  SearchResultsViewController.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "Route.h"
#import "RouteSearchModel.h"

@interface SearchResultsViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>

@property (weak, nonatomic) IBOutlet UILabel *tripSynopsis;
@property (weak, nonatomic) IBOutlet UILabel *tripMetadata;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *activityIndicator;

@property (strong, nonatomic) RouteSearchModel *routeSearchModel;

@property (strong, nonatomic) Route *cheapRoute;
@property (strong, nonatomic) Route *easyRoute;
@property (strong, nonatomic) Route *fastRoute;
@property (strong, nonatomic) Route *otherRoute;

@end
