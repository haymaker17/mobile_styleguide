//
//  OutOfPocketListViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "QEEntryCell.h"
#import "QEFormVC.h"
#import "ReportData.h"
#import "UploadBannerDelegate.h"
#import "UploadQueueVCDelegate.h"

@class iPadHomeVC;

@interface OutOfPocketListViewController : MobileViewController <UITableViewDelegate, UITableViewDataSource, UIAlertViewDelegate, UIActionSheetDelegate, UploadBannerDelegate, UploadQueueVCDelegate>
{
	UITableView				*tableList;
	NSMutableArray			*curKeys;			// Filtered keys in sorted order
	NSMutableArray			*allKeys;			// All keys in sorted order
	NSMutableDictionary		*pCards;			// card info, keyed by pcaKey
	NSMutableArray			*pCardKeys;			// pcaKeys in ordered list
	NSString				*curFilter;			// Either pcaKey or 'ALL'
	NSMutableDictionary		*selectedRows;
	NSMutableDictionary		*oopeDict;
	UILabel					*lblBack, *titleLabel; 
	UIImageView				*ivBack;
	BOOL					showedNo;
	BOOL					selected, drewEdit, doReload, inDeleteMode, inAddToReportMode;
	BOOL					deleteMEReturned, deletePCTReturned, deleteCCTReturned;
	iPadHomeVC				*__weak padHomeVC;
	BOOL					isShowingNextController;
	ReportData				*rpt;
    BOOL                    isQuickPush;
    BOOL                    bannerAdjusted;
    int                     currentItemCount;
}

@property int               currentItemCount;
@property BOOL                    bannerAdjusted;
@property BOOL                    isQuickPush;
@property (strong, nonatomic) IBOutlet UITableView		*tableList;
@property (strong, nonatomic) NSMutableArray			*allKeys;
@property (strong, nonatomic) NSMutableArray			*curKeys;
@property (strong, nonatomic) NSMutableDictionary		*selectedRows;
@property (strong, nonatomic) NSMutableDictionary		*oopeDict;
@property (strong, nonatomic) NSMutableDictionary		*pCards;
@property (strong, nonatomic) NSMutableArray			*pCardKeys;
@property (strong, nonatomic) NSString					*curFilter;
@property (strong, nonatomic) ReportData				*rpt;

@property (strong, nonatomic) UILabel					*lblBack;
@property (strong, nonatomic) UILabel					*titleLabel; 
@property (strong, nonatomic) UIImageView				*ivBack;
@property BOOL showedNo;
@property (weak,nonatomic) iPadHomeVC					*padHomeVC;
@property (assign,nonatomic) BOOL						isShowingNextController;

@property BOOL drewEdit;
@property BOOL doReload;
@property BOOL inDeleteMode;
@property BOOL inAddToReportMode;

-(void)buttonCancelPressed:(id)sender;
-(void)buttonEditPressed:(id)sender;
-(void)buttonAddPressed:(id)sender;
- (void)clearSelectionForTableView:(UITableView *)tableView indexPath:(NSIndexPath *)indexPath;

-(void)buttonAddToReportPressed:(id)sender;
-(void)buttonDeleteSelectedPressed:(id)sender;

-(void) makeBtnEdit;
-(void)buttonAddToReportOnePressed:(id)sender;
-(void)butonSelectAll:(id)sender;
-(void)butonUnSelectAll:(id)sender;
-(void)makeSelectAllButtons;
-(void)makeUnSelectAllButtons;
-(void)makeDeleteButton:(int)count;
-(void)confirmDelete:(id)sender;
-(void) setupRefreshingToolbar:(NSString *)text ShowActivity:(BOOL)showActivity;
-(void)doConfigureToolBar:(id)sender;

-(void) goToSelectReport;

-(UIBarButtonItem *)makeAddToReportButton:(int)count;
-(void)removeReceiptUploadedMessage;
-(void)loadExpenses;

-(void)buttonActionPressed:(id)sender;
-(void)filterEntries;

extern NSString* const FILTER_ALL;
extern NSString* const FILTER_CORP_CARDS;

-(IBAction) closeView:(id)sender;

-(void)removeSelected;

-(void) loadEntryForm:(NSIndexPath *) indexPath;

-(void)configureToolBar;
-(void) goToReportDetailScreen;
-(void) pushQuick:(id)sender;

-(void) adjustViewForUploadBanner;
-(void) adjustViewForNoUploadBanner;
@end
