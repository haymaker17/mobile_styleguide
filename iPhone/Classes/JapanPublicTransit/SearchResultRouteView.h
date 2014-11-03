//
//  SearchResultRouteView.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/21/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "Route.h"

@interface SearchResultRouteView : UIView <UITableViewDataSource, UITableViewDelegate>

@property (weak, nonatomic) IBOutlet UIView *view;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UILabel *duration;
@property (weak, nonatomic) IBOutlet UILabel *fare;

@property (strong, nonatomic) Route *route;

@end
