//
//  TrainStationsVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainStationsVC.h"
#import "TrainStationsData.h"

@implementation TrainStationsVC
@synthesize tableList, aList, cityList, stateList, aSections, txtFrom, txtTo, isFrom, cpListOfItems, searching, lblStations, bcdFrom, bcdTo, tbTop, tbBottom, isShowingStates;
@synthesize loadingView, lblLoading, bbiTitle;
@synthesize cancelBtn,doneBtn, parentVC;

#pragma mark -
#pragma mark MVC Methods
-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:TRAIN_STATIONS])
	{

		if(aList != nil)
			[aList removeAllObjects];
		
		TrainStationsData *stationsData = (TrainStationsData *)msg.responder;

		for(NSString *stationCode in stationsData.keys)
		{
			TrainStationData *station = (stationsData.items)[stationCode];
			[aList addObject:station];
		}
		
		self.isShowingStates = NO;
		self.stateList = nil;
		[self makeListOfCitiesInState:nil];
		
		[tableList reloadData];
	}
	
	[loadingView setHidden:YES];

}

-(NSString *)getViewIDKey
{
	return @"TRAIN_STATIONS";
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
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

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
	//[loadingView setHidden:YES];
	self.tbTop.topItem.title = [Localizer getLocalizedText:@"Stations"];
	cancelBtn.title = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	doneBtn.title = [Localizer getLocalizedText:@"LABEL_DONE_BTN"];
	
	lblStations.text = [Localizer getLocalizedText:@"Loading Stations"];
	// Mob-3054
	lblLoading.text = [Localizer getLocalizedText:@"Loading Stations"];
	
	txtFrom.placeholder = [Localizer getLocalizedText:@"Station"];
	txtTo.placeholder = [Localizer getLocalizedText:@"To Station"];
	[self fetchStations:self];
	
    [super viewDidLoad];
	[self initStations ];
	self.title = [Localizer getLocalizedText:@"Rail"];
	[self textFromStarted:self];
	
	txtTo.text = bcdTo.val;
	
	if(isFrom)
		txtFrom.text = bcdFrom.val;
	else 
	{
		txtFrom.text = bcdTo.val;
		toName = bcdTo.val;
	}
	
	if(bcdTo.val != nil)
		toName = bcdTo.val;
	
	if(bcdFrom.val != nil)
		fromName = bcdFrom.val;
	
	[self configureToolbar];
	
	[bbiTitle setTitle:[Localizer getLocalizedText:@"Stations"]];
    
    searchBar.tintColor = [UIColor darkBlueConcur_iOS6];
    searchBar.alpha = 0.9f;
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



#pragma mark List Initializer
-(void)initStations
{
	cpListOfItems = [[NSMutableArray alloc] initWithObjects:nil];
	aList = [[NSMutableArray alloc] initWithObjects:nil];
	cityList = [[NSMutableArray alloc] initWithObjects:nil];
	stateList = nil;
	isShowingStates = NO;
}
	
	
#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	//return [aSections count];
	return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	//NSMutableArray *a = [aList objectAtIndex:section];
	if (isShowingStates)
	{
		return 1 + [stateList count];  // One "All states and provinces" row + a row for every state
	}
	else
	{
		if (searching)
			return [cpListOfItems count];
		else 
			return [cityList count];
	}
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	//NSUInteger section = [indexPath section];
	
	TrainBookingCell *cell = (TrainBookingCell*)[tableView dequeueReusableCellWithIdentifier:@"TRAIN_BOOKING_CELL"];
	if (cell == nil)
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TrainBookingCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[TrainBookingCell class]])
				cell = (TrainBookingCell *)oneObject;
	}
	
	if (isShowingStates)
	{
		if (row == 0)
			cell.lbl.text = [Localizer getLocalizedText:@"All States & Provinces"];
		else
			cell.lbl.text = stateList[(row - 1)];

		[cell.seg setHidden:YES];
		[cell.lbl setHidden:NO];
		[cell.lblValue setHidden:YES];
	}
	else
	{
		//NSArray *aStation = [cityList objectAtIndex:row];
		TrainStationData *station = cityList[row];
		
		if(searching)
		{
			//aStation =  [cpListOfItems objectAtIndex:row];
			station = cpListOfItems[row];
		}
		
		cell.lbl.text = station.stationCode; // [aStation objectAtIndex:1];
        if (station.stationName != nil && station.city && station.stationState!= nil && station.stationState != nil ) {
            cell.lblValue.text = [NSString stringWithFormat:@"%@ (%@, %@)", station.stationName, station.city, station.stationState]; // [aStation objectAtIndex:0];
        }
        else if (station.stationName != nil && station.city != nil && station.stationState == nil) {
            cell.lblValue.text = [NSString stringWithFormat:@"%@ (%@)", station.stationName, station.city];
        }
		
		[cell.seg setHidden:YES];
		[cell.lbl setHidden:NO];
		[cell.lblValue setHidden:NO];
		//[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
	}
	
	return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	//NSUInteger section = [indexPath section];
	
	if (isShowingStates)
	{
		if (row == 0)
			[self showCitiesInState:nil];	// Show cities in all states and provinces
		else
			[self showCitiesInState:stateList[(row - 1)]];
	}
	else
	{
		TrainStationData *station = cityList[row];
		if(searching)
			station = cpListOfItems[row];
		
		NSString *stationText = [NSString stringWithFormat:@"(%@) %@", station.stationCode, station.stationName];
		
		txtFrom.text = stationText;
		iataFrom = station.stationCode;
		fromName = [NSString stringWithFormat:@"(%@) %@", station.stationCode, station.stationName]; //station.stationName;

        if(isFrom)
        {
            bcdFrom.val = [NSString stringWithFormat:@"(%@) %@", station.stationCode, station.stationName];
            bcdFrom.stationDepart = station;
        }
        else
        {
            bcdTo.val = [NSString stringWithFormat:@"(%@) %@", station.stationCode, station.stationName];
            bcdTo.stationDepart = station;
        }

		searching = NO;
		//[tableList reloadData];
		[self doneView:self];
	}
}


