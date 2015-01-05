//
//  OutOfPocketListViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "OutOfPocketListViewController.h"
#import "ExSystem.h" 

#import "OutOfPocketData.h"
#import "OOPEntry.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"
#import "OutOfPocketDeleteData.h"
#import "SelectReportViewController.h"
#import "ConcurMobileAppDelegate.h"
#import "CCardTransaction.h"
#import "PCardTransaction.h"
#import "LabelConstants.h"
#import "SmartExpenseManager.h"
#import "AttendeeManager.h"
#import "MobileAlertView.h"
#import "DateTimeConverter.h"
#import "ReportDetailViewController.h"
#import "MobileActionSheet.h"
#import "ReportDetailViewController_iPad.h"
#import "UploadQueue.h"
#import "UploadQueueViewController.h"
#import "ViewConstants.h"
#import "UserConfig.h"
#import "NSStringAdditions.h"

@implementation OutOfPocketListViewController
@synthesize allKeys, curKeys, pCards, pCardKeys, curFilter, rpt, bannerAdjusted;
@synthesize tableList, oopeDict, ivBack, lblBack, titleLabel, showedNo, selectedRows, drewEdit, doReload, inDeleteMode,padHomeVC, isShowingNextController;
@synthesize inAddToReportMode, isQuickPush, currentItemCount;

const NSInteger EDITING_HORIZONTAL_OFFSET = 35;

NSString* const FILTER_ALL = @"ALL";
NSString* const FILTER_CORP_CARDS = @"CORP_CARDS";

#define kAlertViewAddToReport 102091
#define kAlertViewAddToReportWithSmartExpense 102092
#define kAlertViewAddSmartExpensesToReport 102093
#define kRefreshingTag 102888

#pragma mark - Mobile View Controller Methods
-(NSString *)getViewIDKey
{
	return OUT_OF_POCKET_LIST;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:DOWNLOAD_USER_CONFIG])
        return;
    
	if([msg.idKey isEqualToString:UPLOAD_IMAGE_DATA] )
	{
        if ([self isViewLoaded]) 
        {
            [self setupRefreshingToolbar:[Localizer getLocalizedText:@"Receipt uploaded"] ShowActivity:NO];
        }
		
		//MOB-1854
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW", @"N", @"REFRESH_TOOLBAR", nil];
		[[ExSystem sharedInstance].msgControl createMsg:OOPES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
//		[tableView reloadData];
	}
	else if ([msg.idKey isEqualToString:@"FORCE_FETCH"] && [self isViewLoaded]) 
	{
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW", nil];
		[[ExSystem sharedInstance].msgControl createMsg:OOPES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
	else if ([msg.idKey isEqualToString:OOPES_DATA])
	{
		if ([self isViewLoaded])
		{
			[self hideWaitView];
//			MOB-3371: I don't know why we have to set it to NO for editing.  If an image is is done uploading and we are in edit then we need to stay in edit.
//				[tableView setEditing:NO];
		}
		OutOfPocketData *oopData = (OutOfPocketData *)msg.responder;
		self.oopeDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];

		self.pCards = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		self.pCardKeys = [[NSMutableArray alloc] init];
		if (oopData.pCards != nil)
		{
			NSArray* allCards = [oopData.pCards allValues];
			for (int ix = 0; ix < [allCards count]; ix++)
			{
				PersonalCardData * card = (PersonalCardData*) allCards[ix];
				NSString* pcaKey = card.pcaKey;
				[pCardKeys addObject:pcaKey];
				pCards[pcaKey] = card;
			}
		}
		
		NSMutableArray *aSort = [[NSMutableArray alloc] initWithObjects:nil];
		
		for(NSString *key in oopData.oopes)
		{
			[aSort addObject:(oopData.oopes)[key]];
		}
		
		//oopData.oope.tranDate
		NSSortDescriptor *aSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"tranDate" ascending:NO];
		[aSort sortUsingDescriptors:@[aSortDescriptor]];
		
		[self.allKeys removeAllObjects];
		
		for(int i = 0; i < [aSort count]; i++)
		{
			OOPEntry* e = (OOPEntry*)aSort[i];
			NSString *key = [e getIdKey];
			[self.allKeys addObject:key];
			(self.oopeDict)[key] = e;
		}		
		NSMutableDictionary * pBag = msg.parameterBag;
		if (pBag != nil)
		{
			if (self.rpt == nil)
			{
				self.rpt = pBag[@"REPORT"];
				if (self.rpt != nil && inAddToReportMode)
				{
					[self buttonAddToReportOnePressed:nil];
				}
			}
			NSString* pcaKey = pBag[@"FILTER"];
			if (pcaKey != nil)
			{
				self.curFilter = pcaKey;
			}
		}
		[self filterEntries];
		
		//MOB-1854
		BOOL refreshToolbar = YES; 
		if (pBag != nil)
		{
			NSString* strRefreshTb = pBag[@"REFRESH_TOOLBAR"];
			if (strRefreshTb != nil && [strRefreshTb isEqualToString:@"N"])
				refreshToolbar = NO;
		}
		
		if (refreshToolbar && [self isViewLoaded])
			[self configureToolBar];
        
        NSDictionary *dictionary = @{@"Mobile Entry Count": [NSString stringWithFormat:@"%lu", (unsigned long)[oopData.oopes count]], @"Card Count": [NSString stringWithFormat:@"%lu", (unsigned long)[oopData.pCards count]], @"Receipt Count": @""};
        [Flurry logEvent:@"Mobile Entry: List" withParameters:dictionary];
	}
	else if ([msg.idKey isEqualToString:DELETE_OOP_DATA])
	{
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
        
		if (msg.parameterBag != nil && [PCT_TYPE isEqualToString:(NSString*)(msg.parameterBag)[@"TYPE"]])
		{
			deletePCTReturned = TRUE;
		}
		else if (msg.parameterBag != nil && [CCT_TYPE isEqualToString:(NSString*)(msg.parameterBag)[@"TYPE"]])
		{
			deleteCCTReturned = TRUE;
		}
		else 
		{
			deleteMEReturned = TRUE;
		}
		
		OutOfPocketDeleteData *delData = (OutOfPocketDeleteData *)msg.responder;
		NSMutableDictionary *delDict = delData.returnFailures;
		for(NSString *key in delDict)
		{
//			NSMutableDictionary *returnInfo = [delDict objectForKey:key];
//			if([returnInfo objectForKey:@"STATUS"] != nil & ([[returnInfo objectForKey:@"STATUS"] isEqualToString:@"FAILURE"]))
//			{
//				//throw errors
//			}
		}

		if (deleteMEReturned && deletePCTReturned && deleteCCTReturned)
		{
			// Let's refresh when both deletion returned
			[selectedRows removeAllObjects];
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW", nil];
			[[ExSystem sharedInstance].msgControl createMsg:OOPES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
		}
		
		
		//let's refresh the cache for summary data
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
		[[ExSystem sharedInstance].msgControl createMsg:SUMMARY_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
		
		inDeleteMode = NO; // After deleting items, we are no longer in delete mode
	}
	else if ([msg.idKey isEqualToString:ADD_TO_REPORT_DATA])
	{
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
		
		AddToReportData *auth = (AddToReportData *)msg.responder;
		NSString* errMsg = msg.errBody;
		if (msg.errBody == nil && msg.responseCode != 200)
        {
            errMsg = [Localizer getLocalizedText:@"Failed to add to report"];
        }
        
        if (errMsg != nil) 
		{
			MobileAlertView *alert = [[MobileAlertView alloc] 
                                      initWithTitle:[Localizer getLocalizedText:@"Connection Error"]
                                      message:msg.errBody
                                      delegate:nil 
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]  
                                      otherButtonTitles:nil];
			[alert show];
		} 
		else
		{
            self.rpt = auth.rpt;
            if ([auth hasFailedEntry])
            {
                MobileAlertView *alert = [[MobileAlertView alloc] 
                                      initWithTitle:[Localizer getLocalizedText:@"Import Error"]
                                      message:[Localizer getLocalizedText:@"Failed to import entry"]
                                      delegate:nil 
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]  
                                      otherButtonTitles:nil];
                [alert show];

            }
            [self goToReportDetailScreen];
        }
	}		
	else if (msg.parameterBag != nil && (msg.parameterBag)[@"DID_SPLIT_SMART_EXPENSE"] != nil)
	{
		[self filterEntries];
		if ([self isViewLoaded])
			[self.tableList reloadData];	
	}
	
	if ([curKeys count] < 1 || curKeys == nil) 
	{//show we gots no data view
		if ([self isViewLoaded])
		{
            [self.navigationController setToolbarHidden:YES];
			[self showNoDataView:self];
			
            if (bannerAdjusted)
            {
                self.bannerAdjusted = NO;       //re-insert upload banner over the no data view
                [self adjustViewForUploadBanner];
            }
		}
	}
	else if (curKeys != nil & [curKeys count] > 0)
	{//refresh from the server, after an initial no show...
		if ([self isViewLoaded])
		{
			[self hideNoDataView];
            self.navigationController.toolbarHidden = NO;
                        
			if(!tableList.isEditing)
				[self makeBtnEdit];
		}
	}
}


