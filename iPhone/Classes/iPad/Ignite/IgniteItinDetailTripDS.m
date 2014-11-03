//
//  IgniteItinDetailTripDS.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteItinDetailTripDS.h"
#import "EntitySegment.h"
#import "EntitySegmentLocation.h"
#import "IgniteSegmentCell.h"  
#import "DateTimeFormatter.h"
#import "SegmentData.h"
#import "TripManager.h"
#import "TripData.h"

@interface IgniteItinDetailTripDS (Private)
-(void) configureCellHotel:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellAir:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellCar:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellRail:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellParking:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellRide:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellMeeting:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment;
-(void) configureCellDining:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment;
-(void) initSections;
-(IgniteSegmentCell*) createUnscheduledCell:(EntitySegment *)segment;
- (void) addMeeting:(EntitySalesOpportunity*) opp afterIndexPath:(NSIndexPath*) indexPath atPoint:(CGPoint) pt;
- (void) addDining;
@end

@implementation IgniteItinDetailTripDS
@synthesize tableList;
@synthesize delegate = _delegate;
@synthesize /*fetchedResultsController, */managedObjectContext;
@synthesize lstDates, dictSegmentsByDate;
@synthesize trip, tripKey;
@synthesize droppedData, droppedCell, dictScheduledOppNames;

- (void) setSeedData:(NSManagedObjectContext *)con withTripKey:(NSString*) tKey withTrip:(EntityTrip*) t withTable:(UITableView*) tbl withDelegate:(id<IgniteItinDetailTripDelegate>)del
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
    [self initSections];
}

- (BOOL) isOpportunityScheduled:(NSString*) oppId
{
    return [self.dictScheduledOppNames containsObject:oppId];
}

- (void) addScheduledOpportunity:(NSString*) oppId
{
    [self.dictScheduledOppNames addObject:oppId];
}

- (void) initDictOppNames
{
    self.dictScheduledOppNames = [[NSMutableSet alloc] init];
    for (NSString *key in lstDates)
    {
        NSMutableArray *segments = [self.dictSegmentsByDate objectForKey:key];
        
        for (EntitySegment* seg in segments)
        {
            if ([seg.type isEqualToString:SEG_TYPE_EVENT])
            {
                NSString *oppName = [SegmentData getAttribute:@"OpportunityId" fromSegment:seg];
                if ([oppName length])
                    [self.dictScheduledOppNames addObject:oppName];
            }
        }
    }
}

// Sort segments 
-(void) initSections
{
    self.lstDates = [[NSMutableArray alloc] initWithObjects: nil];
    self.dictSegmentsByDate = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    
    self.lstDates = [TripData makeSegmentArrayGroupedByDate:trip];
    self.dictSegmentsByDate = [TripData makeSegmentDictGroupedByDate:trip];
    
    [self initDictOppNames];
    // Add Dining Recommendation if not there already
    [self addDining];
}

// Add a dining segment to first day, if last segment is not dining
- (void) addDining
{
    if (lstDates == nil || [lstDates count]==0)
        return;
    
    NSString *key = [lstDates objectAtIndex:0];
    NSMutableArray *segments = [self.dictSegmentsByDate objectForKey:key];
    
    int segCount = [segments count];
    if (segCount <= 0)
        return;
    
    EntitySegment* lastSeg = [segments objectAtIndex:(segCount-1)];
    EntitySegment* secondToLastSeg = segCount > 1 ? [segments objectAtIndex:(segCount-2)]:nil;
    // Check last two segments for dining, in case the user fixed the dining row
    // TODO - check all segment after 5pm
    if ([lastSeg.type isEqualToString:SEG_TYPE_DINING] || [secondToLastSeg.type isEqualToString:SEG_TYPE_DINING])
        return;
    
    // Get start/end date from existing segment in the same section
    NSString *startDateStr = lastSeg.relStartLocation.dateLocal;
    NSDate *startDate = [DateTimeFormatter getNSDate:startDateStr Format:@"yyyy-MM-dd'T'HH:mm:ss"  TimeZone:[NSTimeZone timeZoneWithName:@"GMT"]];
    NSDate *startDateAtDawn = [DateTimeFormatter getDateWithoutTimeInGMT:startDate];
    NSDate *diningStartDate = [startDateAtDawn dateByAddingTimeInterval:60*6*239.98]; // 19:30:00pm
    NSDate *diningEndDate = [startDateAtDawn dateByAddingTimeInterval:60*6*239.99]; // 20:30:00pm
    
    EntitySegment* segDining = [TripManager makeNewSegment:self.trip manContext:[self managedObjectContext]];
    segDining.relTrip = self.trip;
    segDining.segmentName = @"Dining Genius";
    segDining.vendorName = @"";
    segDining.type = SEG_TYPE_DINING;
    segDining.relStartLocation.dateLocal = [DateTimeFormatter formatDateTimeForTravelCliqbookByDate:diningStartDate];
    segDining.relStartLocation.address = @"";
    
    NSString *city = [TripData getFirstDestination:self.trip];
    segDining.relStartLocation.city = city; //@"San Francisco"; // Get segment destination
    segDining.relStartLocation.state = @"";
    segDining.relStartLocation.postalCode = @"";

    segDining.relEndLocation.dateLocal = [DateTimeFormatter formatDateTimeForTravelCliqbookByDate:diningEndDate];
    segDining.status = STATUS_SEGMENT_UNSCHEDULED;
    [TripManager saveItWithContext:droppedData manContext:[self managedObjectContext]];
    
    [segments addObject:segDining];
    // Resort segment orders
    self.dictSegmentsByDate = [TripData makeSegmentDictGroupedByDate:trip];
}

