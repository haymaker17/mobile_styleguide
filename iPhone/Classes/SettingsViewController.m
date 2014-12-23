//
//  SettingsViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 1/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SettingsViewController.h"
#import "ExSystem.h" 

#import "DataConstants.h"

#import "MCLogging.h"
#import "FileManager.h"
#import "ExceptionLogging.h"
#import "MobileAlertView.h"
#import "ApplicationLock.h"
#import "TripItUnlink.h"

#import "KeychainManager.h"
#import "HomePageCell.h"
#import "CTENetworkSettings.h"

#import "AnalyticsTracker.h"

//#import <DropboxSDK/DropboxSDK.h>

#define kUnlinkFromTripIt 505022

@interface SettingsViewController ()

-(void) showCacheIsCleanAlert;

@end

@implementation SettingsViewController


@synthesize btnSaveButton,btnCancelButton;
@synthesize btnSave;
@synthesize myToolBar, cameFromLogin;
@synthesize tableSettings;
@synthesize sections;
@synthesize	appInfoRows, connectionRows, logRows, loginRows, cacheRows, featureRows, socialRows, resetRows, logoutRows;
@synthesize dictRowData, padHomeVC, isDropboxLinked, unlinkFromTripItAlertView;

#if TARGET_IPHONE_SIMULATOR
@synthesize simulatorRows;
#endif

#define kSectionAppInfo @"APP_INFO"
#define kSectionConnection @"CONNECTION"
#define kSectionLogin @"LOGIN"
#define kSectionLog @"LOG"
#define kSectionCache @"CACHE"
#define kSectionSocial @"SOCIAL"
#define kSectionReset @"RESET"
#define kSectionLogout @"LOGOUT"

#define kSectionAppInfoName @"APP_NAME"
#define kSectionAppInfoVersion @"VERSION"
#define kSectionLoginSaveUser @"LOGIN_SAVEUSERNAME"
#define kSectionLoginAuto @"LOGIN_AUTO"
#define kSectionTouchID @"TOUCH_ID"
#define kOffersValidityChecking @"OFFERS_VALIDITY_CHECK"
#define kDropbox @"DROPBOX"
#define kTripIt @"TRIPIT"

#if TARGET_IPHONE_SIMULATOR
#define kSectionSimulator @"SIMULATOR"
#define kRowSimulatorNetworkConnectivity @"SIMULATOR_NETWORK_CONNECTIVITY"
#endif

#define kRowSendLog 0
#define kRowCacheMode @"CACHE_MODE"
#define kRowNetworkError @"NETWORK_ERROR"

#define kTagUserName 901
#define kTagAutoLogin 902
#define kTagTouchID 903
#define kTagPanel 904
#define kTagOffer 905
#define kTagNetworkError 906
#define kTagCacheMode 907
#define kDropboxAlertViewTag 910


- (id)init
{
    self = [super initWithNibName:@"SettingsView" bundle:nil];
    if (self) {
        self.cameFromLogin = NO;
    }
    return self;
}

- (id)initBeforeUserLogin
{
    self = [super initWithNibName:@"SettingsView" bundle:nil];
    if (self) {
        self.cameFromLogin = YES;
    }
    return self;
}

-(NSString *)getDisplayMethod:(NSString *)fromView
{
	return VIEW_DISPLAY_TYPE_MODAL;
}

-(NSString *)getViewIDKey
{
	return SETTINGS_VIEW;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_MODAL;
}

-(void)respondToFoundData:(Msg *)msg
{//respond to data that might be coming from the cache
	if ([msg.idKey isEqualToString:UNLINK_FROM_TRIPIT])
	{
        [self hideWaitView];
        
        TripItUnlink* unlinkData = (TripItUnlink*) msg.responder;
        if (msg.responseCode == 200 && [unlinkData isActionStatusSuccess])
        {
            [ExSystem sharedInstance].isTripItLinked = NO;
            [[ExSystem sharedInstance] saveSettings];
            
            for (int i = 0; i < [sections count]; i++)
            {
                NSString *sectionName = (NSString*)sections[i];
                if (sectionName != nil && [sectionName isEqualToString:kTripIt])
                {
                    [sections removeObjectAtIndex:i];
                    break;
                }
            }
            
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Unlink Succeeded"] message:[Localizer getLocalizedText:@"Your account is no longer linked to TripIt."] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
            [av show];
            
            iPadHomeVC* padVC = [ConcurMobileAppDelegate findiPadHomeVC];
            [padVC checkStateOfTrips];
        }
        else
        {
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Error"] message:[Localizer getLocalizedText:@"The account could not be unlinked."] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
            [av show];
        }
        
        [tableSettings reloadData];
    }
}

