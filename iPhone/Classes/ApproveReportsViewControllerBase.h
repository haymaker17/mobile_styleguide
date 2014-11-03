//
//  ApproveReportsViewControllerBase.h
//  ConcurMobile
//
//  Created by Yuri Kiryanov on 3/12/10.
//  Stripped and then made to work by Justin Kramer on 04.03.2010
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "ReportData.h"
#import "ReportApprovalListCell.h"
#import "SubmitNeedReceiptsViewController.h"
#import "ReportRejectionViewController.h"

@interface ApproveReportsViewControllerBase : MobileViewController
	<UIAlertViewDelegate, SubmitNeedReceiptsDelegate, ReportRejectionDelegate>
{
	UILabel					*lblBack;
	UIImageView				*ivBack;
	BOOL					isSubmitting;
	UIView					*refreshView;
	ReportData				*rpt;
	NSString				*role;  // MOBILE_EXPENSE_MANAGER/TRAVELER/PROCESSOR role
	BOOL					isPad;
	
	// Common table gradient background
	UITableView				*tableList;
	UIImageView				*tableBackgroundImage;
}

@property BOOL					isSubmitting;
@property BOOL					isPad;

@property (strong, nonatomic) UILabel					*lblBack;
@property (strong, nonatomic) UIImageView				*ivBack;

@property (strong, nonatomic) ReportData				*rpt;
@property (strong, nonatomic) UIView					*refreshView;
@property (strong, nonatomic) NSString					*role;
@property (strong, nonatomic) IBOutlet UITableView		*tableList;
@property (strong, nonatomic) IBOutlet UIImageView		*tableBackgroundImage;

-(void)setupToolbar;
-(void)setupToolbarWithMessage:(NSString*) msg withActivity:(BOOL) fAct;

-(IBAction) actionApprove:(id)sender;
-(IBAction) actionReject:(id)sender;
-(IBAction) actionSubmit:(id)sender;

-(BOOL) isApproving;
-(BOOL) canEdit;

-(void) setupRefreshingToolbar;

-(void)drawHeaderRpt:(id)thisObj HeadLabel:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt LabelLine1:(UILabel *)labelLine1 LabelLine2:(UILabel *)labelLine2 
	  HeadBackground:(UIImageView *) ivHeadBack;

-(void)drawHeaderEntry:(EntryData *)thisEntry HeadLabel:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt LabelLine1:(UILabel *)labelLine1 LabelLine2:(UILabel *)labelLine2 
		HeadBackground:(UIImageView *) ivHeadBack;

-(void)makeEntryCell:(ReportApprovalListCell *)cell Entry:(EntryData *)entry;
-(UITableViewCell *) makeDrillCell:(UITableView*)tblView withText:(NSString*)command withImage:(NSString*)imgName enabled:(BOOL)flag;

-(void) addHomeButton;

- (NSString*) getVendorString:(NSString*) vendor WithLocation:(NSString*) locationName;

-(void)afterChoiceToRateApp;
-(void)afterChoiceToRateAppApproval;


-(BOOL) canSubmit;
- (void) submitReport;

- (void)cancelSubmitAfterReceipts;
- (void)confirmSubmitAfterReceipts;

extern NSString * const SUBMIT_ERROR_NO_ENTRY_MSG;
extern NSString * const SUBMIT_ERROR_UNDEF_MSG;
extern NSString * const SUBMIT_ERROR_RPT_XCT_LEVEL_MSG;

@end