#pragma mark - View Controller methods
-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:YES];
    self.navigationController.toolbarHidden = NO;
    int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
    if (itemNum > 0)
    {
        [self makeUploadView];
        [self adjustViewForUploadBanner];
    }
    else
        [self adjustViewForNoUploadBanner];
    
    // MOB-9786 Get NoShow custom names when add from 
    if ([UserConfig getSingleton] == nil)
		[[ExSystem sharedInstance].msgControl createMsg:DOWNLOAD_USER_CONFIG CacheOnly:@"NO" ParameterBag:nil SkipCache:YES];
}


- (void)viewDidAppear:(BOOL)animated 
{
	[super viewDidAppear:animated];

	self.isShowingNextController = NO;
	
	if([UIDevice isPad])
	{
		[self loadExpenses];
		[ExSystem sharedInstance].sys.topViewName = [self getViewIDKey]; //settings.topViewName
	}
	
	[tableList reloadData];
//    if(isQuickPush)
//    {
//        [self performSelector:@selector(pushQuick:) withObject:nil afterDelay:0.05f];
        
}


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
    [super viewDidLoad];
    [self loadExpenses];
    [ExSystem sharedInstance].sys.topViewName = [self getViewIDKey]; //settings.topViewName
    
	if([UIDevice isPad])
	{
		self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
		self.navigationItem.leftBarButtonItem =  btnClose;
	}
	
	self.navigationController.toolbarHidden = NO;
	
    self.curKeys = [[NSMutableArray alloc] init];

    self.allKeys = [[NSMutableArray alloc] init];
	
    self.oopeDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    
    self.pCards = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];

    self.pCardKeys = [[NSMutableArray alloc] init];
	
	[self.tableList setAllowsSelectionDuringEditing:YES];
	
    self.selectedRows = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		
	if (curKeys != nil && [curKeys count] > 0)
		[self makeBtnEdit];

    UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
    self.navigationItem.rightBarButtonItem = btnAdd;
    
    self.bannerAdjusted = NO;
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	[tableList reloadData];
}

-(void) didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation 
{
	[tableList reloadData];
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

-(void)viewWillDisappear:(BOOL)animated 
{ 
	//	self.messageTextString = textEdit.text;
	[super viewWillDisappear:animated]; 
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    NSLog(@"LOW MEMORY WARNNG from OOP List View");
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark -
#pragma mark Save State on Memory Blow Out
//-(void)viewWillAppear:(BOOL)animated 
//{ 
//	if(doReload)
//	{
//		doReload = NO;
//		if(curKeys == nil || [curKeys count] == 0)
//		{
//			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW", nil];
//			[[ExSystem sharedInstance].msgControl createMsg:OOPES_DATA CacheOnly:@"YES" ParameterBag:pBag SkipCache:NO RespondTo:self];
//			[pBag release];
//		}
//		
//		
//		[tableList reloadData];
//		[self.tableList setAllowsSelectionDuringEditing:YES];
//		self.selectedRows = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
//		[self.selectedRows release];
//	}
//	
//	[self configureToolBar];
//
//	[super viewWillAppear:animated]; 
//}



#pragma mark - Action Methods
-(void)buttonActionPressed:(id)sender
{
	NSMutableArray* cardNames = [[NSMutableArray alloc] init];
								 
	[cardNames addObject:[Localizer getLocalizedText:@"All Expenses"]];
	for (int ix = 0; pCardKeys != nil && ix < [pCardKeys count]; ix++)
	{
		NSString* pcaKey = (NSString*)pCardKeys[ix];
		PersonalCardData * card = (PersonalCardData*) pCards[pcaKey];
		NSString* cardName = card.cardName;
		[cardNames addObject:cardName];		
	}

	BOOL hasCct = FALSE;
	for(NSString *key in self.oopeDict)
	{
		if ([oopeDict[key] isCorporateCardTransaction])
		{
			hasCct = TRUE;
			break;
		}
	}
	
	if (hasCct)
	{
		[cardNames addObject:[Localizer getLocalizedText:@"Corporate Card"]];
	}
		
	UIActionSheet * filterAction = nil;

	filterAction = [[MobileActionSheet alloc] initWithTitle:nil//[Localizer getLocalizedText:@"Required Fields Missing"]
												 delegate:self 
										cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
								   destructiveButtonTitle:nil
									  otherButtonTitles:cardNames[0],
									[cardNames count]<2?nil:cardNames[1],
					[cardNames count]<3?nil:cardNames[2],
					[cardNames count]<4?nil:cardNames[3],
					[cardNames count]<5?nil:cardNames[4], nil
					];

	
	
	if([UIDevice isPad])
		[filterAction showFromBarButtonItem:sender animated:YES];
	else 
	{
		filterAction.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
		[filterAction showFromToolbar:[ConcurMobileAppDelegate getBaseNavigationController].toolbar];
	}
	 
}

- (void) refreshFilter
{
    [self filterEntries];
	[self doConfigureToolBar:nil];
	if (curKeys == nil || [curKeys count] < 1) 
	{//show we gots no data view
        [self.navigationController setToolbarHidden:YES];
		[self showNoDataView:self];
		self.navigationItem.rightBarButtonItem = nil;
	}
	else {
		[self hideNoDataView];
		if(!tableList.isEditing)
			[self makeBtnEdit];
	}
}

- (void)actionSheet:(UIActionSheet *)actionSheet
clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if (buttonIndex == actionSheet.cancelButtonIndex)
		return;
	
	if (buttonIndex == 0)
	{
		self.curFilter = FILTER_ALL;
	} 
	else if (buttonIndex > [pCardKeys count])
	{
		self.curFilter = FILTER_CORP_CARDS;
	}
	else
	{
		self.curFilter = pCardKeys[buttonIndex-1];
	}

    [self refreshFilter];
}


#pragma mark - Filter Stuff
- (void)filterEntries
{
	NSString* cardName = nil;

	if (curFilter != nil && [curFilter isEqualToString:FILTER_CORP_CARDS])
	{
		// Mob-3109
		self.title = [NSString stringWithFormat:@"%@ - %@", [Localizer getLocalizedText:@"LABEL_EXPENSES"],[Localizer getLocalizedText:@"Corporate Cards"]];		
	}
	else if (curFilter != nil && ![curFilter isEqualToString:FILTER_ALL])
	{
		PersonalCardData * card = (PersonalCardData*) pCards[curFilter];
		cardName = card.cardName;
		self.title = [NSString stringWithFormat:@"%@ - %@",[Localizer getLocalizedText:@"LABEL_EXPENSES"], cardName];
	}
	else
	{
		// Mob-2512,2513 Localization of Expenses header title
		self.title = [Localizer getViewTitle:OUT_OF_POCKET_LIST];
	}
	
	[curKeys removeAllObjects];
	for(int i = 0; i < [allKeys count]; i++)
	{
		NSString* key = (NSString*)allKeys[i];
		OOPEntry * e = (OOPEntry*) oopeDict[key];
		
		if (![[SmartExpenseManager getInstance] doesMobileEntryBelongToIntactSmartExpense:e])
		{
			if (self.curFilter == nil|| [self.curFilter isEqualToString:FILTER_ALL])
			{
				[curKeys addObject:key];
			}
			else if ([e isCorporateCardTransaction] && self.curFilter != nil && [self.curFilter isEqualToString:FILTER_CORP_CARDS])
			{
				[curKeys addObject:key];
			}
			else if ([e isPersonalCardTransaction])
			{
				PCardTransaction* pct = (PCardTransaction*) e;
				NSString* pcaKey = pct.pcaKey;
				NSString* pCardName = pct.cardName;
				
				if ((pcaKey != nil && [pcaKey isEqualToString:self.curFilter])
					||(pCardName != nil && [pCardName isEqualToString:cardName]))
				{
					[curKeys addObject:key];
				}
			}
		}
	}
	
	if ([self isViewLoaded])
		[self.tableList reloadData];	
}


#pragma mark - Add New Quick Expense
-(void)buttonAddPressed:(id)sender
{
    NSDictionary *dictionary = @{@"Came From": @"Expense List"};
    [Flurry logEvent:@"Mobile Entry: Create2" withParameters:dictionary];
    
    QEFormVC *formVC = [[QEFormVC alloc] initWithEntryOrNil:nil];
    [self.navigationController pushViewController:formVC animated:YES];
    self.curFilter = FILTER_ALL;
}



#pragma mark - Edit Methods
-(void)buttonEditPressed:(id)sender
{
	inDeleteMode = YES;
	
	NSString *cancel = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	[self.tableList setEditing:YES animated:YES];
	
	UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:cancel style:UIBarButtonItemStyleBordered target:self action:@selector(buttonCancelPressed:)];
	self.navigationItem.rightBarButtonItem = nil;
    self.navigationItem.rightBarButtonItem = btnCancel;
	
	if([ExSystem connectedToNetwork])
	{
		[self makeDeleteButton:0];
	}
	else 
		[self makeOfflineBar];

	
	[tableList reloadData];
}

