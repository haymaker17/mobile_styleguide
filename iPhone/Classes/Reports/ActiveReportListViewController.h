//
//  ActiveReportListViewController.h
//  ConcurMobile
//
//  Created by yiwen on 4/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MobileTableViewController.h"
#import "iPadHomeVC.h"
#import "ReportDetailViewController_iPad.h"
#import "EntityReport.h"
#import "ReportCreatedDelegate.h"

@interface ActiveReportListViewController : MobileTableViewController <NSFetchedResultsControllerDelegate, UITableViewDelegate, ReportCreatedDelegate, UITableViewDataSource, UIAlertViewDelegate>
{
	NSMutableArray			*aKeys;
	NSMutableDictionary		*rals;
	UILabel					*lblBack, *titleLabel; 
	UIImageView				*ivBack;
	BOOL					showedNo;
	BOOL					fetch, doReload;
	BOOL					isPad;
	
	iPadHomeVC				*iPadHome;
	MobileViewController	*fromMVC;
	NSMutableDictionary		*groupedReportsLookup;
	NSMutableArray			*aSections;
    
    EntityReport            *reportCacheData;

}

@property (strong, nonatomic) MobileViewController      *fromMVC;
@property (strong, nonatomic) iPadHomeVC				*iPadHome;
@property (strong, nonatomic) NSMutableArray			*aKeys;
@property (strong, nonatomic) NSMutableDictionary		*rals;
@property (strong, nonatomic) UILabel					*lblBack;
@property (strong, nonatomic) UILabel					*titleLabel; 
@property (strong, nonatomic) UIImageView				*ivBack;
@property BOOL showedNo;
@property BOOL isPad;
@property BOOL fetch;
@property BOOL doReload;
@property BOOL isRptOpen;
@property (nonatomic, strong) EntityReport              *reportCacheData;
@property (nonatomic, strong) NSFetchedResultsController *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext    *managedObjectContext;

@property (strong, nonatomic) NSMutableDictionary		*groupedReportsLookup;
@property (strong, nonatomic) NSMutableArray			*aSections;

@property BOOL enablefilterUnsubmittedActiveReports;

-(void)loadReports;
-(void)timeToDie;
-(void)prepareReportGroups;
-(void)updateReport:(ReportData*) rpt;
-(void)addToGrouped:(ReportData*)rpt;
-(NSString*)getReportGroupName:(ReportData*)rpt;
//-(void)createSections;

-(IBAction) buttonAddPressed:(id)sender;
-(void)setupToolbar;

// ReportCreatedDelegate 
-(void) reportCreated:(ReportData*) rpt;

@end
