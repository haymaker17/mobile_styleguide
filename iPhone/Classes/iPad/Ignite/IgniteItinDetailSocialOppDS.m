//
//  IgniteItinDetailSocialOppDS.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteItinDetailSocialOppDS.h"
#import "IgniteOpportunityCell.h"
#import "TripManager.h"
#import "EntitySalesOpportunity.h"
#import "FormatUtils.h"
#import "TripData.h"
#import "SalesForceUserManager.h"

@implementation IgniteItinDetailSocialOppDS
@synthesize tableList, trip, tripKey, managedObjectContext, fetchedResultsController, lstContact;
@synthesize delegate = _delegate;
@synthesize draggedCell, draggedData;

- (void)initOppData
{
    [self fetchedResults];
    [self.tableList reloadData];   
}

-(void) setSeedData:(NSManagedObjectContext *)con withTripKey:(NSString *)tKey withTrip:(EntityTrip *)t withTable:(UITableView *)tbl withDelegate:(id<IgniteItinDetailSocialOppDelegate>) del
{
    self.managedObjectContext = con;
    self.tripKey = tKey;
    self.trip = t;
    if (tKey != nil)
    {
        self.trip = [[TripManager sharedInstance] fetchByTripKey:tripKey];
    }
    self.tableList = tbl;
    self.tableList.delegate = self;
    self.tableList.dataSource = self;
    
    self.delegate = del;
    
    // Get opportunities for the destination 
    NSString *city = [TripData getFirstDestination:self.trip];
    if (city != nil)
    {
        if ([[ExSystem sharedInstance] shouldSendRequestsOverNetwork])
        {
            [self.delegate loadingSocialOppData];
            NSMutableDictionary* pBag = [NSMutableDictionary dictionaryWithObject:city forKey:@"CITY"];
            [[ExSystem sharedInstance].msgControl createMsg:SALES_OPPORTUNITIES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        }
        else {
            // Still needs to hide the wait view
            [self.delegate socialOppDataReceived];
            [self initOppData];
        }
    }
}

-(void) didProcessMessage:(Msg *)msg
{
    [self respondToFoundData:msg];
}

-(void) respondToFoundData:(Msg *)msg
{
	if([msg.idKey isEqualToString:SALES_OPPORTUNITIES_DATA])
	{
        [self.delegate socialOppDataReceived];
        [self initOppData];
    }
}


-(void) dealloc
{
    [MsgHandler cancelAllRequestsForDelegate:self];
    
}


#pragma mark - Cell Config
- (void)configureCell:(IgniteOpportunityCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntitySalesOpportunity *opp = (EntitySalesOpportunity *)managedObject;
    
    cell.lblName.text = opp.contactName;
	cell.lblAmount.text = [FormatUtils formatMoneyWithNumber:opp.opportunityAmount crnCode:@"USD"];
    
    cell.lblAccountLocation.text= [NSString stringWithFormat:@"%@, %@", opp.accountName, opp.accountCity];
    cell.lblPhone.text = opp.opportunityName;
    
//    NSString *imageUrl = [NSString stringWithFormat:@"https://na9.salesforce.com/services/data/v20.0/sobjects/attachment/%@/body", opp.contactId];
//    NSString *imageUrl = [NSString stringWithFormat:@"https://na9.salesforce.com/services/data/v20.0/sobjects/attachment/%@/body", @"00PE00000015OL1MAM"];
    
    UIImage *img = [UIImage imageNamed: @"placeholder_headshot"];
    [cell.ivProfile setImage: img];

    if ([opp.contactId length] && [opp.contactId hasPrefix:@"/services/data"])
    {//Image url needs "/body" to get the binary image data.
        NSString *imageUrl = [NSString stringWithFormat:@"https://na9.salesforce.com%@/body", opp.contactId];
        NSString *imageCacheName = [NSString stringWithFormat:@"Photo_Small_Contact_%@", opp.contactName];
        [[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:imageUrl RespondToImage:img IV:cell.ivProfile MVC:nil ImageCacheName:imageCacheName OAuth2AccessToken:[[SalesForceUserManager sharedInstance] getAccessToken]];
//        [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    }
    
    if ([self.delegate isOpportunityScheduled:opp])
    {
        cell.backgroundView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"cell_light_gray_bckgrd"]];
        cell.selectedBackgroundView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"cell_light_gray_bckgrd"]];

        // None of that worked
//        cell.backgroundView.backgroundColor = [UIColor grayColor];
//        cell.vwBack.backgroundColor = [UIColor grayColor];
//        cell.highlighted = YES;
//        cell.selectionStyle = UITableViewCellSelectionStyleGray;
//        cell.selected = YES;
    }
    
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    
    return fetchedResultsController== nil ? 0: 1; 