- (void) initData
{
	NSString *ver = [NSString stringWithFormat:@"%@",[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"]];
	
    NSString *testValidity = nil;
   
    if ([ExSystem sharedInstance].isCorpSSOUser)
    {
        self.sections = [[NSMutableArray alloc] initWithObjects:kSectionAppInfo, kSectionConnection, kSectionLog, kSectionCache, kSectionReset, testValidity, nil];
    }
    else
    {
        if ([Config isGov])
            self.sections = [[NSMutableArray alloc] initWithObjects:kSectionAppInfo, kSectionConnection, kSectionLogin, kSectionLog, kSectionCache, kSectionReset, testValidity, nil];
        else
            self.sections = [[NSMutableArray alloc] initWithObjects:kSectionAppInfo, kSectionConnection, kSectionLogin, kSectionLog, kSectionCache, kSectionReset, testValidity, nil];
    }
    
    if(!cameFromLogin)
        [sections addObject:kSectionLogout];
    
#if TARGET_IPHONE_SIMULATOR
    [sections addObject:kSectionSimulator];
#endif
    
    self.dictRowData = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];

    
    self.appInfoRows = [[NSMutableArray alloc] initWithObjects:kSectionAppInfoName, kSectionAppInfoVersion, nil];

    dictRowData[kSectionAppInfoName] = [Config isGov] ? @"ConcurGov" : @"Concur Mobile";
    dictRowData[kSectionAppInfoVersion] = ver;

    self.connectionRows = [[NSMutableArray alloc] initWithObjects:kSectionConnection, nil];
    dictRowData[kSectionConnection] = [ExSystem sharedInstance].entitySettings.uri;
    
    if ([Config isGov])
        self.loginRows = [[NSMutableArray alloc] initWithObjects:kSectionLoginSaveUser, nil];
    else
        self.loginRows = [[NSMutableArray alloc] initWithObjects:kSectionLoginSaveUser, kSectionLoginAuto, nil];
    
    // add touchID based on site-setting
    if ([Config isTouchIDEnabled])
        [self.loginRows addObject:kSectionTouchID];

	if([[ExSystem sharedInstance].entitySettings.saveUserName isEqualToString:@"YES"])
		dictRowData[kSectionLoginSaveUser] = @"YES";
    else
        dictRowData[kSectionLoginSaveUser] = @"NO";
    
    //NSLog(@"[ExSystem sharedInstance].entitySettings.autoLogin = %@", [ExSystem sharedInstance].entitySettings.autoLogin);
	if([[ExSystem sharedInstance].entitySettings.autoLogin isEqualToString:@"YES"])
		dictRowData[kSectionLoginAuto] = @"YES";
    else
        dictRowData[kSectionLoginAuto] = @"NO";
    
    // Toogle value based on entitySettings(client logic)
    if ([[ExSystem sharedInstance].entitySettings.enableTouchID isEqualToString:@"YES"])
        dictRowData[kSectionTouchID] = @"YES";
    else
        dictRowData[kSectionTouchID] = @"NO";

    self.cacheRows = [[NSMutableArray alloc] initWithObjects:kSectionCache, nil];
    
    dictRowData[kSectionCache] = @"Clear Cache";

    self.logRows = [[NSMutableArray alloc] initWithObjects:kSectionLog, nil];
	dictRowData[kSectionLog] = @"Send Log";
    
    self.resetRows = [[NSMutableArray alloc] initWithObjects:kSectionReset, nil];
	dictRowData[kSectionReset] = @"Reset";
    
    
    // Add logout for new home
    self.logoutRows = [[NSMutableArray alloc] initWithObjects:kSectionLogout, nil];
    dictRowData[kSectionLogout] = @"Logout";
    
#if TARGET_IPHONE_SIMULATOR
    self.simulatorRows = [[NSMutableArray alloc] initWithObjects:kRowSimulatorNetworkConnectivity, nil];
    BOOL connectivity = [[ExSystem sharedInstance] networkConnectivity];
    dictRowData[kRowSimulatorNetworkConnectivity] = (connectivity ? @"YES": @"NO");
#endif
    

    if([[ExSystem sharedInstance].offersValidityChecking isEqualToString:@"YES"] ){
        dictRowData[kOffersValidityChecking] = @"YES";
    }
    else {
        dictRowData[kOffersValidityChecking] = @"NO";
    }
}

- (void)viewDidAppear:(BOOL)animated {
	[super viewDidAppear:animated];
	
	// Mob-2520 fixed the navigation bar and localization of save/cancel buttons & title label strings
	btnSaveButton.title = [Localizer getLocalizedText:@"LABEL_SAVE_BTN"];
	self.title = [Localizer getLocalizedText:@"LABEL_SETTINGS"];
	
	[tableSettings reloadData];

    if ([Config isDevBuild]) {
        self.navigationController.navigationBarHidden = NO;
    }
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
    [super viewDidLoad];
    
    [self initData];
    
    if (![ExSystem is7Plus]) {
        self.myToolBar.tintColor = [UIColor darkBlueConcur_iOS6];
    }
    self.navigationController.navigationBar.alpha = 0.9f;
    self.title = [Localizer getLocalizedText:@"LABEL_SETTINGS"];
	
	if([UIDevice isPad])
	{
		tableSettings.autoresizingMask = UIViewAutoresizingNone;
		tableSettings.frame = CGRectMake(0, myToolBar.frame.size.height, tableSettings.frame.size.width, tableSettings.frame.size.height);
		tableSettings.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        //[self initData];
	}
    
    if ([Config isCorpHome]) {
        // don't add this button to the 9.0 UI
        self.btnCancelButton = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(cancelSettings:)];
        self.navigationItem.leftBarButtonItem = btnCancelButton;
    }
    else if ([Config isGov])
    {
        self.btnCancelButton = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Cancel"] style:UIBarButtonItemStyleBordered target:self action:@selector(cancelSettings:)];
        self.navigationItem.leftBarButtonItem = btnCancelButton;
    }

    self.btnSaveButton = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Save"] style:UIBarButtonItemStyleBordered target:self action:@selector(saveSettings:)];
    self.navigationItem.rightBarButtonItem = btnSaveButton;

    // views should hide it's own toolbar
    [self.navigationController setToolbarHidden:YES animated:YES];
    
    [AnalyticsTracker initializeScreenName:@"Settings"];
}


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    
    [AnalyticsTracker resetScreenName];
}