- (void) segmentUpdated:(EntitySegment*) segment
{
    [self initSections];
    [self.tableList reloadData];
}

-(void) dealloc
{
    [MsgHandler cancelAllRequestsForDelegate:self];

    
//    [fetchedResultsController release];
    
}


-(void) didProcessMessage:(Msg *)msg
{
    
}



#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [lstDates count];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if ([lstDates count] == 0)
        return 0;
    
    NSString *key = [lstDates objectAtIndex:section];
    NSArray *nameSection = (NSArray*)[dictSegmentsByDate objectForKey:key];
    
    return [nameSection count];

//    id <NSFetchedResultsSectionInfo> sectionInfo = [[self.fetchedResultsController sections] objectAtIndex:section];
//    return [sectionInfo numberOfObjects];
}

- (EntitySegment*) segmentFromIndexPath:(NSIndexPath *) indexPath
{
    NSUInteger section = [indexPath section];
    
    NSString *key = [lstDates objectAtIndex:section]; 
    NSArray *segments = [dictSegmentsByDate objectForKey:key];
    EntitySegment *segment = [segments objectAtIndex:indexPath.row];
    return segment;
}
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    EntitySegment *segment = [self segmentFromIndexPath:indexPath];
    
    IgniteSegmentCell *cell = nil;
    if (![SegmentData isSegmentScheduled:segment])
    {
        cell = [self createUnscheduledCell:segment];
    }
    else {
        cell = (IgniteSegmentCell *)[tableList dequeueReusableCellWithIdentifier: @"TripSegmentCell"];
        if (cell == nil)  
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"IgniteItinTripSegmentCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[IgniteSegmentCell class]])
                    cell = (IgniteSegmentCell *)oneObject;
        }
        CGRect rect = cell.lblTime.frame;
        cell.lblTime.frame = CGRectMake(rect.origin.x, rect.origin.y, 65, 21);
        cell.lblTime.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:20.0f];
        cell.lblTime.numberOfLines = 1;
    }    

    if([segment.type isEqualToString:SEG_TYPE_EVENT])
        [self configureCellMeeting:cell segment:segment];
    if([segment.type isEqualToString:SEG_TYPE_DINING])
        [self configureCellDining:cell segment:segment];
    if([segment.type isEqualToString:SEG_TYPE_AIR])
        [self configureCellAir:cell segment:segment];
    else if([segment.type isEqualToString:SEG_TYPE_CAR])
        [self configureCellCar:cell segment:segment];
    else if([segment.type isEqualToString:SEG_TYPE_HOTEL])
        [self configureCellHotel:cell segment:segment];
    else if([segment.type isEqualToString:SEG_TYPE_RIDE])
        [self configureCellRide:cell segment:segment];
    else if([segment.type isEqualToString:SEG_TYPE_RAIL])
        [self configureCellRail:cell segment:segment];
    else if([segment.type isEqualToString:SEG_TYPE_PARKING])
        [self configureCellParking:cell segment:segment];
    
//    [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];

    return cell;
}




#pragma mark -
#pragma mark Table Delegate Methods 
//-(UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
//{
//    return nil;
//}

//- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section 
//{	
//    return [self.lstDates objectAtIndex:section];
//}

-(UIView *) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    CGRect lblRect =  CGRectMake(8, 6, 412, 17); // 6 px paddling

    UILabel* labelView = [[UILabel alloc] initWithFrame:lblRect];
    UIView* footerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 412, 30)];
    footerView.autoresizingMask =UIViewAutoresizingFlexibleWidth;
    footerView.backgroundColor = [UIColor colorWithRed:21.0/255.0 green:21.0/255.0 blue:21.0/255.0 alpha:1.0f];
    [labelView setText:[self.lstDates objectAtIndex:section]];
    [labelView setBackgroundColor:[UIColor clearColor]];
    [labelView setFont:[UIFont fontWithName:@"HelveticaNeue" size:16.0f]];
    [labelView setTextColor:[UIColor whiteColor]];
    [labelView setShadowColor:[UIColor whiteColor]]; 
//    [labelView setShadowOffset:CGSizeMake(1.0f, 1.0f)];
    labelView.numberOfLines = 1;
    labelView.lineBreakMode = NSLineBreakByTruncatingTail;
    labelView.autoresizingMask = UIViewAutoresizingFlexibleWidth; // Do not adjust height
    [footerView addSubview:labelView];

    return footerView;
}

-(CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 30;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    EntitySegment *segment = [self segmentFromIndexPath:indexPath];
    UITableViewCell *cell = [self.tableList cellForRowAtIndexPath:indexPath];
    if (self.delegate != nil)
        [self.delegate segmentSelected:segment withCell:cell];
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 84;	
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
    return 0;
}

#pragma drag and drop
- (void) moveMeetingAfterIndexPath:(NSIndexPath*) indexPath
{
    NSIndexPath* oldPath = [self getDroppedCellIndexPath];
    NSIndexPath *newPath = [NSIndexPath indexPathForRow:indexPath.row+1 inSection:indexPath.section];
//    NSLog(@"OldPath %@, newPath%@", oldPath, newPath); 
    NSString *oldKey = [lstDates objectAtIndex:[oldPath section]];
    NSMutableArray *oldSegments = [self.dictSegmentsByDate objectForKey:oldKey];
    [oldSegments removeObjectAtIndex:[oldPath row]];
    NSString *newKey = [lstDates objectAtIndex:[newPath section]];
    NSMutableArray *newSegments = [self.dictSegmentsByDate objectForKey:newKey];
    if ([oldKey isEqualToString:newKey] && [oldPath row] < [newPath row]) // If in the same section in front of the new row, reduce row by one
    {
        newPath = indexPath;
    }
    [newSegments insertObject:self.droppedData atIndex:[newPath row]];
    
//    [self.tableList reloadData];
    [self.tableList beginUpdates];
    [self.tableList deleteRowsAtIndexPaths:[NSArray arrayWithObject:oldPath] withRowAnimation:UITableViewRowAnimationFade];
    [self.tableList insertRowsAtIndexPaths:[NSArray arrayWithObject:newPath] withRowAnimation:UITableViewRowAnimationFade];
    [self.tableList endUpdates];
}

- (UITableViewCell*)startDroppedCellAtPoint:(CGPoint)point withData:(EntitySalesOpportunity *) opp
{
    NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"IgniteItinUnscheduledCell" owner:self options:nil];
    for (id oneObject in nib)
        if ([oneObject isKindOfClass:[IgniteSegmentCell class]])
            self.droppedCell = (IgniteSegmentCell *)oneObject;
    
    CGRect frame = CGRectMake(point.x, point.y, self.tableList.frame.size.width, droppedCell.frame.size.height);
    
    droppedCell.selectionStyle = UITableViewCellSelectionStyleGray;
    droppedCell.lblHeading.text = @"Unscheduled Meeting";
    droppedCell.lblSub1.text = opp.accountName;
    droppedCell.lblSub2.text = opp.contactName;
    droppedCell.ivIcon.image = [UIImage imageNamed:@"itin_icon_event"];
    droppedCell.highlighted = YES;
    droppedCell.frame = frame;
    droppedCell.alpha = 0.8;
    
    return droppedCell;
}