-(void) makeBtnEdit
{
}


-(void)buttonCancelPressed:(id)sender
{
	[tableList setEditing:NO];
	[self makeBtnEdit];
	inDeleteMode = NO;
	inAddToReportMode = NO;
    UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
    self.navigationItem.rightBarButtonItem = btnAdd;

    
	UIBarButtonItem *btnAction = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(buttonActionPressed:)];
	UIBarButtonItem *btnAddToReport = [self makeAddToReportButton:-2];
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
    UIBarButtonItem *btnEdit = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Edit"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonEditPressed:)];
    
	NSArray *toolbarItems = @[btnEdit, flexibleSpace, btnAction, flexibleSpace, btnAddToReport];
	[self setToolbarItems:toolbarItems animated:YES];
	
	[selectedRows removeAllObjects];
	[tableList reloadData];
}


#pragma mark - Alert View Delegate Methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	[self hideWaitView];
	if (alertView.tag == kAlertViewAddToReport)
	{
		if (buttonIndex == 1)
			[self goToSelectReport];
	}
	else if (alertView.tag == kAlertViewAddSmartExpensesToReport)
	{
		if (buttonIndex == 1)
			[self goToSelectReport];
	}
	else
	{
		if(buttonIndex == 1)
			[self buttonDeleteSelectedPressed:self];
		
	}
}


