//
//  IgniteTripsDS.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteTripsDS.h"
#import "EntityTrip.h"
#import "IgniteTripCell.h"
#import "DateTimeFormatter.h"

@implementation IgniteTripsDS

@synthesize tableList, tripFilter, managedObjectContext, fetchedResultsController;
@synthesize delegate = _delegate;

-(void) setSeedData:(NSManagedObjectContext *)con withTripFilter:(NSString *)filter withTable:(UITableView *)tbl withDelegate:(id<IgniteTripsDelegate>) del
{
    self.managedObjectContext = con;
    self.tripFilter = filter;
    self.tableList = tbl;
    self.tableList.delegate = self;
    self.tableList.dataSource = self;
    
    self.delegate = del;
    //    [self initSections];
    
}

- (void)resetTable:(UITableView*) tbl
{
    self.tableList = tbl;
    self.tableList.delegate = self;
    self.tableList.dataSource = self;
}

-(void) didProcessMessage:(Msg *)msg
{
    
}



#pragma mark - Cell Config
- (void)configureCell:(IgniteTripCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityTrip *trip = (EntityTrip *)managedObject;
    

    cell.lblTripName.text = trip.tripName;
    // Temp fix for ARC
    NSString *startFormatted = [DateTimeFormatter formatDateTimeMediumByDate:trip.tripStartDateLocal];
    NSString *endFormatted = [DateTimeFormatter formatDateTimeMediumByDate:trip.tripEndDateLocal];
//    NSString *startFormatted = [DateTimeFormatter formatDateShortbyDate:trip.tripStartDateLocal];
//	NSString *endFormatted = [DateTimeFormatter formatDateShortbyDate:trip.tripEndDateLocal];
    
	cell.lblDates.text = [NSString stringWithFormat:@"%@ - %@", startFormatted, endFormatted];

    cell.lblTripDescription.text= trip.tripDescription;// @"Dreamforce 2012";

//	[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1; 
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
//    NSLog(@"%d", [[self.fetchedResultsController fetchedObjects] count]);
    return [[self.fetchedResultsController fetchedObjects] count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    IgniteTripCell *cell = (IgniteTripCell *)[self.tableList dequeueReusableCellWithIdentifier: @"IgniteTripCell"];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"IgniteTripCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[IgniteTripCell class]])
                cell = (IgniteTripCell *)oneObject;
    }
    
    [self configureCell:cell atIndexPath:indexPath];
    return cell;
}

#pragma mark -
#pragma mark Table Delegate Methods 
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityTrip *trip = (EntityTrip *)managedObject;
    if (self.delegate != nil)
        [self.delegate tripSelected:trip];
    
    [tableView deselectRowAtIndexPath:indexPath animated:NO];

}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 83;	
}

#pragma mark - Fetched results controller
- (void)fetchedResults 
{
    if (fetchedResultsController == nil) {
        
        NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityTrip" inManagedObjectContext:self.managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"tripEndDateLocal" ascending:YES];
        [fetchRequest setSortDescriptors:[NSArray arrayWithObject:sort]];
        
        NSFetchedResultsController *theFetchedResultsController = 
        [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest 
                                            managedObjectContext:self.managedObjectContext sectionNameKeyPath:nil 
                                                       cacheName:nil] ;
        self.fetchedResultsController = theFetchedResultsController;
        fetchedResultsController.delegate = self;
        
        
    }
    NSError *error;
	if (![fetchedResultsController performFetch:&error]) {
		// Update to handle the error appropriately.
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"IgniteTripsDS: fetchedResults %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];	
	}   
    
}



#pragma mark - Fetched results controller delegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
//    [self.delegate startHomeUpdate];
    [self.tableList beginUpdates];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo
           atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type
{
    switch(type)
    {
        case NSFetchedResultsChangeInsert:
            [self.tableList insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [self.tableList deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject
       atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type
      newIndexPath:(NSIndexPath *)newIndexPath
{
    UITableView *tableView = self.tableList;
    
    switch(type)
    {
            
        case NSFetchedResultsChangeInsert:
            [tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeUpdate:
            [self configureCell:(IgniteTripCell*)[self.tableList cellForRowAtIndexPath:indexPath] atIndexPath:indexPath];
            break;
            
        case NSFetchedResultsChangeMove:
            [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
            [tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath]withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableList endUpdates];
//    [self.delegate finishHomeUpdate];
}


@end
