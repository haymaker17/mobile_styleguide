    //
//  CardsListViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CardsListViewController.h"
#import "RootViewController.h"
#import "OutOfPocketData.h"
#import "OOPEntry.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"
#import "OutOfPocketEntryCell.h"
#import "OutOfPocketDeleteData.h"
#import "CardsGetPersonalAndTransactions.h"

@implementation CardsListViewController

@synthesize tableView, aKeys, dict, ivBack, lblBack, titleLabel, showedNo, selectedRows, waitView, sections, drewEdit;
#define kCELL_HEIGHT 70

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return  CARDS_LIST;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:CARDS_PERSONAL_TRAN_DATA])
	{
		[waitView setHidden:YES];
		[tableView setEditing:NO];
		CardsGetPersonalAndTransactions *pCardData = (CardsGetPersonalAndTransactions *)msg.responder;
		self.aKeys = pCardData.keys;
		self.sections = pCardData.keys;
		self.dict = pCardData.cards;
		[self.tableView reloadData];
		
//		UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
//		UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
//		flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
		if(rootViewController.settings.isOffLine)
		{
			[self makeOfflineBar];
		}
		else 
		{
			NSMutableArray *toolbarItems = [NSArray arrayWithObjects: nil];
			[self setToolbarItems:toolbarItems animated:YES];
		}
		//[btnAdd release];
		//[flexibleSpace release];
	}
	else if ([msg.idKey isEqualToString:DELETE_OOP_DATA])
	{
//		OutOfPocketDeleteData *delData = (OutOfPocketDeleteData *)msg.responder;
//		NSMutableDictionary *delDict = delData.returnFailures;
//		for(NSString *key in delDict)
//		{
//			NSMutableDictionary *returnInfo = [delDict objectForKey:key];
//			if([returnInfo objectForKey:@"STATUS"] != nil & ([[returnInfo objectForKey:@"STATUS"] isEqualToString:@"FAILURE"]))
//			{
//				//throw errors
//			}
//		}
//		//[tableView setEditing:NO];
//		[selectedRows removeAllObjects];
//		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW", self, @"RESPOND_DIRECTLY_TO", nil];
//		[rootViewController.msgControl createMsg:OOPES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES];
//		[pBag release];
	}
	
	if ([aKeys count] < 1 || aKeys == nil) 
	{//show we gots no data view
		[emptyView setHidden:NO];
		self.navigationItem.rightBarButtonItem == nil;
	}
	else if (aKeys != nil & [aKeys count] > 0)
	{//refresh from the server, after an initial no show...
		[emptyView setHidden:YES];

	}
	
	
}
/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
 - (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
 if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
 // Custom initialization
 }
 return self;
 }
 */

/*
 // Implement loadView to create a view hierarchy programmatically, without using a nib.
 - (void)loadView {
 }
 */




