//
//  iPadHomeVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 9/6/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>
#import "ExSystem.h" 

#import "OutOfPocketListViewController.h"
#import "TripsViewController.h"
#import "MobileViewController.h"
#import "TripData.h"
#import "SegmentData.h"
#import "UILayoutView.h"
#import "OutOfPocketData.h"
#import "AppsMenuVC.h"
#import "SummaryData.h"
#import "CardButton.h"
#import "DetailViewController.h"
#import "LoginViewController.h"
#import "AirBookingCriteriaVC.h"
#import "ReportSummaryViewController.h"
#import "RoundedRectView.h"
#import "AdsViewController.h"
#import "ViewPadIntro.h"
#import "WhatsNewView.h"
#import "TripManager.h"
#import "SafetyCheckInVC.h"
#import "CarRatesData.h"
#import "Localizer.h"

@class ReceiptStoreListView;
@class ReportApprovalListViewController;
@class ActiveReportListViewController;
@class AppsMenuVC;
@class TripItLinkVC;

@interface iPadHomeVC : MobileViewController <UIPopoverControllerDelegate, UITableViewDelegate, UIScrollViewDelegate, UIAlertViewDelegate,
		UnifiedImagePickerDelegate,UIActionSheetDelegate,
        ReportCreatedDelegate, UploadQueueVCDelegate> {
	NSMutableArray				*aList, *aKeys;
	NSMutableDictionary			*dictList;
	UIScrollView				*scrollerButtons;
    ViewPadIntro                *viewPadIntro;
	
	RootViewController          *rootVC;
	UIPopoverController			*popoverController;
	
	OutOfPocketListViewController	*oopeListVC;
	OutOfPocketListViewController	*oopeFilterListVC;
	TripsViewController				*tripsListVC;
	ActiveReportListViewController	*reportsListVC;
	ReportApprovalListViewController *approvalsListVC;
	ReceiptStoreListView			*receiptStoreVC;
	
	NSMutableDictionary				*homeViews;
	UISplitViewController			*splitViewController;
	
	UIBarButtonItem					*rootPopoverButtonItem;
	TripData						*currentTrip;
	NSString						*sectionExpenseName, *sectionTripName, *sectionReportName;
	
	UIImageView						*ivBarLeft, *ivBarRight, *ivBarMiddle, *ivTwitBackground;
	
	int								middleX;
	UIButton						*btnAddExpense;
	UIScrollView					*scrollerExpense, *scrollerTripDetails;
	UILabel							*lblTripName, *lblTripData, *lblExpense, *lblTripWait, *lblTripDate, *lblExpenseWait;
	
	UIView							*viewTrip, *viewExpense, *viewTripDetails, *viewTripWait, *viewExpenseWait;
	
	UILabel							*lblCurrentTrip, *lblOffline;
	
	UIImageView						*ivTripInfo;
	UIImageView						*ivTripWait;
	BOOL							hasAir, hasRail;
	SegmentData						*segmentActive;
	UIImageView						*ivBackground;
	UIButton						*btnLogout, *btnSettings, *btnUpload;
	NSMutableArray                  *aBtns;
	
	NSMutableDictionary				*tripBits, *dictSegRows, *dictSkins;
    NSMutableArray					*keys;
	TripsData						*tripsData;
	
	BOOL							isExpenseOnly, isTravelOnly, isItinViewerOnly;
	BOOL							requireHomeScreenRefresh;
	UIImageView						*skinExpense;
	UIImageView						*imageExpenseWait;
	BOOL							enableActiveTrips;
	
 
	UILayoutView					*canvas;
	UILayoutView					*headerPanel;
	UILayoutView					*mainPanel;
	UILayoutView					*contentPanel;
	UILayoutView					*h2;
	UILayoutView					*h1_2;
	UILayoutView					*h1_3;	
	UILayoutView					*h1_4;	
	UILayoutView					*separatorView;
	UILayoutView					*currentTripSummaryView;
	UIImageView						*ivTripIcon;
	UILabel							*lblTripSummary;
	UILabel							*lblTripDated;
	UILayoutView					*tripCategoryIconsView;
	UILayoutView					*addFunctionsView;
	UILayoutView					*expSummaryView;
	UILayoutView					*adsView;
	UIButton						*addFuncPrev;
	UIButton						*addFuncNext;
	NSArray							*dataOOP;
	UIButton						*expSummaryNext;
	UIButton						*expSummaryPrev;
	UIButton						*showAdBtn;
	UIScrollView					*addFuncScroller;
	UIScrollView					*expSummaryScroller;

	UIImageView						*headerImgView;
	NSMutableArray					*adsArray;
	BOOL							touchedTripView;
	UIImageView						*ivActionButtonsLeftArrow;
	UIImageView						*ivActionButtonsRightArrow;
	UIImageView						*ivExpSummaryButtonsLeftArrow;
	UIImageView						*ivExpSummaryButtonsRightArrow;
	UIImageView						*ivTravelUserPromoBanner;
	SummaryData						*summaryData;

    CarRatesData					*carRatesData;
	
	BOOL showWhatsNew;
	UILabel					*lblNewTitle;
	UIScrollView				*scrollerNew;
	UIButton					*btnNewOK;
	UIImageView				*ivNewBackground;
	WhatsNewView					*viewNew;
	
	
	// Bump help dialog
	UILabel					*lblBumpHelpTitle;
	UILabel					*lblBumpHelpText1;
	UILabel					*lblBumpHelpText2;
	UIButton				*btnBumpShare;
	UIButton				*btnBumpCancel;
	UIImageView				*ivBumpBackground;
	UIView					*viewBumpHelp;
	UIButton				*btnUploadReceipt;
	
    // State variables for invoke multiple modal dialogs - add car mileage
    NSMutableDictionary		*wizPBag;
    BOOL					wizDlgDismissed;
            
    // New Expense
    BOOL                        addExpenseClicked;
            
    //Single User
    BOOL    isSingleUser;
    UIButton *btnAddMileage;
    UIButton *btnCreateReport;
    UITableView *reportsTableView;
    UITableView *tripItTripsTableView;            
    UITableView *dashboardTableView;
            UIWebView *tripItWebView;
    TripsViewController *tripsVC;
    UIView  *loadingContentView;
    UIImageView *bkQuickActionPanel;
    UIImageView *bkMenuPanel;
    UIImageView *bkContentPanel;
    UIButton *btnReportsTable;
    UIButton *btnTripItTable;
    UIView *menuPanel;
    RoundedRectView *contentTableContainer;
            
    BOOL                    hideReceiptStore;
            
    BOOL                    checkedMileage;
    NSMutableDictionary         *postLoginAttribute;
}

