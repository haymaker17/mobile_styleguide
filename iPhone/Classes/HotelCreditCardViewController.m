//
//  HotelCreditCardViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelCreditCardViewController.h"
#import "ExSystem.h" 

#import "HotelCreditCardCell.h"
#import "UserConfig.h"
#import "CreditCard.h"


@implementation HotelCreditCardViewController


@synthesize creditCardIndex;
@synthesize fromView, parentVC, isFromAir, airShopFilteredResultsVC;
@synthesize creditCards;

-(void)closeView
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"DONTPUSHVIEW", @"YES", @"POPTOVIEW", @"YES", @"SHORT_CIRCUIT", nil];
	
	if (creditCardIndex != nil)
		[pBag setValue:creditCardIndex forKey:@"CREDIT_CARD_INDEX"];
	
	if([fromView isEqualToString:@"TRAIN_BOOKING"])
	{
		[parentVC chooseCard:[creditCardIndex intValue]];
		[self.navigationController popViewControllerAnimated:YES];
		return;
	}
	
	// Return to the view from which we originated
	if([UIDevice isPad])
	{
		int parentIndex = (int)[self.navigationController.viewControllers count] - 2;
		
		UIViewController *vc = (self.navigationController.viewControllers)[parentIndex];
		MobileViewController *mvc = (MobileViewController *)vc;
		Msg *msg = [[Msg alloc] init];
		msg.idKey = @"SHORT_CIRCUIT";
		msg.parameterBag = pBag;
		[mvc respondToFoundData:msg];
		[self.navigationController popViewControllerAnimated:YES];
	}
	else 
		[ConcurMobileAppDelegate switchToView:fromView viewFrom:HOTEL_CREDIT_CARD ParameterBag:pBag];
}


#pragma mark -
#pragma mark MobileViewController Methods

-(NSString *)getViewIDKey
{
	return HOTEL_CREDIT_CARD;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
	{
		if ((msg.parameterBag)[@"CREDIT_CARD_INDEX"] != nil)
		{
			self.creditCardIndex = (NSNumber*)(msg.parameterBag)[@"CREDIT_CARD_INDEX"];
		}
		
		if ((msg.parameterBag)[@"FROM_VIEW"] != nil)
		{
			self.fromView = (NSString*)(msg.parameterBag)[@"FROM_VIEW"];
		}

        if ((msg.parameterBag)[@"CREDIT_CARDS"] != nil)
            self.creditCards = (NSArray*)(msg.parameterBag)[@"CREDIT_CARDS"];
        
		[tblView reloadData];
	}
}


#pragma mark -
#pragma mark View lifecycle

/*
- (void)viewDidLoad {
    [super viewDidLoad];

    // Uncomment the following line to preserve selection between presentations.
    self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}
*/

/*
- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}
*/

/*
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}
*/
/*
- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
}
*/
/*
- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
}
*/

// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return (self.creditCards == nil ? 0 : 1);
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return [self.creditCards count];
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	
	CreditCard *creditCard = self.creditCards[row];
	HotelCreditCardCell *cell = [HotelCreditCardCell makeCell:tableView owner:self cardName:creditCard.name cardNumber:creditCard.maskedNumber];
	
	if (creditCardIndex != nil)
	{
		NSUInteger index = [creditCardIndex integerValue];
		if (index == row)
			cell.accessoryType = UITableViewCellAccessoryCheckmark;
	}
	
	return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	self.creditCardIndex = [NSNumber numberWithInteger:row];
	[tblView reloadData];
    
    if(airShopFilteredResultsVC != nil)
    {
        [airShopFilteredResultsVC chooseCard:(int)row];
    }
	[self closeView];
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
}






@end

