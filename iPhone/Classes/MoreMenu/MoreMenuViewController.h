//
//  MoreMenuViewController.h
//  ConcurMobile
//
//  Created by ernest cho on 3/11/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MoreMenuData.h"
#import "iPadHome9VC.h"

@interface MoreMenuViewController : UITableViewController

@property (strong,nonatomic) iPadHome9VC *ipadHome;
@property (copy,nonatomic) void (^tapHome)(void);

@end
