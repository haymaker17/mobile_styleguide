//
//  RootViewController_iPad.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "RootViewController_iPad.h"
#import "DetailViewController.h"
#import "DetailExpensesViewController.h"
//#import "LoginViewController.h"
#import "TripsListNavViewController.h"
#import "OutOfPocketListViewController.h"
#import "RootCellPad.h"
#import "ReportApprovalListViewController.h"
#import "ActiveReportListViewController.h"
#import "SettingsViewController.h"
#import "DetailApprovalViewController.h"
#import "iPadHomeVC.h"

@implementation RootViewController_iPad

@synthesize detailViewController, rvc, menuArray;

@synthesize popoverController, splitViewController, rootPopoverButtonItem, navigationBar;

#pragma mark -
#pragma mark System Buttons
-(void)makeSystemButtons
{
	UIBarButtonItem *btnLogout = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Logout"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonLogoutPressed:)];
	UIBarButtonItem *btnSettings = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Settings"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonSettingsPressed:)];
//	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
//	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];	
//	
//    NSMutableArray *items = [[toolbar items] mutableCopy];
//    [items addObject:flexibleSpace];
//	[items addObject:btnLogout];
//	[items addObject:btnSettings];
//    [toolbar setItems:items animated:YES];
//    [items release];
//	
//	[flexibleSpace release];
	
	self.navigationItem.rightBarButtonItem = nil; //remove the right button
	self.navigationItem.rightBarButtonItem = btnLogout;

	self.navigationItem.leftBarButtonItem = nil; //remove the right button
	self.navigationItem.leftBarButtonItem = btnSettings;
	
	[btnLogout release];
	[btnSettings release];
}



- (IBAction)buttonLogoutPressed:(id)sender
{
	[self.navigationController popToRootViewControllerAnimated:YES];
	
	if(popoverController != nil)
		[popoverController dismissPopoverAnimated:YES];

	[self.tableView deselectRowAtIndexPath:[self.tableView indexPathForSelectedRow] animated:NO];
	
	[[ExSystem sharedInstance].entitySettings setAutoLogin:@"NO"];
	[[ExSystem sharedInstance] saveSettings];
	
	[ExSystem sharedInstance].sys.sessionId = nil; //@"";
	if(![[ExSystem sharedInstance].sys.isOffline isEqualToNumber:[NSNumber numberWithBool:YES]])
		[[ExSystem sharedInstance] clearRoles];

//	LoginViewController *lvc = [[[LoginViewController alloc] initWithNibName:@"LoginView" bundle:nil] autorelease];
//	[lvc setRootViewController:rvc];
////	lvc.rvcPad = self;
////	lvc.modalPresentationStyle = UIModalPresentationFormSheet;
//	
//	
//
//	[self presentModalViewController:lvc animated:YES];

}


- (IBAction)buttonSettingsPressed:(id)sender
{
	SettingsViewController *svc = [[[SettingsViewController alloc] initWithNibName:@"SettingsView" bundle:nil] autorelease];
	svc.modalPresentationStyle = UIModalPresentationFormSheet;

	[self presentModalViewController:svc animated:YES];
	
}

#pragma mark -
#pragma mark Rotation support

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return YES;
}


- (void)splitViewController:(UISplitViewController*)svc willHideViewController:(UIViewController *)aViewController withBarButtonItem:(UIBarButtonItem*)barButtonItem forPopoverController:(UIPopoverController*)pc {
    
    // Keep references to the popover controller and the popover button, and tell the detail view controller to show the button.
    barButtonItem.title = [Localizer getLocalizedText:@"Navigator"];
    self.popoverController = pc;
    self.rootPopoverButtonItem = barButtonItem;
    UIViewController <SubstitutableDetailViewController> *myDVC = [splitViewController.viewControllers objectAtIndex:1];
    [myDVC showRootPopoverButtonItem:rootPopoverButtonItem];
	
	self.navigationController.navigationBar.barStyle = UIBarStyleBlackTranslucent;
}