#pragma mark -
#pragma mark SearchBar methods
- (void) searchBarSearchButtonClicked:(UISearchBar *)theSearchBar {
    [theSearchBar resignFirstResponder];
    if (isShowingStates)
		return;
    
	NSString *srchTxt = theSearchBar.text;
	
	if(srchTxt != nil && [srchTxt length] > 0)
	{
		searching = YES;
		[self searchTableView:theSearchBar.text];
		[tableList reloadData];
		//searching = NO;
	}
	else
	{
		searching = NO;
		[tableList reloadData];
	}
	
}

//-(IBAction) startSearchFrom:(id)sender
//{
//	if (isShowingStates)
//		return;
//	
//	NSString *srchTxt = txtFrom.text;
//	
//	if(srchTxt != nil && [srchTxt length] > 0)
//	{
//		searching = YES;
//		[self searchTableView:txtFrom.text];
//		[tableList reloadData];
//		//searching = NO;
//	}
//	else
//	{
//		searching = NO;
//		[tableList reloadData];
//	}
//	
//}
//
//
//-(IBAction) startSearchTo:(id)sender
//{
//	if (isShowingStates)
//		return;
//
//	NSString *srchTxt = txtTo.text;
//	
//	if(srchTxt != nil && [srchTxt length] > 0)
//	{
//		searching = YES;
//		[self searchTableView:txtTo.text];
//		[tableList reloadData];
//		//searching = NO;
//	}
//	else
//	{
//		searching = NO;
//		[tableList reloadData];
//	}
//	
//}