- (void) addMeeting:(EntitySalesOpportunity*) opp afterIndexPath:(NSIndexPath*) indexPath atPoint:(CGPoint) pt
{
    if (self.droppedCell != nil)
        return;
    
    [self startDroppedCellAtPoint:pt withData:opp]; // Create cell early to block duplicate creation

    NSIndexPath *newPath = [NSIndexPath indexPathForRow:indexPath.row+1 inSection:indexPath.section];
    
    // Open a cell and morph the dragged cell, if hasn't been done before
    NSString *key = [lstDates objectAtIndex:[indexPath section]];
    NSMutableArray *segments = [self.dictSegmentsByDate objectForKey:key];
    
    // Get start/end date from existing segment in the same section
    EntitySegment* existingSegment = [segments objectAtIndex:0];
    NSString *startDateStr = existingSegment.relStartLocation.dateLocal;
    NSDate *startDate = [DateTimeFormatter getNSDate:startDateStr Format:@"yyyy-MM-dd'T'HH:mm:ss"  TimeZone:[NSTimeZone timeZoneWithName:@"GMT"]];
    NSDate *startDateAtDawn = [DateTimeFormatter getDateWithoutTimeInGMT:startDate];
    NSDate *mtgStartDate = [startDateAtDawn dateByAddingTimeInterval:60*6*145]; // 2:30pm
    NSDate *mtgEndDate = [startDateAtDawn dateByAddingTimeInterval:60*6*155]; // 3:30pm
    
    self.droppedData = [TripManager makeNewSegment:self.trip manContext:[self managedObjectContext]];
    droppedData.relTrip = self.trip;
    droppedData.segmentName = @"Unscheduled Meeting";
    [SegmentData setAttribute:@"OpportunityId" withValue:opp.opportunityId toSegment:droppedData];
    [SegmentData setAttribute:@"OpportunityName" withValue:opp.opportunityName toSegment:droppedData];
    [SegmentData setAttribute:@"AccountName" withValue:opp.accountName toSegment:droppedData];
    [SegmentData setAttribute:@"ContactName" withValue:opp.contactName toSegment:droppedData];
    droppedData.type = SEG_TYPE_EVENT;
    droppedData.relStartLocation.dateLocal = [DateTimeFormatter formatDateTimeForTravelCliqbookByDate:mtgStartDate];
    droppedData.relEndLocation.dateLocal = [DateTimeFormatter formatDateTimeForTravelCliqbookByDate:mtgEndDate];
    droppedData.status = STATUS_SEGMENT_UNSCHEDULED;
    [TripManager saveItWithContext:droppedData manContext:[self managedObjectContext]];
    
//    if(indexPath != nil)
//    {
//        NSIndexPath *newPath = [NSIndexPath indexPathForRow:indexPath.row+1 inSection:indexPath.section];
        [segments insertObject:droppedData atIndex:newPath.row];
        [self.tableList insertRowsAtIndexPaths:[NSArray arrayWithObject:newPath] withRowAnimation:UITableViewRowAnimationMiddle];
//    }
//    else
//    { // NO need: IndexPath is always non-nil.  could be -1
//        [segments addObject:droppedData];
//        [self.tableList reloadData]; // TODO - animation
//    }
    
}

- (NSIndexPath*) getDroppedCellIndexPath
{
    for (int ix = 0; ix < [self.lstDates count]; ix++)
    {
        NSArray* segs = [self.dictSegmentsByDate objectForKey:[self.lstDates objectAtIndex:ix]];
        for (int jx = 0; jx < [segs count]; jx++)
        {
            if ([segs objectAtIndex:jx]==self.droppedData)
                return [NSIndexPath indexPathForRow:jx inSection:ix];
        }
    }
    return nil;
}

