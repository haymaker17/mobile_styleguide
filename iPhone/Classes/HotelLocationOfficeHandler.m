//
//  HotelLocationOfficeHandler.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelLocationOfficeHandler.h"
#import "HotelLocationViewController.h"
#import "OfficeLocationResult.h"
#import "ExSystem.h" 



@implementation HotelLocationOfficeHandler


@synthesize officeLocations;
@synthesize filteredOfficeLocations;


#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return (filteredOfficeLocations == nil ? 0 : [filteredOfficeLocations count]);
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
    // MOB-12548 : 
    cell.textLabel.font = [UIFont fontWithName:@"Helvetica Neue" size:14.0];
    cell.textLabel.numberOfLines = 3;
    
	OfficeLocationResult *officeLocation = filteredOfficeLocations[row];
    // MOB-12548 : Strip newlines so address fits in correctly in cell
    NSString *location = [[officeLocation.location componentsSeparatedByCharactersInSet:[NSCharacterSet newlineCharacterSet]] componentsJoinedByString:@" "];
	cell.textLabel.text = location ;
    
    return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
	OfficeLocationResult *officeLocation = filteredOfficeLocations[row];
	[hotelLocationViewController locationSelected:officeLocation];
}


#pragma mark -
#pragma mark Search methods

-(void)doCancel
{
	hotelLocationViewController.activeSearchBar.text = @"";
	[hotelLocationViewController.activeSearchBar resignFirstResponder];
	[self doSearch:@""];
}

-(void)doSearch:(NSString *)searchText
{
	self.mostRecentSearchText = [NSString stringWithString:searchText];
	
	[self filterOfficeLocations:searchText];

	// Updating the view causes the cached results (or an empty table) to be shown
	[hotelLocationViewController updateView];
}

-(void)filterOfficeLocations:(NSString*)searchText
{
	if (filteredOfficeLocations == nil)
	{
		NSMutableArray* newArray = [[NSMutableArray alloc] initWithCapacity:[officeLocations count]];
		self.filteredOfficeLocations = newArray;
	}
	
	[filteredOfficeLocations removeAllObjects];
	
	if (searchText == nil || [searchText length] == 0)
	{
		[filteredOfficeLocations addObjectsFromArray:officeLocations];
	}
	else
	{
		NSString *lowercaseSearchText = [searchText lowercaseString];
		
		NSUInteger officeCount = [officeLocations count];
		for (int i = 0; i < officeCount; i++)
		{
			OfficeLocationResult *officeLocation = (OfficeLocationResult *)officeLocations[i];
			NSString *lowercaseLocation = [officeLocation.location lowercaseString];
			NSRange range = [lowercaseLocation rangeOfString:lowercaseSearchText];
			if (range.location != NSNotFound)
				[filteredOfficeLocations addObject:officeLocations[i]];
		}
	}
}

#pragma mark -
#pragma mark Handler activity methods

- (void)becameActiveHotelLocationHandler
{
	hotelLocationViewController.activeSearchBar.placeholder = [Localizer getLocalizedText:@"Select an office location"];

	hotelLocationViewController.activeSearchBar.showsCancelButton = YES;

	if (hotelLocationViewController.activeSearchBar.text == nil || [hotelLocationViewController.activeSearchBar.text length] == 0)
		[hotelLocationViewController.activeSearchBar resignFirstResponder];
	else
		[hotelLocationViewController.activeSearchBar becomeFirstResponder];
}


# pragma mark -
# pragma Lifecycle methods


@end
