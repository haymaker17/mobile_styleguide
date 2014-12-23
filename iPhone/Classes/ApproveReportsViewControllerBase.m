//
//  ApproveReportsViewControllerBase.m
//  ConcurMobile
//
//  Created by Yuri Kiryanov on 3/12/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "FeedbackManager.h"
#import "ApproveReportsViewControllerBase.h"
#import "ApproveEntriesViewController.h"
#import "ExSystem.h" 

#import "FormatUtils.h"
#import "LabelConstants.h"
#import "SubmitReportData.h"
#import "ReportRejectionViewController.h"
#import "MobileAlertView.h"
#import "Config.h"

//static int traceLevel = 3;
//#define LOG_IF(level, x) { if(level<=traceLevel) x; }

#define		kBtnApproveTag		101777
#define		kBtnSendBackTag		101778

#define		kAlertViewApprove	101777
#define		kAlertViewText		101779
#define		kAlertViewSubmit	101780
#define		kAlertViewRateApp	101781
#define		kAlertViewRateAppApproval	101782

@implementation ApproveReportsViewControllerBase

@synthesize ivBack, lblBack, rpt, refreshView, isPad, isSubmitting;
@synthesize role, tableList, tableBackgroundImage;

NSString * const SUBMIT_ERROR_NO_ENTRY_MSG = @"SUBMIT_ERROR_NO_ENTRY_MSG";
NSString * const SUBMIT_ERROR_UNDEF_MSG = @"SUBMIT_ERROR_UNDEF_MSG";
NSString * const SUBMIT_ERROR_RPT_XCT_LEVEL_MSG = @"SUBMIT_ERROR_RPT_XCT_LEVEL_MSG";

#pragma mark Common Action Methods
-(BOOL) isApproving
{
	if (self.role == nil || ![@"MOBILE_EXPENSE_TRAVELER" isEqualToString:self.role])
		return TRUE;
	else 
		return FALSE;
}
-(BOOL) canSubmit
{
	//NSLog(@"self.rpt.apsKey = %@", self.rpt.apsKey);
	return !isSubmitting && self.rpt.apsKey != nil && ([self.rpt.apsKey isEqualToString:@"A_NOTF"] || [self.rpt.apsKey isEqualToString:@"A_RESU"]) && self.rpt.entries != nil && [self.rpt.entries count] > 0;
}

-(BOOL) canEdit
{
	return ![self isApproving] && (!isSubmitting && self.rpt != nil && (self.rpt.rptKey == nil || self.rpt.apsKey != nil && ([self.rpt.apsKey isEqualToString:@"A_NOTF"] || [self.rpt.apsKey isEqualToString:@"A_RESU"])));
}


// Process submit/approve/sendback action results
-(void) respondToFoundData:(Msg *)msg
{
	NSString * curRole = msg.parameterBag == nil? nil : (msg.parameterBag)[@"ROLE"];
	if (curRole != nil)
		self.role = curRole;
		
	if ([msg.idKey isEqualToString:APPROVE_REPORTS_DATA])
	{//this is the entry point for send back and also for approve
		[self setupToolbar];
		
		if (msg.errBody != nil) 
		{
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:msg.errCode
								  message:msg.errBody
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
								  otherButtonTitles:nil];
			[alert show];
		}
		else 
		{
            //[AppRating offerChoiceToRateApp:self alertTag:kAlertViewRateAppApproval];
            // DISABLE feedback manager for Gov
            if (![Config isGov])
            {
                [[FeedbackManager sharedInstance] requestRatingFromViewController:self withBlock:^{
                    [self afterChoiceToRateApp];
                }];
            }
		}
		
		[self hideWaitView];
	}
	else if ([msg.idKey isEqualToString:SUBMIT_REPORT_DATA]) 
	{//this is the entry point for submit
		isSubmitting = FALSE;

        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
        
		NSString* errMsg = msg.errBody;
		if (errMsg == nil && msg.responder != nil)
		{
			SubmitReportData* srd = (SubmitReportData*) msg.responder;
			if (srd.reportStatus != nil && srd.reportStatus.errMsg != nil)
			{
				if ([srd.reportStatus.status isEqualToString:@"not_an_approver"])
				{
					errMsg = [Localizer getLocalizedText:@"ERROR_ACCOUNT_NOT_CONFIGURED"];
				}
				else if ([srd.reportStatus.status isEqualToString:@"error.submit.missing_reqd_fields"])
				{
					errMsg = [Localizer getLocalizedText:@"ERROR_REQUIRED_FIELDS_MISSING"];
				}
				else
				{
					errMsg = srd.reportStatus.errMsg;
				}
			}
		}
		
		if (errMsg != nil) 
		{
            NSDictionary *dictionary = @{@"Success": @"No", @"Failure": errMsg};
            [Flurry logEvent:@"Reports: Submit" withParameters:dictionary];
            
			[self setupToolbar];

			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:[Localizer getLocalizedText:@"ERROR_SUBMIT_FAILED"]
								  message:errMsg
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
		}
		else 
		{
            NSDictionary *dictionary = @{@"Success": @"Yes"};
            [Flurry logEvent:@"Reports: Submit" withParameters:dictionary];
            
			//[AppRating offerChoiceToRateApp:self alertTag:kAlertViewRateApp];
            
            // DISABLE feedback manager for Gov
            if (![Config isGov])
            {
                [[FeedbackManager sharedInstance] requestRatingFromViewController:self withBlock:^{
                    [self afterChoiceToRateApp];
                }];
            }
		}
		
		// To refresh to get submit exception
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
									 [self getViewIDKey], @"TO_VIEW", nil];

		[[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORT_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
	
}


#pragma mark App Rating Methods
-(void)afterChoiceToRateAppApproval
{
    [[ExSystem sharedInstance].cacheData removeCache:APPROVE_REPORT_DETAIL_DATA UserID:[ExSystem sharedInstance].userName RecordKey:rpt.rptKey];
    NSMutableDictionary* parameterBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"POP_TO_ROOT_VIEW", nil];
    [ConcurMobileAppDelegate switchToView:APPROVE_REPORTS viewFrom:APPROVE_ENTRIES ParameterBag:parameterBag];
}