-(void)confirmDelete:(id)sender
{
	if([selectedRows count] <= 0)
		return;
	
	SmartExpenseManager *smartExpenseManager = [SmartExpenseManager getInstance];
	BOOL hasSmartExpense = NO;
	for (NSString *key in selectedRows) 
	{
		int x = [key intValue];
		NSString *killKey = curKeys[x];
		OOPEntry* obj = (OOPEntry*)oopeDict[killKey];
		if ([obj isPersonalCardTransaction])
		{
			if ([smartExpenseManager isSmartExpensePctKey:obj.pctKey])
			{
				hasSmartExpense = YES;
				break;
			}
		}
		else if ([obj isCorporateCardTransaction])
		{
			if ([smartExpenseManager isSmartExpenseCctKey:obj.cctKey])
			{
				hasSmartExpense = YES;
				break;
			}
		}
	}		
	
	NSString* nsQuestion;
	if (hasSmartExpense)
		nsQuestion = [Localizer getLocalizedText:@"Are you sure about smart expenses"];
	else
		nsQuestion = [Localizer getLocalizedText:@"Are you sure"];
	
	UIAlertView* alert = [[MobileAlertView alloc] initWithTitle:
			 [Localizer getLocalizedText:@"Confirm Delete"] 
									   message:nsQuestion delegate:self cancelButtonTitle:
			 [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
							 otherButtonTitles:[Localizer getLocalizedText:@"OK"], 
			 nil];

	[alert show];
}


#pragma mark - AddToReport Stuff
#define kButtonA2RW 110
#define kButtonA2RH 30
-(UIBarButtonItem *)makeAddToReportButton:(int)count
{
    if (![[ExSystem sharedInstance] siteSettingAllowsExpenseReports])
        return nil;
    
    NSString *addText = [Localizer getLocalizedText:@"Add to Report"];
    
    if(count > 0)
        addText = [NSString stringWithFormat:@"%@ (%d)", addText, count];
    //CGFloat fontSize = 13.0;
    
    CGSize textSize = [addText sizeWithFont:[UIFont boldSystemFontOfSize:12]];
    //CGSize textSize = [addText sizeWithFont:[UIFont boldSystemFontOfSize:13] minFontSize:fontSize actualFontSize:fontSize forWidth:150 lineBreakMode:NSLineBreakByTruncatingTail];
    
    CGFloat w = 150.0;
    CGFloat h = 30.0;
    
    if((textSize.width + 20) < w)
        w = textSize.width + 20;
    
    if(count == -2)
        return [ExSystem makeColoredButton:@"BLUE_INACTIVE" W:w H:h Text:addText SelectorString:@"buttonAddToReportOnePressed:" MobileVC:self];
    else if(count == -1 || count == 0)
        return [ExSystem makeColoredButton:@"BLUE_INACTIVE" W:w H:h Text:addText SelectorString:@"buttonAddToReportPressed:" MobileVC:self];
    else if(count > 0)
        return [ExSystem makeColoredButton:@"BLUE" W:w H:h Text:addText SelectorString:@"buttonAddToReportPressed:" MobileVC:self];
    
    return nil;
}


-(void)buttonAddToReportPressed:(id)sender
{
	if ([selectedRows count] == 0)
		return;
	
	int undefinedCount = 0;
	int noReceiptCount = 0;
    BOOL hasCards = NO;
    
	for (NSString *key in selectedRows) 
	{
		int x = [key intValue];
		NSString *selKey = curKeys[x];
		OOPEntry* oop = (OOPEntry*)oopeDict[selKey];
		if (oop.hasReceipt == nil || [oop.hasReceipt isEqualToString:@"N"])
		{
			noReceiptCount ++;
		}
        
		if ([oop.expKey isEqualToString:@"UNDEF"])
		{
			undefinedCount ++;
		}
		
        if(oop.isCardTransaction)
            hasCards = YES;
	}
	
	// Mob-2523 Localize cancel button title string 
	//NSString *cancel = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	
	//Mob-3099
	/*
     if (noReceiptCount > 0 || undefinedCount > 0)
     {
     NSString* noRcpUndefTypeMsg = @"";
     
     // Mob-2603 Made the message more user friendly.
     if (noReceiptCount > 0 && undefinedCount == 0)
     {
     noRcpUndefTypeMsg = [NSString stringWithFormat:@"%@\n%@",[Localizer getLocalizedText:@"NO_RECEIPT_MSG"],[Localizer getLocalizedText:@"CONFIRM_CONTINUE"]];//noReceiptCount];
     }
     else if (undefinedCount > 0 && noReceiptCount == 0)
     {
     noRcpUndefTypeMsg = [NSString stringWithFormat:@"%@\n%@",[Localizer getLocalizedText:@"UNDEFINED_MSG"], [Localizer getLocalizedText:@"CONFIRM_CONTINUE"]];//undefinedCount];
     }
     else {
     noRcpUndefTypeMsg = [NSString stringWithFormat:@"%@\n%@\n%@",[Localizer getLocalizedText:@"UNDEFINED_MSG"],[Localizer getLocalizedText:@"NO_RECEIPT_MSG"],
     [Localizer getLocalizedText:@"CONFIRM_CONTINUE"]];//noReceiptCount];
     }
     
     
     UIAlertView *alert = [[MobileAlertView alloc] 
     initWithTitle:[Localizer getLocalizedText:@"Warning"]
     message:noRcpUndefTypeMsg
     delegate:self 
     cancelButtonTitle:cancel 
     otherButtonTitles:[Localizer getLocalizedText:@"OK"], nil];
     alert.tag = (hasSmartExpense ? kAlertViewAddToReportWithSmartExpense : kAlertViewAddToReport);
     [alert show];
     [alert release];
     
     }
     else 
	 */
//	if (hasSmartExpense)
//	{
//		[self showSmartExpenseAlert];
//	}
//	else
//	{
//    if ( ![UIDevice isPad] )
//    {
//        RootViewController* rvc = [ConcurMobileAppDelegate findRootViewController];
//        [rvc forceReload];
//    }
    
    [self goToSelectReport];

    // MOB-11978
    NSString *camefromVC = self.cameFrom;
    if([camefromVC isEqualToString:@"Report"])
    {
        camefromVC = @"Report Header";
    }
    else
    {
        camefromVC = @"Expense List";
    }
    
    NSDictionary *dictionary = @{@"How many added": [NSString stringWithFormat:@"%lu", (unsigned long)[selectedRows count]], @"Came From": camefromVC, @"Has Credit Cards": (hasCards?@"Yes":@"No"), @"Has Receipts": (noReceiptCount >0? @"Yes":@"No")};
    [Flurry logEvent:@"Mobile Entry: Add to Report" withParameters:dictionary];
//	}
}


-(void) addToReport:(NSMutableArray*) meKeys pctKeys:(NSArray*) pctKeys cctKeys:(NSArray*) cctKeys atnMap:(NSDictionary*) meAtnMap
{
	[self showWaitView];
    
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 SELECT_REPORT, @"TO_VIEW",
								 meKeys, @"ME_KEYS", 
								 nil];
	if (pctKeys != nil)
		pBag[@"PCT_KEYS"] = pctKeys;
	
	if (cctKeys != nil)
		pBag[@"CCT_KEYS"] = cctKeys;
	
	if (self.rpt.rptKey != nil)
		pBag[@"RPT_KEY"] = self.rpt.rptKey;
	
	if (meAtnMap != nil)
		pBag[@"ME_ATN_MAP"] = meAtnMap;
	
	[[ExSystem sharedInstance].msgControl createMsg:ADD_TO_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	
	
}
- (void) goToSelectReport
{
	// collect all selected meKeys
	NSMutableArray *meKeys = [[NSMutableArray alloc] initWithObjects:nil];
	NSMutableArray *pctKeys = [[NSMutableArray alloc] initWithObjects:nil];
	NSMutableArray *cctKeys = [[NSMutableArray alloc] initWithObjects:nil];
	NSMutableDictionary *meAtnMap = [[NSMutableDictionary alloc] init];
	
	for (NSString *key in selectedRows) 
	{
		int x = [key intValue];
		NSString *selKey = curKeys[x];
		OOPEntry* obj = (OOPEntry*)oopeDict[selKey];
		if ([obj isPersonalCardTransaction])
		{
			[pctKeys addObject:obj.pctKey];
		}
		else if ([obj isCorporateCardTransaction])
		{
			[cctKeys addObject:obj.cctKey];
		}
		else {
			[meKeys addObject:obj.meKey];
		}
		
		NSMutableArray* attendees = obj.attendees;
		if (attendees == nil)
		{
			attendees = (NSMutableArray*)[[AttendeeManager sharedInstance] getAttendeesForMeKey:obj.meKey];
		}
		if (attendees != nil)
		{
			meAtnMap[obj.meKey] = attendees;
		}
	}
	
	if (self.rpt != nil)
	{
		[self addToReport:meKeys pctKeys:pctKeys cctKeys:cctKeys atnMap:meAtnMap];
		return;
	}
	//takes you to the select report view
	SelectReportViewController * pVC = [[SelectReportViewController alloc] initWithNibName:@"SelectReportViewController" bundle:nil];
	pVC.meKeys = meKeys;
	pVC.pctKeys = pctKeys;
	pVC.cctKeys = cctKeys;
	pVC.meAtnMap = meAtnMap;
	pVC.parentMVC = self;
	
    [self.navigationController pushViewController:pVC animated:YES];
}


#pragma mark - Delete Button
#define kButtonWidth 100
-(void)makeDeleteButton:(int)count
{
	UIView *v = [[UIView alloc] initWithFrame:CGRectMake(0, 0, kButtonWidth, 30)];
	
	UIButton *button = nil; //[UIButton buttonWithType:UIButtonTypeCustom];

	if(count == 0)
        button = [ExSystem makeColoredButtonRegular:@"DELETE_INACTIVE" W:kButtonWidth H:30 Text:@"" SelectorString:@"confirmDelete:" MobileVC:self];
//		[button setBackgroundImage:[[UIImage imageNamed:@"delete_button30.png"]
//									stretchableImageWithLeftCapWidth:21.0f 
//									topCapHeight:0.0f]
//						  forState:UIControlStateNormal];
	else 
        button = [ExSystem makeColoredButtonRegular:@"DELETE" W:kButtonWidth H:30 Text:@"" SelectorString:@"confirmDelete:" MobileVC:self];
//		[button setBackgroundImage:[[UIImage imageNamed:@"delete_button30_active.png"]
//									stretchableImageWithLeftCapWidth:21.0f
//									topCapHeight:0.0f]
//						  forState:UIControlStateNormal];
	
	//set the frame of the button to the size of the image (see note below)
	button.frame = CGRectMake(0, 0, kButtonWidth, 30);

	if (count > 0)
		[button addTarget:self action:@selector(confirmDelete:) forControlEvents:UIControlEventTouchUpInside];
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(21, 0, kButtonWidth - 21, 30)];

	lbl.font = [UIFont boldSystemFontOfSize:12];
	lbl.textColor = [UIColor colorWithRed:243/255.0 green:228/255.0 blue:229/255.0 alpha:1.0f];//[UIColor lightGrayColor]; //[UIColor colorWithRed:222/255.0 green:137/255.0 blue:145/255.0 alpha:1.0f];
	if (count <= 0)
        lbl.shadowColor = [UIColor lightGrayColor];
    else
        lbl.shadowColor = [UIColor blackColor];
    
	lbl.shadowOffset = CGSizeMake(0, -1);
	lbl.backgroundColor = [UIColor clearColor];
	lbl.textAlignment = NSTextAlignmentCenter;
	NSString *delText = [Localizer getLocalizedText:@"Delete"];
	if(count > 0)
	{
		lbl.textColor =  [UIColor whiteColor];
		lbl.shadowColor = [UIColor darkGrayColor];
		delText = [NSString stringWithFormat:@"%@ (%d)", delText, count];
	}
	lbl.text = delText;
	
	[v addSubview:button];
	[v addSubview:lbl];
	
	//create a UIBarButtonItem with the button as a custom view
	UIBarButtonItem *customBarItem = [[UIBarButtonItem alloc] initWithCustomView:v];
	
	NSArray *toolbarItems = @[customBarItem];
	[self setToolbarItems:toolbarItems animated:NO];
}


