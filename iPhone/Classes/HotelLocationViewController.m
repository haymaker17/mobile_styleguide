//
//  HotelLocationViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelLocationViewController.h"
#import "HotelLocationAddressHandler.h"
#import "HotelLocationOfficeHandler.h"
#import "ExSystem.h" 

#import "LocationResult.h"
#import "FindLocation.h"
#import "GovFindDutyLocation.h"
#import "OfficeLocationResult.h"
#import "SystemConfig.h"
#import "Config.h"

@interface HotelLocationViewController()
@property (nonatomic, strong) NSTimer *timer;
@end

@implementation HotelLocationViewController

@synthesize addressOnlyTableView;
@synthesize addressAndOfficeTableView;
@synthesize addressOnlySearchBar;
@synthesize addressAndOfficeSearchBar;
@synthesize tBar;
@synthesize segmentedControl;
@synthesize tag;
@synthesize locationDelegate;
@synthesize initialSearchLocation;
@synthesize activeHotelLocationHandler;
@synthesize hotelLocationAddressHandler;
@synthesize hotelLocationOfficeHandler;
@synthesize neverShowOffices, cancelBtn, isAirportOnly;

@dynamic activeTableView;
@dynamic activeSearchBar;


#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return HOTEL_LOCATION;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_MODAL;
}


-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:FIND_LOCATION] && !msg.errBody)
	{
		if ((msg.parameterBag)[@"ADDRESS"] != nil)
		{
			// If the address that was search on still matches the text in the search field,
			// then show the search results
			NSString *address = (NSString*)(msg.parameterBag)[@"ADDRESS"];
			NSString *currentAddress = self.activeSearchBar.text;
			if ([address isEqualToString:currentAddress])
			{
				FindLocation *findLocation = (FindLocation *)msg.responder;
				NSMutableArray* results = findLocation.locationResults;
				[activeHotelLocationHandler foundResults:results forAddress:address];
			}
		}
	}
    else if ([msg.idKey isEqualToString:GOV_FIND_DUTY_LOCATION] && !msg.errBody && !msg.errCode)
	{
		if ((msg.parameterBag)[@"ADDRESS"] != nil)
		{
			// If the address that was search on still matches the text in the search field,
			// then show the search results
			NSString *address = (NSString*)(msg.parameterBag)[@"ADDRESS"];
			NSString *currentAddress = self.activeSearchBar.text;
			if ([address isEqualToString:currentAddress])
			{
				GovFindDutyLocation *findLocation = (GovFindDutyLocation *)msg.responder;
				NSMutableArray* results = findLocation.locationResults;
				[activeHotelLocationHandler foundResults:results forAddress:address];
			}
		}
	}
}