-(void)afterChoiceToRateApp
{

    
	[[ExSystem sharedInstance].cacheData removeCache:ACTIVE_REPORT_DETAIL_DATA UserID:[ExSystem sharedInstance].userName RecordKey:rpt.rptKey];
	 // TODO - update cache with response data
	 NSMutableDictionary *parameterBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"POP_TO_ROOT_VIEW", nil];
	 [ConcurMobileAppDelegate switchToView:ACTIVE_REPORTS viewFrom:ACTIVE_ENTRIES ParameterBag:parameterBag];
}


#pragma mark Toolbar Methods
-(void)setupToolbar
{
	if(![ExSystem connectedToNetwork])
	{
		[self makeOfflineBar];
	}
	else
	{
		UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
		flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
		
		NSMutableArray *toolbarItems = [NSMutableArray arrayWithCapacity:5];
		
		SEL leftSelector = @selector(actionReject:);
		SEL rightSelector = @selector(actionApprove:);
		
		// Mob-2517,2518 Localization of Send Back & Approve
		NSString* leftBtnLabel = [Localizer getLocalizedText:@"APPROVE_SENDBACK_BUTTON_TITLE"];
		NSString* rightBtnLabel = [Localizer getLocalizedText:@"APPROVE_APPROVE_BUTTON_TITLE"];
		
		if (![self isApproving])
		{
			leftSelector = nil;
			if ([self canSubmit] && !isPad)
			{
				rightSelector = @selector(actionSubmit:);
				// Mob-2516 Localization of Submit label text
				rightBtnLabel = [Localizer getLocalizedText:@"LABEL_SUBMIT_BTN"];
			}
			else 
			{
				rightSelector = nil;
			}
		}
		
		if (leftSelector != nil)
		{
			UIBarButtonItem *btnLeft = [[UIBarButtonItem alloc] initWithTitle:leftBtnLabel style:UIBarButtonItemStyleBordered target:self action:leftSelector];
			[toolbarItems addObject:btnLeft];
		}
		
		[toolbarItems addObject:flexibleSpace];
		
		if (rightSelector != nil)
		{
			UIBarButtonItem *btnRight = [[UIBarButtonItem alloc] initWithTitle:rightBtnLabel style:UIBarButtonItemStyleBordered target:self action:rightSelector];
			[toolbarItems addObject:btnRight];
		}
	//	[rootViewController.navigationController.toolbar setHidden:NO];
		[self.navigationController.toolbar setHidden:NO];
		[self setToolbarItems:toolbarItems animated:YES];
	}
	
}

-(void) setupRefreshingToolbar
{
	[self setupToolbarWithMessage:[Localizer getLocalizedText:@"Refreshing"] withActivity:YES];
}

