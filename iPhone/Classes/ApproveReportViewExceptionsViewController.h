//
//  ApproveReportViewExceptionsViewController.h
//  ConcurMobile
//
//  Created by Yuri Kiryanov on 3/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ApproveReportsViewControllerBase.h"

@interface ApproveReportViewExceptionsViewController  : ApproveReportsViewControllerBase 
<UITableViewDelegate, UITableViewDataSource> 
{
	UILabel *txtEmployee;
	UILabel *txtTotal;
	UILabel *txtName;
	
	UITableView *tableView;

	NSMutableArray			*listExpenseExceptions;
}

@property (nonatomic, retain) IBOutlet UITableView *tableView;

@property (nonatomic, retain) IBOutlet UILabel *txtEmployee;
@property (nonatomic, retain) IBOutlet UILabel *txtTotal;
@property (nonatomic, retain) IBOutlet UILabel *txtName;

@property (nonatomic, retain) NSMutableArray		*listExpenseExceptions;

@end