- (void)splitViewController:(UISplitViewController*)svc willShowViewController:(UIViewController *)aViewController invalidatingBarButtonItem:(UIBarButtonItem *)barButtonItem {
	
    // Nil out references to the popover controller and the popover button, and tell the detail view controller to hide the button.
    UIViewController <SubstitutableDetailViewController> *myDVC = [splitViewController.viewControllers objectAtIndex:1];
    [myDVC invalidateRootPopoverButtonItem:rootPopoverButtonItem];
    self.popoverController = nil;
    self.rootPopoverButtonItem = nil;
	
	self.navigationController.navigationBar.barStyle = UIBarStyleBlackOpaque;
}

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad 
{
    [super viewDidLoad];

    self.clearsSelectionOnViewWillAppear = NO;
    self.contentSizeForViewInPopover = CGSizeMake(320.0, 500.0);
	
	if(detailViewController == nil)
	{
		detailViewController = [splitViewController.viewControllers objectAtIndex:1];
	}
	
	if ([ConcurMobileAppDelegate findRootViewController] == nil) 
	{
		[[ConcurMobileAppDelegate findRootViewController] doBaseInit];
	}
	
	self.title = [Localizer getLocalizedText:@"Navigator"];
	self.navigationController.navigationBar.barStyle = UIBarStyleBlackTranslucent;
	
	[self loadMainMenu];
}

-(void) loadMainMenu
{
	self.menuArray = [[NSMutableArray alloc] initWithObjects: nil]; //@"Rail", 
	if ([[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER] || [[ExSystem sharedInstance] hasRole:ROLE_ITINVIEWER_USER]) 
		[menuArray addObject:@"Trips"];
	
	if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER]) 
		[menuArray addObject:@"Expenses"];
	
	if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER]) 
		[menuArray addObject:@"Reports"];
	
	if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER]) 
		[menuArray addObject:@"Approvals"];
	
	[menuArray addObject:@"Home"];
	[self.tableView reloadData];
}


-(void) viewDidUnload {
    [super viewDidUnload];

    self.splitViewController = nil;

    self.rootPopoverButtonItem = nil;
}

 - (void)viewDidAppear:(BOOL)animated {
     [super viewDidAppear:animated];
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)aTableView {
    // Return the number of sections.
    return 1;
}


