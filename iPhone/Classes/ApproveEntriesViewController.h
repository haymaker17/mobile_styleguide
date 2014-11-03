//
//  ApproveEntriesViewController.h
//  ConcurMobile
//
//  Created by yiwen on 1/22/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ApproveReportsViewControllerBase.h"
#import "ApproveReportExpenseCell.h"

@interface ApproveEntriesViewController : ApproveReportsViewControllerBase 
	<UITableViewDelegate, UITableViewDataSource> 
{
	UILabel *txtEmployee;
	UILabel *txtTotal;
	UILabel *txtName;

	UITableView *tableView;
	
	NSMutableArray *listSummaryRows;
}

@property (nonatomic, retain) IBOutlet UITableView *tableView;

@property (nonatomic, retain) IBOutlet UILabel *txtEmployee;
@property (nonatomic, retain) IBOutlet UILabel *txtTotal;
@property (nonatomic, retain) IBOutlet UILabel *txtName;

@property (nonatomic, retain) NSMutableArray *listSummaryRows;

-(IBAction) viewExpenseDetails : (NSUInteger ) row;
-(IBAction) viewReportSummary : (NSUInteger ) row;
-(IBAction) viewExceptions : (NSUInteger ) row;
-(IBAction) viewReceipts : (NSUInteger ) row;
-(IBAction) viewComments : (NSUInteger ) row;

@end