-(void)findLocationsForAddress:(NSString*)address
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:address, @"ADDRESS", nil];
    if(isAirportOnly)
        pBag[@"AIRPORT_ONLY"] = @"true";
    
	[[ExSystem sharedInstance].msgControl createMsg:FIND_LOCATION CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(UITableView*)activeTableView
{
	return (addressOnlyTableView.hidden ? addressAndOfficeTableView : addressOnlyTableView);
}

-(UISearchBar*)activeSearchBar
{
	return (addressOnlyTableView.hidden ? addressAndOfficeSearchBar : addressOnlySearchBar);
}

-(void)updateView
{
	[self.activeTableView reloadData];
}


#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	tBar.topItem.title = [Localizer getLocalizedText:@"SEARCH_LOCN"];
	cancelBtn.title = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	
	[segmentedControl setTitle:[Localizer getLocalizedText:@"Address"] forSegmentAtIndex:0];
	[segmentedControl setTitle:[Localizer getLocalizedText:@"Office"] forSegmentAtIndex:1];

	addressOnlyTableView.rowHeight = 40;
	addressAndOfficeTableView.rowHeight = 40;
	
	addressOnlyTableView.delegate = self;		// UIScrollViewDelegate
	addressAndOfficeTableView.delegate = self;	// UIScrollViewDelegate
	
	if (hotelLocationAddressHandler == nil)
		self.hotelLocationAddressHandler = [[HotelLocationAddressHandler alloc] initWithHotelLocationViewController:self];
	
	if (hotelLocationOfficeHandler == nil)
		self.hotelLocationOfficeHandler = [[HotelLocationOfficeHandler alloc] initWithHotelLocationViewController:self];

	// For some reason, the table is not scrolling unless this is set programmatically.
	addressOnlyTableView.bounces = YES;
	addressAndOfficeTableView.bounces = YES;

    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
	
	if([UIDevice isPad])
	{
		tBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
		addressOnlySearchBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
		addressAndOfficeSearchBar.tintColor = [UIColor colorWithRed:162.0/255.0 green:160.0/255.0 blue:160.0/255.0 alpha:1];
	}
    
    tBar.tintColor = [UIColor darkBlueConcur_iOS6];
    tBar.alpha = 0.9f;
    
    addressOnlySearchBar.tintColor = [UIColor darkBlueConcur_iOS6];
    addressOnlySearchBar.alpha = 0.9f;
    addressAndOfficeSearchBar.tintColor = [UIColor darkBlueConcur_iOS6];
    addressAndOfficeSearchBar.alpha = 0.9f;
    // MOB-20252: fixes to enable accessibility for UIAutomation
    [addressOnlySearchBar setIsAccessibilityElement:YES];
    [addressOnlySearchBar setAccessibilityIdentifier:@"addressOnlySearchBar"];
    [addressAndOfficeSearchBar setIsAccessibilityElement:YES];
    [addressAndOfficeSearchBar setAccessibilityIdentifier:@"addressAndOfficeSearchBar"];

}

- (void)viewWillAppear:(BOOL)animated
{
	SystemConfig *systemConfig = [SystemConfig getSingleton];
	if (neverShowOffices ||
		(systemConfig == nil || systemConfig.officeLocations == nil))
	{
		addressOnlyTableView.hidden = NO;
		addressAndOfficeTableView.hidden = YES;
		segmentedControl.hidden = YES;
		
		if (initialSearchLocation != nil && initialSearchLocation.location != nil)
		{
			self.addressOnlySearchBar.text = initialSearchLocation.location;
			hotelLocationAddressHandler.mostRecentSearchText = [NSString stringWithString:initialSearchLocation.location];
			hotelLocationOfficeHandler.mostRecentSearchText = nil;
		}
		
		activeHotelLocationHandler = hotelLocationAddressHandler;
		[activeHotelLocationHandler becameActiveHotelLocationHandler];
		[self updateView];
	}
	else
	{
		((HotelLocationOfficeHandler*)hotelLocationOfficeHandler).officeLocations = systemConfig.officeLocations;
		
		addressOnlyTableView.hidden = YES;
		addressAndOfficeTableView.hidden = NO;
		segmentedControl.hidden = NO;
		
		NSUInteger requiredSegmentIndex = (initialSearchLocation != nil && [initialSearchLocation isKindOfClass:[OfficeLocationResult class]] ? 1 : 0);
		HotelLocationHandler *requiredHotelLocationHandler = (requiredSegmentIndex == 0 ? hotelLocationAddressHandler : hotelLocationOfficeHandler);
		HotelLocationHandler *otherHotelLocationHandler = (requiredSegmentIndex == 1 ? hotelLocationAddressHandler : hotelLocationOfficeHandler);
		
		if (initialSearchLocation != nil)
		{
			self.activeSearchBar.text = initialSearchLocation.location;
			requiredHotelLocationHandler.mostRecentSearchText = [NSString stringWithString:initialSearchLocation.location];
			otherHotelLocationHandler.mostRecentSearchText = nil;
		}
		else
		{
			self.activeSearchBar.text = nil;
			requiredHotelLocationHandler.mostRecentSearchText = nil;
			otherHotelLocationHandler.mostRecentSearchText = nil;
		}
		
		if (segmentedControl.selectedSegmentIndex != requiredSegmentIndex)
		{
			// Setting the segment index causes the segmentedControlValueChanged method to be called
			// which will perform additional logic and setting up.
			segmentedControl.selectedSegmentIndex = requiredSegmentIndex;
		}
		else
		{
			activeHotelLocationHandler = requiredHotelLocationHandler;
			[activeHotelLocationHandler becameActiveHotelLocationHandler];
			[self updateView];
		}
	}
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewDidLayoutSubviews
{
    if ([self respondsToSelector:@selector(topLayoutGuide)])
    {
        CGRect viewBounds = self.view.bounds;
        CGFloat topBarOffset = self.topLayoutGuide.length;
        
        [self.view setFrame:CGRectMake(viewBounds.origin.x, topBarOffset, viewBounds.size.width, viewBounds.size.height-topBarOffset)];
    }
}

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
	return [activeHotelLocationHandler numberOfSectionsInTableView:tableView];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return [activeHotelLocationHandler tableView:tableView numberOfRowsInSection:section];
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return [activeHotelLocationHandler tableView:tableView cellForRowAtIndexPath:indexPath];
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	return [activeHotelLocationHandler tableView:tableView didSelectRowAtIndexPath:indexPath];
}


