//
//  HotelLocationHandler.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelLocationHandler.h"
#import "HotelLocationViewController.h"


@implementation HotelLocationHandler


@synthesize hotelLocationViewController;
@synthesize mostRecentSearchText;


#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 0; // overriden
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 0; // overriden
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return nil; // overriden
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	// overriden
}


#pragma mark -
#pragma mark Search methods

-(void)doCancel
{
	// overriden
}

-(void)doSearch:(NSString *)searchText
{
	// overriden
}


#pragma mark -
#pragma mark Found results methods

-(void)foundResults:(NSMutableArray*)results forAddress:(NSString*)address
{
	// overridden
}


#pragma mark -
#pragma mark Handler activity methods

- (void)becameActiveHotelLocationHandler
{
	// overriden
}


# pragma mark -
# pragma Lifecycle methods

- (id)initWithHotelLocationViewController:(HotelLocationViewController *)vc
{
	self = [super init];
    if (self)
    {
        self.hotelLocationViewController = vc;
    }
	return self;
}

@end
