////
////  RootViewController.h
////  ConcurMobile
////
////  Created by Paul Kramer on 10/27/09.
////  Copyright 2009 __MyCompanyName__. All rights reserved.
////
//
//#import <UIKit/UIKit.h>
//
#import "Msg.h"
#import "MobileViewController.h"
//#import "Authenticate.h"
//#import "DateTimeFormatter.h"
//#import "ExpenseTypesViewController.h"
//#import "HomePageCell.h"
#import "iAd/ADBannerView.h"
//#import "PersonalCardData.h"
//#import "ViewPanel.h"
//#import "ReportData.h"
#import "CarRatesData.h"
//#import "UnifiedImagePicker.h"
//#import "HomeField.h"
//#import "ViewConstants.h"
//#import "Flurry.h"
//#import "HomeManager.h"
//#import "BaseManager.h"
//#import "EntityHome.h"
//#import "TripData.h"
//#import "UserConfig.h"
//#import "WhatsNewView.h"
//#import "TripManager.h"
//#import "GovWarningMessagesData.h"
//
//@class LoginViewController;
//@class ExpenseListViewController;
//@class EntryListViewController;
//@class TripsViewController;
//@class TripDetailsViewController;
//@class ItinDetailsViewController;
//@class MapViewController;
//
//@class FindMe;
//@class SettingsViewController;
//@class OutOfPocketData;
//@class SummaryData;
//@class TripsData;
//
@interface RootViewController : MobileViewController  {
//
//	CarRatesData				*carRatesData;
//	FindMe						*findMe;
//	UINavigationBar				*navBar;
//	NSMutableDictionary			*viewState, *rowData;
//	NSString					*topView;
//	UITableView					*tableList;
//
//	NSMutableArray				*sectionData, *sectionKeys, *sections;
//    NSMutableArray              *aTripRows, *aExpenseRows, *aFindTravelRows;
//    NSMutableDictionary         *dictRowData;
//
//	BOOL						hasDrawn;
//	BOOL						inLowMemory, isFindingMe;
//    
//	//iAds
//	UIView						*_contentView;
//	id							_adBannerView;
//	BOOL						_adBannerViewIsVisible;
//
//	NSMutableDictionary			*meImageStatusDict;
//	BOOL						_wasLandscape;
//	BOOL						enablefilterUnsubmittedActiveReports;
//	
//    //Whats new
//	UILabel						*lblNewTitle;
//	UIScrollView				*scrollerNew;
//	UIButton					*btnNewOK;
//	UIImageView					*ivNewBackground;
//	UIView						*viewNew;
//	BOOL						showWhatsNew;
//	
//	// Bump help dialog
//	UILabel						*lblBumpHelpTitle;
//	UILabel						*lblBumpHelpText1;
//	UILabel						*lblBumpHelpText2;
//	UIButton					*btnBumpShare;
//	UIButton					*btnBumpCancel;
//	UIImageView					*ivBumpBackground;
//	UIView						*viewBumpHelp;
//
//    BOOL                        cameFromTripIt;
//	
//	//Receipt Migration 7.3
//	NSMutableArray				*unassociatedReceiptObjects;
//    NSMutableArray              *aAppRows;
//    
//    EntityHome                  *homeData;
//    UIView                      *viewFooter;
//    UILabel                     *lblFooter;
//    EntityTrip                  *currentTrip;
//    
//    // New Expense - to distinguish btwn Add Expense and Add Mileage
//    BOOL                        addExpenseClicked;
//    BOOL                        isFetchingEntryForm;
//	
//	BOOL						isLoggingOutAndReauthenticating;
//    UIView                      *viewSUHeader;
//    
//    BOOL                         requireHomeScreenRefresh;
//    
//    UIImageView                 *ivTripItAd;
//    UIButton                    *btnTripItAd;
//
//    WhatsNewView                *whatsNewView;
//    
//    BOOL                       bannerAdjusted;
//    
//    // Invoice count
//    NSString                    *invoiceCount;
//
//    NSMutableDictionary         *postLoginAttribute;
//    EntityWarningMessages       *allMessages;
}
//
//@property BOOL bannerAdjusted;
//@property (strong, nonatomic) WhatsNewView                *whatsNewView;
//@property BOOL cameFromTripIt;
//@property (strong, nonatomic) IBOutlet UIImageView                 *ivTripItAd;
//@property (strong, nonatomic) IBOutlet UIButton                    *btnTripItAd;
//
//@property BOOL      addExpenseClicked;
//@property BOOL      isFetchingEntryForm;
//@property (nonatomic, strong) EntityTrip                  *currentTrip;
//@property (strong, nonatomic) IBOutlet UIView                      *viewFooter;
//@property (strong, nonatomic) IBOutlet UILabel                     *lblFooter;
//
//@property (strong, nonatomic) IBOutlet UIView             *viewSUHeader;
//
//@property (nonatomic, strong) EntityHome                  *homeData;
//@property (nonatomic, strong) NSFetchedResultsController    *fetchedResultsController;
//@property (nonatomic, strong) NSManagedObjectContext        *managedObjectContext;
//
//@property (strong, nonatomic) NSMutableDictionary         *dictRowData;
//@property (strong, nonatomic) NSMutableDictionary         *rowData;
//@property (strong, nonatomic) NSMutableArray              *aTripRows;
//@property (strong, nonatomic) NSMutableArray              *aExpenseRows;
//@property (strong, nonatomic) NSMutableArray              *aFindTravelRows;
//@property (strong, nonatomic) NSMutableArray              *aAppRows;
//
//@property (strong, nonatomic) IBOutlet UILabel					*lblBumpHelpTitle;
//@property (strong, nonatomic) IBOutlet UILabel					*lblBumpHelpText1;
//@property (strong, nonatomic) IBOutlet UILabel					*lblBumpHelpText2;
//@property (strong, nonatomic) IBOutlet UIButton					*btnBumpShare;
//@property (strong, nonatomic) IBOutlet UIButton					*btnBumpCancel;
//@property (strong, nonatomic) IBOutlet UIImageView				*ivBumpBackground;
//@property (strong, nonatomic) IBOutlet UIView					*viewBumpHelp;
//
//@property BOOL showWhatsNew;
//@property (strong, nonatomic) IBOutlet UILabel					*lblNewTitle;
//@property (strong, nonatomic) IBOutlet UIScrollView				*scrollerNew;
//@property (strong, nonatomic) IBOutlet UIButton					*btnNewOK;
//@property (strong, nonatomic) IBOutlet UIImageView				*ivNewBackground;
//@property (strong, nonatomic) IBOutlet UIView					*viewNew;
//
@property (strong, nonatomic) CarRatesData						*carRatesData;
//@property BOOL													enablefilterUnsubmittedActiveReports;
//
//@property (nonatomic,strong) NSMutableArray						*unassociatedReceiptObjects;
//@property BOOL									wasLandscape;
//@property BOOL									isFindingMe;
//@property BOOL									inLowMemory;
//@property (strong, nonatomic) FindMe			*findMe;
//@property (strong, nonatomic) NSMutableDictionary			*viewState;
//@property (strong, nonatomic) IBOutlet UINavigationBar		*navBar;
//@property (strong, nonatomic) NSString						*topView;
//@property (strong, nonatomic) NSMutableDictionary			*meImageStatusDict;
//@property BOOL												hasDrawn;
//@property (nonatomic, strong) IBOutlet NSMutableArray		*sections;
//
//
//@property (nonatomic, strong) IBOutlet UITableView			*tableList;
//@property (nonatomic, strong) NSMutableArray				*sectionData;
//@property (nonatomic, strong) NSMutableArray				*sectionKeys;
////end homepage properties
//
//@property (nonatomic, strong) IBOutlet UIView				*contentView;
//@property (nonatomic, strong) id							adBannerView;
//@property (nonatomic) BOOL									adBannerViewIsVisible;
//@property (nonatomic, assign) BOOL							isLoggingOutAndReauthenticating;
//
//@property (strong, nonatomic) NSString						*invoiceCount;
//