- (void)dealloc 
{
    if (unlinkFromTripItAlertView != nil)
		[unlinkFromTripItAlertView clearDelegate];
}


#pragma mark Button Methods
-(IBAction)cancelSettings:(id)sender
{
	if([UIDevice isPad])
    {
        [self dismissViewControllerAnimated:YES completion:nil];
    }
	else 
	{
        [self dismissViewControllerAnimated:YES completion:nil];
	}

}

-(IBAction)resetSettings:(id)sender
{
    // MOB-11263 reset SSO url too
    [[ExSystem sharedInstance] clearCompanySSOLoginPageUrl];
    [[ExSystem sharedInstance] clearCompanyCode];
    
    //MOB-10828
    dictRowData[kSectionConnection] = [Config isGov] ? @"https://cge.concursolutions.com" : @"https://www.concursolutions.com";
	[tableSettings reloadData];
}

#if TARGET_IPHONE_SIMULATOR
-(void) saveSimulatorSettings
{
    if ([dictRowData[kRowSimulatorNetworkConnectivity] isEqualToString:@"YES"])
        [ExSystem sharedInstance].networkConnectivity = YES;
    else
        [ExSystem sharedInstance].networkConnectivity = NO;
    
    [[ExSystem sharedInstance] saveNetworkConnectionSettings];
}
#endif

//For offline automation testing
//Save networkConnectivity to NetworkConnectionSettings.plist
-(void) saveNetworkConnection:(BOOL)hasConnection
{
    if (hasConnection)
        [ExSystem sharedInstance].networkConnectivity = YES;
    else
        [ExSystem sharedInstance].networkConnectivity = NO;
    
    [[ExSystem sharedInstance] saveNetworkConnectionSettings];
}

-(IBAction)saveSettings:(id)sender
{
    NSString* newAutoLogin = dictRowData[kSectionLoginAuto];
    if (![[ExSystem sharedInstance].entitySettings.autoLogin isEqualToString:newAutoLogin])
    {
        NSDictionary *dictionary = @{@"Action": @"Auto Login", @"New Value": newAutoLogin};
        [Flurry logEvent:@"Settings: Action" withParameters:dictionary];
    }
    NSString* newSaveUserName = dictRowData[kSectionLoginSaveUser];
    if (![[ExSystem sharedInstance].entitySettings.saveUserName isEqualToString:newSaveUserName])
    {
        NSDictionary *dictionary = @{@"Action": @"Save User Name", @"New Value": newSaveUserName};
        [Flurry logEvent:@"Settings: Action" withParameters:dictionary];
        
    }
    
    [ExSystem sharedInstance].entitySettings.autoLogin = dictRowData[kSectionLoginAuto];
    [ExSystem sharedInstance].entitySettings.saveUserName = dictRowData[kSectionLoginSaveUser];
    [ExSystem sharedInstance].entitySettings.enableTouchID = dictRowData[kSectionTouchID];
    
#if TARGET_IPHONE_SIMULATOR
    [self saveSimulatorSettings];
#endif
    
    [ExSystem sharedInstance].entitySettings.uri = dictRowData[kSectionConnection];

    [[ExSystem sharedInstance] saveSettings];
    
    [self dismissViewControllerAnimated:YES completion:nil];
}


- (IBAction)sendLogAction:(id)sender
{
	if (![MFMailComposeViewController canSendMail])
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:[Localizer getLocalizedText:@"Mail Unavailable"]
							  message:[Localizer getLocalizedText:@"This device is not configured for sending mail."]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
	}
	else
	{
		NSData* regularLogData = nil;
		NSData* exceptionLogData = nil;
		
		NSString* logFileName = [[MCLogging getInstance] createLogSummaryWithSettings];
		if (logFileName != nil)
		{
			regularLogData = [NSData dataWithContentsOfFile:logFileName];
		}
		
		NSString *exceptionFileName = [ExceptionLogging exceptionFilePath];
		if (exceptionFileName != nil)
		{
			exceptionLogData = [NSData dataWithContentsOfFile:exceptionFileName];
		}
		
		MFMailComposeViewController *mailComposer = [[MFMailComposeViewController alloc] init];
		NSArray* recipients = @[@"mobilesupport@concur.com"];
		
		mailComposer.mailComposeDelegate = self;
		[mailComposer setToRecipients:recipients];
		[mailComposer setSubject:[Localizer getLocalizedText:@"Mobile Log"]];
		[mailComposer setMessageBody:@"" isHTML:NO];
		
		if (regularLogData != nil && regularLogData.length > 0)
			[mailComposer addAttachmentData:regularLogData mimeType:@"text/plain" fileName:@"MobileLog.txt"];
		
		if (exceptionLogData != nil && exceptionLogData.length > 0)
			[mailComposer addAttachmentData:exceptionLogData mimeType:@"text/plain" fileName:@"ExceptionLog.txt"];
		
		[self presentViewController:mailComposer animated:YES completion:nil];
		
	}
}