- (void) removeDroppedData
{
    if (self.droppedData == nil)
        return;

    NSIndexPath* droppedCellIndex = [self getDroppedCellIndexPath];
    NSMutableArray *segs = [self.dictSegmentsByDate objectForKey:[self.lstDates objectAtIndex:droppedCellIndex.section]];
    [segs removeObjectAtIndex:droppedCellIndex.row];
    
    [self.tableList deleteRowsAtIndexPaths:[NSArray arrayWithObject:droppedCellIndex] withRowAnimation:UITableViewRowAnimationFade];

    [self.droppedCell removeFromSuperview];
    self.droppedCell = nil;
    self.droppedData = nil;
}
/*// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
    // TODO update your model
}

// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}

#pragma mark -
#pragma mark UIGestureRecognizer

- (void)handlePanning:(UIPanGestureRecognizer *)gestureRecognizer
{
    switch ([gestureRecognizer state]) {
        case UIGestureRecognizerStateBegan:
            [self startDragging:gestureRecognizer];
            break;
        case UIGestureRecognizerStateChanged:
            [self doDrag:gestureRecognizer];
            break;
        case UIGestureRecognizerStateEnded:
        case UIGestureRecognizerStateCancelled:
        case UIGestureRecognizerStateFailed:
            [self stopDragging:gestureRecognizer];
            break;
        default:
            break;
    }
}

#pragma mark -
#pragma mark Helper methods for dragging

- (void)startDragging:(UIPanGestureRecognizer *)gestureRecognizer
{
    CGPoint pointInSrc = [gestureRecognizer locationInView:srcTableView];
    CGPoint pointInDst = [gestureRecognizer locationInView:dstTableView];
    
    if([srcTableView pointInside:pointInSrc withEvent:nil])
    {
        [self startDraggingFromSrcAtPoint:pointInSrc];
        dragFromSource = YES;
    }
    else if([dstTableView pointInside:pointInDst withEvent:nil])
    {
        [self startDraggingFromDstAtPoint:pointInDst];
        dragFromSource = NO;
    }
}

- (void)startDraggingFromSrcAtPoint:(CGPoint)point
{
    NSIndexPath* indexPath = [srcTableView indexPathForRowAtPoint:point];
    UITableViewCell* cell = [srcTableView cellForRowAtIndexPath:indexPath];
    if(cell != nil)
    {
        CGPoint origin = cell.frame.origin;
        origin.x += srcTableView.frame.origin.x;
        origin.y += srcTableView.frame.origin.y;
        
        [self initDraggedCellWithCell:cell AtPoint:origin];
        cell.highlighted = NO;
        
        if(draggedData != nil)
        {
            [draggedData release];
            draggedData = nil;
        }
        draggedData = [[srcData objectAtIndex:indexPath.row] retain];
    }
}

- (void)startDraggingFromDstAtPoint:(CGPoint)point
{
    NSIndexPath* indexPath = [dstTableView indexPathForRowAtPoint:point];
    UITableViewCell* cell = [dstTableView cellForRowAtIndexPath:indexPath];
    if(cell != nil)
    {
        CGPoint origin = cell.frame.origin;
        origin.x += dropArea.frame.origin.x;
        origin.y += dropArea.frame.origin.y;
        
        [self initDraggedCellWithCell:cell AtPoint:origin];
        cell.highlighted = NO;
        
        if(draggedData != nil)
        {
            [draggedData release];
            draggedData = nil;
        }
        draggedData = [[dstData objectAtIndex:indexPath.row] retain];
        
        // remove old cell
        [dstData removeObjectAtIndex:indexPath.row];
        [dstTableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationMiddle];
        pathFromDstTable = [indexPath retain];
        
        [UIView animateWithDuration:0.2 animations:^
         {
             CGRect frame = dstTableView.frame;
             frame.size.height = kCellHeight * [dstData count];
             dstTableView.frame = frame;
         }];
        
    }
}

- (void)doDrag:(UIPanGestureRecognizer *)gestureRecognizer
{
    if(draggedCell != nil && draggedData != nil)
    {
        CGPoint translation = [gestureRecognizer translationInView:[draggedCell superview]];
        [draggedCell setCenter:CGPointMake([draggedCell center].x + translation.x,
                                           [draggedCell center].y + translation.y)];
        [gestureRecognizer setTranslation:CGPointZero inView:[draggedCell superview]];
    }
}

- (void)stopDragging:(UIPanGestureRecognizer *)gestureRecognizer
{
    if(draggedCell != nil && draggedData != nil)
    {
        if([gestureRecognizer state] == UIGestureRecognizerStateEnded
           && [dropArea pointInside:[gestureRecognizer locationInView:dropArea] withEvent:nil])
        {            
            NSIndexPath* indexPath = [dstTableView indexPathForRowAtPoint:[gestureRecognizer locationInView:dstTableView]];
            if(indexPath != nil)
            {
                [dstData insertObject:draggedData atIndex:indexPath.row];
                [dstTableView insertRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationMiddle];
            }
            else
            {
                [dstData addObject:draggedData];
                [dstTableView reloadData];
            }
        }
        else if(!dragFromSource && pathFromDstTable != nil)
        {
            // insert cell back where it came from
            [dstData insertObject:draggedData atIndex:pathFromDstTable.row];
            [dstTableView insertRowsAtIndexPaths:[NSArray arrayWithObject:pathFromDstTable] withRowAnimation:UITableViewRowAnimationMiddle];
            
            [pathFromDstTable release];
            pathFromDstTable = nil;
        }
        
        [UIView animateWithDuration:0.3 animations:^
         {
             CGRect frame = dstTableView.frame;
             frame.size.height = kCellHeight * [dstData count];
             dstTableView.frame = frame;
         }];
        
        [draggedCell removeFromSuperview];
        [draggedCell release];
        draggedCell = nil;
        
        [draggedData release];
        draggedData = nil;
    }
}

#pragma mark -
#pragma mark UITableViewDataSource

- (BOOL)tableView:(UITableView*)tableView canMoveRowAtIndexPath:(NSIndexPath*)indexPath
{
    // disable build in reodering functionality
    return NO;
}

- (void)tableView:(UITableView*)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    // enable cell deletion for destination table
    if([tableView isEqual:dstTableView] && editingStyle == UITableViewCellEditingStyleDelete)
    {
        [dstData removeObjectAtIndex:indexPath.row];
        [dstTableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
        
        [UIView animateWithDuration:0.2 animations:^
         {
             CGRect frame = dstTableView.frame;
             frame.size.height = kCellHeight * [dstData count];
             dstTableView.frame = frame;
         }];
    }
}

- (NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section
{
    // tell our tables how many rows they will have
    int count = 0;
    if([tableView isEqual:srcTableView])
    {
        count = [srcData count];
    }
    else if([tableView isEqual:dstTableView])
    {
        count = [dstData count];
    }
    return count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return kCellHeight;
}

- (UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath
{
    UITableViewCell* result = nil;
    if([tableView isEqual:srcTableView])
    {
        result = [self srcTableCellForRowAtIndexPath:indexPath];
    }
    else if([tableView isEqual:dstTableView])
    {
        result = [self dstTableCellForRowAtIndexPath:indexPath];
    }
    
    return result;
}

#pragma mark -
#pragma mark Helper methods for table stuff

- (UITableViewCell*)srcTableCellForRowAtIndexPath:(NSIndexPath*)indexPath
{
    // tell our source table what kind of cell to use and its title for the given row
    UITableViewCell *cell = [srcTableView dequeueReusableCellWithIdentifier:kCellIdentifier];
    if (cell == nil)
    {
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault
                                       reuseIdentifier:kCellIdentifier] autorelease];
        
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        cell.textLabel.textColor = [UIColor darkGrayColor];
    }
    cell.textLabel.text = [[srcData objectAtIndex:indexPath.row] description];
    
    return cell;
}

- (UITableViewCell*)dstTableCellForRowAtIndexPath:(NSIndexPath*)indexPath
{
    // tell our destination table what kind of cell to use and its title for the given row
    UITableViewCell *cell = [dstTableView dequeueReusableCellWithIdentifier:kCellIdentifier];
    if (cell == nil)
    {
        cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault
                                       reuseIdentifier:kCellIdentifier] autorelease];
        
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
        cell.textLabel.textColor = [UIColor darkGrayColor];
    }
    cell.textLabel.text = [[dstData objectAtIndex:indexPath.row] description];
    
    return cell;
}
*/