- (NSInteger)tableView:(UITableView *)aTableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    return [menuArray count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
	int row = indexPath.row;
	
    static NSString *CellIdentifier = @"RootCellPad";
	
	RootCellPad *cell = (RootCellPad *)[tableView dequeueReusableCellWithIdentifier: CellIdentifier];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"RootCellPad" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[RootCellPad class]])
				cell = (RootCellPad *)oneObject;
	}
	
    NSString *menuItem = [menuArray objectAtIndex:row];
    // Configure the cell.
	cell.lblLabel.text = [menuArray objectAtIndex:indexPath.row];
    
	if([menuItem isEqualToString:@"Trips"])
		cell.iv.image = [UIImage imageNamed:@"home_trip.png"];
	else if([menuItem isEqualToString:@"Expenses"])
		cell.iv.image = [UIImage imageNamed:@"home_card_charges.png"];
	else if([menuItem isEqualToString:@"Approvals"])
		cell.iv.image = [UIImage imageNamed:@"home_approval.png"];
	else if([menuItem isEqualToString:@"Reports"])
		cell.iv.image = [UIImage imageNamed:@"home_reports.png"];
	else if([menuItem isEqualToString:@"Rail"])
		cell.iv.image = [UIImage imageNamed:@"home_rail.png"];
	else if([menuItem isEqualToString:@"Home"])
		cell.iv.image = [UIImage imageNamed:@"iPadhome.png"];

    return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)aTableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    /*
     When a row is selected, set the detail view controller's detail item to the item associated with the selected row.
     */
    //detailViewController.detailItem = [menuArray objectAtIndex:indexPath.row]; //[NSString stringWithFormat:@"Row %d", indexPath.row];
	//int rowMod;
	
	NSString *menuItem = [menuArray objectAtIndex:indexPath.row];
	//@"Trips", @"Expenses", @"Reports", @"Approvals", @"Rail"
	
	if([menuItem isEqualToString:@"Trips"])
	{
		TripsListNavViewController *nextView =  [[TripsListNavViewController alloc] initWithNibName:@"TripsListNavi" bundle:nil];
		nextView.rvcPad = self;

		[nextView loadTrips];
		//This assumes you have another table view controller called NextViewController
		//We assign it to the instance variable "nextView"
		[self.navigationController pushViewController:nextView animated:YES];
		[nextView release];
	}
	else if([menuItem isEqualToString:@"Approvals"])
	{
		ReportApprovalListViewController *nextView = [[ReportApprovalListViewController alloc] initWithNibName:@"ReportApprovalListViewController" bundle:nil];
		nextView.rvcPad = self;
		nextView.isPad = YES;
		[nextView loadApprovals];

		[self.navigationController pushViewController:nextView animated:YES];
		[nextView release];
	}
	else if([menuItem isEqualToString:@"Expenses"])
	{
		[self switchToDetail:menuItem ParameterBag:nil];
	}
	else if([menuItem isEqualToString:@"Reports"])
	{
		//detailViewController.viewType = @"REPORTS";
		ActiveReportListViewController *nextView = [[ActiveReportListViewController alloc] initWithNibName:@"ReportApprovalListViewController" bundle:nil];
		nextView.rvcPad = self;
		nextView.isPad = YES;
		[nextView loadReports];

		[self.navigationController pushViewController:nextView animated:YES];
		[nextView release];
	}
	else if([menuItem isEqualToString:@"Home"])
	{
		[self switchToDetail:menuItem ParameterBag:nil];
	}
	
}


- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return 68;
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc. that aren't in use.
}


- (void)dealloc {
    [detailViewController release];
	[rvc release];
	[menuArray release];
	[popoverController release];
    [rootPopoverButtonItem release];
	[navigationBar release];
    [super dealloc];
}

-(void)resetButtons
{
	if(menuArray != nil)
		[menuArray release];
	
	self.menuArray = [[NSMutableArray alloc] initWithObjects: nil]; //@"Rail", 
	
	if ([[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER]|| [[ExSystem sharedInstance] hasRole:ROLE_ITINVIEWER_USER]) 
		[menuArray addObject:@"Trips"];
	
	if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER]) 
		[menuArray addObject:@"Expenses"];
	
	if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER]) 
		[menuArray addObject:@"Reports"];
	
	if ([[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_MANAGER]) 
		[menuArray addObject:@"Approvals"];
	
	[self.tableView reloadData];
}