@property BOOL              hideReceiptStore;
@property (strong, nonatomic) ViewPadIntro        *viewPadIntro;
//Single user
@property (strong, nonatomic) IBOutlet UIButton *btnReportsTable;
@property (strong, nonatomic) IBOutlet UIButton *btnTripItTable;
@property (strong, nonatomic) IBOutlet UIImageView *bkQuickActionPanel;
@property (strong, nonatomic) IBOutlet UIImageView *bkMenuPanel;
@property (strong, nonatomic) IBOutlet UIImageView *bkContentPanel;
@property (strong, nonatomic) IBOutlet UIView  *loadingContentView;
@property (strong, nonatomic) IBOutlet UITableView *reportsTableView;
@property (strong, nonatomic) IBOutlet UITableView *tripItTripsTableView;
@property (strong, nonatomic) IBOutlet UIWebView *tripItWebView;
@property (strong, nonatomic) IBOutlet UITableView *dashboardTableView;
@property (strong, nonatomic) IBOutlet UIButton *btnAddMileage;
@property (strong, nonatomic) IBOutlet UIButton *btnCreateReport;
@property (strong, nonatomic) TripsViewController *tripsVC;
@property (strong, nonatomic) IBOutlet UIView *menuPanel;
@property (strong, nonatomic) IBOutlet RoundedRectView *contentTableContainer;
//Concur
@property (strong, nonatomic) IBOutlet UILabel *lblOffline;
@property BOOL      addExpenseClicked;
@property (nonatomic,strong)	IBOutlet UIButton				*btnUploadReceipt;
@property BOOL showWhatsNew;
@property (strong, nonatomic) IBOutlet UILabel					*lblNewTitle;
@property (strong, nonatomic) IBOutlet UIScrollView				*scrollerNew;
@property (strong, nonatomic) IBOutlet UIButton					*btnNewOK;
@property (strong, nonatomic) IBOutlet UIImageView				*ivNewBackground;
@property (strong, nonatomic) IBOutlet WhatsNewView					*viewNew;

@property (strong, nonatomic) CarRatesData			*carRatesData;

