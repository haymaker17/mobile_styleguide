//
//  AppsMenuVC.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 11/11/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "AppsMenuVC.h"
#import "AppsMenuCell.h"

#define kLogo @"IVLOGO"
#define kAppName @"APPNAME"
#define kSelector @"ACTION"

@implementation AppsMenuVC

#pragma mark -
#pragma mark View lifecycle


- (void)viewDidLoad 
{
    [super viewDidLoad];
    
    self.appsList = [[NSMutableArray alloc] init];
    
	if([[ExSystem sharedInstance] hasRole:@"Taxi_User"] && [[ExSystem sharedInstance].sys.expenseCtryCode isEqualToString:@"US"])
    {
        //Taxi Magic
        NSDictionary *taxi = @{kAppName: @"Taxi Magic",kLogo: @"taxi_magic_logo",kSelector: @"buttonTaxiPressed:"};
        [self.appsList addObject:taxi];
    }
    
    //GateGuru
    if([[ExSystem sharedInstance] hasRole:@"GateGuru_User"])
    {
        NSDictionary *airport = @{kAppName: @"GateGuru",kLogo: @"gateguru_logo",kSelector: @"buttonAirportsPressed:"};
        [self.appsList addObject:airport];
    }
    
    // Metr0
    if([[ExSystem sharedInstance] hasRole:@"Metro_User"])
    {
        NSDictionary *metro = @{kAppName: @"Metr0",kLogo: @"metro",kSelector: @"buttonMetroPressed:"};
        [self.appsList addObject:metro];
    }
    
	
    self.bookingAppsList = [[NSMutableArray alloc] init];

    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER])
    {
        //Air
        NSDictionary *air = @{kAppName: @"LABEL_AIR",kLogo: @"itin_icon_flight",kSelector: @"buttonAirPressed:"};
        [self.bookingAppsList addObject:air];
        //Car
        NSDictionary *car = @{kAppName: @"Car",kLogo: @"itin_icon_car",kSelector: @"buttonCarPressed:"};
        [self.bookingAppsList addObject:car];
        
        //Hotel
        NSDictionary *lodging = @{kAppName: @"Hotel",kLogo: @"itin_icon_lodging",kSelector: @"buttonHotelPressed:"};
        [self.bookingAppsList addObject:lodging];
        
        if ([[ExSystem sharedInstance] hasRole:ROLE_AMTRAK_USER] || [[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER])
        {
            //Rail
            NSDictionary *lodging = @{kAppName: @"Rail",kLogo: @"itin_icon_rail",kSelector: @"buttonRailPressed:"};
            [self.bookingAppsList addObject:lodging];
        }
    }
    else if(([[ExSystem sharedInstance] hasRole:ROLE_AMTRAK_USER] || [[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER]) && ![[ExSystem sharedInstance] hasRole:ROLE_ITINVIEWER_USER])
    {
        //Rail
        NSDictionary *lodging = @{kAppName: @"Rail",kLogo: @"itin_icon_rail",kSelector: @"buttonRailPressed:"};
        [self.bookingAppsList addObject:lodging];
    }
        
	if([UIDevice isPad])
		self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
	
	[self.appsTable setBackgroundColor:[UIColor whiteColor]];
}

-(void)viewDidUnload
{
	self.appsList = nil;
    self.bookingAppsList = nil;
	self.iPadHome = nil;
	[super viewDidUnload];
}

#pragma mark -
#pragma mark Configure cell
-(void) configureCell:(AppsMenuCell *)cell forIndexPath:(NSIndexPath *)indexPath
{
    NSInteger row = [indexPath row];
    NSDictionary *data = nil;
    
    if (self.displayAppsOfType == BOOKING_APPS)
    {
        data = (NSDictionary *)(self.bookingAppsList)[row];
        cell.lblAppName.text = [data[kAppName] localize];
    }
    else 
    {
        data = (NSDictionary *)(self.appsList)[row];
        cell.lblAppName.text = data[kAppName];
    }
    
    cell.ivLogo.image =[UIImage imageNamed:data[kLogo]];
    
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    if (self.displayAppsOfType == BOOKING_APPS)
    {
        return [self.bookingAppsList count];
    }
    else
    {
        return [self.appsList count];
    }
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellId = @"AppsMenuCell";
    
    AppsMenuCell *cell = (AppsMenuCell *)[tableView dequeueReusableCellWithIdentifier:CellId];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"AppsMenuCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[AppsMenuCell class]])
				cell = (AppsMenuCell *)oneObject;
	}

    [self configureCell:cell forIndexPath:indexPath];
    
    return cell;
}

#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath 
{
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
	NSString * funct = (self.displayAppsOfType == BOOKING_APPS)?(self.bookingAppsList)[[indexPath row]][kSelector]:(self.appsList)[[indexPath row]][kSelector];
	SEL selector = NSSelectorFromString(funct);
	
	if (self.iPadHome != nil && [self.iPadHome respondsToSelector:selector] )
	{
        // MOB-10860 Need to pass the self object to the selector, since the selector expect an object.  Under ARC, this is necessary.
        [self.iPadHome performSelector:selector withObject:self afterDelay:0];
	}
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}




@end

