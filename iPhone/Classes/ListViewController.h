//
//  ListViewController.h
//  ConcurMobile
//
//  Created by Shifan Wu on 7/23/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "OptionsSelectDelegate.h"

@interface ListViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, weak) id<OptionsSelectDelegate>     delegate;
@property (nonatomic, strong) NSArray *dataSourceArray;
@property (nonatomic, strong) NSIndexPath *defaultSelectedIdxPath;
@property (strong, nonatomic) IBOutlet UITableView *tableView;

@end
