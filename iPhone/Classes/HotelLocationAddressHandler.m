//
//  HotelLocationAddressHandler.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelLocationAddressHandler.h"
#import "HotelLocationViewController.h"
#import "LocationResult.h"
#import "ExSystem.h" 



@implementation HotelLocationAddressHandler


@synthesize cache;
@synthesize cacheHistory;
@synthesize locationResults;


NSUInteger const MAX_CACHE_ENTRIES = 10;


#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return (locationResults == nil ? 0 : [locationResults count]);
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Cell";
	
    NSUInteger row = [indexPath row];
    
    __autoreleasing UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
    
    // Configure the cell...
	LocationResult *locationResult = locationResults[row];
	cell.textLabel.text = locationResult.location;
    
    return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
	LocationResult *locationResult = locationResults[row];
	[hotelLocationViewController locationSelected:locationResult];
}


#pragma mark -
#pragma mark Search methods
-(void)doSearch:(NSString *)searchText
{
	self.mostRecentSearchText = [NSString stringWithString:searchText];
	
	// Check the cache
	self.locationResults = (searchText == nil ? nil : [self cachedResultsForKey:searchText]);
	
	// Updating the view causes the cached results (or an empty table) to be shown
	[hotelLocationViewController updateView];
	
	// If it's not in the cache, and it's at least 3 characters, then send out a new search request
	if (self.locationResults == nil && [searchText length] > 2)
	{
		[hotelLocationViewController findLocationsForAddress:searchText];
	}
}


#pragma mark -
#pragma mark Found results methods

-(void)foundResults:(NSMutableArray*)results forAddress:(NSString*)address
{
	[self updateCacheWithResults:results forKey:address];
	
	self.locationResults = results;

	[hotelLocationViewController updateView];
}


#pragma mark -
#pragma mark Cache methods

-(void)updateCacheWithResults:(NSMutableArray*)results forKey:(NSString*)key
{
	// Create the cache and cache history, if they do not already exist
	if (cache == nil)
		self.cache = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	if (cacheHistory == nil)
		self.cacheHistory = [[NSMutableArray alloc] initWithObjects:nil];
	
	// If the key is already in the cache, then just update it
	if (cache[key] != nil)
	{
		cache[key] = results;
	}
	else
	{
		// If the cache is already full, then remove the oldest entry (beginning of array = oldest, end of array = newest)
		if ([cacheHistory count] >= MAX_CACHE_ENTRIES)
		{
			NSString* oldestKey = cacheHistory[0];
			[cacheHistory removeObjectAtIndex:0];
			[cache removeObjectForKey:oldestKey];
		}
		
		// Add the key to the cache
		cache[key] = results;
		
		// Add the key to the end of the cache history (beginning of array = oldest, end of array = newest)
		[cacheHistory addObject:key];
	}
}

-(NSMutableArray*)cachedResultsForKey:(NSString*)key
{
	NSMutableArray* results = nil;
	
	if (cache != nil)
	{
		results = cache[key];
	}
	
	return results;
}


#pragma mark -
#pragma mark Handler activity methods

- (void)becameActiveHotelLocationHandler
{
	hotelLocationViewController.activeSearchBar.placeholder = [Localizer getLocalizedText:@"Address point of interest airport code"];
	hotelLocationViewController.activeSearchBar.showsCancelButton = NO;
	[hotelLocationViewController.activeSearchBar becomeFirstResponder];
}


# pragma mark -
# pragma Lifecycle methods



@end
