//
//  SettingsViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import <MessageUI/MessageUI.h>
#import <MessageUI/MFMailComposeViewController.h>
#import "SettingsBaseCell.h"
#import "TextEditVC.h"
#import "TextEditDelegate.h"
#import "iPadHomeVC.h"

@class MobileAlertView;

@interface SettingsViewController : MobileViewController <UITableViewDelegate, UITableViewDataSource, MFMailComposeViewControllerDelegate, TextEditDelegate, UIAlertViewDelegate>
{
	NSMutableArray		*sections, *appInfoRows, *connectionRows, *loginRows,  *logRows, *cacheRows, *featureRows, *socialRows, *resetRows, *logoutRows;
    
#if TARGET_IPHONE_SIMULATOR
    NSMutableArray		*simulatorRows;
#endif
    
    NSMutableDictionary *dictRowData;
	UIBarButtonItem		*btnSave, *btnSaveButton, *btnCancelButton;
	UITableView			*tableSettings;

	UIToolbar			*myToolBar;

	BOOL				cameFromLogin;
	NSIndexPath			*uriIndexPath;
    
    iPadHomeVC          *padHomeVC;
    BOOL                isDropboxLinked;
    MobileAlertView     *unlinkFromTripItAlertView;
}

@property BOOL                isDropboxLinked;
@property (strong, nonatomic) iPadHomeVC          *padHomeVC;
@property BOOL					cameFromLogin;
@property (strong, nonatomic) IBOutlet UITableView			*tableSettings;

@property (strong, nonatomic) IBOutlet UIBarButtonItem *btnSaveButton;
@property (strong, nonatomic) IBOutlet UIBarButtonItem *btnCancelButton; 

@property (strong, nonatomic) IBOutlet UIBarButtonItem *btnSave;

@property (nonatomic, strong) IBOutlet UIToolbar *myToolBar;

@property (nonatomic, strong) NSMutableArray		*sections;
@property (nonatomic, strong) NSMutableArray		*appInfoRows;
@property (nonatomic, strong) NSMutableArray		*connectionRows;
@property (nonatomic, strong) NSMutableArray		*logRows;
@property (nonatomic, strong) NSMutableArray		*loginRows;
@property (nonatomic, strong) NSMutableArray		*cacheRows;
@property (nonatomic, strong) NSMutableArray		*featureRows;
@property (nonatomic, strong) NSMutableArray		*socialRows;
@property (nonatomic, strong) NSMutableArray		*resetRows;
@property (nonatomic, strong) NSMutableArray		*logoutRows;
@property (nonatomic, strong) NSMutableArray        *eReceiptRows;

#if TARGET_IPHONE_SIMULATOR
@property (nonatomic, strong) NSMutableArray		*simulatorRows;
#endif

@property (nonatomic, strong) NSMutableDictionary *dictRowData;

@property (nonatomic, strong) MobileAlertView       *unlinkFromTripItAlertView;

- (id)init;
- (id)initBeforeUserLogin;

-(IBAction)saveSettings:(id)sender;
-(IBAction)cancelSettings:(id)sender;
-(IBAction)resetSettings:(id)sender;

-(IBAction)backgroundTap:(id)sender;
-(void)confirmCacheClear:(id)sender;
-(void)buttonLogoutPressed:(id)sender;

- (void) textFieldTouched:(id)sender;

- (void)didPressTripIt;

// TextEditDelegate
-(void) textUpdated:(NSObject*) context withValue:(NSString*) value;

@end