-(void)buttonDeleteSelectedPressed:(id)sender
{
	if ([selectedRows count]==0)
		return;
	
	SmartExpenseManager *smartExpenseManager = [SmartExpenseManager getInstance];
	
	NSMutableDictionary *killMEKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	NSMutableDictionary *killPCTKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	NSMutableDictionary *killCCTKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];

	for (NSString *key in selectedRows) 
	{
		int x = [key intValue];
		NSString *killKey = curKeys[x];
		OOPEntry* obj = (OOPEntry*)oopeDict[killKey];
		if ([obj isPersonalCardTransaction])
		{
			killPCTKeys[obj.pctKey] = obj.pctKey;
			if ([smartExpenseManager isSmartExpensePctKey:obj.pctKey])
			{
				NSString *meKey = (smartExpenseManager.smartExpensePctKeys)[obj.pctKey];
				killMEKeys[meKey] = meKey;
			}
		}
		else if ([obj isCorporateCardTransaction])
		{
			killCCTKeys[obj.cctKey] = obj.cctKey;
			if ([smartExpenseManager isSmartExpenseCctKey:obj.cctKey])
			{
				NSString *meKey = (smartExpenseManager.smartExpenseCctKeys)[obj.cctKey];
				killMEKeys[meKey] = meKey;
			}
		}
		else if ([obj isOOPEntry])
		{
			killMEKeys[obj.meKey] = obj.meKey;
		}

	}
	
	[self showWaitView];
	[self makeDeleteButton:0];

	//OK, the thought here is that the post will start  from here, but, we also want it to respond to here.

	deleteMEReturned = TRUE;
	deletePCTReturned = TRUE;
	deleteCCTReturned = TRUE;
	if ([killMEKeys count] > 0) 
	{
		deleteMEReturned = FALSE;
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW", killMEKeys, @"KILL_KEYS", nil];
		[[ExSystem sharedInstance].msgControl createMsg:DELETE_OOP_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
	if ([killPCTKeys count] > 0) 
	{
		deletePCTReturned = FALSE;
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW", 
									 killPCTKeys, @"KILL_KEYS", PCT_TYPE, @"TYPE", nil];
		[[ExSystem sharedInstance].msgControl createMsg:DELETE_OOP_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
	if ([killCCTKeys count] > 0) 
	{
		deleteCCTReturned = FALSE;
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW", 
									 killCCTKeys, @"KILL_KEYS", CCT_TYPE, @"TYPE", nil];
		[[ExSystem sharedInstance].msgControl createMsg:DELETE_OOP_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
}




#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [curKeys count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    NSUInteger row = [indexPath row];
	NSString *key = curKeys[row];
	
	OOPEntry* entry = (OOPEntry*) oopeDict[key];
	
	static NSString *cellIdentity = @"QEEntryCell";
    QEEntryCell *cell = (QEEntryCell *)[tableView dequeueReusableCellWithIdentifier: cellIdentity];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:cellIdentity owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[QEEntryCell class]])
                cell = (QEEntryCell *)oneObject;
    }


    cell.ivIcon2.hidden = YES;
    cell.ivIcon1.hidden = YES;
    
	if ([entry isCardTransaction])
	{
		if ([entry isCorporateCardTransaction])
		{
			//cell.type = CCT_TYPE;
			cell.lblSub2.text = ((CCardTransaction*)entry).cardTypeName; 
		}
		else if ([entry isPersonalCardTransaction]) 
		{
			//cell.type = PCT_TYPE;
			cell.lblSub2.text = ((PCardTransaction*)entry).cardName;
		}
        cell.ivIcon2.hidden = NO;
        cell.ivIcon2.image = [UIImage imageNamed: @"icon_card_19"];
	}

	cell.lblHeading.text = entry.expName;
	cell.lblSub1.text = [DateTimeFormatter formatDateMediumByDate:entry.tranDate];	
	
	cell.lblAmount.text = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", entry.tranAmount] crnCode:entry.crnCode];
	if (entry.vendorName != nil) 
	{
		if(entry.locationName != nil)
			cell.lblSub2.text = [NSString stringWithFormat:@"%@ - %@", entry.vendorName, entry.locationName];
		else 
			cell.lblSub2.text = entry.vendorName;
	}
	else 
	{
		cell.lblSub2.text = entry.locationName;
	}	
	
//    NSLog(@"[entry hasReceipt] %@", [entry hasReceipt]);
    if([[entry hasReceipt] isEqualToString:@"Y"])
    {
        /* MOB-5783
         OK, I'm using the icon_receipt_button icon, not the icon_receipt_19 image.*/
        if([entry isCardTransaction])
        {
            cell.ivIcon1.hidden = NO;
            cell.ivIcon1.image = [UIImage imageNamed: @"icon_receipt_button"];
        }
        else
        {
            cell.ivIcon2.hidden = NO;
            cell.ivIcon2.image = [UIImage imageNamed: @"icon_receipt_button"];
        }
    }
	
//	cell.drawReceiptPos = 0;
//	if(entry.hasReceipt != nil && [entry.hasReceipt isEqualToString:@"Y"])
//	{
//		cell.drawReceiptPos = 1;
//	}	
	
	if(tableList.editing)
	{
		NSString *sRow = [NSString stringWithFormat:@"%lu", (unsigned long)row];
		if (selectedRows[sRow] != nil && self.inDeleteMode) 
			cell.ivSelected.image = [UIImage imageNamed:@"check_redselect"]; // cell.isSelected = YES;
        else if (selectedRows[sRow] != nil && !self.inDeleteMode) 
			cell.ivSelected.image = [UIImage imageNamed:@"check_greenselect"]; // cell.isSelected = YES;
		else 
			cell.ivSelected.image = [UIImage imageNamed:@"check_unselect"]; //cell.isSelected = NO;
	}
    else
        cell.ivSelected.image = [UIImage imageNamed:@"check_unselect"]; //cell.isSelected = NO;
	
	if (!tableList.isEditing) 
		[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
	else 
		[cell setAccessoryType:UITableViewCellAccessoryNone];
	
	return cell;
}

#pragma mark -
#pragma mark Table Delegate Methods 
-(NSIndexPath *)tableView:(UITableView *)tableView willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    return indexPath; 
}


-(void)tableView:(UITableView *)tableView willBeginEditingRowAtIndexPath:(NSIndexPath *)indexPath
{
		//int x = 0;
}