-(void) setupToolbarWithMessage:(NSString*) msg withActivity:(BOOL) fAct
{
	if(![ExSystem connectedToNetwork])
	{
		[self makeOfflineBar];
		return;
	}
	
	UIView *rView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 150, 50)];
	
	UILabel *refreshText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 150, 50)];

	[refreshText setTextColor:[UIColor whiteColor]];
	[refreshText setText:msg];
	[refreshText setFont:[UIFont boldSystemFontOfSize:11.0f]];

	[refreshText setBackgroundColor:[UIColor clearColor]];
	[refreshText setTextAlignment:NSTextAlignmentCenter];
	[refreshText setNumberOfLines:0];
	
	[refreshText setShadowColor:[UIColor colorWithWhite:0.0f alpha:0.5f]];
	[refreshText setShadowOffset:CGSizeMake(0.0f, -1.0f)];
	
	if (fAct)
	{
		UIActivityIndicatorView *activity = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(65, 15, 20, 20)];
		activity.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin;
		[activity setHidesWhenStopped:YES];
		[activity setActivityIndicatorViewStyle:UIActivityIndicatorViewStyleWhiteLarge];
		[activity startAnimating];
		
		[rView addSubview:activity];
	}
	[rView addSubview:refreshText];
	
	
	UIBarButtonItem *titleItem = [[UIBarButtonItem alloc] initWithCustomView:rView];
	titleItem.tag = 999;
	
	SEL leftSelector = @selector(actionReject:);
	SEL rightSelector = @selector(actionApprove:);
	
	// Mob-2517,2518 Localization of Send Back & Approve
	NSString* leftBtnLabel = [Localizer getLocalizedText:@"APPROVE_SENDBACK_BUTTON_TITLE"];
	NSString* rightBtnLabel = [Localizer getLocalizedText:@"APPROVE_APPROVE_BUTTON_TITLE"];
	
	if (![self isApproving])
	{
		leftSelector = nil;
		if ([self canSubmit] && !isPad)
		{
			rightSelector = @selector(actionSubmit:);
			// Mob-2516 Localization of Submit label text
			rightBtnLabel = [Localizer getLocalizedText:@"LABEL_SUBMIT_BTN"];
		}
		else {
			rightSelector = nil;
		}
	}
	
	//[rootViewController.navigationController.toolbar setHidden:NO];
	[self.navigationController.toolbar setHidden:NO];
	
	NSMutableArray *toolbarItems = [NSMutableArray arrayWithCapacity:5];
	if (leftSelector != nil)
	{
		UIBarButtonItem *btnLeft = [[UIBarButtonItem alloc] initWithTitle:leftBtnLabel style:UIBarButtonItemStyleBordered target:self action:leftSelector];
		[toolbarItems addObject:btnLeft];
	}
	
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	[toolbarItems addObject:flexibleSpace];
	[toolbarItems addObject:titleItem];
	[toolbarItems addObject:flexibleSpace];

	
	if (rightSelector != nil)
	{
		UIBarButtonItem *btnRight = [[UIBarButtonItem alloc] initWithTitle:rightBtnLabel style:UIBarButtonItemStyleBordered target:self action:rightSelector];
		[toolbarItems addObject:btnRight];
	}
	
	[self setToolbarItems:toolbarItems animated:YES];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
	self.title = [Localizer getViewTitle:[self getViewIDKey]];

    [super viewDidLoad];

//	// Add Home button
//	UIBarButtonItem *homeButton = //[[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItem target:self action:@selector(buttonAddPressed:)];
//	[[[UIBarButtonItem alloc]
//	  initWithTitle:@"Home"
//	  style:UIBarButtonItemStylePlain
//	  target:self
//	  action:@selector(actionGoHome:)]
//	 autorelease];
//	[self.navigationItem setRightBarButtonItem:homeButton animated:NO];
	
	if(![UIDevice isPad])
	{
		[self addHomeButton];
	
		// Show breeze gradient background for grouped table
		//if (tableList != nil && tableList.style == UITableViewStyleGrouped)
		//	NSLog(@"Color %@", tableList.backgroundColor);
		//	[tableList setBackgroundColor:[UIColor clearColor]];
	}
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




#pragma mark SubmitNeedReceipts Delegate
- (void)cancelSubmitAfterReceipts
{
	[self dismissViewControllerAnimated:YES completion:nil];
}

- (void)confirmSubmitAfterReceipts
{
	[self dismissViewControllerAnimated:YES completion:nil];
	[self submitReport];
}

#pragma mark Wait and No Data Views
-(void)makeRefreshView
{
//	if([ExSystem isLandscape])
//		refreshView = [[UIView alloc] initWithFrame:CGRectMake(0, 200, 320, 40)];
//	else 
		refreshView = [[UIView alloc] initWithFrame:CGRectMake(0, 335, 480, 40)];
	
	refreshView.autoresizingMask = UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleRightMargin;
	refreshView.backgroundColor = [UIColor clearColor];
	refreshView.alpha = 0.5f;
	UIActivityIndicatorView *activity = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(142, 2, 37, 37)];
	activity.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleBottomMargin;
	[activity setHidesWhenStopped:YES];
	[activity setActivityIndicatorViewStyle:UIActivityIndicatorViewStyleWhiteLarge];
	[activity startAnimating];
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 2, 320, 37)];
	[lbl setText:[Localizer getLocalizedText:@"Refreshing"]];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setTextAlignment:NSTextAlignmentCenter];
	[lbl setFont:[UIFont boldSystemFontOfSize:18.0f]];
	[lbl setTextColor:[UIColor whiteColor]];
	[lbl setShadowColor:[UIColor colorWithWhite:0.0f alpha:0.5f]];
	[lbl setShadowOffset:CGSizeMake(0.0f, -1.0f)];
	lbl.autoresizingMask = UIViewAutoresizingFlexibleWidth;
	[refreshView addSubview:lbl];
	[refreshView addSubview:activity];
	refreshView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleBottomMargin;
	[refreshView setHidden:YES];
	