#pragma mark -
#pragma mark ViewController Methods
- (void)viewDidAppear:(BOOL)animated 
{
	[tableView setContentOffset:CGPointMake(0, 0) animated:NO];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW", self, @"RESPOND_DIRECTLY_TO", nil];
	[rootViewController.msgControl createMsg:OOPES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES];
	[pBag release];
	
	if ([aKeys count] < 1 || aKeys == nil) 
	{//show we gots no data view
		[emptyView setHidden:NO];
		self.navigationItem.rightBarButtonItem == nil;
	}
	else if (aKeys != nil & [aKeys count] > 0)
	{//refresh from the server, after an initial no show...
		[emptyView setHidden:YES];
		if(self.navigationItem.rightBarButtonItem == nil)
			[self makeBtnEdit];

	}
	
	[super viewDidAppear:animated];
}


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad 
{
	aKeys = [[NSMutableArray alloc] initWithObjects:nil];
	sections = [[NSMutableArray alloc] initWithObjects:nil];
	dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	[rootViewController.navigationController.toolbar setHidden:NO];
//	UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
//	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
//	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	if(rootViewController.settings.isOffLine)
	{
		[self makeOfflineBar];
	}
	else 
	{
		NSMutableArray *toolbarItems = [NSArray arrayWithObjects: nil];
		[self setToolbarItems:toolbarItems animated:YES];
	}
//	[btnAdd release];
//	[flexibleSpace release];
	
	[self.tableView setAllowsSelectionDuringEditing:YES];
	selectedRows = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	waitView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 400)];
	waitView.backgroundColor = [UIColor blackColor];
	waitView.alpha = 0.5f;
	
	UIActivityIndicatorView *activity = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(142, 150, 37, 37)];
	[activity setHidesWhenStopped:YES];
	[activity setActivityIndicatorViewStyle:UIActivityIndicatorViewStyleWhiteLarge];
	[activity startAnimating];
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 190, 320, 37)];
	[lbl setText:@"Removing Transactions"];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setTextAlignment:UITextAlignmentCenter];
	[lbl setFont:[UIFont boldSystemFontOfSize:18.0f]];
	[lbl setTextColor:[UIColor whiteColor]];
	[lbl setShadowColor:[UIColor colorWithWhite:0.0f alpha:0.5f]];
	[lbl setShadowOffset:CGSizeMake(0.0f, -1.0f)];
	[waitView addSubview:lbl];
	[waitView addSubview:activity];
	[waitView setHidden:YES];
	[activity release];
	[lbl release];
	[self makeNoDataView:@"No Card Transactions"];
	[self makeBtnEdit];
    [super viewDidLoad];
}

-(void) makeBtnEdit
{
	if(aKeys != nil & [aKeys count] > 0)
	{
		UIBarButtonItem *btnEdit = [[UIBarButtonItem alloc] initWithTitle:@"Edit" style:UIBarButtonItemStyleBordered target:self action:@selector(buttonEditPressed:)];
		self.navigationItem.rightBarButtonItem = nil;
		[self.navigationItem setRightBarButtonItem:btnEdit animated:NO];
		[btnEdit release];
		drewEdit = YES;
	}
}

-(void)buttonAddPressed:(id)sender
{
	OOPEntry *entry = [[OOPEntry alloc] init];
	entry.tranDate = [NSDate date];
	//entry.locationName = rootViewController.findMe.city;
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: entry, @"ENTRY", nil];
	[rootViewController switchToView:OUT_OF_POCKET_FORM viewFrom:OUT_OF_POCKET_LIST ParameterBag:pBag];
	[pBag release];
	[entry release];
}

-(void)buttonEditPressed:(id)sender
{
	[self.tableView setEditing:YES animated:YES];
	UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:@"Cancel" style:UIBarButtonItemStyleBordered target:self action:@selector(buttonCancelPressed:)];
	self.navigationItem.rightBarButtonItem = nil;
	self.navigationItem.rightBarButtonItem = btnCancel;
	[btnCancel release];
	
	UIBarButtonItem *btnDelete = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemTrash target:self action:@selector(buttonDeleteSelectedPressed:)];
	UIBarButtonItem *btnAddToReport = [[UIBarButtonItem alloc] initWithTitle:@"Add to Report" style:UIBarButtonItemStyleBordered target:self action:@selector(buttonAddToReportPressed:)];
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSMutableArray *toolbarItems = [NSArray arrayWithObjects: btnDelete, flexibleSpace, btnAddToReport, nil];
	[self setToolbarItems:toolbarItems animated:YES];
	[btnDelete release];
	[btnAddToReport release];
	[flexibleSpace release];
	
	[tableView reloadData];
}


-(void)buttonAddToReportPressed:(id)sender
{
	
}


-(void)buttonDeleteSelectedPressed:(id)sender
{
//	NSMutableDictionary *killKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
//	for (NSString *key in selectedRows) 
//	{
//		int x = [key intValue];
//		NSString *killKey = [aKeys objectAtIndex:x];
//		[killKeys setObject:killKey forKey:killKey];
//	}
//	
//	
//	if ([killKeys count] > 0) 
//	{
//		[waitView setHidden:NO];
//		[waitView removeFromSuperview];
//		[self.view addSubview:waitView];
//		[self.view bringSubviewToFront:waitView];
//		//OK, the thought here is that the post will start  from here, but, we also want it to respond to here.
//		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW", killKeys, @"KILL_KEYS", self, @"RESPOND_DIRECTLY_TO", nil];
//		[rootViewController.msgControl createMsg:DELETE_OOP_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES];
//		[pBag release];
//	}
//	
}