#pragma mark Text Methods
- (BOOL)textFieldShouldReturn:(UITextField *)doneButtonPressed 
{//hitting enter or go in the keyboard acts as though you have pressed the sign in button
	////NSLog(@"Keyboard Done Pressed");
	
	//[URITextField resignFirstResponder];
	[self saveSettings:btnSave];
	
	return YES;
}

- (IBAction)backgroundTap:(id)sender 
{//clears the keyboard from the view
	//[URITextField resignFirstResponder];	
}

- (void) textFieldTouched:(id)sender {
    // Display the panel
	
	[tableSettings scrollToRowAtIndexPath:uriIndexPath atScrollPosition:UITableViewScrollPositionTop animated:YES];
	
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [sections count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString *sectionKey = sections[section];
    
    if([sectionKey isEqualToString:kSectionAppInfo])
        return [appInfoRows count];
    else if([sectionKey isEqualToString:kSectionConnection])
        return [connectionRows count];
    else if([sectionKey isEqualToString:kSectionLogin])
        return [loginRows count];
    else if([sectionKey isEqualToString:kSectionLog])
        return [logRows count];
    else if([sectionKey isEqualToString:kSectionCache])
        return [cacheRows count];
    else if([sectionKey isEqualToString:kSectionReset])
        return [resetRows count];
    else if([sectionKey isEqualToString:kSectionLogout])
        return [logoutRows count];
#if TARGET_IPHONE_SIMULATOR
    else if([sectionKey isEqualToString:kSectionSimulator])
        return [simulatorRows count];
#endif

    else if ([sectionKey isEqualToString:kOffersValidityChecking])
        return 1;
    else if ([sectionKey isEqualToString:kDropbox])
        return 1;
    else if ([sectionKey isEqualToString:kTripIt])
        return 1;
    else
        return 0;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
    
    NSString *sectionKey = sections[section];
    NSString *rowKey = nil;
    
    if([sectionKey isEqualToString:kSectionAppInfo])
        rowKey = appInfoRows[row];
    else if([sectionKey isEqualToString:kSectionConnection])
        rowKey = connectionRows[row];
    else if([sectionKey isEqualToString:kSectionLogin])
        rowKey = loginRows[row];
    else if([sectionKey isEqualToString:kSectionLog])
        rowKey = logRows[row];
    else if([sectionKey isEqualToString:kSectionCache])
        rowKey = cacheRows[row];
    else if([sectionKey isEqualToString:kSectionReset])
        rowKey = resetRows[row];
    else if([sectionKey isEqualToString:kSectionLogout])
        rowKey = logoutRows[row];
#if TARGET_IPHONE_SIMULATOR
    else if([sectionKey isEqualToString:kSectionSimulator])
        rowKey = simulatorRows[row];
#endif
    else if ([sectionKey isEqualToString:kOffersValidityChecking])
        rowKey = 0;
    else if ([sectionKey isEqualToString:kDropbox])
        rowKey = 0;
    else if ([sectionKey isEqualToString:kTripIt])
        rowKey = 0;
    
	if ([sectionKey isEqualToString:kSectionAppInfo])
	{
		//all info cells all the time	
		SettingsBaseCell *cell = (SettingsBaseCell *)[tableView dequeueReusableCellWithIdentifier: @"LabelCell"];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"LabelCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[SettingsBaseCell class]])
					cell = (SettingsBaseCell *)oneObject;
		}
		
        cell.lblHeading.text = dictRowData[rowKey];
        
		if (row == 0)
			cell.lblSubheading.text =[Localizer getLocalizedText:@"LABEL_SETTINGS_VIEW_APP_NAME"];
		else if (row == 1)
			cell.lblSubheading.text =[Localizer getLocalizedText:@"LABEL_SETTINGS_VIEW_VERSION"];
        [cell setSelectionStyle:UITableViewCellSelectionStyleNone];
		return cell;
	}
	else if ([sectionKey isEqualToString:kSectionConnection])
	{
		//all text edit cells all the time	
		SettingsBaseCell *cell = (SettingsBaseCell *)[tableView dequeueReusableCellWithIdentifier: @"EditableTextCell"];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"EditableTextCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[SettingsBaseCell class]])
					cell = (SettingsBaseCell *)oneObject;
		}
        
		if (row == 0)
		{
			cell.lblSubheading.text = [Localizer getLocalizedText:@"LABEL_SETTINGS_VIEW_URL"];
			cell.lblHeading.text = dictRowData[rowKey];

		}

        cell.value = dictRowData[rowKey];
        cell.rowKey = rowKey;
        cell.dictRowData = dictRowData;

		return cell;
	}
	else if ([sectionKey isEqualToString:kSectionLogin])
	{
		if ([rowKey isEqualToString:kSectionLoginAuto] && 
			[[ExSystem sharedInstance].entitySettings.disableAutoLogin isEqualToString:@"Y"])
		{
			SettingsBaseCell *cell = (SettingsBaseCell *)[tableView dequeueReusableCellWithIdentifier: @"LabelFlippedCell"];
			if (cell == nil)  
			{
				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"LabelFlippedCell" owner:self options:nil];
				for (id oneObject in nib)
					if ([oneObject isKindOfClass:[SettingsBaseCell class]])
						cell = (SettingsBaseCell *)oneObject;
			}
			
			cell.lblHeading.text =[Localizer getLocalizedText:@"Auto Login"];
			cell.lblSubheading.text = [Localizer getLocalizedText:@"Disabled by administrator"];
			
			return cell;
		}
		else
		{
			SettingsBaseCell *cell = (SettingsBaseCell *)[tableView dequeueReusableCellWithIdentifier: @"SwitchCell"];
			if (cell == nil)  
			{
				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SwitchCell" owner:self options:nil];
				for (id oneObject in nib)
					if ([oneObject isKindOfClass:[SettingsBaseCell class]])
						cell = (SettingsBaseCell *)oneObject;
			}
			
			if ([rowKey isEqualToString:kSectionLoginSaveUser])
			{
				cell.lblHeading.text =[Localizer getLocalizedText:@"LABEL_SETTINGS_VIEW_REMEMBER_USER"];
				cell.switchView.tag = kTagUserName;
                [cell setSelectionStyle:UITableViewCellSelectionStyleNone];
			}
			else if ([rowKey isEqualToString:kSectionLoginAuto])
			{
				cell.lblHeading.text =[Localizer getLocalizedText:@"Auto Login"];
				cell.switchView.tag = kTagAutoLogin;
			}
            else if ([rowKey isEqualToString:kSectionTouchID])
            {
                cell.lblHeading.text = [Localizer getLocalizedText:@"Touch ID"];
                cell.switchView.tag = kTagTouchID;
                [cell.switchView addTarget:self action:@selector(switchToggled:) forControlEvents:UIControlEventValueChanged];
            }
            
			NSString *switchState = dictRowData[rowKey];
			
			if ([switchState isEqualToString:@"NO"])
				[cell.switchView setOn:FALSE];
			else 
				[cell.switchView setOn:TRUE];
			
			cell.value = dictRowData[rowKey];
			cell.rowKey = rowKey;
			cell.dictRowData = dictRowData;
			
			return cell;
		}
	}
	else if([sectionKey isEqualToString:kSectionLog])
	{
		SettingsBaseCell *cell = (SettingsBaseCell *)[tableView dequeueReusableCellWithIdentifier: @"ButtonCell"];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ButtonCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[SettingsBaseCell class]])
					cell = (SettingsBaseCell *)oneObject;
		}
        
        cell.lblHeading.shadowColor = nil;
        cell.lblHeading.shadowOffset = CGSizeMake(0, 0);
        cell.backgroundColor = [UIColor whiteColor];
        cell.lblHeading.textColor = [UIColor blackColor];
		cell.lblHeading.text = [Localizer getLocalizedText:@"Send Log"];
        [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
        
		return cell;
	}
    else if([sectionKey isEqualToString:kSectionReset])
	{
		SettingsBaseCell *cell = (SettingsBaseCell *)[tableView dequeueReusableCellWithIdentifier: @"ButtonCell"];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ButtonCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[SettingsBaseCell class]])
					cell = (SettingsBaseCell *)oneObject;
		}
		cell.lblHeading.text = [Localizer getLocalizedText:@"Reset"];
		return cell;
	}
    else if([sectionKey isEqualToString:kSectionLogout])
	{
		SettingsBaseCell *cell = (SettingsBaseCell *)[tableView dequeueReusableCellWithIdentifier: @"ButtonCell"];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ButtonCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[SettingsBaseCell class]])
					cell = (SettingsBaseCell *)oneObject;
		}
		cell.lblHeading.text = [Localizer getLocalizedText:@"Logout"];
		return cell;
	}
