//
//  ApproveExpenseDetailViewController.h
//  ConcurMobile
//
//  Created by Yuri on 1/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ApproveReportsViewControllerBase.h"

@interface ApproveExpenseDetailViewController : ApproveReportsViewControllerBase 
<UITableViewDelegate, UITableViewDataSource> {
	UILabel *txtEmployee;
	UILabel *txtTotal;
	UILabel *txtName;
	UIImageView *topBar;
	
 	UITableView            *tableView;
	
	NSDictionary			*currentEntry;
	NSMutableArray			*listSection;
}

@property (nonatomic, retain) IBOutlet UITableView *tableView;

@property (nonatomic, retain) IBOutlet UILabel *txtEmployee;
@property (nonatomic, retain) IBOutlet UILabel *txtTotal;
@property (nonatomic, retain) IBOutlet UILabel *txtName;
@property (nonatomic, retain) IBOutlet UIImageView *topBar;

@property (nonatomic, retain) NSDictionary				*currentEntry;
@property (nonatomic, retain) NSMutableArray			*listSection;

@end