@property (strong, nonatomic)   NSArray				*dataOOP;
@property (strong, nonatomic)   SummaryData					*summaryData;
@property (assign, nonatomic) BOOL							requireHomeScreenRefresh;
@property (strong, nonatomic)  IBOutlet	UILayoutView		*canvas;
@property (strong, nonatomic)  IBOutlet	UILayoutView		*headerPanel;
@property (strong, nonatomic)  IBOutlet	UILayoutView		*mainPanel;
@property (strong, nonatomic)  IBOutlet	UILayoutView		*contentPanel;
@property (strong, nonatomic)  IBOutlet	UILayoutView		*h2;
@property (strong, nonatomic)  IBOutlet UILayoutView		*h1_2;
@property (strong, nonatomic)  IBOutlet UILayoutView		*h1_3;	
@property (strong, nonatomic)  IBOutlet UILayoutView		*h1_4;	
@property (strong, nonatomic)  IBOutlet	UILayoutView		*separatorView;
@property (strong, nonatomic)  IBOutlet UILayoutView		*currentTripSummaryView;
@property (strong, nonatomic)  IBOutlet UIImageView			*ivTripIcon;
@property (strong, nonatomic)  IBOutlet UILabel				*lblTripSummary;
@property (strong, nonatomic)  IBOutlet UILabel				*lblTripDated;
@property (strong, nonatomic)  IBOutlet UILayoutView		*tripCategoryIconsView;
@property (strong, nonatomic)  IBOutlet UIImageView			*ivTravelUserPromoBanner;
@property (strong, nonatomic)  UIImageView					*ivTripWait;

@property (strong, nonatomic)  IBOutlet UILayoutView		*addFunctionsView;
@property (strong, nonatomic)  IBOutlet UILayoutView		*expSummaryView;
@property (strong, nonatomic)  IBOutlet UILayoutView		*adsView;
@property (strong, nonatomic)  IBOutlet UIButton			*addFuncPrev;
@property (strong, nonatomic)  IBOutlet UIButton			*addFuncNext;
@property (strong, nonatomic)  IBOutlet UIButton			*expSummaryPrev;
@property (strong, nonatomic)  IBOutlet UIButton			*expSummaryNext;
@property (strong, nonatomic)  IBOutlet UIButton			*showAdBtn;
@property (strong, nonatomic)  IBOutlet UIScrollView		*addFuncScroller;
@property (strong, nonatomic)  IBOutlet UIScrollView		*expSummaryScroller;

@property (strong, nonatomic)  IBOutlet UIImageView			*headerImgView;
@property (strong, nonatomic)  NSMutableArray						*adsArray;
@property (assign, nonatomic)  BOOL							touchedTripView;

@property BOOL							enableActiveTrips;
@property (strong, nonatomic) IBOutlet UIButton						*btnAddExpense;
@property BOOL isExpenseOnly;
@property BOOL isTravelOnly;
@property BOOL isItinViewerOnly;

@property (strong, nonatomic) IBOutlet UIButton						*btnLogout;
@property (strong, nonatomic) IBOutlet UIButton				*btnSettings;
@property (strong, nonatomic) IBOutlet UIButton             *btnUpload;
@property (strong, nonatomic) ReportApprovalListViewController	*approvalsListVC;
@property (strong, nonatomic) ActiveReportListViewController	*reportsListVC;
@property (strong,nonatomic)  ReceiptStoreListView				*receiptStoreVC;
@property (strong, nonatomic) TripsData						*tripsData;
@property (nonatomic, strong) NSMutableDictionary			*tripBits;
@property (nonatomic, strong) NSMutableDictionary			*dictSegRows;
@property (nonatomic, strong) NSMutableArray				*keys;
@property (nonatomic, strong) NSMutableDictionary			*dictSkins;

@property (strong, nonatomic) IBOutlet UIScrollView		*scrollerButtons;
@property (strong, nonatomic) NSMutableArray			*aList;
@property (strong, nonatomic) NSMutableArray			*aKeys;
@property (strong, nonatomic) NSMutableDictionary		*dictList;
@property (nonatomic, strong) RootViewController        *rootVC;
@property (nonatomic ,strong) TripData						*currentTrip;

@property (nonatomic, strong) OutOfPocketListViewController		*oopeListVC;
@property (nonatomic, strong) OutOfPocketListViewController	*oopeFilterListVC;
@property (nonatomic, strong) TripsViewController				*tripsListVC;
@property (nonatomic, strong) NSMutableDictionary				*homeViews;

@property (nonatomic, strong) UIBarButtonItem					*rootPopoverButtonItem;

@property (nonatomic, strong) IBOutlet UIImageView				*ivBarLeft;
@property (nonatomic, strong) IBOutlet UIImageView				*ivBarRight;
@property (nonatomic, strong) IBOutlet UIImageView				*ivBarMiddle;
@property (nonatomic, strong) IBOutlet UIImageView				*ivTripInfo;
@property (nonatomic, strong) IBOutlet UIImageView				*skinExpense;
@property (nonatomic, strong) IBOutlet UIImageView				*imageExpenseWait;