-(BOOL) existsInCopy:(TrainStationData *) currStation
{
	NSString *currStationCode = currStation.stationCode;
	
	for (TrainStationData *station in cpListOfItems)
	{
		NSString *sTemp = station.stationCode;
		
		if([sTemp isEqualToString:currStationCode])
		{
			//NSLog(@"sTemp = %@", sTemp);
			return YES;
		}
	}
	
	return NO;
	
}


- (void) searchTableView:(NSString *)searchText
{
	if (isShowingStates)
		return;
	
	[cpListOfItems removeAllObjects];
	searching = YES;
	
	if ([searchText length] == 3) 
	{
		//lets look at station codes and make these bubble to the top
		for (TrainStationData *station in cityList)
		{
			NSString *sTemp = station.stationCode;
			
			NSRange titleResultsRange = [sTemp rangeOfString:searchText options:NSCaseInsensitiveSearch];
			
			if (titleResultsRange.length > 0)
			{
				[cpListOfItems addObject:station];
			}
		}
	}
	
	
	for (TrainStationData *station in cityList)
	{
		NSString *sTemp = station.stationCode;
		
		if ([searchText length] == 3) 
		{
			if(![self existsInCopy:station])
			{
				sTemp = station.stationName;
				
				NSRange titleResultsRange = [sTemp rangeOfString:searchText options:NSCaseInsensitiveSearch];
				
				if (titleResultsRange.length > 0)
				{
					[cpListOfItems addObject:station];
				}
			}
		}
		else 
		{
			NSRange titleResultsRange = [sTemp rangeOfString:searchText options:NSCaseInsensitiveSearch];
			
			if (titleResultsRange.length > 0)
			{
				[cpListOfItems addObject:station];
			}
			else 
			{
				sTemp = station.stationName;
				
				titleResultsRange = [sTemp rangeOfString:searchText options:NSCaseInsensitiveSearch];
				
				if (titleResultsRange.length > 0)
				{
					[cpListOfItems addObject:station];
				}
			}
		}
	}
}


#pragma mark -
#pragma mark Text Field Delegate
- (BOOL)textFieldShouldClear:(UITextField *)textField
{
	
	textField.placeholder = [Localizer getLocalizedText:@"Station"]; //MOB-3757
	return YES;
}

- (BOOL)textFieldShouldReturn:(UITextField *)theTextField 
{
	[theTextField resignFirstResponder];

	return YES;
}

-(IBAction)textFromStarted:(id)sender
{
//	isFrom = YES;
//	//txtFrom.backgroundColor = [UIColor grayColor];
//	txtFrom.textColor = [UIColor blueColor];
//
//	//txtTo.backgroundColor = [UIColor whiteColor];
//	txtTo.textColor = [UIColor blackColor];
//	
//	[self configureStationsLabel];
}

-(IBAction)textToStarted:(id)sender
{
//	isFrom = NO;
//	//txtFrom.backgroundColor = [UIColor whiteColor];
//	txtFrom.textColor = [UIColor blackColor];
//	//txtTo.backgroundColor = [UIColor grayColor];
//	txtTo.textColor = [UIColor blueColor];
//	//txtTo.background = [UIImage imageNamed:@"blue_logo"];
//	//txtFrom.background = nil;
//	[self configureStationsLabel];
}

-(void)configureStationsLabel
{
	NSString *text;
	
	if (isShowingStates)
	{
		if (isFrom)
			text = [Localizer getLocalizedText:@"Available States/Provinces"];
		else
			text = [Localizer getLocalizedText:@"Available States/Provinces"];
	}
	else
	{
		if (isFrom)
			text = [Localizer getLocalizedText:@"Available Stations"];
		else
			text = [Localizer getLocalizedText:@"Available Stations"];
	}
	
	lblStations.text = text;
}

-(IBAction)textReset:(id)sender
{
	if (isShowingStates)
		return;
	
	searching = NO;
	[tableList reloadData];
}


