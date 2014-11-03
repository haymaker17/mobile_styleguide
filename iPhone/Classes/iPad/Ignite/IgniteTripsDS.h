//
//  IgniteTripsDS.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "IgniteTripsDelegate.h"

@interface IgniteTripsDS : NSObject <NSFetchedResultsControllerDelegate, 
    UITableViewDelegate, UITableViewDataSource,
    ExMsgRespondDelegate>
{
    UITableView                         *tableList;
    
    id<IgniteTripsDelegate>             __weak _delegate;
    NSFetchedResultsController          *fetchedResultsController;
    NSManagedObjectContext              *managedObjectContext;
    
    
    // tripFilter: CURRENT, PAST, FUTURE
	NSString                            *tripFilter;
//    NSMutableArray                      *lstTrips;
    
}


@property (nonatomic, strong) UITableView                       *tableList;

@property (nonatomic, weak) id<IgniteTripsDelegate>           delegate;
//@property (nonatomic, retain) NSMutableArray                    *lstTrips;
@property (nonatomic, strong) NSString                          *tripFilter;

@property (nonatomic, strong) NSFetchedResultsController        *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext            *managedObjectContext;

- (void) fetchedResults;
- (void) respondToFoundData:(Msg *)msg;
- (void) setSeedData:(NSManagedObjectContext *)con withTripFilter:(NSString *)filter withTable:(UITableView *)tbl withDelegate:(id<IgniteTripsDelegate>) del;
- (void)resetTable:(UITableView*) tbl; // handle memory warning


@end