-(void)buttonCancelPressed:(id)sender
{
	[tableView setEditing:NO];
	[self makeBtnEdit];
	
//	UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
//	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
//	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSMutableArray *toolbarItems = [NSArray arrayWithObjects: nil];
	[self setToolbarItems:toolbarItems animated:YES];
//	[btnAdd release];
//	[flexibleSpace release];
	[selectedRows removeAllObjects];
	[tableView reloadData];
}

// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation 
{
    // Return YES for supported orientations
    return YES;
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	[tableView reloadData];
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc 
{
	NSLog(@"CardsListVC dealloc");
	[tableView release];
	[aKeys release];
	[dict release];
	[ivBack release];
	[lblBack release];
	[titleLabel release];
	[selectedRows release];
	[waitView release];
	[sections release];
    [super dealloc];
}



#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return [sections count];
    
}


- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
	if ([sections count] == 0)
        return 0;
	
	NSString *key = [sections objectAtIndex:section];
	PersonalCardData *pCard = [dict objectForKey:key];
	return [pCard.trans count];
    
}

#define kXOffset 85

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
	NSString *key = [sections objectAtIndex:section];
	PersonalCardData *pcard = [dict objectForKey:key];
	NSString *tranKey = [pcard.tranKeys objectAtIndex:row];
	PCardTransaction *tran = [pcard.trans objectForKey:tranKey];
	
//	OutOfPocketEntryCell *cell = (OutOfPocketEntryCell *)[tableView dequeueReusableCellWithIdentifier: @"OutOfPocketEntryCell"];
//	if (cell == nil)  
//	{
//		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"OutOfPocketEntry" owner:self options:nil];
//		for (id oneObject in nib)
//			if ([oneObject isKindOfClass:[OutOfPocketEntryCell class]])
//				cell = (OutOfPocketEntryCell *)oneObject;
//	}
	
	static NSString *cellIdentity = @"OutOfPocketEntryCell";
	OutOfPocketEntryCell *cell = (OutOfPocketEntryCell *)[tableView dequeueReusableCellWithIdentifier: cellIdentity];
	if (cell == nil)  
	{
		cell = [[[OutOfPocketEntryCell alloc] initWithFrame:CGRectZero reuseIdentifier:cellIdentity] autorelease];

	}
	
	
	cell.isLandscape = [self isLandscape];
	cell.firstText = tran.expName;
	cell.lastText = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", tran.tranAmount] crnCode:tran.crnCode];
	cell.line2 = [DateTimeFormatter formatDateMediumByDate:tran.tranDate];
	cell.line3 = tran.vendorName;
	
	[cell.lblHead setHidden:YES];
	[cell.lblAmount setHidden:YES];
	[cell.lbl1 setHidden:YES];
	[cell.lbl2 setHidden:YES];