#pragma mark -
#pragma mark Toolbar Metheads
- (void)configureToolbar
{
    self.navigationController.toolbarHidden = NO;
    
	if (isShowingStates)
	{
		NSArray *toolbarItems = @[];
		[self.navigationController setToolbarItems: toolbarItems ];
	}
	else
	{
		UIBarButtonItem *btnState = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"By State or Province"] style:UIBarButtonItemStyleBordered target:self action:@selector(showStates:)];
		NSArray *toolbarItems = @[btnState];
		[self setToolbarItems: toolbarItems ];
	}
}

-(IBAction)showCities:(id)sender
{
	[self showCitiesInState:nil]; // Show cities in all states
}

-(void)showCitiesInState:(NSString*)state
{
	[self makeListOfCitiesInState:state];

	isShowingStates = NO;
	searching = NO;
	
	[self configureStationsLabel];
	[tableList reloadData];
	[self scrollToFirstRow];
	[self configureToolbar];
}

-(IBAction)showStates:(id)sender
{
	// Figure out the list of states, if that hasn't already been done.
	if (stateList == nil)
		[self makeListOfStates];
	
	isShowingStates = YES;
	searching = NO;
	
	[self configureStationsLabel];
	[tableList reloadData];
	[self scrollToFirstRow];
	[self configureToolbar];
}

-(void)scrollToFirstRow
{
	if (isShowingStates)
	{
		if([stateList count] < 1)
			return;
	}
	else
	{
		if (searching)
		{
			if([cpListOfItems count] < 1)
				return;
		}
		else 
		{
			if([cityList count] < 1)
				return;
		}
	}
	
	NSUInteger path[2] = {0, 0};
	NSIndexPath *indexPath = [[NSIndexPath alloc] initWithIndexes:path length:2];
	[tableList scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionTop animated:NO];
}

-(void)makeListOfStates
{
	if (aList == nil)
		return;
	
	NSMutableDictionary* stateDict = [[NSMutableDictionary alloc] init];
	NSString *eachStationState;
	for (TrainStationData *eachStation in aList)
	{
		eachStationState = eachStation.stationState;
		if (eachStationState != nil)
			stateDict[eachStationState] = eachStationState;
	}
	
	self.stateList = [stateDict keysSortedByValueUsingComparator:(NSComparator)^(id left, id right)
					  {
						  return [((NSString*)left) caseInsensitiveCompare:((NSString*)right)];
					  }
					  ];
	
}

-(void)makeListOfCitiesInState:(NSString*)state
{
	if (state == nil)
	{
		self.cityList = [NSMutableArray arrayWithArray:aList];
		return;
	}
	
	NSString* lowerCaseSelectedState = [state lowercaseString];
	[cityList removeAllObjects];
	
	for (TrainStationData *eachStation in aList)
	{
		NSString *lowerCaseStationState = [eachStation.stationState lowercaseString];
		if ([lowerCaseStationState isEqualToString:lowerCaseSelectedState])
		{
			[cityList addObject:eachStation];
		}
	}
}
 
		 
-(IBAction)cancelView:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];	
}

-(IBAction)doneView:(id)sender
{
	//bcdTo.val = toName;
	//bcdFrom.val = fromName;
	[parentVC.tableList reloadData];
	
	[self.navigationController popViewControllerAnimated:YES]; // dismissModalViewControllerAnimated:YES];	
}


#pragma mark -
#pragma mark Fetching Methods
-(void)fetchStations:(id)sender
{
	[loadingView setHidden:NO];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
	
//	NSString *recordKey = @"0";
//	CacheMetaData *cmd = [[ExSystem sharedInstance].cacheData getCacheInfo:TRAIN_STATIONS UserID:[ExSystem sharedInstance].userName RecordKey:recordKey];
//	
//	if(cmd == nil)
		[[ExSystem sharedInstance].msgControl createMsg:TRAIN_STATIONS CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
//	else 
//		[[ExSystem sharedInstance].msgControl createMsg:TRAIN_STATIONS CacheOnly:@"YES" ParameterBag:pBag SkipCache:NO RespondTo:self];
		
	
}

@end