#if TARGET_IPHONE_SIMULATOR
	else if ([sectionKey isEqualToString:kSectionSimulator])
	{
		//all on off cells all the time
        SettingsBaseCell *cell = (SettingsBaseCell *)[tableView dequeueReusableCellWithIdentifier: @"SwitchCell"];
        if (cell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SwitchCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[SettingsBaseCell class]])
                    cell = (SettingsBaseCell *)oneObject;
        }
        
        cell.lblHeading.text =@"Network Connectivity";
        NSString *switchState = dictRowData[rowKey];
        
        if ([switchState isEqualToString:@"NO"])
            [cell.switchView setOn:FALSE];
        else
            [cell.switchView setOn:TRUE];
        
        cell.value = dictRowData[rowKey];
        cell.rowKey = rowKey;
        cell.dictRowData = dictRowData;
		return cell;
	}
#endif

	else if([sectionKey isEqualToString:kSectionCache])
	{
        
        if ([rowKey isEqualToString:kRowCacheMode] || [rowKey isEqualToString:kRowNetworkError])
        {
            SettingsBaseCell *cell = (SettingsBaseCell *)[tableView dequeueReusableCellWithIdentifier: @"SwitchCell"];
            if (cell == nil)
			{
				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SwitchCell" owner:self options:nil];
				for (id oneObject in nib)
					if ([oneObject isKindOfClass:[SettingsBaseCell class]])
						cell = (SettingsBaseCell *)oneObject;
			}
            
            if ([rowKey isEqualToString:kRowNetworkError])
            {
                //shouldErrorResponsesBeHandledSilently.  currently: 
                cell.lblHeading.text = @"Handle Network Errors Silently"; // TODO: localize if ignite becomes a product instead of just a demo
                cell.switchView.tag = kTagNetworkError;
            }
            else if ([rowKey isEqualToString:kRowCacheMode])
            {
                cell.lblHeading.text = @"Network"; // TODO: localize if ignite becomes a product instead of just a demo
                cell.switchView.tag = kTagCacheMode;
            }
            
            NSString *switchState = dictRowData[rowKey];
			
			if ([switchState isEqualToString:@"NO"])
				[cell.switchView setOn:FALSE];
			else
				[cell.switchView setOn:TRUE];
			
			cell.value = dictRowData[rowKey];
			cell.rowKey = rowKey;
			cell.dictRowData = dictRowData;
			
			return cell;
        }
        else if([rowKey isEqualToString:kSectionCache])
        {
       		SettingsBaseCell *cell = (SettingsBaseCell *)[tableView dequeueReusableCellWithIdentifier: @"ButtonCell"];
            if (cell == nil)
            {
                NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ButtonCell" owner:self options:nil];
                for (id oneObject in nib)
                    if ([oneObject isKindOfClass:[SettingsBaseCell class]])
                        cell = (SettingsBaseCell *)oneObject;
            }
            cell.lblHeading.shadowColor = nil;
            cell.lblHeading.shadowOffset = CGSizeMake(0, 0);
            cell.lblHeading.textColor = ([ExSystem connectedToNetwork] ? [UIColor blackColor] : [UIColor grayColor]);
            cell.backgroundColor = [UIColor whiteColor];
            cell.lblHeading.text = [Localizer getLocalizedText:@"Clear out the local cache"];

            return cell;
        }
	}

    if ([sectionKey isEqualToString:kOffersValidityChecking])
    {
		//all on off cells all the time	
        SettingsBaseCell *cell = (SettingsBaseCell *)[tableView dequeueReusableCellWithIdentifier: @"SwitchCell"];
        if (cell == nil)  
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SwitchCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[SettingsBaseCell class]])
                    cell = (SettingsBaseCell *)oneObject;
        }
        
        cell.lblHeading.shadowColor = nil;
        cell.lblHeading.shadowOffset = CGSizeMake(0, 0);
        cell.lblHeading.textColor = [UIColor blackColor];
        cell.backgroundColor = [UIColor whiteColor];
        cell.lblHeading.text = @"Offers Validity Check";
        cell.switchView.tag = kTagOffer;
        NSString *switchState = [ExSystem sharedInstance].offersValidityChecking;
        
        if ([switchState isEqualToString:@"NO"])
            [cell.switchView setOn:FALSE];
        else 
            [cell.switchView setOn:TRUE];
        
        cell.value = dictRowData[rowKey];
        cell.rowKey = rowKey;
        cell.dictRowData = dictRowData;
		return cell;
    }
    else if ([sectionKey isEqualToString:kDropbox])
    {
        HomePageCell *cell = (HomePageCell *)[tableView dequeueReusableCellWithIdentifier: @"MoreCell"];
        if (cell == nil)  
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"MoreCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[HomePageCell class]])
                    cell = (HomePageCell *)oneObject;
        }