//
//	for (UIView *iView in [cell.contentView subviews]) 
//	{
//		if (iView.tag >= 900) 
//			[iView removeFromSuperview];
//	}
//	
//	UILabel *lbl1 = [[UILabel alloc] initWithFrame:CGRectMake(20, 5, 200, 14)];
//	lbl1.text = tran.expName;
//	lbl1.font = [UIFont boldSystemFontOfSize:13];
//	[lbl1 setTextColor:[UIColor blackColor]];
//	[lbl1 setLineBreakMode:UILineBreakModeTailTruncation];
//	[lbl1 setNumberOfLines:1];
//	[lbl1 setBackgroundColor:[UIColor clearColor]];
//	lbl1.tag = 900;
//	
//	UILabel *lbl2 = [[UILabel alloc] initWithFrame:CGRectMake(20, 20, 200, 14)];
//	lbl2.text =[DateTimeFormatter formatDateMediumByDate:tran.datePosted];
//	lbl2.font = [UIFont systemFontOfSize:13];
//	[lbl2 setTextColor:[UIColor grayColor]];
//	[lbl2 setLineBreakMode:UILineBreakModeTailTruncation];
//	[lbl2 setNumberOfLines:1];
//	[lbl2 setBackgroundColor:[UIColor clearColor]];
//	lbl2.tag = 900;
//
//	UILabel *lbl3 = [[UILabel alloc] initWithFrame:CGRectMake(20, 35, 270, 14)];
//	lbl3.text = tran.description;
//	lbl3.font = [UIFont systemFontOfSize:13];
//	[lbl3 setTextColor:[UIColor grayColor]];
//	[lbl3 setLineBreakMode:UILineBreakModeTailTruncation];
//	[lbl3 setNumberOfLines:1];
//	[lbl3 setBackgroundColor:[UIColor clearColor]];
//	lbl3.tag = 900;
//	
//	UILabel *lblAmt = [[UILabel alloc] initWithFrame:CGRectMake(210, 5, 100, 14)];
//	lblAmt.font = [UIFont boldSystemFontOfSize:13];
//	[lblAmt setTextColor:[UIColor blackColor]];
//	[lblAmt setLineBreakMode:UILineBreakModeTailTruncation];
//	[lblAmt setNumberOfLines:1];
//	[lblAmt setTextAlignment:UITextAlignmentRight];
//	//lblAmt.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin;
//	[lblAmt setBackgroundColor:[UIColor clearColor]];
//	//UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin;
//	lblAmt.tag = 900;
//	
//	[cell.contentView addSubview:lbl1];
//	[cell.contentView addSubview:lbl2];
//	[cell.contentView addSubview:lbl3];
//	[cell.contentView addSubview:lblAmt];
//	lblAmt.text = [FormatUtils formatMoney:tran.amount crnCode:pcard.crnCode];
	
	
//	int offSet = 85;
//	if (tableView.isEditing)
//	{
//		lblAmt.frame = CGRectMake(160, 5, 100, 14);
//		lbl3.frame = CGRectMake(20, 35, 240, 14);
//		offSet = 20;
//	}
//	else 
//	{
//		lblAmt.frame = CGRectMake(185, 5, 100, 14);
//		lbl3.frame = CGRectMake(20, 35, 270, 14);
//	}
//	
//	[lbl1 release];
//	[lbl2 release];
//	[lbl3 release];
//	[lblAmt release];
//	
//	cell.selectionStyle = UITableViewCellSelectionStyleNone;
//	
//	UIImageView *indicator;
//	NSString *sRow = [NSString stringWithFormat:@"%d-%d", section, row];
//	if ([selectedRows objectForKey:sRow] != nil) 
//	{
//		indicator = [[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"IsSelected.png"]] autorelease];
//	}
//	else
//	{
//		indicator = [[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"NotSelected.png"]] autorelease];
//	}
//	const NSInteger IMAGE_SIZE = 30;
//	const NSInteger SIDE_PADDING = 5;
//	
//	indicator.tag = 902;
//	indicator.frame = CGRectMake(-offSet + SIDE_PADDING, 11, IMAGE_SIZE, IMAGE_SIZE);
//	//indicator.frame = CGRectMake(0, 0, 30, 30);
//	[cell.contentView addSubview:indicator];
//	
//	if (!tableView.isEditing) 
//		[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
//	else 
//		[cell setAccessoryType:UITableViewCellAccessoryNone];
	
	if(tableView.editing)
	{
//		NSString *sRow = [NSString stringWithFormat:@"%d", row];
//		if ([selectedRows objectForKey:sRow] != nil) 
//			cell.isSelected = YES;
//		else 
//			cell.isSelected = NO;
		
		UIImageView *indicator;
		NSString *sRow = [NSString stringWithFormat:@"%d-%d", section, row];
		if ([selectedRows objectForKey:sRow] != nil) 
		{
			cell.isSelected = YES;
		}
		else
		{
			cell.isSelected = NO;
		}
	}
	else 
		cell.isSelected = NO;
	
	
	return cell;
}

