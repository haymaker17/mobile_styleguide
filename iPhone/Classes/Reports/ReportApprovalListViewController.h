//
//  ReportApprovalListViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "ReportApprovalListCell.h"
#import "iPadHomeVC.h"

@interface ReportApprovalListViewController : MobileViewController  <UITableViewDelegate, UITableViewDataSource> 
{
	BOOL selected;
	BOOL					isPad;
	iPadHomeVC				*iPadHome;
	MobileViewController	*fromMVC;
}

@property (nonatomic, strong) iPadHomeVC				*iPadHome;
@property (nonatomic, strong) MobileViewController		*fromMVC;
@property BOOL isPad;
@property (strong, nonatomic) IBOutlet UITableView		*tableList;
@property (strong, nonatomic) NSMutableArray			*aKeys;
@property (strong, nonatomic) NSMutableDictionary		*selectedRows;
@property (strong, nonatomic) NSMutableDictionary		*rals;
@property (strong, nonatomic) UILabel					*lblBack;
@property (strong, nonatomic) UILabel					*titleLabel; 
@property (strong, nonatomic) UIImageView				*ivBack;
@property BOOL showedNo;
@property BOOL fetch;
@property BOOL forceFetchTripApprovalList;

//AJC -- is this code needed? delete after 2013-09-20 if not needed by then
//-(void)buttonCancelPressed:(id)sender;
//-(void)buttonEditPressed:(id)sender;
//-(void)buttonAddPressed:(id)sender;
//- (void)clearSelectionForTableView:(UITableView *)tableView indexPath:(NSIndexPath *)indexPath;
//- (BOOL)selected;
//-(void)buttonAddToReportPressed:(id)sender;
//-(void)buttonDeleteSelectedPressed:(id)sender;
//AJC -- is this code needed? delete after 2013-09-20 if not needed by then

- (id)initWithSummaryData:(SummaryData*)summaryData;
- (void)loadApprovals;

@end