#pragma mark - Cell Config
-(IgniteSegmentCell*) createUnscheduledCell:(EntitySegment *)segment
{
    IgniteSegmentCell *cell = (IgniteSegmentCell *)[tableList dequeueReusableCellWithIdentifier: @"UnscheduledTripSegmentCell"];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"IgniteItinUnscheduledCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[IgniteSegmentCell class]])
                cell = (IgniteSegmentCell *)oneObject;
    }
    return cell;
}

-(void)configureCellMeeting:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment
{
    if ([SegmentData isSegmentScheduled:segment])
    {
        cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_event"];
        
        NSString *departTime = @"";
        departTime = [DateTimeFormatter formatTimeForTravel:segment.relStartLocation.dateLocal];
        
        NSArray *aTime = [departTime componentsSeparatedByString:@" "];
        if([aTime count] == 2)
        {
            cell.lblTime.text = [aTime objectAtIndex:0];
            cell.lblAmPm.text = [aTime objectAtIndex:1];
        }
        else
        {
            cell.lblTime.text = departTime;
            cell.lblAmPm.text = @"";
        }
        cell.lblHeading.text = segment.segmentName; // @"Meeting on oppportunity
    }
    else 
    {
        // Do not display anydata until drag drop finishes
        if (self.droppedData != nil && self.droppedData == segment)
        {
            cell.ivIcon.image = nil;
            cell.lblHeading.text = @"";
            cell.lblSub1.text = @"";
            cell.lblSub2.text = @"";
            return;
        }
        cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_blue_event"];
        
        cell.lblHeading.text = @"Unscheduled Meeting";
    }
    
    if(segment.vendorName != nil)
        cell.lblSub1.text = segment.vendorName; // Account Name
    else
        cell.lblSub1.text = @"";

    cell.lblSub2.text = segment.descriptionSeg; // Contact Name

}

