//
//  IgniteItinDetailSocialOppDS.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExMsgRespondDelegate.h"
#import "EntityTrip.h"
#import "IgniteItinDetailSocialOppDelegate.h"
#import "EntitySalesOpportunity.h"
#import "IgniteOpportunityCell.h"

@interface IgniteItinDetailSocialOppDS : NSObject <NSFetchedResultsControllerDelegate, 
    UITableViewDelegate, UITableViewDataSource,
    ExMsgRespondDelegate>
{
    UITableView                             *tableList;
    
    id<IgniteItinDetailSocialOppDelegate>   __weak _delegate;
    NSFetchedResultsController              *fetchedResultsController;
    NSManagedObjectContext                  *managedObjectContext;
    
    
    // Trip data, seeded by tripKey
	NSString                                *tripKey;
    EntityTrip                              *trip;
    NSMutableArray                          *lstContact;  // contacts

    // Drag & drop
    IgniteOpportunityCell                   *draggedCell;
    EntitySalesOpportunity                  *draggedData;

}


@property (nonatomic, strong) UITableView                           *tableList;

@property (nonatomic, weak) id<IgniteItinDetailSocialOppDelegate> delegate;
@property (nonatomic, strong) NSMutableArray                        *lstContact;

@property (nonatomic, strong) NSString                              *tripKey;
@property (nonatomic, strong) EntityTrip                            *trip;

@property (nonatomic, strong) NSFetchedResultsController    *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext                *managedObjectContext;

@property (nonatomic, strong) IgniteOpportunityCell                 *draggedCell;
@property (nonatomic, strong) EntitySalesOpportunity                *draggedData;

- (void) fetchedResults;
- (void) respondToFoundData:(Msg *)msg;
- (void) setSeedData:(NSManagedObjectContext *)con withTripKey:(NSString *)tKey withTrip:(EntityTrip *)t withTable:(UITableView *)tbl withDelegate:(id<IgniteItinDetailSocialOppDelegate>) del;

- (UITableViewCell*)startDraggedCellWithCell:(UITableViewCell*)cell AtPoint:(CGPoint)point WithIndexPath:(NSIndexPath*) path;

- (void) cancelDrop;
@end