#pragma mark -
#pragma mark Table Delegate Methods 
- (NSString *)tableView:(UITableView *)tableView 
titleForHeaderInSection:(NSInteger)section
{
    NSString *key = [sections objectAtIndex:section];
	PersonalCardData *pCard = [dict objectForKey:key];
    return pCard.cardName;
}

-(NSIndexPath *)tableView:(UITableView *)tableView 
willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    return indexPath; 
}

-(void)tableView:(UITableView *)tableView willBeginEditingRowAtIndexPath:(NSIndexPath *)indexPath
{
	int x = 0;
}

-(void)tableView:(UITableView *)tableView didEndEditingRowAtIndexPath:(NSIndexPath *)indexPath
{
	int x = 0;
}

//- (void)tableView:(UITableView *)tableView 
//accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath
//{
//	UIButton *infoButton = [UIButton buttonWithType:UIButtonTypeInfoLight];
//	infoButton.tag = 600001;
//	[rootViewController switchViews:infoButton ParameterBag:nil];
//}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    NSUInteger row = [newIndexPath row];
	NSUInteger section = [newIndexPath section];

	if (tableView.isEditing)
	{
		selected = !selected;
		NSString *sRow = [NSString stringWithFormat:@"%d-%d", section, row];
//		NSString *sRow = [NSString stringWithFormat:@"%d", row];
		if ([selectedRows objectForKey:sRow] != nil) 
		{
			[selectedRows removeObjectForKey:sRow];
		}
		else
		{
			[selectedRows setObject:sRow forKey:sRow];
		}
		
		[tableView reloadData];
//		
//		//		[(RootViewController *)tableView.delegate updateSelectionCount];
//		//		
//		//		UITableViewCell *cell =
//		//		[tableView cellForRowAtIndexPath:
//		//		 [(RootViewController *)tableView.delegate
//		//		  indexPathForCellController:self]];
//		//		
//		//		if (!cell)
//		//		{
//		//			//
//		//			// This path will be taken if the row is not visible
//		//			//
//		//			return;
//		//		}
//		
//		//		UIImageView *indicator = (UIImageView *)[cell.contentView viewWithTag:SELECTION_INDICATOR_TAG];
//		//		if (selected)
//		//		{
//		//			indicator.image = [UIImage imageNamed:@"IsSelected.png"];
//		//			cell.backgroundView.backgroundColor = [UIColor colorWithRed:223.0/255.0 green:230.0/255.0 blue:250.0/255.0 alpha:1.0];
//		//		}
//		//		else
//		//		{
//		//			indicator.image = [UIImage imageNamed:@"NotSelected.png"];
//		//			cell.backgroundView.backgroundColor = [UIColor whiteColor];
//		//		}
	}
	else
	{
//		
//		NSString *key = [self.aKeys objectAtIndex:row];
//		OOPEntry *entry = [dict objectForKey:key];
//		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: entry, @"ENTRY", nil];
//		[rootViewController switchToView:OUT_OF_POCKET_FORM viewFrom:OUT_OF_POCKET_LIST ParameterBag:pBag];
//		[pBag release];
	}
}


- (void)clearSelectionForTableView:(UITableView *)tableView indexPath:(NSIndexPath *)indexPath
{
	if (selected)
	{
		[self tableView:tableView didSelectRowAtIndexPath:indexPath];
		selected = NO;
	}
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return 50;
}


- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
	return YES;
}

- (void)updateSelectionCount
{

}

- (void)cancel:(id)sender
{
	[self showActionToolbar:NO];
	
	UIBarButtonItem *editButton =
	[[[UIBarButtonItem alloc]
	  initWithTitle:@"Edit"
	  style:UIBarButtonItemStylePlain
	  target:self
	  action:@selector(edit:)]
	 autorelease];
	[self.navigationItem setRightBarButtonItem:editButton animated:NO];
	
	NSInteger row = 0;

	
	[self.tableView setEditing:NO animated:YES];
}

- (NSIndexPath *)indexPathForCellController:(id)cellController
{
	NSInteger sectionIndex;
	
	return nil;
}

@end