//        if (![[DBSession sharedSession] isLinked]) 
//            cell.lblHeading.text = [Localizer getLocalizedText:@"Link Dropbox"];
//        else
//            cell.lblHeading.text = [Localizer getLocalizedText:@"Unlink Dropbox"];
        
        if (isDropboxLinked) {
            cell.lblHeading.text = [Localizer getLocalizedText:@"Unlink Dropbox"];
        }
        cell.iv.image = [UIImage imageNamed:@"icon_dropbox"];
        return cell;
    }
    else if ([sectionKey isEqualToString:kTripIt])
    {
        HomePageCell *cell = (HomePageCell *)[tableView dequeueReusableCellWithIdentifier: @"MoreCell"];
        if (cell == nil)  
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"MoreCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[HomePageCell class]])
                    cell = (HomePageCell *)oneObject;
        }
        cell.lblHeading.text = [Localizer getLocalizedText:@"Unlink from TripIt"];
        cell.iv.image = [UIImage imageNamed:@"icon_tripit"];
        return cell;
    }
    
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    return nil;
}



#pragma mark -
#pragma mark Table View Delegate Methods

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    NSString *sectionKey = sections[section];

    if([sectionKey isEqualToString:kSectionAppInfo])
        return [Localizer getLocalizedText:@"Application Information"];
    else if([sectionKey isEqualToString:kSectionConnection])
        return [Localizer getLocalizedText:@"Connection Settings"];
    else if([sectionKey isEqualToString:kSectionLogin])
        return [Localizer getLocalizedText:@"Login Settings"];
    else if([sectionKey isEqualToString:kSectionLog])
        return [Localizer getLocalizedText:@"Log"];
    else if([sectionKey isEqualToString:kSectionCache])
        return [Localizer getLocalizedText:@"Cache"];
    else if([sectionKey isEqualToString:kSectionReset])
        return [Localizer getLocalizedText:@"Reset to factory defaults"];
    else if([sectionKey isEqualToString:kSectionLogout])
        return [Localizer getLocalizedText:@"Log out from Concur Mobile"];
    if ([sectionKey isEqualToString:kOffersValidityChecking])
        return @"Offers Validity Checking";
    else 
        return @"";
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 54;	
}


- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
	
	// Let's remember the selection
	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
    NSString *sectionKey = sections[section];
    NSString *rowKey = nil;
    
    if([sectionKey isEqualToString:kSectionAppInfo])
        rowKey = appInfoRows[row];
    else if([sectionKey isEqualToString:kSectionConnection])
        rowKey = connectionRows[row];
    else if([sectionKey isEqualToString:kSectionLogin])
        rowKey = loginRows[row];
    else if([sectionKey isEqualToString:kSectionLog])
        rowKey = logRows[row];
    else if([sectionKey isEqualToString:kSectionCache])
        rowKey = cacheRows[row];
    else if([sectionKey isEqualToString:kSectionReset])
        rowKey = resetRows[row];
    else if([sectionKey isEqualToString:kSectionLogout])
        rowKey = logoutRows[row];
    else if ([sectionKey isEqualToString:kOffersValidityChecking])
        rowKey = 0;
    else if ([sectionKey isEqualToString:kDropbox])
        rowKey = 0;
    else if ([sectionKey isEqualToString:kTripIt])
        rowKey = 0;
	
	if ([sectionKey isEqualToString:kSectionLog])
	{
        NSDictionary *dictionary = @{@"Action": @"Send Log"};
        [Flurry logEvent:@"Settings: Action" withParameters:dictionary];

		[self sendLogAction:self];
	}
	else if ([sectionKey isEqualToString:kSectionCache])
	{
        if ([ExSystem connectedToNetwork])
        {
            NSDictionary *dictionary = @{@"Action": @"Clear Cache"};
            [Flurry logEvent:@"Settings: Action" withParameters:dictionary];

            if ([rowKey isEqualToString:kSectionCache])
                [self confirmCacheClear:self];
        }
        else
            [tableView deselectRowAtIndexPath:indexPath animated:NO];
	}
    else if ([sectionKey isEqualToString:kSectionReset])
    {
        NSDictionary *dictionary = @{@"Action": @"Reset"};
        [Flurry logEvent:@"Settings: Action" withParameters:dictionary];

        [self resetSettings:self];
    }
    else if ([sectionKey isEqualToString:kSectionLogout])
    {
        NSDictionary *dictionary = @{@"Action": @"Logout"};
        [Flurry logEvent:@"Settings: Action" withParameters:dictionary];
        [self buttonLogoutPressed:self];
    }
    else if ([sectionKey isEqualToString:kSectionConnection])
    {
        NSDictionary *dictionary = @{@"Action": @"Connection"};
        [Flurry logEvent:@"Settings: Action" withParameters:dictionary];
        

        TextEditVC *vc = [[TextEditVC alloc] initWithNibName:@"TextEditVC" bundle:nil];
        [vc setSeedData:dictRowData[rowKey] context:rowKey
            delegate:self
            tip:[Localizer getLocalizedText: @"Enter in the URL that connects to the Concur servers" ]
            title:[Localizer getLocalizedText:@"LABEL_SETTINGS_VIEW_URL"]
                 prompt:nil isNumeric:NO isPassword:NO err:nil];

        // MOB-18218 disable auto-correction for server connection url
        [vc setDisableAutoCorrect:YES];
        
        // delegate - vc.dictRowData = dictRowData;
        [self.navigationController pushViewController:vc animated:YES];
    }
    else if ([sectionKey isEqualToString:kOffersValidityChecking])
    {
        NSDictionary *dictionary = @{@"Action": @"Offer Validity"};
        [Flurry logEvent:@"Settings: Action" withParameters:dictionary];

        if ([[ExSystem sharedInstance].offersValidityChecking isEqualToString:@"YES"])
            [[ExSystem sharedInstance].offersValidityChecking isEqualToString:@"NO"];
        else
            [[ExSystem sharedInstance].offersValidityChecking isEqualToString:@"YES"];
    }
    else if([sectionKey isEqualToString:kTripIt])
        [self didPressTripIt];
    
     [tableView deselectRowAtIndexPath:indexPath animated:NO];
}
    
