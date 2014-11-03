//
//  IgniteItinDetailTripDS.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExMsgRespondDelegate.h"
#import "EntityTrip.h"
#import "IgniteItinDetailTripDelegate.h"
#import "EntitySalesOpportunity.h"
#import "IgniteSegmentCell.h"

@interface IgniteItinDetailTripDS : NSObject <NSFetchedResultsControllerDelegate, 
    UITableViewDelegate, UITableViewDataSource,
    ExMsgRespondDelegate>
{
    UITableView                         *tableList;
    
    id<IgniteItinDetailTripDelegate>    __weak _delegate;
//    NSFetchedResultsController      *fetchedResultsController;
    NSManagedObjectContext              *managedObjectContext;
    

    // Trip data, seeded by tripKey
	NSString                            *tripKey;
    EntityTrip                          *trip;
	NSMutableDictionary                 *dictSegmentsByDate;   // tripBits
    NSMutableArray                      *lstDates;  // keys

    EntitySegment                       *droppedData;   // Unscheduled meeting
    IgniteSegmentCell                   *droppedCell;
    
    NSMutableSet                        *dictScheduledOppNames; // A set of opportunity names already have meeting scheduled in trip
}


@property (nonatomic, strong) UITableView                       *tableList;

@property (nonatomic, weak) id<IgniteItinDetailTripDelegate>  delegate;
@property (nonatomic, strong) NSMutableArray                    *lstDates;
@property (nonatomic, strong) NSMutableDictionary               *dictSegmentsByDate;

@property (nonatomic, strong) NSString                          *tripKey;
@property (nonatomic, strong) EntityTrip                        *trip;

//@property (nonatomic, retain) NSFetchedResultsController    *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext            *managedObjectContext;
@property (nonatomic, strong) EntitySegment                     *droppedData;
@property (nonatomic, strong) IgniteSegmentCell                 *droppedCell;

@property (nonatomic, strong) NSMutableSet                      *dictScheduledOppNames;

//- (void) fetchedResults;
- (void) respondToFoundData:(Msg *)msg;
- (void) setSeedData:(NSManagedObjectContext *)con withTripKey:(NSString *)tKey withTrip:(EntityTrip *)t withTable:(UITableView *)tbl withDelegate:(id<IgniteItinDetailTripDelegate>) del;

// Drag & drop
- (void) addMeeting:(EntitySalesOpportunity*) opp afterIndexPath:(NSIndexPath*) indexPath atPoint:(CGPoint)point;
- (NSIndexPath*) getDroppedCellIndexPath;
- (void) removeDroppedData;
- (void) moveMeetingAfterIndexPath:(NSIndexPath*) indexPath;
- (BOOL) isOpportunityScheduled:(NSString*) oppId;
- (void) addScheduledOpportunity:(NSString*) oppId;
- (EntitySegment*) segmentFromIndexPath:(NSIndexPath *) indexPath;

// Segment edit
- (void) segmentUpdated:(EntitySegment*) segment;

@end