#pragma mark -
#pragma mark Detail View Switching
//- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
-(void)switchToDetail:(NSString *)menuItem ParameterBag:(NSMutableDictionary *)pBag
{
    UIViewController <SubstitutableDetailViewController> *dVC = nil;
	
	if([menuItem isEqualToString:@"Home"])
	{
		iPadHomeVC *newDetailViewController = [[iPadHomeVC alloc] initWithNibName:@"iPadHomeVC" bundle:nil];
		dVC = newDetailViewController;
	}
	else if([menuItem isEqualToString:@"Expenses"])
	{
		DetailExpensesViewController *newDetailViewController = [[DetailExpensesViewController alloc] initWithNibName:@"DetailExpensesViewController" bundle:nil];
		dVC = newDetailViewController;
	}
	else if ([menuItem isEqualToString:@"Trip"])
	{
		if([[splitViewController.viewControllers objectAtIndex:1] isKindOfClass:[DetailViewController class]])
		{
			DetailViewController *vc = [splitViewController.viewControllers objectAtIndex:1];
			TripData *trip = [pBag objectForKey:@"TRIP"];
			[vc displayTrip:trip TripKey:trip.tripKey];
			
			// Dismiss the popover if it's present.
			if (popoverController != nil) {
				[popoverController dismissPopoverAnimated:YES];
			}
			return;
		}

		DetailViewController *newDetailViewController = [[DetailViewController alloc] initWithNibName:@"DetailView" bundle:nil];
		[newDetailViewController.ivLogo setHidden:YES];

		TripData *trip = [pBag objectForKey:@"TRIP"];
		[newDetailViewController displayTrip:trip TripKey:trip.tripKey];
		
		dVC = newDetailViewController;

	}
	else if ([menuItem isEqualToString:@"Report"])
	{
		DetailApprovalViewController *newDetailViewController = [[DetailApprovalViewController alloc] initWithNibName:@"DetailApprovalViewController" bundle:nil];
		newDetailViewController.isReport = YES;
		//newDetailViewController.parentVC = self;
		//newDetailViewController.rvcPad = self;
		newDetailViewController.role = ROLE_EXPENSE_TRAVELER;
		ReportData *rpt = [pBag objectForKey:@"REPORT"];
		
		if(rpt.receiptImageAvailable != nil && [rpt.receiptImageAvailable isEqualToString:@"Y"])
			newDetailViewController.hasReceipt = YES;
		
		if(rpt.lastComment != nil)
			newDetailViewController.hasComment = YES;
		
		
		
		NSMutableDictionary *pBagNew = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
									 ACTIVE_ENTRIES, @"TO_VIEW", nil];
		
		[[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORT_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBagNew SkipCache:NO RespondTo:newDetailViewController];
		[pBagNew release];
		
		dVC = newDetailViewController;
	}
	else if ([menuItem isEqualToString:@"Approval"])
	{
		ReportData *rpt = [pBag objectForKey:@"REPORT"];
		
		DetailApprovalViewController *newDetailViewController = [[DetailApprovalViewController alloc] initWithNibName:@"DetailApprovalViewController" bundle:nil];
		newDetailViewController.role = ROLE_EXPENSE_MANAGER;

		if(rpt.receiptImageAvailable != nil && [rpt.receiptImageAvailable isEqualToString:@"Y"])
			newDetailViewController.hasReceipt = YES;
		
		if(rpt.lastComment != nil)
			newDetailViewController.hasComment = YES;
		
		NSMutableDictionary *pBagNew = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY",
									 APPROVE_ENTRIES, @"TO_VIEW", nil];
		[[ExSystem sharedInstance].msgControl createMsg:APPROVE_REPORT_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBagNew SkipCache:NO RespondTo:newDetailViewController];
		[pBagNew release];
		
		dVC = newDetailViewController;
	}


	
    // Update the split view controller's view controllers array.
    NSArray *viewControllers = [[NSArray alloc] initWithObjects:self.navigationController, dVC, nil];
    splitViewController.viewControllers = viewControllers;
	
	if ([menuItem isEqualToString:@"Trip"])
	{
		//[dVC.ivLogo setHidden:YES];
		DetailViewController *vc = [splitViewController.viewControllers objectAtIndex:1];
		[vc.ivLogo setHidden:YES];
	}
	else if ([menuItem isEqualToString:@"Expenses"])
	{
		[(DetailExpensesViewController *)dVC loadExpenses];
	}
	
    [viewControllers release];
    
    // Dismiss the popover if it's present.
    if (popoverController != nil) {
        [popoverController dismissPopoverAnimated:YES];
    }
	
    // Configure the new view controller's popover button (after the view has been displayed and its toolbar/navigation bar has been created).
    if (rootPopoverButtonItem != nil) {
        [dVC showRootPopoverButtonItem:self.rootPopoverButtonItem];
    }
	
    [dVC release];
}

- (void)showRootPopoverButtonItem:(UIBarButtonItem *)barButtonItem
{

}
- (void)invalidateRootPopoverButtonItem:(UIBarButtonItem *)barButtonItem
{
    
}

@end