//
//  ChooseLineViewController.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/11/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ChooseLineViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>

@property (weak, nonatomic) IBOutlet UITableView *tableView;

@property (copy, nonatomic) NSString *stationKey;
@property (strong, nonatomic) NSString *notificationName;

@end
