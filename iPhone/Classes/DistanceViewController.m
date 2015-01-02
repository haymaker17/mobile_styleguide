//
//  DistanceViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "DistanceViewController.h"
#import "ExSystem.h" 



@implementation DistanceViewController

@synthesize values;
@synthesize distanceValue;
@synthesize isDistanceMetric;
@synthesize unitsButton;

-(IBAction)btnUnits:(id)sender
{
	BOOL oldIsMetric = [isDistanceMetric boolValue];
	BOOL newIsMetric = !oldIsMetric;
	
	NSNumber* isMetric = @(newIsMetric);
	self.isDistanceMetric = isMetric;
	
	//unitsButton.title = [self getUnitsButtonTitle];
	//[tblView reloadData];
	
	[self closeView];
}

-(NSString*)getUnitsButtonTitle
{
	NSString* title = [isDistanceMetric boolValue] ? [Localizer getLocalizedText:@"Switch to Miles"] : [Localizer getLocalizedText:@"Switch to KM"];
	return title;
}

-(void)closeView
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:distanceValue, @"DISTANCE_VALUE", isDistanceMetric, @"IS_METRIC_DISTANCE", @"YES", @"DONTPUSHVIEW", @"YES", @"POPTOVIEW", @"YES", @"SHORT_CIRCUIT", nil];

    NSUInteger vcCount = [self.navigationController.viewControllers count];
    UIViewController *vc = (self.navigationController.viewControllers)[vcCount - 2];
    MobileViewController *mvc = (MobileViewController *)vc;
    Msg *msg = [[Msg alloc] init];
    msg.idKey = @"SHORT_CIRCUIT";
    msg.parameterBag = pBag;
    [mvc respondToFoundData:msg];
    [self.navigationController popViewControllerAnimated:YES];
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return DISTANCE;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
	{
		if ((msg.parameterBag)[@"DISTANCE_VALUE"] != nil && (msg.parameterBag)[@"IS_METRIC_DISTANCE"] != nil)
		{
			self.distanceValue = (NSNumber*)(msg.parameterBag)[@"DISTANCE_VALUE"];
			self.isDistanceMetric = (NSNumber*)(msg.parameterBag)[@"IS_METRIC_DISTANCE"];
			[tblView reloadData];
			unitsButton.title = [self getUnitsButtonTitle];
		}
	}
}

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	
	tblView.rowHeight = 40;

	self.values = @[@1,
					    @2,
					    @5,
					    @10,
					    @15,
						@25,
						@100];

	NSString* title = [self getUnitsButtonTitle];
	
	UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	self.unitsButton = [[UIBarButtonItem alloc] initWithTitle:title style:UIBarButtonItemStyleBordered target:self action:@selector(btnUnits:)];
    
	NSArray *toolbarItems = @[flexibleSpace, unitsButton];
	[self setToolbarItems:toolbarItems animated:NO];
	
	
	// Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}


-(void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
    self.navigationController.toolbarHidden = NO;
}


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
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [values count];
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Cell";

    NSUInteger row = [indexPath row];
    
	NSNumber *value = (NSNumber*)values[row];
	int iValue = [value intValue];
	
	NSString *valueString = (iValue == 100) ? [Localizer getLocalizedText:@"Greater than 25"] : [value stringValue];
	NSString *valueFormatString;

	NSString *mile = [Localizer getLocalizedText:@"%@ mile"];
	NSString *miles = [Localizer getLocalizedText:@"%@ miles"];
	NSString *kilometer = [Localizer getLocalizedText:@"%@ km"];
	NSString *kilometers = [Localizer getLocalizedText:@"%@ kms"];
	if ([isDistanceMetric boolValue])
	{
		valueFormatString = (iValue > 1 ? kilometers : kilometer);
	}
	else
	{
		valueFormatString = (iValue > 1 ? miles : mile);
	}
	
	
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }

	cell.textLabel.text = [NSString stringWithFormat:valueFormatString, valueString];
	
	int iDistanceValue = [self.distanceValue intValue];
	if (iValue == iDistanceValue)
		cell.accessoryType = UITableViewCellAccessoryCheckmark;
	
    return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
	self.distanceValue = values[row];
	[self closeView];
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload
{
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
}



@end

