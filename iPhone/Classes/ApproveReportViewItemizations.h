//
//  ApproveReportViewItemizations.h
//  ConcurMobile
//
//  Created by Yuri Kiryanov on 3/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ApproveReportsViewControllerBase.h"

@interface ApproveReportViewItemizations : ApproveReportsViewControllerBase 
<UITableViewDelegate, UITableViewDataSource> {

	UILabel *txtExpense;
	UILabel *txtTotal;
	UIImageView *topBar;
	
	UITableView            *tableView;
	UINavigationBar        *navBar;
	
	NSDictionary			*currentEntry;
	NSMutableArray			*listSection;
}

@property (nonatomic, retain) IBOutlet UITableView *tableView;

@property (nonatomic, retain) IBOutlet UILabel *txtExpense;
@property (nonatomic, retain) IBOutlet UILabel *txtTotal;
@property (nonatomic, retain) IBOutlet UIImageView *topBar;

@property (nonatomic, retain) NSDictionary				*currentEntry;
@property (nonatomic, retain) NSMutableArray			*listSection;

@end