//	UIButton *btn = [UIButton buttonWithType:UIButtonTypeRoundedRect];
//	btn.frame = CGRectMake(110, 0, 100, 40);
//	[btn setTitle:@"Refreshing" forState:UIControlStateNormal];
//	//btn.backgroundColor = [UIColor whiteColor];
//	[refreshView addSubview:btn];
//	[btn release];
	[self.view addSubview:refreshView];
}

- (void) submitReport
{
	[self showWaitView];
	isSubmitting = TRUE;
	[self setupToolbar];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 ACTIVE_ENTRIES, @"TO_VIEW",  
								 self.rpt.rptKey, @"ID_KEY", 
								 nil];
	
	[[ExSystem sharedInstance].msgControl createMsg:SUBMIT_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	
}

#pragma mark -
#pragma mark ReportRejectionDelegate Methods

- (void)rejectedWithComment:(NSString*)comment
{
	//NSLog(@"rptKey = %@, procKey = %@, currentSeq = %@", rpt.rptKey, rpt.processInstanceKey, rpt.currentSequence);
	
	NSArray *toolbarItems = @[];
	[self setToolbarItems:toolbarItems animated:YES];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
								 APPROVE_ENTRIES, @"TO_VIEW", @"YES", @"SKIP_CACHE",comment, @"SendBackComment", nil];
	[[ExSystem sharedInstance].msgControl createMsg:APPROVE_REPORTS_DATA_REJECT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

- (void)rejectionCancelled
{
	// no op
}

#pragma mark -
#pragma mark Approval SendBack Sumit Methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if(alertView.tag == kAlertViewApprove & buttonIndex == 1)
	{
		NSArray *toolbarItems = @[];
		[self setToolbarItems:toolbarItems animated:YES];
		//NSLog(@"rptKey = %@, procKey = %@, currentSeq = %@", rpt.rptKey, rpt.processInstanceKey, rpt.currentSequence);
		
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
									 APPROVE_ENTRIES, @"TO_VIEW", @"YES", @"SKIP_CACHE", nil];
		//NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY", @"YES", @"SHORT_CIRCUIT", nil]
		[[ExSystem sharedInstance].msgControl createMsg:APPROVE_REPORTS_DATA_APPROVE CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
		
        [Flurry logEvent:@"Reports: Approve Report"];
		
	}
	else if(alertView.tag == kAlertViewApprove)
	{
		[self hideWaitView];
	}
	else if (alertView.tag == kAlertViewSubmit)
	{
		if (buttonIndex == 0){
			[self hideWaitView];
		}
		if (buttonIndex == 1){
			
			isSubmitting = TRUE;
			[self setupToolbar];
			[self showWaitView];
            
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
										 ACTIVE_ENTRIES, @"TO_VIEW", 
										 self.rpt.rptKey, @"ID_KEY", 
										 nil];
			
			[[ExSystem sharedInstance].msgControl createMsg:SUBMIT_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
		}
		
	}
}