-(void)tableView:(UITableView *)tableView didEndEditingRowAtIndexPath:(NSIndexPath *)indexPath
{
	//int x = 0;
}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    NSUInteger row = [newIndexPath row];

	if (self.inDeleteMode)
	{
		NSString *key = curKeys[row];
		OOPEntry* entry = (OOPEntry*) oopeDict[key];
		if ([entry isCardTransaction])
        {
            // MOB-4835
            BOOL canDeleteCardTran = [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"ALLOW_TRANS_DELETE" withType:@"CARD"]];
            if (!canDeleteCardTran)
                return;
        }
	}
	
	if (tableView.isEditing)
	{
		selected = !selected;
		NSString *sRow = [NSString stringWithFormat:@"%lu", (unsigned long)row];
		
		//NSString *key = [curKeys objectAtIndex:row];
		//OOPEntry* entry = (OOPEntry*) [oopeDict objectForKey:key];

//		if (!inDeleteMode)
//		{
//			UIAlertView *alert = [[MobileAlertView alloc] 
//								  initWithTitle:nil
//								  message:[Localizer getLocalizedText:@"CANNOT_ADD_TO_RPT_WAIT_IMAGE"] 
//								  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] otherButtonTitles:nil];
//			[alert show];
//			[alert release];
//			return;
//		}
			
		if (selectedRows[sRow] != nil) 
		{
			[selectedRows removeObjectForKey:sRow];
		}
		else
		{
			selectedRows[sRow] = sRow;
		}
		
		if([selectedRows count] < 1)
			[self makeSelectAllButtons];
		
		if([selectedRows count] == [curKeys count])
			[self makeUnSelectAllButtons];

		if([selectedRows count] > 0)
			[self buttonAddToReportOnePressed:self];
		
		[tableView reloadData];
		
		if(inDeleteMode)
			[self makeDeleteButton:[selectedRows count]];
	}
	else
	{
        [self loadEntryForm:newIndexPath];

		
	}
}



- (void)clearSelectionForTableView:(UITableView *)tableView indexPath:(NSIndexPath *)indexPath
{
	if (selected & tableView.editing)
	{
		[self tableView:tableView didSelectRowAtIndexPath:indexPath];
		selected = NO;
	}
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return 74;
}


- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
	if (self.inDeleteMode)
	{
		NSUInteger row = [indexPath row];

		NSString *key = curKeys[row];
		OOPEntry* entry = (OOPEntry*) oopeDict[key];
		return ![entry isCardTransaction];
	}
	return YES;
}


#pragma mark - Some Utility Stuff
//-(void) createUI
//{
//	// MOB-4238: A crash will occur when performSelector on createUI is called twice in close succession
//	// causing a "Pushing same view controller instance more than once is not supported" exception.
//	//
//	// This method sets the isShowingNextController flag to YES when it shows the next controller.
//	// It will not attempt to show the next controller again until the flag is cleared.  The flag is
//	// cleared in ViewDidAppear.
//	//
//	if (self.isShowingNextController)
//	{
//		return;
//	}
//	
//	self.isShowingNextController = YES;
//    
//	if([UIDevice isPad])
//	{
//		[self.navigationController pushViewController:nextController animated:YES];
//	}
//	else {
//		ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//		if (delegate.navController.topViewController != nextController)
//			[delegate.navController pushViewController:nextController animated:YES];
//	}
//}
//
//- (void) doneLoadingForm
//{
//    //NSLog( @"Finished loading OOPE Form");
//	if(nextController != nil)
//	{
//		[nextController release];
//		nextController = nil;
//	}
//}


- (void)updateSelectionCount
{
//	NSInteger count = 0;
//	
//	for (MultiSelectCellController *cellController in [tableGroups objectAtIndex:0])
//	{
//		if ([cellController selected])
//		{
//			count++;
//		}
//	}
//	
//	actionButton.title = [NSString stringWithFormat:@"No action (%ld)", count];
//	actionButton.enabled = (count != 0);
}

- (void)cancel:(id)sender
{
	//[self showActionToolbar:NO];
	
	UIBarButtonItem *editButton =
	[[UIBarButtonItem alloc]
	  initWithTitle:[Localizer getLocalizedText:@"Edit"]
	  style:UIBarButtonItemStylePlain
	  target:self
     action:@selector(closeView:)];
	[self.navigationItem setRightBarButtonItem:editButton animated:NO];
	
//	for (MultiSelectCellController *cellController in [tableGroups objectAtIndex:0])
//	{
//		[cellController clearSelectionForTableView:self.tableView
//										 indexPath:[NSIndexPath indexPathForRow:row inSection:0]];
//		row++;
//	}
	
	[self.tableList setEditing:NO animated:YES];
}

- (NSIndexPath *)indexPathForCellController:(id)cellController
{
//	NSInteger sectionIndex;
//	NSInteger sectionCount = [tableGroups count];
//	for (sectionIndex = 0; sectionIndex < sectionCount; sectionIndex++)
//	{
//		NSArray *section = [tableGroups objectAtIndex:sectionIndex];
//		NSInteger rowIndex;
//		NSInteger rowCount = [section count];
//		for (rowIndex = 0; rowIndex < rowCount; rowIndex++)
//		{
//			NSArray *row = [section objectAtIndex:rowIndex];
//			if ([row isEqual:cellController])
//			{
//				return [NSIndexPath indexPathForRow:rowIndex inSection:sectionIndex];
//			}
//		}
//	}
	
	return nil;
}


//used to show save state
-(void) setupRefreshingToolbar:(NSString *)text ShowActivity:(BOOL)showActivity
{
	float viewW = 100;
	float viewH = 44;
	UIView *rView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, viewW, viewH)];
	
	UILabel *refreshText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, viewW, viewH)];
	
	[refreshText setTextColor:[UIColor whiteColor]];
	[refreshText setText:text];
	[refreshText setFont:[UIFont boldSystemFontOfSize:13.0f]];
	
	[refreshText setBackgroundColor:[UIColor clearColor]];
	[refreshText setTextAlignment:NSTextAlignmentCenter];
	
	[refreshText setShadowColor:[UIColor colorWithWhite:0.0f alpha:0.5f]];
	[refreshText setShadowOffset:CGSizeMake(0.0f, -1.0f)];
	refreshText.numberOfLines = 2;
	[refreshText setLineBreakMode:NSLineBreakByWordWrapping];
	
	if(showActivity)
	{
		UIActivityIndicatorView *activity = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake((viewW - 20) / 2, (viewH - 20) /2, 20, 20)];
		activity.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin;
		[activity setHidesWhenStopped:YES];
		[activity setActivityIndicatorViewStyle:UIActivityIndicatorViewStyleWhiteLarge];
		[activity startAnimating];
		
		[rView addSubview:activity];
	}
	[rView addSubview:refreshText];
	
	
	UIBarButtonItem *titleItem = [[UIBarButtonItem alloc] initWithCustomView:rView];
	titleItem.tag = kRefreshingTag;
	
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	flexibleSpace.tag = kRefreshingTag;
	//NSMutableArray *toolbarItems = [NSArray arrayWithObjects: flexibleSpace, titleItem, flexibleSpace, nil];
	NSMutableArray *redux = [[NSMutableArray alloc] initWithArray:self.toolbarItems];
	if([redux count] >= 1)
	{
		[redux insertObject:flexibleSpace atIndex:1];
		[redux insertObject:titleItem atIndex:2];
	}
	else {
		[redux addObject:flexibleSpace];
		[redux addObject:titleItem];
		[redux addObject:flexibleSpace];
	}

	[self setToolbarItems:redux animated:YES];
	
	[self performSelector:@selector(removeReceiptUploadedMessage) withObject:nil afterDelay:2.0f];
}


-(void)removeReceiptUploadedMessage
{
	NSMutableArray *redux = [[NSMutableArray alloc] init];
	
	for (UIBarItem *item in self.toolbarItems)
	{
		if (item.tag != kRefreshingTag)
		{
			[redux addObject:item];
		}
	}
	
	[self setToolbarItems:redux animated:YES];
}


