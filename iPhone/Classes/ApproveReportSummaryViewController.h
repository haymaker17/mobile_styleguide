//
//  ApproveReportSummaryViewController.h
//  ConcurMobile
//
//  Created by Yuri Kiryanov on 3/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ApproveReportsViewControllerBase.h"

@interface ApproveReportSummaryViewController  : ApproveReportsViewControllerBase 
<UITableViewDelegate, UITableViewDataSource> 
{
	UILabel *txtEmployee;
	UILabel *txtTotal;
	UILabel *txtName;

	UITableView *tableView;

	NSMutableArray			*reportHeaderFields;
	NSMutableArray			*companyDisbursementFields;
	NSMutableArray			*employeeDisbursementsFields;

	NSMutableArray			*listSection;
}

@property (nonatomic, retain) IBOutlet UILabel *txtEmployee;
@property (nonatomic, retain) IBOutlet UILabel *txtTotal;
@property (nonatomic, retain) IBOutlet UILabel *txtName;

@property (nonatomic, retain) IBOutlet UITableView *tableView;

@property (nonatomic, retain) NSMutableArray			*reportHeaderFields;
@property (nonatomic, retain) NSMutableArray			*companyDisbursementFields;
@property (nonatomic, retain) NSMutableArray			*employeeDisbursementsFields;

@property (nonatomic, retain) NSMutableArray			*listSection;


@end