-(void)configureCellDining:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment
{
    if ([SegmentData isSegmentScheduled:segment])
    {
        cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_dine"];
        
        NSString *departTime = @"";
        departTime = [DateTimeFormatter formatTimeForTravel:segment.relStartLocation.dateLocal];
        
        NSArray *aTime = [departTime componentsSeparatedByString:@" "];
        if([aTime count] == 2)
        {
            cell.lblTime.text = [aTime objectAtIndex:0];
            cell.lblAmPm.text = [aTime objectAtIndex:1];
        }
        else
        {
            cell.lblTime.text = departTime;
            cell.lblAmPm.text = @"";
        }

        cell.lblHeading.text = segment.segmentName; // @"Dining"
        cell.lblSub1.text = [SegmentData getAttribute:@"VendorName" fromSegment:segment]; // Account Name
        cell.lblSub2.text = [SegmentData getAttribute:@"Address" fromSegment:segment]; // Contact Name
    }
    else 
    {
        cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_blue_recommend"];
        
        cell.lblHeading.text = segment.segmentName;
        cell.lblSub1.text = segment.relStartLocation.city;
        cell.lblSub2.text = @"";
    }

}

// Copied from TripDetailsViewController.m
-(void) configureCellParking:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_parking"];
    
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblHeading.text = vendorName;
    
    NSString *departTime = @"";
    departTime = [DateTimeFormatter formatTimeForTravel:segment.relStartLocation.dateLocal];
    
    NSArray *aTime = [departTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        cell.lblTime.text = [aTime objectAtIndex:0];
        cell.lblAmPm.text = [aTime objectAtIndex:1];
    }
    else
    {
        cell.lblTime.text = departTime;
        cell.lblAmPm.text = @"";
    }
    
    if(segment.relStartLocation.address != nil)
        cell.lblSub1.text = segment.relStartLocation.address;
    else
        cell.lblSub1.text = @"";
    
    cell.lblSub2.text = [SegmentData getCityStateZip:segment.relStartLocation];
}

-(void)configureCellHotel:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_hotel"];
    
    NSString *vendorName;
    if (segment.segmentName != nil)
        vendorName = segment.segmentName;
    else if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblHeading.text = vendorName;
    
    cell.lblAmPm.text = @"";//[Localizer getLocalizedText:@"In"];
    cell.lblTime.text = [Localizer getLocalizedText:@"Check  In"];
    CGRect rect = cell.lblTime.frame;
    cell.lblTime.frame = CGRectMake(rect.origin.x, rect.origin.y, rect.size.width, 40);
    cell.lblTime.font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:15.0f];
    cell.lblTime.numberOfLines = 2;
    
    EntitySegmentLocation* loc = segment.relStartLocation;
    if ([@"San Francisco" isEqualToString: loc.city] || [@"SFO" isEqualToString:loc.cityCode])
    {
        cell.lblSub1.text = @"335 Powell Street";
        cell.lblSub2.text = @"San Francisco, CA 94102";
    }
    else
    {    
        if(segment.relStartLocation.address != nil)
            cell.lblSub1.text = segment.relStartLocation.address;
        else
            cell.lblSub1.text = @"";
        
        cell.lblSub2.text = [SegmentData getCityStateZip:segment.relStartLocation];
    }
}


-(void) configureCellRide:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_taxi"];
    
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblHeading.text = vendorName;
    
    NSString *departTime = @"";
    departTime = [DateTimeFormatter formatTimeForTravel:segment.relStartLocation.dateLocal];
    
    NSArray *aTime = [departTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        cell.lblTime.text = [aTime objectAtIndex:0];
        cell.lblAmPm.text = [aTime objectAtIndex:1];
    }
    else
    {
        cell.lblTime.text = departTime;
        cell.lblAmPm.text = @"";
    }
    
    if(segment.relStartLocation.address != nil)
        cell.lblSub1.text = segment.relStartLocation.address;
    else
        cell.lblSub1.text = @"";
    
    cell.lblSub2.text = [SegmentData getCityStateZip:segment.relStartLocation];
}