#pragma mark Utility Methods
-(IBAction) closeView:(id)sender
{
	if([UIDevice isPad])
	{
		[self dismissViewControllerAnimated:YES completion:nil];	
		
		// Mob-3438
		if(self.padHomeVC != nil)
		{
			[padHomeVC refreshSummaryData];
			[padHomeVC refreshOOPData];
		}
	}
	
}

-(void)removeSelected
{
	if ([selectedRows count]==0)
		return;
	
	[tableList setEditing:NO];
	[self loadExpenses];
	
//	NSMutableDictionary *killMEKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
//	NSMutableDictionary *killPCTKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
//	
//	NSMutableArray *a = [[NSMutableArray alloc] initWithObjects:nil];
//	
//	for (NSString *key in selectedRows) 
//	{
//		NSArray *tokens = [key componentsSeparatedByString:@"-"];
//		NSString *sSection = [tokens objectAtIndex:0];
//		NSString *sRow = [tokens objectAtIndex:1];
//		int section = [sSection intValue];
//		int row = [sRow intValue];
//		
//		//
//		//NSUInteger row = [indexPath row];
//		NSString *foundKey = [curKeys objectAtIndex:row];
//		[a addObject:sRow];
//		
//		OOPEntry* entry = (OOPEntry*) [oopeDict objectForKey:key];
//		//
//		
////		NSMutableArray *a = [aSections objectAtIndex:section];
////		
////		NSString *killKey = @""; //[aKeys objectAtIndex:x];
////		
////		if([[a objectAtIndex:row] isKindOfClass:[OOPEntry class]])
////		{
////			OOPEntry *e = [a objectAtIndex:row];
////			killKey = [e getIdKey];			
////			[killMEKeys setObject:killKey forKey:killKey];
////			
////			//smoke the cached images
////			[ImageUtil killImageFromDocumentsFolder:[NSString stringWithFormat:@"RECEIPT_%@_%@.png", killKey, [ExSystem sharedInstance].userName]];
////			
////			NSString *key = killKey; //[NSString stringWithFormat:@"ME%@", killKey];
////			[oopData.oopes removeObjectForKey:key];
////			
////			
////		}
////		else {
////			//			killKey = ((PCardTransaction*) obj).pctKey;
////			//			[killPCTKeys setObject:killKey forKey:killKey];
////		}
//		
//	}
//	
//	for(NSString *key in a)
//	{
//		[curKeys remo
//	
//	[self sortAndFillTableData:oopData.oopes];
//	[self.tableView reloadData];
//	
//	[killMEKeys release];
//	[killPCTKeys release];
//	
//	[self killButtons];
}

#pragma NoDataMasterViewDelegate method
-(void) actionOnNoData:(id)sender
{
    if (curFilter != nil && ![curFilter isEqualToString:FILTER_ALL])
    {
        self.curFilter = FILTER_ALL;
        [self hideNoDataView];
        [self refreshFilter];
    }
    else
    {
        [self buttonAddPressed:nil];        
    }
}

-(BOOL)canShowActionOnNoData
{
    return self.rpt == nil && ((curFilter != nil && ![curFilter isEqualToString:FILTER_ALL]));
}

-(NSString*) titleForNoDataView
{
    if (curFilter != nil && ![curFilter isEqualToString:FILTER_ALL])
    {
        return [@"No Card Charges" localize]; 
    }
    else
    {
        return [Localizer getLocalizedText:@"No Expenses"];
    }
}

-(NSString*) buttonTitleForNoDataView
{
    if (curFilter != nil && ![curFilter isEqualToString:FILTER_ALL])
    {
        return [@"All Expenses" localize]; 
    }
    else
    {
        return [Localizer getLocalizedText:@"Add Expense"];
    }
}
 
#pragma mark - Upload Queue Banner View adjustment
-(void) adjustViewForUploadBanner
{
    if (!bannerAdjusted)
    {
        self.bannerAdjusted = YES;
        self.uploadView.delegate = self;
        self.tableList.frame = CGRectMake(0, uploadView.frame.size.height, tableList.frame.size.width, tableList.frame.size.height - uploadView.frame.size.height);
        
        [self.view addSubview:uploadView];
        [self.view bringSubviewToFront:uploadView];
    }
}

-(void) adjustViewForNoUploadBanner
{
    if (self.uploadView != nil && bannerAdjusted == YES)
    {
        [self.uploadView removeFromSuperview];
        self.uploadView = nil;
        self.bannerAdjusted = NO;
        [self.tableList setFrame:CGRectMake(0, 0, tableList.frame.size.width, tableList.frame.size.height + uploadView.frame.size.height)];
    }
}

-(void) showUploadViewController
{
    //memorize itemCount before displaying upload queue, when dismiss upload queue, if item count changed update iPadHomeVC.
    self.currentItemCount = [[UploadQueue sharedInstance] visibleQueuedItemCount];
    UploadQueueViewController *vc = [[UploadQueueViewController alloc] initWithNibName:@"UploadQueueViewController" bundle:nil];
    vc.delegate = self;
    UIBarButtonItem *btnUpload = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Upload"] style:UIBarButtonItemStyleBordered target:vc action:@selector(startUpload)];
    vc.title = [Localizer getLocalizedText:@"Upload Queue"];
    vc.navigationItem.rightBarButtonItem = btnUpload;
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark - Toolbar Stuff
-(void)loadExpenses
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"DETAIL_VIEW", @"TO_VIEW", nil];
	[[ExSystem sharedInstance].msgControl createMsg:OOPES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];//MOB-3411: skipCache was YES, which meant offline did not work
}

//the button to un-select all items in the list was pressed
-(void)butonUnSelectAll:(id)sender
{
	[selectedRows removeAllObjects];
	[tableList reloadData];
	[self makeSelectAllButtons];
}


-(void)makeSelectAllButtons
{
	int selCount = [selectedRows count];
	if(selCount == 0)
		selCount = -1;
	
	NSString *selectAll = [Localizer getLocalizedText:@"Select All"];
	
	UIBarButtonItem *btnSelectAll = [[UIBarButtonItem alloc] initWithTitle:selectAll style:UIBarButtonItemStyleBordered target:self action:@selector(butonSelectAll:)];
	UIBarButtonItem *btnAddToReport = [self makeAddToReportButton:selCount]; //[[UIBarButtonItem alloc] initWithTitle:addToReport style:UIBarButtonItemStyleBordered target:self action:@selector(buttonAddToReportPressed:)];
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSArray *toolbarItems = @[btnSelectAll, flexibleSpace, btnAddToReport];
	[self setToolbarItems:toolbarItems animated:NO];
}


-(void)makeUnSelectAllButtons
{
	int selCount = [selectedRows count];
	if(selCount == 0)
		selCount = -1;
	
	NSString *unselectAll = [Localizer getLocalizedText:@"Unselect All"];
	
	UIBarButtonItem *btnUnSelectAll = [[UIBarButtonItem alloc] initWithTitle:unselectAll style:UIBarButtonItemStyleBordered target:self action:@selector(butonUnSelectAll:)];
	UIBarButtonItem *btnAddToReport = [self makeAddToReportButton:selCount]; // [[UIBarButtonItem alloc] initWithTitle:addToReport style:UIBarButtonItemStyleBordered target:self action:@selector(buttonAddToReportPressed:)];
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSArray *toolbarItems = @[btnUnSelectAll, flexibleSpace, btnAddToReport];
	[self setToolbarItems:toolbarItems animated:NO];
}


//the button to select all items in the list was pressed
-(void)butonSelectAll:(id)sender
{
	for(int i = 0; i < [curKeys count]; i++)
	{
		NSString *sRow = [NSString stringWithFormat:@"%d", i];
		selectedRows[sRow] = sRow;	
	}
	[tableList reloadData];
	[self makeUnSelectAllButtons];
}