#pragma mark - TripIt
- (void)didPressTripIt
{
    self.unlinkFromTripItAlertView = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Please Confirm"] message:[Localizer getLocalizedText:@"Are you sure you want to unlink from your TripIt account?"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Cancel"] otherButtonTitles:[Localizer getLocalizedText:@"OK"], nil];
    [unlinkFromTripItAlertView show];
    unlinkFromTripItAlertView.tag = kUnlinkFromTripIt;
}

#pragma mark -
#pragma mark TextEditDelegate
-(void) textUpdated:(NSObject*) context withValue:(NSString*) value
{
    NSString* rowKey = (NSString*) context;
    (self.dictRowData)[rowKey] = value;
}

#pragma mark -
#pragma mark logout Stuff

-(void)buttonLogoutPressed:(id)sender
{
    // MOB-5946 buttonLogoutPressed will attempt to MODALLY present a nav view whose root view is
    // the login view.  But this (settings) view is already being shown MODALLY in a nav view whose
    // root view is this (settings) view.  To prevent a crash from arising from an overlap of
    // the dismisal of this (settings) view and the presentation of the next (login) view, this
    // (settings) view will be dimissed WITHOUT animation.
    //
 	[self dismissViewControllerAnimated:NO completion:nil];
 
	[[ApplicationLock sharedInstance] onLogoutButtonPressed];
}

#pragma mark -
#pragma mark MFMailComposeViewControllerDelegate Methods
- (void)mailComposeController:(MFMailComposeViewController*)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError*)err
{
	NSString* alertTitle = nil;
	NSString* alertMessage = nil;
	
	switch (result)
	{
		case MFMailComposeResultSent:
			alertTitle = [Localizer getLocalizedText:@"Mail Queued"];
			alertMessage = [Localizer getLocalizedText:@"The log mail has been placed in your outbox."];
			break;
		case MFMailComposeResultSaved:
			alertTitle = [Localizer getLocalizedText:@"Mail Saved"];
			alertMessage = [Localizer getLocalizedText:@"The log mail has been saved in your Drafts folder."];
			break;
		case MFMailComposeResultFailed:
			alertTitle = [Localizer getLocalizedText:@"Send Failed"];
			if (err != nil)
			{
				alertMessage = [err localizedFailureReason];
			}
			else
			{
				alertMessage = [Localizer getLocalizedText:@"The log could not be sent."];
			}
			break;
		default:
			break;
	}
	
	if (alertTitle != nil && alertMessage != nil)
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle: alertTitle
							  message: alertMessage
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
	}
	
	[self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark -
#pragma mark Clear Cache Methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if(buttonIndex == 1 && alertView.tag == 999)
	{
		[FileManager cleanCache];
        [self performSelector:@selector(showCacheIsCleanAlert) withObject:nil afterDelay:0.5f];
	}
    else if(alertView.tag == kDropboxAlertViewTag)
	{
        [tableSettings reloadData];
    }
    else if (alertView.tag == kUnlinkFromTripIt)
    {
        if (buttonIndex == 1)
        {
            [unlinkFromTripItAlertView clearDelegate];
            self.unlinkFromTripItAlertView = nil;
            
            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil]; // This mvc will not be called unless pbag is provided
            [[ExSystem sharedInstance].msgControl createMsg:UNLINK_FROM_TRIPIT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
            
            [self showWaitView];
        }
        else
        {
            [tableSettings reloadData];
        }
    }
	
}

-(void) showCacheIsCleanAlert
{
    UIAlertView * doneAlert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Cache Done Title"]
                                                             message:[Localizer getLocalizedText:@"Done cache killing"]
															delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                                   otherButtonTitles:nil];
    [doneAlert show];

}

-(void)confirmCacheClear:(id)sender
{	
	// Mob-2602 Changed to a more user friendly message
	NSString* nsQuestion = [Localizer getLocalizedText:@"ALERT_CLEAR_CACHE"];

	UIAlertView* alert = [[MobileAlertView alloc] initWithTitle:
						  [Localizer getLocalizedText:@"Confirm Delete"] 
													message:nsQuestion delegate:self cancelButtonTitle:
						  [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
										  otherButtonTitles:[Localizer getLocalizedText:@"OK"], 
						  nil];
	alert.tag = 999;
	[alert show];
}

- (void) switchToggled:(id)sender
{
    if ([self.dictRowData[kSectionTouchID] isEqualToString:@"NO"])
    {
        NSString *eventLlabel = [NSString stringWithFormat:@"Fingerprint value: %@", @"OFF"];
        [AnalyticsTracker logEventWithCategory:@"Settings" eventAction:@"Toggle" eventLabel:eventLlabel eventValue:nil];
    }
    else if ([self.dictRowData[kSectionTouchID] isEqualToString:@"YES"])
    {
        NSString *eventLlabel = [NSString stringWithFormat:@"Fingerprint value: %@", @"ON"];
        [AnalyticsTracker logEventWithCategory:@"Settings" eventAction:@"Toggle" eventLabel:eventLlabel eventValue:nil];
    }
}

@end