@property (nonatomic, strong) IBOutlet UIScrollView				*scrollerExpense;
@property (nonatomic, strong) IBOutlet UIScrollView				*scrollerTwit;
@property (nonatomic, strong) IBOutlet UIScrollView				*scrollerTripDetails;

@property (nonatomic, strong) IBOutlet UILabel					*lblTripName;
@property (nonatomic, strong) IBOutlet UILabel					*lblTripData;
@property (nonatomic, strong) IBOutlet UILabel					*lblExpense;
@property (nonatomic, strong) IBOutlet UILabel					*lblTripWait;
@property (nonatomic, strong) IBOutlet UILabel					*lblTripDate;
@property (nonatomic, strong) IBOutlet UILabel					*lblExpenseWait;

@property (nonatomic, strong) IBOutlet UIView					*viewTrip;
@property (nonatomic, strong) IBOutlet UIView					*viewExpense;

@property (nonatomic, strong) IBOutlet UIView					*viewTripDetails;
@property (nonatomic, strong) UIView							*viewTripWait;
@property (nonatomic, strong) IBOutlet UIView					*viewExpenseWait;

@property (nonatomic, strong) IBOutlet UIImageView				*ivBackground;
@property (nonatomic, strong) IBOutlet UIImageView				*ivTwitBackground;
@property (nonatomic, strong) IBOutlet UIImageView              *ivTwitIcon;

@property (nonatomic, strong) IBOutlet UILabel					*lblCurrentTrip;
@property (nonatomic, strong) SegmentData						*segmentActive;
@property (nonatomic, strong) IBOutlet UIImageView						*ivActionButtonsLeftArrow;
@property (nonatomic, strong) IBOutlet UIImageView						*ivActionButtonsRightArrow;
@property (nonatomic, strong) IBOutlet UIImageView						*ivExpSummaryButtonsLeftArrow;
@property (nonatomic, strong) IBOutlet UIImageView						*ivExpSummaryButtonsRightArrow;

@property (strong, nonatomic) IBOutlet UILabel					*lblBumpHelpTitle;
@property (strong, nonatomic) IBOutlet UILabel					*lblBumpHelpText1;
@property (strong, nonatomic) IBOutlet UILabel					*lblBumpHelpText2;
@property (strong, nonatomic) IBOutlet UIButton					*btnBumpShare;
@property (strong, nonatomic) IBOutlet UIButton					*btnBumpCancel;
@property (strong, nonatomic) IBOutlet UIImageView				*ivBumpBackground;
@property (strong, nonatomic) IBOutlet UIView					*viewBumpHelp;

@property (strong, nonatomic) NSMutableDictionary               *wizPBag;
@property BOOL                                                  wizDlgDismissed;

@property (strong, nonatomic) IBOutlet UILabel					*lblNoData;
@property (strong, nonatomic) IBOutlet UIButton                  *btnNoData;
@property (strong, nonatomic) IBOutlet UIView					*viewNoData;
@property (nonatomic, strong) NSMutableDictionary               *postLoginAttribute;

// API for transition in add car mileage dialogs
-(void)nextStepForWizard;
-(void)wizardDlgDismissed;

- (IBAction)buttonExpensesPressed:(id)sender;
- (IBAction)buttonTripsPressed:(id)sender;
-(void)switchToDetail:(NSString *)menuItem ParameterBag:(NSMutableDictionary *)pBag;

- (IBAction)buttonSettingsPressed:(id)sender;
- (IBAction)buttonLogoutPressed:(id)sender;
- (void)onLogout;
- (void)showManualLoginView;
- (void)doPostLoginInitialization;
- (void) savePrarmetersAfterLogin:(NSDictionary *) pBag;
- (void)fetchHomePageData;

//-(void)fixUpSectionInfo;

-(void) adjustHomeButtons:(UIViewController *) vc;
-(UIView *) makeHomeButton:(NSString *)btnTitle ImageName:(NSString *)imageName SelectorName:(NSString *)selectorName ButtonPos:(int)btnPos ViewToAddTo:(UIViewController *) vc;
-(void) makeHomeButtons:(BOOL) isHome ViewToAddTo:(UIViewController *) vc;

-(void)makeExpenseView:(NSString *) btnTitle SelectorName:(NSString *)selectorName ImageName:(NSString *)imageName Row:(int)row;
-(void) loadExpenseSection:(int)cardCount ExpenseCount:(int)expenseCount ReportCount:(int)reportCount ApprovalCount:(int)approvalCount;

