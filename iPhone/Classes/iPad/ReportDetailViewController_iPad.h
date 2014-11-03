//
//  ReportDetailViewController_iPad.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/28/10.
//  Copyright 2010 Concur. All rights reserved.
//
#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "TripsData.h"
#import "KeyValue.h"
#import "ReportData.h"
#import "DetailApproverList.h"
#import "ReportViewControllerBase.h"
#import "ReportApprovalListViewController.h"
#import "iPadHomeVC.h"
#import "PadExpenseEntryCell.h"
#import "OutOfPocketListViewController.h"
#import "TripsViewController.h"
#import "ExpenseTypesViewController.h"

#import "UnifiedImagePicker.h"
#import "ReceiptStoreDetailViewController.h"
#import "ReceiptStoreListView.h"
#import "ReceiptDetailViewController.h"
#import "AdsViewController.h"

#import "ReceiptEditorDelegate.h"

@class ActiveReportListViewController;

@interface ReportDetailViewController_iPad : ReportViewControllerBase <UIPopoverControllerDelegate, UITableViewDelegate
                                                                    , UITableViewDataSource
                                                                    , UIAlertViewDelegate
                                                                    , DetailApprovalListDelegate 
                                                                    , UIActionSheetDelegate
                                                                    , ExpenseTypeDelegate
                                                                    , UINavigationControllerDelegate
                                                                    , UnifiedImagePickerDelegate
                                                                    , ReceiptStoreDelegate
                                                                    ,ReceiptEditorDelegate
                                                                    , ReceiptDetailViewDelegate>
{
    UIPopoverController *popoverController;
	//UIPopoverController *pickerPopOver;
    
    id detailItem;
    UILabel *detailDescriptionLabel;
	NSString				*viewType;
	
	NSMutableArray			*aKeys;
	NSMutableArray			*aSections;
	NSMutableArray			*aSectionHeaders;
	NSMutableDictionary		*selectedRows;
	NSMutableDictionary		*oopeDict;
	
	UIPopoverController		*oopePopOver;
	
	BOOL					deleteMEReturned, deletePCTReturned, hasComment, isReport;
	
	ReportData				*rptDetail, *previousRpt, *nextRpt;

	UIView					*holdView;
	UILabel					*holdText;
	UIActivityIndicatorView	*holdActivity;
	
	iPadHomeVC				*iPadHome;
	UIImageView				*ivBackground, *ivBlotter;
	UIImageView				*ivBarLeft, *ivBarRight, *ivBarMiddle;
	UIScrollView			*scroller;
	NSMutableArray			*aBtns;
	int						middleX;
	
	UIButton				*btnSubmit, *btnSummary, *btnReceipts, *btnComments, *btnApprove, *btnReject, *btnNext, *btnPrevious, *btnExceptions, *btnAttachReceipt, *btnExport;
	UIButton				*btnAddEntry;
	NSMutableDictionary		*entryDetailsDict;
	
	TripsViewController				*tripsListVC;
	ActiveReportListViewController	*reportsListVC;
	
	UILabel					*lblReportName, *lblReportDescription, *lblReportDate, *lblReportStatus, *lblTotalAmount, *lblTotalRequested, *lblDivider, *lblExpensesTitle, *lblOffline;
	
	DetailApproverList		*detailApprovalListVC;
	UIImageView				*ivLoading;
	UIScrollView			*scrollerButtons;
	NSMutableArray			*reportKeys;
	NSMutableDictionary		*reportDictionary;
	
	UILabel					*lblTableBack;
	UIImageView				*ivTableTop;
	UIImageView				*ivTableBottom;
	UIImageView				*headerImg;
	UIImage					*receiptImage;
	
	// State variables for add expense
	NSMutableDictionary		*pBagAddExpense;
	BOOL					etDlgDismissed;
    
	NSMutableDictionary		*_dictExpensesToFetch;
    
    //Single User
    AdsViewController       *adsView;
    UIButton                *btnHome;
    
    BOOL hideReceiptStore;
}