-(IBAction) actionApprove:(id)sender
{
	NSString* nsQuestion = [Localizer getLocalizedText:@"APPROVE_QUESTION_AREYOUSURE"];
	UIAlertView* alert = [[MobileAlertView alloc] initWithTitle:
						  [Localizer getLocalizedText:@"APPROVE_PLEASE_CONFIRM"] 
													message:nsQuestion delegate:self cancelButtonTitle:
						  [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
										  otherButtonTitles:[Localizer getLocalizedText:@"APPROVE_OK"], 
						  nil];
	
	alert.tag = kAlertViewApprove;
	[alert show];
}


-(IBAction) actionReject:(id)sender
{
	ReportRejectionViewController *rejectionVC = [[ReportRejectionViewController alloc] initWithNibName:@"ReportRejectionViewController" bundle:nil];
	rejectionVC.reportRejectionDelegate = self;
	[self presentViewController:rejectionVC animated:YES completion:nil];
}

// Go directly to home page
-(IBAction) actionGoHome:(id)sender
{
    // MOB-10878 just popToRootView with either YES/NO fixed the crash
    [self.navigationController popToRootViewControllerAnimated:YES];
// The following code causes crash
//	parameterBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: @"YES", @"POP_TO_ROOT_VIEW", @"YES", @"DONTPUSHVIEW", nil];
//	[ConcurMobileAppDelegate switchToView:HOME_PAGE viewFrom:[self getViewIDKey] ParameterBag:parameterBag];
}

- (UILabel*) makeLabel: (CGRect) rect alert:(UIAlertView*) alert
{
	UILabel *label = [[UILabel alloc] initWithFrame:rect];
	label.font = [UIFont systemFontOfSize:12];
	label.textColor = [UIColor whiteColor];
	label.backgroundColor = [UIColor clearColor];
	label.shadowColor = [UIColor blackColor];
	label.shadowOffset = CGSizeMake(0,-1);
	label.lineBreakMode = NSLineBreakByWordWrapping;
	label.numberOfLines = 999;
	label.textAlignment = NSTextAlignmentLeft;
	[alert addSubview:label];
	return label;
}

// Submit the report
-(IBAction) actionSubmit:(id)sender
{
	// Test imageRequired to make sure we are using a reportDetail object; otherwise, we must wait for the report detail to come back
	// from the server
	if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle: [Localizer getLocalizedText:@"Cannot Submit Report"]
							  message: [Localizer getLocalizedText:@"OFFLINE_MSG"]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
		return;		
	}
	
	if (self.rpt == nil || self.rpt.imageRequired == nil)
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle: [Localizer getLocalizedText:@"Cannot Submit Report"]
							  message: [Localizer getLocalizedText:@"MSG_WAIT_FETCHING_INFO"]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
		return;	
	}
	
	if([ExSystem connectedToNetwork] && self.rpt != nil && self.rpt.imageRequired != nil)
	{
		// Check for no_entries, undefined expense type, & red flag exceptions before submit
// Mob-4145: We should not be blocking a submit for exceptions. On the web site, the users are not shown an alert or blocked from submitting the report on exceptions.
		NSString *cannotSubmitMsg = nil;
//		if (rpt.severityLevel != nil && [rpt.severityLevel isEqualToString:@"ERROR"] )
//		{
//			cannotSubmitMsg = [Localizer getLocalizedText:SUBMIT_ERROR_RPT_XCT_LEVEL_MSG];
//		}
		// Mob-4145 We still need to check for no entry or undefined
		if (rpt.entries == nil || [rpt.entries count] ==0)
		{
			cannotSubmitMsg = [Localizer getLocalizedText:SUBMIT_ERROR_NO_ENTRY_MSG];
		}
		else
		{
			NSMutableArray* rpeKeys = rpt.keys;
			NSMutableDictionary* entries = rpt.entries;
			for (int ix = 0; cannotSubmitMsg == nil && ix < [rpeKeys count]; ix++)
			{
				EntryData* entry = (EntryData*)entries[rpeKeys[ix]];
				if (entry != nil && (entry.expKey != nil && [entry.expKey isEqualToString:@"UNDEF"] ||
									 entry.expKey == nil && [entry.expName isEqualToString:@"Undefined"]))
				{
					cannotSubmitMsg = [Localizer getLocalizedText:SUBMIT_ERROR_UNDEF_MSG];
				}
			}
		}
		
		if (cannotSubmitMsg != nil)
		{
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle: [Localizer getLocalizedText:@"Cannot Submit Report"]
								  message: cannotSubmitMsg
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]  
								  otherButtonTitles:nil];
			[alert show];
			return;
		}
		
		NSString* receiptRequired = self.rpt.imageRequired;
		NSMutableArray *entriesNeedReceipt = [[NSMutableArray alloc] init];

		
		if (receiptRequired!= nil && [receiptRequired isEqualToString:@"Y"])
		{
			// We need to figure out entries needs receipts
			NSArray * keys = self.rpt.keys;
			NSDictionary * entries = self.rpt.entries;
			for (int ix = 0; ix < [keys count]; ix++)
			{
				NSString * key = (NSString*)keys[ix];
				EntryData*entry = (EntryData *) entries[key];
				
				// Logic adapted from SubmitWizard.js
				if ([@"Y" isEqualToString:entry.imageRequired] || [@"Y" isEqualToString: entry.receiptRequired])
				{
					if (entry.ereceiptId == nil &&  (entry.hasMobileReceipt == nil || [@"N" isEqualToString:entry.hasMobileReceipt])
						&& entry.receiptImageId == nil)
					{
						[entriesNeedReceipt addObject:entry];
					}
				}				
			}
		}
		
		NSString *alertTitle = [Localizer getLocalizedText:@"CONFIRM_REPORT_SUBMISSION"];
		NSString *alertMessage = [Localizer getLocalizedText:@"MSG_RECEIPT_NOT_REQUIRED"];
		NSString *cancelButtonText = [[NSString alloc] initWithString:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
		// Mob-2516 Localization of Submit label text
		NSString *submitButtonText = [Localizer getLocalizedText:@"LABEL_SUBMIT_BTN"];
		
		if ([entriesNeedReceipt count] >0)
		{
			// show modal dialog with entries need receipts
			SubmitNeedReceiptsViewController * dlg = [[SubmitNeedReceiptsViewController alloc] initWithNibName:@"SubmitNeedReceiptsView" bundle:nil];
			dlg.delegate = self;
			dlg.entryList = entriesNeedReceipt;
			[self presentViewController:dlg animated:YES completion:nil];
			
		} else {
			
			if (receiptRequired!= nil && [receiptRequired isEqualToString:@"Y"])
				alertMessage = [Localizer getLocalizedText:@"MSG_ADDITIONAL_RECEIPT_NOT_REQUIRED"];
			
			UIAlertView *alert = [[MobileAlertView alloc] initWithTitle: alertTitle 
															message: alertMessage
														   delegate: self 
												  cancelButtonTitle: cancelButtonText 
												  otherButtonTitles: submitButtonText, nil];
			alert.tag = kAlertViewSubmit;
			
			[alert show];
		}
	}
}


