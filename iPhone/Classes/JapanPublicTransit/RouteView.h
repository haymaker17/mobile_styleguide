//
//  RouteView.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/19/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Route.h"

@interface RouteView : UIView <UITableViewDataSource, UITableViewDelegate>

@property (weak, nonatomic) IBOutlet UIView *view;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UIButton *disclosureIndicator;

@property (strong, nonatomic) Route *route;

@end