@property (readonly, nonatomic) UIView              *leftPaneHeaderView;
@property (readonly, nonatomic) UIView              *leftPaneFooterView;
@property (strong, nonatomic) IBOutlet UITableView  *rightTableView;

@property BOOL hideReceiptStore;
@property (nonatomic, strong) UIButton                *btnHome;
@property (nonatomic, strong)     AdsViewController       *adsView;

@property (strong, nonatomic)  IBOutlet UIImageView			*headerImg;
@property (strong, nonatomic) NSMutableArray			*reportKeys;
@property (strong, nonatomic) NSMutableDictionary		*reportDictionary;

@property (strong, nonatomic) IBOutlet UIScrollView		*scrollerButtons;

@property (nonatomic, strong) IBOutlet UIImageView				*ivLoading;

@property (nonatomic, strong) DetailApproverList		*detailApprovalListVC;
@property (nonatomic, strong) NSMutableDictionary			*pBagAddExpense;
@property BOOL etDlgDismissed;

@property (nonatomic, strong) IBOutlet UIButton				*btnNext;
@property (nonatomic, strong) IBOutlet UIButton				*btnPrevious;
@property (nonatomic, strong) UIButton				*btnSummary;
@property (nonatomic, strong) UIButton				*btnReceipts;
@property (nonatomic, strong) UIButton				*btnComments;
@property (nonatomic, strong) UIButton				*btnExceptions;
@property (nonatomic, strong) UIButton				*btnApprove;
@property (nonatomic, strong) UIButton				*btnReject;
@property (nonatomic, strong) UIButton				*btnAttachReceipt;
@property (nonatomic, strong) UIButton				*btnAddEntry;
@property (nonatomic, strong) UIButton				*btnExport;
@property (nonatomic, strong) IBOutlet UILabel		*lblReportName;
@property (nonatomic, strong) IBOutlet UILabel		*lblReportDescription;
@property (nonatomic, strong) IBOutlet UILabel		*lblReportDate;
@property (nonatomic, strong) IBOutlet UILabel		*lblReportStatus;
@property (nonatomic, strong) IBOutlet UILabel		*lblTotalAmount;
@property (nonatomic, strong) IBOutlet UILabel		*lblTotalRequested;
@property (nonatomic, strong) IBOutlet UILabel		*lblDivider;
@property (nonatomic, strong) IBOutlet UILabel		*lblExpensesTitle;

@property (nonatomic, strong) IBOutlet UILabel		*lblTableBack;
@property (nonatomic, strong) IBOutlet UIImageView	*ivTableTop;
@property (nonatomic, strong) IBOutlet UIImageView	*ivTableBottom;

@property (strong, nonatomic) ActiveReportListViewController	*reportsListVC;
@property (nonatomic, strong) UIButton							*btnSubmit;
@property (nonatomic, strong) IBOutlet UIImageView				*ivBarLeft;
@property (nonatomic, strong) IBOutlet UIImageView				*ivBarRight;
@property (nonatomic, strong) IBOutlet UIImageView				*ivBarMiddle;
@property (nonatomic, strong) IBOutlet UIImageView				*ivBackground;
@property (nonatomic, strong) IBOutlet UIImageView				*ivBlotter;
@property (nonatomic, strong) IBOutlet UIScrollView				*scroller;
@property (nonatomic, strong) IBOutlet UIImageView				*ivRptReadyforSubmit;
@property (nonatomic, strong) UIImage *receiptImage;

@property (strong, nonatomic) iPadHomeVC				*iPadHome;

@property BOOL hasComment;
@property BOOL isReport;


@property (strong, nonatomic) IBOutlet UILabel *lblOffline;
@property (nonatomic, strong) IBOutlet UILabel					*holdText;
@property (nonatomic, strong) IBOutlet UIActivityIndicatorView	*holdActivity;