#define hStartX 285
#pragma mark Header Methods
-(void)drawHeaderRpt:(id)thisObj HeadLabel:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt LabelLine1:(UILabel *)labelLine1 LabelLine2:(UILabel *)labelLine2 
HeadBackground:(UIImageView *) ivHeadBack
{
	ReportData* thisRpt = (ReportData*) thisObj;
	if ([self isApproving])
	{
		headLabel.text = thisRpt.employeeName;
		headLabelAmt.text = [FormatUtils formatMoney:thisRpt.totalPostedAmount crnCode:thisRpt.crnCode];
		labelLine1.text = thisRpt.reportName;
	}
	else 
	{
		headLabel.text = thisRpt.reportName;
		headLabelAmt.text = [FormatUtils formatMoney:thisRpt.totalPostedAmount crnCode:thisRpt.crnCode];
		labelLine1.text = thisRpt.apvStatusName;
	}

	int startX = self.view.frame.size.width - 35 + 24 - kIconSize;//hStartX + 24 - kIconSize;
	
//	if ([ExSystem isLandscape]) 
//	{
//		startX = 445 + 24 - kIconSize;
//	}

	[UtilityMethods drawNameAmountLabelsOrientationAdjustedWithResize:headLabel AmountLabel:headLabelAmt LeftOffset:10 RightOffset:10 
													  Width:(int)self.view.frame.size.width];
	int iImagePos = 0;
	
	int imageWidth = kIconSize + 3;
	int imgHW = kIconSize;
	int y = 21;
	int x = startX;
	
	for (UIImageView *iView in [self.view subviews]) 
	{
		if (iView.tag >= 900) 
		{
			[iView removeFromSuperview];
		}
	}
	
	if((thisRpt.hasException != nil && [thisRpt.hasException isEqualToString:@"Y"]) || [thisRpt.exceptions count] > 0)
	{
		BOOL showAlert = [rpt.severityLevel isEqualToString:@"ERROR"];
		if (showAlert == NO)
			for(ExceptionData *ed in thisRpt.exceptions)
			{
				//NSLog(@"ed severityLevel = %@", ed.severityLevel);
				if([ed.severityLevel isEqualToString:@"ERROR"])
				{
					showAlert = YES;
					break;
				}
			}
				
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		if (showAlert) 
			[imgBack setImage:[UIImage imageNamed:@"alert_24X24_PNG.png"]];
		else 
			[imgBack setImage:[UIImage imageNamed:@"warning_24X24_PNG.png"]];
		imgBack.tag = 900;
		imgBack.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin;
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if(thisRpt.receiptImageAvailable != nil && [thisRpt.receiptImageAvailable isEqualToString:@"Y"])
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"view_receipts_24X24_PNG.png"]];
		imgBack.tag = 900;
		imgBack.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin;
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if(thisRpt.everSentBack != nil && [thisRpt.everSentBack isEqualToString:@"Y"])
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"resubmit_24X24_PNG.png"]];
		imgBack.tag = 900;
		imgBack.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin;
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if(thisRpt.lastComment != nil)
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"comments_24X24_PNG.png"]];
		imgBack.tag = 901;
		imgBack.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin;
		[self.view addSubview:imgBack];
//		iImagePos++;
	}
	
	labelLine1.frame = CGRectMake(10, 23, x - 10, 21);
}


