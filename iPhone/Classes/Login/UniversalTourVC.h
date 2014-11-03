//
//  UniversalTourVC.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 10/31/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UniversalTourVC : UITableViewController
@property (weak, nonatomic) IBOutlet UILabel *introLabel;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *nextBarBtn;
@property (nonatomic, copy) void (^onCloseTestDriveTapped)(void);
@property BOOL didSegueFromOtherScreen ;

@end