// Method to handle the first press of addtoreport, which then shows the add to report stuff
-(void)buttonAddToReportOnePressed:(id)sender
{
	inAddToReportMode = YES;
	[self.tableList setEditing:YES animated:YES];
	[self doConfigureToolBar:sender];
}

-(void)configureToolBar
{
	[self performSelector:@selector(doConfigureToolBar:) withObject:nil afterDelay:0.05f];
}

-(void)doConfigureToolBar:(id)sender
{
	if([ExSystem connectedToNetwork])
	{
		if (inAddToReportMode)
		{
			int selCount = [selectedRows count];
			if(selCount == 0)
				selCount = -1;
			
			NSString *cancel = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
			NSString *selectAll = [Localizer getLocalizedText:@"Select All"];
            
			UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:cancel style:UIBarButtonItemStyleBordered target:self action:@selector(buttonCancelPressed:)];
			self.navigationItem.rightBarButtonItem = nil;
            if (self.rpt == nil) // Do not display cancel if import expenses from report detail
                self.navigationItem.rightBarButtonItem = btnCancel;
			
			UIBarButtonItem *btnSelectAll = [[UIBarButtonItem alloc] initWithTitle:selectAll style:UIBarButtonItemStyleBordered target:self action:@selector(butonSelectAll:)];
			UIBarButtonItem *btnAddToReport = [self makeAddToReportButton:selCount];// [[UIBarButtonItem alloc] initWithTitle:addToReport style:UIBarButtonItemStyleBordered target:self action:@selector(buttonAddToReportPressed:)];
			UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
			NSArray *toolbarItems = @[btnSelectAll, flexibleSpace, btnAddToReport];
			[self setToolbarItems:toolbarItems animated:NO];
			return;
		}
		
		UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
		
		UIBarButtonItem *btnAction = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(buttonActionPressed:)];
        
        UIBarButtonItem *btnEdit = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Edit"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonEditPressed:)];
        
		UIBarButtonItem *btnAddToReport = nil;
		NSArray *toolbarItems = nil;
		if (curKeys != nil && [curKeys count] > 0)
		{
			btnAddToReport = [self makeAddToReportButton:-2];// [[UIBarButtonItem alloc] initWithTitle:addToReport style:UIBarButtonItemStyleBordered target:self action:@selector(buttonAddToReportOnePressed:)];
			toolbarItems = @[btnEdit, flexibleSpace, btnAction, flexibleSpace, btnAddToReport];
            [self.navigationController setToolbarHidden:NO];
        }
		// else Negative view hides toolbar -- no longer needed
        if ((self.curFilter == nil || self.curFilter == FILTER_ALL) && self.navigationItem.rightBarButtonItem == nil)
        {
            UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
            self.navigationItem.rightBarButtonItem = btnAdd;
        }
		[self setToolbarItems:toolbarItems animated:NO];
	}
	else 
	{
		[self makeOfflineBar];
	}
}


#pragma mark - Navigation Stuff
-(void) goToReportDetailScreen // Add to Report from Rpt Detail
{
	if([UIDevice isPad])
	{
        // If the background is report detail screen of the same report, then just refresh the big detail view
        
		// use homeVC dismiss worked
		ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        UINavigationController *nav = delegate.padHomeVC.navigationController;
        MobileViewController *mvc = [nav.viewControllers count]>0?
        (MobileViewController *)(nav.viewControllers)[([nav.viewControllers count]-1)]:nil;
        bool createWizard = YES;
        if (mvc != nil && [mvc isKindOfClass: [ReportDetailViewController_iPad class]])
        {
            ReportDetailViewController_iPad* padVc = (ReportDetailViewController_iPad*) mvc;
            if ([padVc.rpt.rptKey isEqualToString:self.rpt.rptKey]) 
                createWizard = NO;
        }
		[delegate.padHomeVC dismissViewControllerAnimated:YES completion:nil];
		
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: self.rpt, @"REPORT", 
									 self.rpt.rptKey, @"ID_KEY", 
									 self.rpt.rptKey, @"RECORD_KEY", 
                                     ROLE_EXPENSE_TRAVELER, @"ROLE",
									 @"YES", @"REPORT_CREATE_WIZARD",
									 @"YES", @"SHORT_CIRCUIT",
									 nil];
		
        if (createWizard)
        {
            // Never used?
            [delegate.padHomeVC switchToDetail:@"Report" ParameterBag:pBag];
        }
        else
        {
            pBag[@"REPORT_CREATE_WIZARD"] = @"NO";
            pBag[@"REPORT_DETAIL"] = self.rpt;
            Msg *msg = [[Msg alloc] init];
            msg.parameterBag = pBag;
            msg.idKey = @"SHORT_CIRCUIT";
            [mvc respondToFoundData:msg];
        }
	}
	else 
	{
		Msg *msg = [[Msg alloc] init];
		
		NSMutableDictionary * pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
									  self.rpt.rptKey, @"ID_KEY", 
									  self.rpt, @"REPORT",
									  self.rpt, @"REPORT_DETAIL",
                                      ROLE_EXPENSE_TRAVELER, @"ROLE",
									  self.rpt.rptKey, @"RECORD_KEY", @"YES", @"SHORT_CIRCUIT", nil];	
		msg.parameterBag = pBag;
        
		int vcCount = [self.navigationController.viewControllers count];
		for (int ix = 0 ; ix < vcCount; ix++ )
		{
			MobileViewController *vc = (MobileViewController *)(self.navigationController.viewControllers)[ix];
			if (vc != nil && [vc isKindOfClass:[ReportDetailViewController class]])
			{
				[vc respondToFoundData:msg];
			}
		}
		
		[self.navigationController popViewControllerAnimated:YES];
		
		// TODO - need to update active list
	}
}


#pragma mark - Load Entry Form
-(void) loadEntryForm:(NSIndexPath *) indexPath
{
    NSString *key = curKeys[indexPath.row];
	OOPEntry* entry = (OOPEntry*) oopeDict[key];
    
    QEFormVC *formVC = [[QEFormVC alloc] initWithEntryOrNil:nil];
    [self.navigationController pushViewController:formVC animated:YES];
    formVC.entry = (EntityMobileEntry *)entry;
    [formVC makeFieldsArray:nil];
    [formVC.tableList reloadData];
    
    formVC.receipt = [[Receipt alloc] init];
    //formVC.receipt.receiptImg = formVC.entry.receiptImage;
    formVC.receipt.receiptId = formVC.entry.receiptImageId;

    if(entry.receiptImage == nil && entry.hasReceipt != nil && [entry.hasReceipt isEqualToString:@"Y"])
    {//ok, they are listing a receipt as being attached to this oope, but nothing is local, just down load it.
        // MOB-8265 Smart expenses' meKey is always null, need to get it from SmartExpenseMeKey
        NSString* meKey = entry.meKey;
//        if (meKey == nil && [entry isKindOfClass:[CardTransaction class]])
//        {
//            CardTransaction* card = (CardTransaction*) entry;
//            meKey = card.smartExpenseMeKey;
//        }
        
        if ([meKey length])
        {
            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", meKey, @"ME_KEY",nil];
            [[ExSystem sharedInstance].msgControl createMsg:OOPE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:formVC];
        }
    }
}

-(void) pushQuick:(id)sender
{
    QEFormVC *vc = [[QEFormVC alloc] initWithEntryOrNil:nil];
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark - UploadQueueVCDelegate Methods

-(void) didDismissUploadQueueVC
{
    [[ExSystem sharedInstance].msgControl createMsg:OOPES_DATA CacheOnly:@"YES" ParameterBag:nil SkipCache:NO RespondTo:self];
//    if (currentItemCount != [[UploadQueue sharedInstance] visibleQueuedItemCount] && [UIDevice isPad])
//        [[NSNotificationCenter defaultCenter] postNotificationName:@"queue item count changed" object:self];
}

@end