#define hStartX 285
#pragma mark Entry Header Methods
-(void)drawHeaderEntry:(EntryData *)thisEntry HeadLabel:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt LabelLine1:(UILabel *)labelLine1 LabelLine2:(UILabel *)labelLine2 
HeadBackground:(UIImageView *) ivHeadBack
{
	NSString *amt = [FormatUtils formatMoney:thisEntry.transactionAmount crnCode:thisEntry.transactionCrnCode];
	headLabel.text = thisEntry.expName;
	headLabelAmt.text = amt;
	labelLine1.text = [CCDateUtilities formatDateToMMMddYYYFromString:thisEntry.transactionDate];
	
	/*
	NSString *vendor = @"";
	NSString *locationName = @"";
	
	if(thisEntry.vendorDescription != nil)
		vendor = [NSString stringWithFormat:@"%@, ", thisEntry.vendorDescription];
	else if(thisEntry.vendorDescription != nil)
		vendor = [NSString stringWithFormat:@"%@", thisEntry.vendorDescription];
	
	if(thisEntry.locationName != nil)
		locationName = thisEntry.locationName;
	
	labelLine2.text = [NSString stringWithFormat:@"%@%@", vendor, locationName];
	*/
	labelLine2.text = [self getVendorString:thisEntry.vendorDescription WithLocation:(NSString*) thisEntry.locationName];
	
	int startX = self.view.frame.size.width - 35 + 24 - kIconSize;//hStartX + 24 - kIconSize;
	[UtilityMethods drawNameAmountLabelsOrientationAdjustedWithResize:headLabel AmountLabel:headLabelAmt LeftOffset:10 RightOffset:10 
													  Width:(int)self.view.frame.size.width];

//	if ([ExSystem isLandscape]) 
//	{
//		startX = 445 + 24 - kIconSize;
//	}

	int iImagePos = 0;
	
	int imageWidth = kIconSize+3;
	int imgHW = kIconSize;
	int y = 21;
	int x = startX;
	
	for (UIImageView *iView in [self.view subviews]) 
	{
		if (iView.tag >= 900) 
		{
			[iView removeFromSuperview];
		}
	}
	
	//exception, comments, credit card, itemization, attendees
	if((thisEntry.hasExceptions != nil && [thisEntry.hasExceptions isEqualToString:@"Y"]) || [thisEntry.exceptions count] > 0)
	{
		BOOL showAlert = NO;
		for(ExceptionData *ed in thisEntry.exceptions)
		{
			//NSLog(@"ed severityLevel = %@", ed.severityLevel);
			if([ed.severityLevel isEqualToString:@"ERROR"])
			{
				showAlert = YES;
				break;
			}
		}
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		if(showAlert)
			[imgBack setImage:[UIImage imageNamed:@"alert_24X24_PNG.png"]];
		else 
			[imgBack setImage:[UIImage imageNamed:@"warning_24X24_PNG.png"]];
		imgBack.tag = 901;
		imgBack.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin;
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if(thisEntry.hasComments != nil && [thisEntry.hasComments isEqualToString:@"Y"])
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"comments_24X24_PNG.png"]];
		imgBack.tag = 902;
		imgBack.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin;
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if(thisEntry.isCreditCardCharge != nil && [thisEntry.isCreditCardCharge isEqualToString:@"Y"])
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"amex_24X24.png"]];
		imgBack.tag = 903;
		imgBack.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin;
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if(thisEntry.isItemized != nil && [thisEntry.isItemized isEqualToString:@"Y"])
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"itemization_24X24_PNG.png"]];
		imgBack.tag = 904;
		imgBack.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin;
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if(thisEntry.hasAttendees != nil && [thisEntry.hasAttendees isEqualToString:@"Y"])
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"attendees_24X24_PNG.png"]];
		imgBack.tag = 905;
		imgBack.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin;
		[self.view addSubview:imgBack];
		iImagePos++;
	}
	
	if(thisEntry.isPersonal != nil && [thisEntry.isPersonal isEqualToString:@"Y"])
	{
		x = startX - (iImagePos * imageWidth);
		CGRect myImageRect = CGRectMake(x, y, imgHW, imgHW);
		UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
		[imgBack setImage:[UIImage imageNamed:@"personal_expense_24X24_PNG.png"]];
		imgBack.tag = 906;
		imgBack.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin;
		[self.view addSubview:imgBack];
//		iImagePos++;
	}
	
	labelLine1.frame = CGRectMake(10, 20, x - 10, 11);
	labelLine2.frame = CGRectMake(10, 31, x - 10, 11);
	
	labelLine1.autoresizingMask = UIViewAutoresizingFlexibleWidth;
	labelLine2.autoresizingMask = UIViewAutoresizingFlexibleWidth;
}


