//
//  SelectReportViewController.h
//  ConcurMobile
//
//  Created by yiwen on 4/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "AddToReportData.h"
#import "iPadHomeVC.h"
#import "ReportActionDelegate.h"
#import "ReportCreatedDelegate.h"

@class MobileAlertView;

@interface SelectReportViewController : MobileViewController  
    <UITableViewDelegate
    , UITableViewDataSource
    , UIAlertViewDelegate
    , ReportCreatedDelegate
    , UITextFieldDelegate> 
{
	UITableView				*tableView;
	NSArray					*rptList;
    NSMutableArray          *sections; // section in Month
    NSMutableDictionary     *sectionDataMap; // Report list under each month
    
    BOOL					showedNo;
	BOOL					selected, isCarMileage;
	BOOL					fetch;
	BOOL					isAcceptingData;
	BOOL					isAddingToReport;
	
	MobileViewController    *parentMVC;
	
	// Pct Keys or Me Keys to add to report
	NSArray                 *meKeys;
	NSArray                 *pctKeys;
	NSArray                 *cctKeys;
    NSArray                 *rcKeys;
	NSDictionary			*meAtnMap;
	
	// Selected RptKey or ReportName
	NSString				*rptKey;
	NSString				*reportName;
	
	NSMutableDictionary		*selectedRows;
	
	MobileAlertView			*alertViewConnectionError;
	MobileAlertView			*alertViewConfirmSelection;
	MobileAlertView			*alertViewFixReportName;
	
	ReportData				*rpt;
	iPadHomeVC				*padHomeVC;
}

@property (strong, nonatomic) iPadHomeVC				*padHomeVC;
@property BOOL isCarMileage;
@property (strong, nonatomic) ReportData				*rpt;
@property (strong, nonatomic) NSMutableDictionary		*selectedRows;
@property (strong, nonatomic) IBOutlet UITableView		*tableView;
@property (strong, nonatomic) NSArray					*rptList;
@property (strong, nonatomic) NSMutableArray            *sections;
@property (strong, nonatomic) NSMutableDictionary       *sectionDataMap;

@property BOOL showedNo;
@property BOOL fetch;

@property (strong, nonatomic) MobileViewController      *parentMVC;

@property (strong, nonatomic) NSArray					*meKeys;
@property (strong, nonatomic) NSArray					*pctKeys;
@property (strong, nonatomic) NSArray					*cctKeys;
@property (strong, nonatomic) NSArray					*rcKeys;
@property (strong, nonatomic) NSArray                   *smartExpenseIds;
@property (strong, nonatomic) NSDictionary				*meAtnMap;


@property (strong, nonatomic) NSString					*rptKey;
@property (strong, nonatomic) NSString					*reportName;

@property (nonatomic, strong) MobileAlertView			*alertViewConnectionError;
@property (nonatomic, strong) MobileAlertView			*alertViewConfirmSelection;
@property (nonatomic, strong) MobileAlertView			*alertViewFixReportName;

@property (nonatomic, strong) NSArray *smartExpenseList;

@property (weak, nonatomic) id<ReportActionDelegate> delegate;

-(void)setupToolbar;
- (void) goToReportDetailScreen:(AddToReportData*)ard;

// ReportCreatedDelegate 
-(void) reportCreated:(ReportData*) rpt;

extern NSString * const ADD_TO_RPT_CONFIRM_TITLE;
extern NSString * const ADD_TO_RPT_CONFIRM_MSG;
extern NSString * const FIX_REPORT_NAME_MSG;

@end