-(void) adjustForPortrait;
-(void) adjustForLandscape;

-(void) updateXIBLabels;
-(IBAction) showCurrentTrip:(id)sender;

- (void)getOrderedSegments:(TripData *)trip;

-(void) loadSkins;
- (IBAction)buttonReportsPressed:(id)sender;

-(void) removeSubViews;
- (IBAction)buttonAddPressed:(id)sender;
-(IBAction) buttonRailPressed:(id)sender;
-(void)popHome:(id)sender;

- (IBAction)buttonReportsFromScrollerPressed:(id)sender;
- (IBAction)buttonApprovalsFromScrollerPressed:(id)sender;

- (void)buttonDiningPressed:(id)sender;
-(void)buttonAirportsPressed:(id)sender;
- (void)switchToAirports:(id)sender;
- (void)buttonTaxiPressed:(id)sender;

-(void) killHomeData;
-(IBAction) buttonHotelPressed:(id)sender;
-(IBAction) buttonCarPressed:(id)sender;

-(void) updateBackgroundsForExpenseOnlyUsers;

-(void)setExpenseOnlyBackgroundLandscape;
-(void)setExpenseOnlyBackgroundPortrait;
-(void)resetSkins;
-(void)switchAd:(NSTimer*)theTimer;
-(void)renderActionButtons;
-(void)showViewsByUserTypes;
-(void)refreshHeader;
-(void)prepareCurrentTripSummaryView:(id)activeTripDetails;
//-(void)prepareAddFunctionsView;
-(IBAction)launchConcurBreezeURL:(id)sender;
-(void)renderDynamicButtonsForView:(NSString*)parent withImage:(NSString*)img andTitle:(NSString*)name andSelector:(SEL)selector andPCAKey:(NSString*)personalCardKey;
-(void)refreshPanelViews;
-(void)cleanUpViews;
//-(NSString*)getCurrentAdImageName;
-(void) addTripCategoryImage:(NSString*)category;
-(void) refreshTripData;
//MOB-10675
//-(void) refreshUIWithTripsData:(TripsData*)tripsData;
- (CGRect) adjustScrollerView:(UIScrollView*)scroller leftArrow:(UIImageView*)ivLeftArrow rightArrow:(UIImageView*)ivRightArrow buttonWidth: (const int) buttonWidth buttonHeight:(const int) buttonHeight offset: (int) offset padding: (int) padding;
-(void)prepareExpSummaryView;
-(void) refreshOOPData;
-(void) checkOffline;
-(void) refreshSkin;
-(void) refreshSkinForOffline;

-(void) tripsPressed:(id)sender;

-(IBAction)showPersonalCarMileage:(id)sender;
-(void) fetchPersonalCarMileageFormFields:(id)sender rptKey:(NSString*) rptKey rpt:(ReportData*) rpt;
-(void) presentPersonalCarMileageForm:(Msg *)msg;
- (void) goToSelectReport;

-(IBAction) closeNew:(id)sender;
-(void) adjustWhatsNewLandscape;
-(void)adjustWhatsNewPortrait;
- (void)buttonMetroPressed:(id)sender;

-(void) getTheRates;
-(void) refreshSummaryData;
- (IBAction)buttonTripItPressed:(id)sender;
-(void) buttonQuickPressed:(id)sender;
- (IBAction)buttonTravelTextPressed:(id)sender;

-(void) forceReload;

-(IBAction) buttonAirPressed:(id)sender;
-(void)buttonInvoicePressed:(id)sender;
-(IBAction)btnUploadReceiptPressed:(id)sender;
-(IBAction) buttonCreateReportPressed:(id)sender;
-(IBAction)buttonCardsPressed:(id) sender;
- (IBAction)segmentSwitch:(id)sender;
-(IBAction)buttonLocationCheckPressed:(id)sender;

//Offline Receipt upload queue support

-(void) queueCountChangedNotification:(NSNotification *) notification;
-(void) adjustViewForUploadButton: (int) numOfItem;
-(void) adjustViewForNoUploadButton;
-(IBAction)btnUploadPressed:(id)sender;

// SUHomeActionDelegate
-(IBAction)buttonApproveReportsPressed:(id)sender;
-(void) finishHomeUpdate;

-(void) refreshReportList;
-(void) refetchData;

-(IBAction) actionOnSUNoData:(id)sender;
-(void) initSUReportsView;

-(void) checkStateOfTrips;

-(void)animate1:(id)sender;
-(void)animate2:(id)sender;
@end
