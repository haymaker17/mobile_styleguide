//
//  MoreVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "MoreVC.h"
#import "SearchYodleeCardsVC.h"
//#import <DropboxSDK/DropboxSDK.h>
#import "SettingsViewController.h"
#import "YodleeCardAgreementVC.h"
#import "MobileAlertView.h"
#import "TripItUnlink.h"

#define kUnlinkFromTripIt 405021

@implementation MoreVC
@synthesize tableList, aRows, unlinkFromTripItAlertView;

- (void)dealloc
{
    
    if (unlinkFromTripItAlertView != nil)
		[unlinkFromTripItAlertView clearDelegate];
    
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    self.title = @"SmartExpense"; // [Localizer getLocalizedText:@"Individual Edition"];

    self.navigationController.navigationBar.alpha = 0.9f;
    
    //MOB-7606
    //Changed "Cancel" to be "Close" in the More view controller.
    UIBarButtonItem *btnCancelButton = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(cancelSettings:)]; 
    self.navigationItem.leftBarButtonItem = btnCancelButton;
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    self.tableList = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self makeRows];
    [self.tableList reloadData];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - Make Data
-(void) makeRows
{
    self.aRows = [[NSMutableArray alloc] initWithObjects: nil];
    
    EditField *f = [[EditField alloc] init];
    f.value = [Localizer getLocalizedText:@"Invite"];
    f.name = @"INVITE";
    f.img = [UIImage imageNamed:@"icon_attendee"];
    [aRows addObject:f];
//    
//    f = [[EditField alloc] init];
//    f.value = [Localizer getLocalizedText:@"Organization"];
//    f.name = @"ORGANIZATION";
//    f.img = [UIImage imageNamed:@"icon_organization"];
//    [aRows addObject:f];
//    [f release];
    
    f = [[EditField alloc] init];
    f.value = [Localizer getLocalizedText:@"Sync credit card"];
    f.name = @"CREDITCARD";
    f.img = [UIImage imageNamed:@"icon_add_card_more"];
    [aRows addObject:f];
    
//    f = [[EditField alloc] init];
//    if (![[DBSession sharedSession] isLinked]) 
//        f.value = [Localizer getLocalizedText:@"Link Dropbox"];
//    else
//        f.value = [Localizer getLocalizedText:@"Unlink Dropbox"];
//    f.name = @"DROPBOX";
//    f.img = [UIImage imageNamed:@"icon_dropbox"];
//    [aRows addObject:f];
    
    if (([ExSystem sharedInstance].isTripItLinked))
    {
        f = [[EditField alloc] init];
        f.value = [Localizer getLocalizedText:@"Unlink from TripIt"];
        f.name = @"TRIPIT";
        f.img = [UIImage imageNamed:@"icon_tripit"];
        [aRows addObject:f];
    }
    
//    f = [[EditField alloc] init];
//    f.value = [Localizer getLocalizedText:@"Abukai Expenses"];
//    f.name = @"ABUKAI";
//    f.img = [UIImage imageNamed:@"icon_abukai"];
//    [aRows addObject:f];
//    [f release];
    
    f = [[EditField alloc] init];
    f.value = [Localizer getLocalizedText:@"Settings"];
    f.name = @"SETTINGS";
    f.img = [UIImage imageNamed:@"icon_settings_su"];
    [aRows addObject:f];
}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSLog(@"row count %lu", (unsigned long)[aRows count]);
    return [aRows count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{

    //all info cells all the time	
    HomePageCell *cell = (HomePageCell *)[tableView dequeueReusableCellWithIdentifier: @"MoreCell"];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"MoreCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[HomePageCell class]])
                cell = (HomePageCell *)oneObject;
    }
    
    EditField *field = aRows[indexPath.row];
    cell.lblHeading.text = field.value;
    cell.lblSubheading.text = @"";
    cell.iv.image = field.img;
    
    return cell;

}



#pragma mark -
#pragma mark Table View Delegate Methods

- (NSString *)tableView:(UITableView *)tableView 
titleForHeaderInSection:(NSInteger)section
{

        return @"";
}


- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 54;	
}


- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath 
{
	EditField *field = aRows[indexPath.row];
    
    if([field.name isEqualToString:@"CREDITCARD"])
        [self showAddCards:nil];
    else if ([field.name isEqualToString:@"TRIPIT"])
        [self didPressTripIt];
    else if([field.name isEqualToString:@"SETTINGS"])
        [self showSettings:nil];
    else if([field.name isEqualToString:@"ABUKAI"])
        [self showAbukai:nil];
}

#pragma mark - TripIt
- (void)didPressTripIt
{
    self.unlinkFromTripItAlertView = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Please Confirm"] message:[Localizer getLocalizedText:@"Are you sure you want to unlink from your TripIt account?"] delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Cancel"] otherButtonTitles:[Localizer getLocalizedText:@"OK"], nil];
    [unlinkFromTripItAlertView show];
    unlinkFromTripItAlertView.tag = kUnlinkFromTripIt;
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
    if (alertView.tag == kUnlinkFromTripIt)
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
            [self makeRows];
            [self.tableList reloadData];
        }
    }
}
#pragma mark - MessageHandlers

-(void)respondToFoundData:(Msg *)msg
{
	[[MCLogging getInstance] log:@"MoreVC::respondToFoundData" Level:MC_LOG_DEBU];
    
	if ([msg.idKey isEqualToString:UNLINK_FROM_TRIPIT])
	{
        [self hideWaitView];
        
        TripItUnlink* unlinkData = (TripItUnlink*) msg.responder;
        if (msg.responseCode == 200 && [unlinkData isActionStatusSuccess])
        {
            [ExSystem sharedInstance].isTripItLinked = NO;
            [[ExSystem sharedInstance] saveSettings];
            
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Unlink Succeeded"] message:[Localizer getLocalizedText:@"Your account is no longer linked to TripIt."] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
            [av show];
        }
        else
        {
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Error"] message:[Localizer getLocalizedText:@"The account could not be unlinked."] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
            [av show];
        }
        
        [self makeRows];
        [self.tableList reloadData];
    }
}

#pragma mark - Yodlee
-(void)showAddCards:(id)sender
{
    // If there is never a card, pop up agreement view
    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_EXPENSE_BREEZE_CARD];
    MobileViewController* vc = nil;
    if (entity == nil)
    {
        vc = [[SearchYodleeCardsVC alloc] initWithNibName:@"SearchYodleeCardsVC" bundle:nil];
    }
    else
    {
        vc = [[YodleeCardAgreementVC alloc] initWithNibName:@"YodleeWebView" bundle:nil];
    }
    
    // Launch Search Yodlee Cards View
	[self.navigationController pushViewController:vc animated:YES];
    
    NSDictionary *dictionary = @{@"Action": @"Add Card from More"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
}

#pragma mark - Settings
-(void)showSettings:(id)sender
{
    SettingsViewController *vc = [[SettingsViewController alloc] init];
    [self.navigationController pushViewController:vc animated:YES];

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


#pragma mark - Abukai
-(void) showAbukai: (id)sender
{
//    NSString *launcher = [NSString stringWithFormat:@"abukaiexpenses://?email=\"%@\"&firstname=\"%@\"&lastname=\"%@\"&company=\"%@\"&source=concur", [ExSystem sharedInstance].sys.fbEmail, [ExSystem sharedInstance].sys.fbFirstName, [ExSystem sharedInstance].sys.fbLastName, [ExSystem sharedInstance].entitySettings.companyName];
//    NSLog(@"launcher = %@", launcher);
    
    NSString *launcher = [NSString stringWithFormat:@"abukaiexpenses://?email=%@&source=concur", [ExSystem sharedInstance].sys.fbEmail];
	BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:launcher]];
	
	if (didLaunch == NO) {
		NSURL *appStoreUrl = [NSURL URLWithString:@"http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewSoftware?id=370662888&mt=8"];
		[[UIApplication sharedApplication] openURL:appStoreUrl];
	}
}
@end