//    return (lstContact != nil && [lstContact count]) > 0 ? 1 :0;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return fetchedResultsController== nil ? 0: [[self.fetchedResultsController fetchedObjects] count];
//    return lstContact == nil ? 0 : [lstContact count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
//    NSUInteger row = [indexPath row];
    
    //NSString *key = [lstContact objectAtIndex:row]; 
//    EntitySegment *segment = [segments objectAtIndex:indexPath.row];
    
    IgniteOpportunityCell *cell = (IgniteOpportunityCell *)[tableList dequeueReusableCellWithIdentifier: @"IgniteOpportunityCell"];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"IgniteOpportunityCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[IgniteOpportunityCell class]])
                cell = (IgniteOpportunityCell *)oneObject;
    }
    [self configureCell:cell atIndexPath:indexPath];
    
    return cell;
}

#pragma mark -
#pragma mark Table Delegate Methods 
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell* cell = [tableView cellForRowAtIndexPath:indexPath];
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntitySalesOpportunity *opp = (EntitySalesOpportunity *)managedObject;
    [self.delegate opportunitySelected:opp withFrame:cell.frame];
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 84;	
}


#pragma mark - Fetched results controller
- (void)fetchedResults 
{
    if (fetchedResultsController == nil) {
        
        NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntitySalesOpportunity" inManagedObjectContext:self.managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSString *city = [TripData getFirstDestination:self.trip];
        if ([city length])
        {
            NSPredicate *pred = [NSPredicate predicateWithFormat:@"(accountCity BEGINSWITH %@)", city];
            [fetchRequest setPredicate:pred];
        }
        NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"contactName" ascending:NO];
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
            [self configureCell:(IgniteOpportunityCell*)[self.tableList cellForRowAtIndexPath:indexPath] atIndexPath:indexPath];
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

#pragma drag and drop
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
    // TODO update your model
}

// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return NO;
}

- (UITableViewCell*)startDraggedCellWithCell:(UITableViewCell*)cell AtPoint:(CGPoint)point WithIndexPath:(NSIndexPath *)indexPath
{
    EntitySalesOpportunity* opp = [self.fetchedResultsController objectAtIndexPath:indexPath];
    
    if ([self.delegate isOpportunityScheduled:opp])
        return nil;
    
    [self.delegate addScheduledOpportunity:opp];
    
    self.draggedData = opp;
    IgniteOpportunityCell* srcCell = (IgniteOpportunityCell*) cell;
    
    // get rid of old cell, if it wasn't disposed already
    if(draggedCell != nil)
    {
        [draggedCell removeFromSuperview];
        self.draggedCell = nil;
    }
    
    CGRect frame = CGRectMake(point.x, point.y, cell.frame.size.width, cell.frame.size.height);
    
//    self.draggedCell = [[[UITableViewCell alloc] init] autorelease];

    NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"IgniteOpportunityCell" owner:self options:nil];
    for (id oneObject in nib)
        if ([oneObject isKindOfClass:[IgniteOpportunityCell class]])
            self.draggedCell = (IgniteOpportunityCell *)oneObject;
    
    draggedCell.selectionStyle = UITableViewCellSelectionStyleGray;
    draggedCell.lblName.text = srcCell.lblName.text;
    draggedCell.lblAccountLocation.text = srcCell.lblAccountLocation.text;
    draggedCell.lblAmount.text = srcCell.lblAmount.text;
    draggedCell.lblPhone.text = srcCell.lblPhone.text;
    draggedCell.textLabel.textColor = cell.textLabel.textColor;
    draggedCell.highlighted = YES;
    draggedCell.frame = frame;
    draggedCell.alpha = 0.8;
    
    [self.tableList reloadRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationNone];
    return draggedCell;
}

- (void) cancelDrop
{
    if (self.draggedData != nil)
    {
        [self.delegate removeScheduledOpportunity:self.draggedData];
        [self.tableList reloadData];
        [self.draggedCell removeFromSuperview];
        self.draggedCell = nil;
        self.draggedData = nil;
    }
}

#pragma mark -
#pragma mark UITableViewDataSource

//- (void)tableView:(UITableView*)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
//{
//    // enable cell deletion for destination table
//    if([tableView isEqual:dstTableView] && editingStyle == UITableViewCellEditingStyleDelete)
//    {
//        [dstData removeObjectAtIndex:indexPath.row];
//        [dstTableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
//        
//        [UIView animateWithDuration:0.2 animations:^
//         {
//             CGRect frame = dstTableView.frame;
//             frame.size.height = kCellHeight * [dstData count];
//             dstTableView.frame = frame;
//         }];
//    }
//}
//
//- (NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section
//{
//    // tell our tables how many rows they will have
//    int count = 0;
//    if([tableView isEqual:srcTableView])
//    {
//        count = [srcData count];
//    }
//    else if([tableView isEqual:dstTableView])
//    {
//        count = [dstData count];
//    }
//    return count;
//}
//

@end