#pragma mark -
#pragma mark Handler callbacks

- (void)locationSelected:(LocationResult *)locationResult
{
	[locationDelegate locationSelected:locationResult tag:tag];
	[self closeView];
}


#pragma mark -
#pragma mark UISearchBarDelegate Methods

- (void)searchBarCancelButtonClicked:(UISearchBar *)searchBar
{
	[activeHotelLocationHandler doCancel];
}

- (void)searchBarSearchButtonClicked:(UISearchBar *)searchBar
{
	if (searchBar.text == nil)
		return;
	
	//TODO: Show wait view
    [self.timer invalidate];
	[activeHotelLocationHandler doSearch:searchBar.text];
	
	[self.activeSearchBar resignFirstResponder];
}

-(void)searchBar:(UISearchBar*)searchBar textDidChange:(NSString*)searchText
{
    // MOB-17506 Disable "auto search" while type for Gov app.
    if (![Config isGov]) {
        // This timer is used to initiate a search only when the user pauses during typing, thereby reducing number of hits on the locationSearch endpoint
        [self.timer invalidate];
        self.timer = [NSTimer scheduledTimerWithTimeInterval:.9 target:self selector:@selector(initiateLocationSearch) userInfo:searchText repeats:NO];
        
    }
}

-(void) initiateLocationSearch
{
    NSString *searchText = [self.timer userInfo];
    [activeHotelLocationHandler doSearch:searchText];
}


#pragma mark -
#pragma mark UIScrollViewDelegate Methods

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
	[self.activeSearchBar resignFirstResponder];
}


#pragma mark -
#pragma mark UISegmentedControl Methods

-(IBAction) segmentedControlValueChanged:(id)sender
{
	NSUInteger index = segmentedControl.selectedSegmentIndex;
	
	// Remember the old handler
	HotelLocationHandler *oldHotelLocationHandler = activeHotelLocationHandler;
	
	// Switch to the new handler
	HotelLocationHandler *newHotelLocationHandler = (index == 0 ? hotelLocationAddressHandler : hotelLocationOfficeHandler);
	
	if (newHotelLocationHandler != oldHotelLocationHandler)
	{
		// Switch to new handler
		self.activeHotelLocationHandler = newHotelLocationHandler;

		// Update the search bar text BEFORE calling becameActiveHotelLocationHandler (which will rely on the updated text)
		self.activeSearchBar.text = (activeHotelLocationHandler.mostRecentSearchText != nil ? activeHotelLocationHandler.mostRecentSearchText : @"");
		[activeHotelLocationHandler becameActiveHotelLocationHandler];
		[activeHotelLocationHandler doSearch:self.activeSearchBar.text];
	}
}


#pragma mark -
#pragma mark Cancel and Close Methods

-(IBAction) btnCancel:(id)sender
{
	[self closeView];
}

-(void)closeView
{
    if([UIDevice isPad])
        [self dismissViewControllerAnimated:YES completion:nil];
    else
        [self dismissViewControllerAnimated:YES completion:nil];
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