-(void) configureCellCar:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_car"];
    
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblHeading.text = vendorName;
    
    NSMutableString *departTime = [NSMutableString string];
    NSMutableString *departDate = [NSMutableString string];
    [SegmentData getDepartTimeString:segment timeStr:departTime dateStr:departDate];
    
    NSArray *aTime = [departTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        cell.lblTime.text = [aTime objectAtIndex:0];
        cell.lblAmPm.text = [aTime objectAtIndex:1];
    }
    else
        cell.lblTime.text = departTime;
    
    cell.lblSub1.text = [SegmentData getAirportCity:segment.relStartLocation];
    
    NSString *location = [SegmentData getCityState:segment.relStartLocation];
    if([location length] == 0 && [segment.classOfCarLocalized length])
        location = segment.classOfCarLocalized;
    
    cell.lblSub2.text = location;
    
    //NSLog(@"segment.bodyTypeName = %@", segment.bodyTypeName);
//    [location release];
}

-(void) configureCellRail:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_rail"];
    
    NSString *railStation = [SegmentData getRailStation:segment.relStartLocation];
    
    NSString *endRailStation = [SegmentData getRailStation:segment.relEndLocation];
    
    cell.lblHeading.text = [NSString stringWithFormat:@"%@ - %@", railStation, endRailStation];
    
    NSMutableString *departTime = [NSMutableString string];
    [SegmentData getDepartTimeString:segment timeStr:departTime dateStr:nil];
    
    NSArray *aTime = [departTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        cell.lblTime.text = [aTime objectAtIndex:0];
        cell.lblAmPm.text = [aTime objectAtIndex:1];
    }
    else
        cell.lblTime.text = departTime;
    
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    if( segment.trainNumber == nil)
        segment.trainNumber = @"--";
    
    cell.lblSub1.text = [NSString stringWithFormat:@"%@ %@", vendorName, segment.trainNumber]; 
    
    NSString *platform = segment.relStartLocation.platform;
    NSString *wagon = segment.wagonNumber;
    if(platform == nil)
        platform = @"--";
    
    if(wagon == nil)
        wagon = @"--";
    
    cell.lblSub2.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"SLV_PLATFORM_WAGON"], platform, wagon];
    
}

-(void) configureCellAir:(IgniteSegmentCell *)cell segment:(EntitySegment *)segment
{
    cell.ivIcon.image = [UIImage imageNamed:@"itin_icon_flight"];
    cell.lblHeading.text = [NSString stringWithFormat:@"%@ %@ %@", segment.relStartLocation.airportCity, [Localizer getLocalizedText:@"SLV_TO"], segment.relEndLocation.airportCity];;
    
    NSMutableString *departTime = [NSMutableString string];
    [SegmentData getDepartTimeString:segment timeStr:departTime dateStr:nil];
    
    NSArray *aTime = [departTime componentsSeparatedByString:@" "];
    if([aTime count] == 2)
    {
        cell.lblTime.text = [aTime objectAtIndex:0];
        cell.lblAmPm.text = [aTime objectAtIndex:1];
    }
    else
        cell.lblTime.text = departTime;
    
    NSString *vendorName;
    if (segment.vendorName != nil)
        vendorName = segment.vendorName;
    else 
        vendorName = segment.vendor;
    
    cell.lblSub1.text = [NSString stringWithFormat:@"%@ %@", vendorName, segment.flightNumber]; 
    
    NSMutableString *term = [NSMutableString string];
    NSMutableString *gate = [NSMutableString string];
    
    [SegmentData getDepartTermGate:segment terminal:term gate:gate];
    
    if ([@"--" isEqualToString:term])
    {
        int cCount = [segment.relStartLocation.airportCity length];
        term = [NSString stringWithFormat:@"%d", (cCount/6+4)];
    }
    if ([@"--" isEqualToString:gate])
    {
        int cCount = [segment.relStartLocation.airportCity length];
        gate = [NSString stringWithFormat:@"%d", (cCount/3+2)];        
    }
    
    cell.lblSub2.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal t Gate t"], term, gate];
}

@end