//@property (nonatomic, strong) NSMutableDictionary           *postLoginAttribute;
//@property (strong, nonatomic) EntityWarningMessages           *allMessages;
//-(IBAction)switchViews:(id)sender ParameterBag:(NSMutableDictionary *)parameterBag;
//-(BOOL)switchToView:(NSString *)to viewFrom:(NSString *)from ParameterBag:(NSDictionary *)parameterBag;
//-(void) refreshTopViewData:(Msg *)msg;
//
//+(NSArray*)getAllViewControllers;
//+(NSArray*)getViewControllersInNavController:(UINavigationController*)navController;
//+(MobileViewController*)getMobileViewControllerByViewIdKey:(NSString*)key;
//+(BOOL) isLoginViewShowing;
//+(BOOL) hasMobileViewController:(MobileViewController*)viewController;
//
//-(MobileViewController *)getController:(NSString *)to;
//
//-(void)doFindMe;
//
////home methods
//-(void)setToolbarHome;
//
//-(void)initSections;
//
//- (void)buttonTripsPressed:(id)sender;
//- (void)buttonApproveReportsPressed:(id)sender;
//- (IBAction)buttonSettingsPressed:(id)sender;
//- (void)buttonDiningPressed:(id)sender;
//- (void)buttonCarPressed:(id)sender;
//- (void)buttonHotelPressed:(id)sender;
//- (void)buttonTaxiPressed:(id)sender;
//-(void)buttonAmtrakPressed:(id)sender;
//- (IBAction)buttonLogoutPressed:(id)sender;
//-(void)buttonOutOfPocketPressed:(id)sender;
//-(void)buttonApproveInvoicesPressed:(id)sender;
//-(void)onLogout;
//
//-(void)buttonReportsPressed:(id)sender;
//
//-(void)switchToReportsList:(id)sender;
//-(void)switchToExpenses:(id)sender;
//-(void)switchToApprovals:(id)sender;
//-(void)fetchData:(NSMutableDictionary *)pBag;
//
////quicke
//-(void) createUI:(MobileViewController *)nextController;
//
//- (void)fixupAdView:(UIInterfaceOrientation)toInterfaceOrientation;
//- (void)createAdBannerView;
//- (int)getBannerHeight:(UIDeviceOrientation)orientation;
//- (int)getBannerHeight ;
//
//-(void)fetchHomePageDataAndSkipCache:(BOOL)shouldSkipCache;
//-(void)fetchHomePageData;
//-(void)buttonAirportsPressed:(id)sender;
//-(void)switchToAirports:(id)sender;
//
//- (NSString*) getPersonalCardCountMessage:(PersonalCardData*) pcData;
//- (NSString*) getRptRowCountMessage:(NSString*)strCount msgKey:(NSString*)key;
//- (NSString*) getCardTransactionCountMessage:(int) count;
//
//
//#ifdef _INC_ATTENDEE_
//#else
//#define _INC_ATTENDEE_ 1
//#endif
//
//-(void)buttonActionPressed:(id)sender;
//
//-(void) showFilteredCorpCards:(id)sender;
//
//- (IBAction)showPersonalCarMileage:(id)sender;
//- (void)openPersonalCarMileage:(UIViewController *)parentView;
//
//- (void) goToSelectReport;
//-(void) presentPersonalCarMileageForm:(Msg *)msg;
//-(void) fetchPersonalCarMileageFormFields:(id)sender rptKey:(NSString*) rptKey  rpt:(ReportData*) rpt;
//-(void)buttonUnsubmmitedReportsPressed:(id)sender;
//
//-(void) adjustWhatsNewLandscape;
//-(IBAction) closeNew:(id)sender;
//-(void) hideNew:(id)sender;
//-(MobileAlertView*) getPrivacyActView:(UIViewController*) del;
//-(void) makeWhatsNew;
//-(NSMutableArray *) makeWhatsNewText;
//- (void)buttonMetroPressed:(id)sender;
//
//-(void)enableSettingsAndLogoutButtons;
//-(void)disableSettingsAndLogoutButtons;
//
//- (void)refreshUIWithOOPData:(OutOfPocketData *)oopData;
//- (void)refreshUIWithSummaryData:(SummaryData*)sd;
//- (void)refreshUIWithTripsData:(TripsData *)tripsData;
//- (void) prepareSections;
//- (void) fetchCarRatesAndSkipCache:(BOOL)shouldSkipCache;
//
//-(void) doPostLoginInitialization;
//-(void) savePrarmetersAfterLogin:(NSDictionary *) pBag;
//-(void) unwindToRootView;
//-(void) addViewControllersToUnwindToArray:(NSMutableArray*)viewControllersToUnwind;
//
//-(void) preparePersonalCardRows: (OutOfPocketData*)oopData;
//-(NSArray*)getCardRowDetails:(OutOfPocketData*)oopData;
//
//-(void) buttonTestPressed:(id)sender;
//
//-(void)buttonCardsPressed:(id)sender;
//-(void)adjustWhatsNewPortrait;
//
//-(IBAction) buttonAddReportPressed:(id)sender;
//
//- (void)configureCell:(HomePageCell *)cell atIndexPath:(NSIndexPath *)indexPath;
//-(void) forceRefetch;
//-(void) clearHomeData;
//-(void)bookingsActionPressed:(id)sender;
//
//-(void)fetchInvoiceCount;
//
//- (WhatsNewView *)getWhatsNewView;
//- (WhatsNewView *)getWhatsNewViewForIPad;
//
//#define kOfferToSendCrashLogAlert		31854
//#define kFollowUpOfSendCrashLogAlert	31855
//
//#define kSECTION_APP @"APPS"
//#define kSECTION_TRIPS @"TRIPS"
//#define kSECTION_EXPENSE @"EXPENSE"
//#define kSECTION_INVOICE @"INVOICE"
//#define kSECTION_BOOKINGS @"BOOKINGS"
//#define kSECTION_INVITE @"INVITE"
//
//#define kSECTION_APP_POS 5
//#define kSECTION_TRIPS_POS 0
//#define kSECTION_EXPENSE_POS 2
//#define kSECTION_INVOICE_POS 3
//#define kSECTION_INVITE_POS 4
//#define kSECTION_BOOKINGS_POS 1
//
//#define kSECTION_TRIPS_CURRENT_BUTTON  @"TRIP_CURRENT_BUTTON"
//#define kSECTION_TRIPS_FIND_TRAVEL  @"TRIPS_FIND_TRAVEL"
//#define kSECTION_TRIPS_TRIPS_BUTTON  @"TRIPS_BUTTON"
//#define kSECTION_EXPENSE_RECEIPTS_BUTTON  @"RECEIPTS_BUTTON"
//#define kSECTION_EXPENSE_REPORTS_BUTTON  @"REPORTS_BUTTON"
//#define kSECTION_EXPENSE_EXPENSES_BUTTON  @"EXPENSES_BUTTON"
//#define kSECTION_EXPENSE_CARDS @"EXPENSE_CARDS"
//#define kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON @"TRAVEL_REQUEST_BUTTON"
//
//#define kSECTION_EXPENSE_REPORTS @"EXPENSE_REPORTS"
//#define kSECTION_EXPENSE_QUICK @"kSECTION_EXPENSE_QUICK"
//#define kSECTION_EXPENSE_BREEZE_CARD @"kSECTION_EXPENSE_BREEZE_CARD"
//#define kSECTION_EXPENSE_APPROVALS @"EXPENSE_APPROVALS"
//#define kSECTION_APP_TOUR @"APP_TOUR"
//#define kSECTION_BOOKINGS_GATE_GURU  @"GATE_GURU"
//#define kSECTION_BOOKINGS_METRO  @"METRO"
//#define kSECTION_BOOKINGS_TAXI_MAGIC  @"TAXI_MAGIC"
//
//#define kSECTION_INVOICE_INVOICE @"INVOICE_INVOICE"
//
//-(void) refetchData;
//-(IBAction) buttonQuickPressed:(id)sender;
//-(IBAction) buttonGovQuickPressed:(id)sender;
//
//-(void) showManualLoginView;
//- (IBAction)buttonTripItPressed:(id)sender;
//-(IBAction)launchConcurBreezeURL:(id)sender;
//- (IBAction)buttonTravelTextPressed:(id)sender;
//
//-(void)buttonInvoicePressed:(id)sender;
//#pragma mark - Switch to Receipts
//-(IBAction)showReceiptViewer:(id)sender;
//
//- (IBAction)showSafetyCheckIn:(id)sender;
//- (void)openSafetyCheckIn:(UIViewController *)parentView;
//
//-(BOOL) checkBookAir;
//
//-(NSString*) getExpensesCountMessage:(int) count;
//-(NSString*) getCardChargesCountMessage:(int) count;
//
//-(void) forceReload;
//-(void) refreshExpenseSummary;
//
//
//-(void) didCloseWhatsNew:(NSNotification *)notification;
//
//-(void) adjustViewForUploadBanner;
//-(void) adjustViewForNoUploadBanner;
@end
