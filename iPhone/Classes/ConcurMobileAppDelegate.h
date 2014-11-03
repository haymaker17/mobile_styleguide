//
//  ConcurMobileAppDelegate.h
//  ConcurMobile
//
//  Created by Paul Kramer on 10/27/09.
//  Copyright __MyCompanyName__ 2009. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "GlobalLocationManager.h"
#import "ReceiptEditorVC.h"
#import "Receipt.h"
#import "QEFormVC.h"
#import "ReportDetailViewController.h"
#import "NotificationEvent.h"

#import "HomeLoaderVC.h"
#import "Home9VC.h"
#import "iPadHome9VC.h"
#import "GoviPadHome9VC.h"
#import "GovHome9VC.h"

@class DetailViewController;
@class iPadHomeVC;
@class RootViewController;
@class RotatingRoundedRectView;
@class ExSystem;



@interface ConcurMobileAppDelegate : NSObject <UIApplicationDelegate>
{
    UIWindow *window;
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
    UISplitViewController *splitViewController;
#endif
    DetailViewController *detailViewController;
	
	UINavigationController *navController;
	
    UIView                  *coverView;
	RotatingRoundedRectView	*authView;
    
    bool                    isFBCallback;
}

@property (nonatomic, strong) IBOutlet UIWindow *window;

@property (nonatomic, strong) IBOutlet UINavigationController *navController;
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
@property (nonatomic, strong) IBOutlet UISplitViewController *splitViewController;
#endif
@property (nonatomic, strong) IBOutlet DetailViewController *detailViewController;
@property (nonatomic, strong) IBOutlet iPadHomeVC			*padHomeVC;
@property (nonatomic, strong) UIView                        *coverView;
@property (nonatomic, strong) RotatingRoundedRectView		*authView;
@property (nonatomic, strong) QEFormVC                      *qeFormVC;
@property (nonatomic, strong) ReceiptEditorVC                *receiptVC;
@property BOOL                                              *isUploadPdfReceipt;

+(UINavigationController*) getBaseNavigationController;
+(iPadHomeVC*) findiPadHomeVC;
+(RootViewController*) findRootViewController;

+(UIViewController*) findHomeVC __attribute__ ((deprecated("The presenter view controller is responsible for presenting and dismissing the presented view controller, the presented view controller should never need to ask the application delegate to know who is it's presenting view controller")));

-(UINavigationController*) getNavigationController;

-(void)createAuthenticatingView;
-(void)destroyAuthenticatingView;

-(void) sendApplicationDidEnterBackgroundNotifications;

@property (nonatomic, strong, readonly) NSManagedObjectContext *managedObjectContext;
@property (nonatomic, strong, readonly) NSManagedObjectModel *managedObjectModel;
@property (nonatomic, strong, readonly) NSPersistentStoreCoordinator *persistentStoreCoordinator;

- (void)saveContext;
- (NSURL *)applicationDocumentsDirectory;
-(void) processNotification:(NSNotification*)notification;

- (void) registerPush;
- (void) initPush:(NSDictionary *)launchOptions;

-(void) switchToPinResetView;
-(void) switchToCompanySignInView;

+(BOOL)isUniversalTourScreenShown;

// Enabled for codecoverage builds only.
#if CODE_COVERAGE
-(void)flushCodeCoverageData;
#endif

// Methods Moved from RootviewController class
// These are generic methods which need not be in RVC, moved these here so we can delete the RVC class and use the homevc as rootviewcontroller.
@property (strong, nonatomic) NSString						*topView;
+(NSArray*)getAllViewControllers;
+(NSArray*)getViewControllersInNavController:(UINavigationController*)navController;
+(MobileViewController*)getMobileViewControllerByViewIdKey:(NSString*)key;
+(Boolean) isLoginViewShowing;
+(BOOL) hasMobileViewController:(MobileViewController*)viewController;
+(void) unwindToRootView;
+(void) addViewControllersToUnwindToArray:(NSMutableArray*)viewControllersToUnwind;
+(BOOL)switchToView:(NSString *)to viewFrom:(NSString *)from ParameterBag:(NSMutableDictionary *)paramBag;
+(void)pushOnToViewStack:(MobileViewController *)toView FromView:(MobileViewController *)fromView;
+(void) refreshTopViewData:(Msg *)msg;
+(void)switchViewsByTag:(id)sender;
+(void)switchViews:(id)sender ParameterBag:(NSMutableDictionary *)paramBag;


@end