@property (strong, nonatomic) IBOutlet UIView					*holdView;
@property (strong, nonatomic) ReportData						*rptDetail;
@property (strong, nonatomic) ReportData						*previousRpt;
@property (strong, nonatomic) ReportData						*nextRpt;

@property (nonatomic, strong) UIPopoverController				*oopePopOver;
//@property (nonatomic, retain) UIPopoverController				*pickerPopOver;
@property (nonatomic, strong) UIPopoverController				*popoverController;

//@property (nonatomic, retain) OutOfPocketFormViewController		*oopeVC;

@property (nonatomic, strong) id detailItem;
@property (nonatomic, strong) IBOutlet UILabel					*detailDescriptionLabel;

@property (nonatomic, strong) NSString							*viewType;

@property (nonatomic, strong) NSMutableArray			*aKeys;
@property (nonatomic, strong) NSMutableArray			*aSections;
@property (nonatomic, strong) NSMutableArray			*aSectionHeaders;
@property (nonatomic, strong) NSMutableDictionary		*selectedRows;
@property (nonatomic, strong) NSMutableDictionary		*oopeDict;
@property (nonatomic, strong) NSMutableDictionary		*entryDetailsDict;

@property (nonatomic, strong) TripsViewController				*tripsListVC;

-(void)nextStepForAddExpense;
-(void)expenseTypeDlgDismissed;

-(void)makeSystemButtons;


-(void)makeDeleteButton:(int)count;
//-(void)confirmDelete:(id)sender;
//-(void)buttonDeleteSelectedPressed:(id)sender;

-(void) initSections;

-(void)setTBUp:(NSString *)empName ReportAmount:(NSString *)rptAmount ReportName:(NSString *)reportName HasReceipt:(BOOL)hasReceipt HasComment:(BOOL)hasComment;
-(void)makeEntries;
- (NSString*) getVendorString:(NSString*) vendor WithLocation:(NSString*) locationName;
- (void)buttonEntryDetailsTapped:(EntryData *)e IndexPath:(NSIndexPath *)newIndexPath;

-(void)makeEntryCell:(PadExpenseEntryCell *)cell Entry:(EntryData *)entry;

- (void)buttonReceiptsTapped:(id)sender;

-(void)makeEntryDetails:(EntryData *)entry;
-(NSString *) fetchSubValue:(EntryData *)e SubType:(NSString *)subType;
- (IBAction)buttonReportsPressed:(id)sender;

- (IBAction)buttonTripsPressed:(id)sender;

- (void)buttonAttendeesTapped:(id)sender IndexPath:(NSIndexPath *)newIndexPath;
- (void)buttonCommentsTapped:(id)sender IndexPath:(NSIndexPath *)newIndexPath;
- (void)buttonExceptionsTapped:(id)sender IndexPath:(NSIndexPath *)newIndexPath;
- (void)buttonItemizationsTapped:(id)sender IndexPath:(NSIndexPath *)newIndexPath;
- (void)buttonSummaryTapped:(id)sender;
- (void) Perform_Swiped_left:(UISwipeGestureRecognizer*)sender;
- (void) Perform_Swiped_right:(UISwipeGestureRecognizer*)sender;
-(void) setUpPreviousNextReports;

-(void)loadReport:(NSMutableDictionary *)pBag;

-(void) adjustLabel:(UILabel *) lblHeading LabelValue:(UILabel*) lblVal HeadingText:(NSString *) headText ValueText:(NSString *) valText ValueColor:(UIColor *) color;

-(void) checkOffline;
-(void) loadEditableEntry:(EntryData *)e IndexPath:(NSIndexPath *)newIndexPath;
-(void) loadEditableReport;

- (void)buttonReceiptTapped:(id)sender IndexPath:(NSIndexPath *)newIndexPath;
-(BOOL) hasReceipt;
-(void) buttonAddEntryTapped:(id)sender;
-(BOOL)onlineCheck;

@end