- (NSString*) getVendorString:(NSString*) vendor WithLocation:(NSString*) locationName
{
	vendor = [vendor stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
	locationName = [locationName stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
	
	if ((vendor == nil||[vendor length]==0) && (locationName == nil || [locationName length]==0))
	{
		return @"";
	} else if (vendor == nil||[vendor length]==0)
	{
		return locationName;
	} else if (locationName == nil|| [locationName length]==0)
	{
		return vendor;
	} else {
		//NSLog(@"Vendor '%@' length = %d", vendor, [vendor length]);
		return [NSString stringWithFormat:@"%@, %@", vendor, locationName];
	}
}

#pragma mark -
#pragma mark Entry Cell Creation
-(void)makeEntryCell:(ReportApprovalListCell *)cell Entry:(EntryData *)entry
{	
	cell.lblAmount.font = [UIFont boldSystemFontOfSize:14];
	cell.lblName.font = [UIFont boldSystemFontOfSize:14];
	
	cell.lblName.text = entry.expName;
	if ([entry.expKey isEqualToString:@"UNDEF"])
	{
		cell.lblName.textColor = [UIColor redColor];
	}
	else {
		cell.lblName.textColor = [UIColor blackColor];
	}

	cell.lblAmount.text = [FormatUtils formatMoney:entry.transactionAmount crnCode:entry.transactionCrnCode];
	// Entry date is local, not UTC
	cell.lblLine1.text = [DateTimeFormatter formatLocalDateMedium:entry.transactionDate];
	
	[UtilityMethods drawNameAmountLabels:cell.lblName AmountLabel:cell.lblAmount];
	
	[cell setAccessoryType:UITableViewCellAccessoryNone];
	
	cell.lblLine2.text = [self getVendorString:entry.vendorDescription WithLocation:entry.locationName];

	int iImagePos = 0;
	int startX = 290;
	int imageWidth = 27;
	int x = 0;
	
	if([ExSystem isLandscape])
		startX = 434;
	
	[cell clearAllImagesInCell];

	//exception, comments, credit card, itemization, attendees
	if((entry.hasExceptions != nil && [entry.hasExceptions isEqualToString:@"Y"]) || [entry.exceptions count] > 0)
	{
		BOOL showAlert = NO;
		for(ExceptionData *ed in entry.exceptions)
		{
			//NSLog(@"ed severityLevel = %@", ed.severityLevel);
			if([ed.severityLevel isEqualToString:@"ERROR"])
			{
				showAlert = YES;
				break;
			}
		}
		
		if(showAlert)
			[cell setImageByPosition:iImagePos imageName:@"alert_24X24_PNG"];
		else 
			[cell setImageByPosition:iImagePos imageName:@"warning_24X24_PNG"];
		iImagePos++;
	}
	
	if(entry.hasMobileReceipt != nil && [entry.hasMobileReceipt isEqualToString:@"Y"])
	{
		[cell setImageByPosition:iImagePos imageName:@"view_receipts_24X24_PNG"];
		iImagePos++;
	}
	
	if(entry.hasComments != nil && [entry.hasComments isEqualToString:@"Y"])
	{
		[cell setImageByPosition:iImagePos imageName:@"comments_24X24_PNG"];
		iImagePos++;
	}
	
	//NSLog(@"entry.isCreditCardCharge = %@", entry.isCreditCardCharge);
	if(entry.isCreditCardCharge != nil && [entry.isCreditCardCharge isEqualToString:@"Y"])
	{
		[cell setImageByPosition:iImagePos imageName:@"amex_24X24"];
		iImagePos++;
	}

	// Personal card and corporate card is mutually exclusive, so total of 4 images max.
	if(entry.isPersonalCardCharge != nil && [entry.isPersonalCardCharge isEqualToString:@"Y"])
	{
		[cell setImageByPosition:iImagePos imageName:@"visa_24X24_PNG"];
		iImagePos++;
	}
	
	
	if(entry.isItemized != nil && [entry.isItemized isEqualToString:@"Y"])
	{
		[cell setImageByPosition:iImagePos imageName:@"itemization_24X24_PNG"];
		iImagePos++;
	}
	
	if(entry.hasAttendees != nil && [entry.hasAttendees isEqualToString:@"Y"])
	{
		[cell setImageByPosition:iImagePos imageName:@"attendees_24X24_PNG"];
		iImagePos++;
	}
	
	if(entry.isPersonal != nil && [entry.isPersonal isEqualToString:@"Y"])
	{
		[cell setImageByPosition:iImagePos imageName:@"personal_expense_24X24_PNG"];
		iImagePos++;
	}
	
	x = startX - (iImagePos * imageWidth);
	cell.lblLine2.autoresizingMask = UIViewAutoresizingNone;
	cell.lblLine2.frame = CGRectMake(5, 31, x - 20, 21);
}

-(UITableViewCell *) makeDrillCell:(UITableView*)tblView withText:(NSString*)command withImage:(NSString*)imgName enabled:(BOOL)flag
{
	UITableViewCell *cell = [tblView dequeueReusableCellWithIdentifier:@"DrillData"];
	if (cell == nil) 
	{
        // initWithFrame:resuseIdentifier: is deprecated
		//cell = [[UITableViewCell alloc] initWithFrame:CGRectZero reuseIdentifier:@"DrillData"];
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault  reuseIdentifier:@"DrillData"];
	}
	
	for (UIImageView *iView in [cell.contentView subviews]) 
	{
		if (iView.tag >= 900) 
		{
			[iView removeFromSuperview];
		}
	}
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(35 - (24-kIconSize)/2, 0, 240, 40)];
	
	[lbl setText:command]; 
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setTextAlignment:NSTextAlignmentLeft];
	[lbl setFont:[UIFont boldSystemFontOfSize:16.0f]];
	[lbl setTextColor:[UIColor blackColor]];
	[lbl setHighlightedTextColor:[UIColor whiteColor]];
	lbl.numberOfLines = 1;
	lbl.lineBreakMode = NSLineBreakByTruncatingTail;
	lbl.tag = 990;
	[cell.contentView addSubview:lbl];

	float height = 40;
	CGRect myImageRect = CGRectMake(5, (height - kIconSize) / 2, kIconSize, kIconSize);
	UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
	[imgBack setImage:[UIImage imageNamed:imgName]];
	imgBack.tag = 900;
	[cell.contentView addSubview:imgBack];
	
	if (flag)
		[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
	return cell;
}

-(void) addHomeButton
{
	UIBarButtonItem *btnHome = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"home20.png"] 
																style:UIBarButtonItemStylePlain target:self action:@selector(actionGoHome:)];
	self.navigationItem.rightBarButtonItem = nil;
	[self.navigationItem setRightBarButtonItem:btnHome animated:NO];
}
@end
