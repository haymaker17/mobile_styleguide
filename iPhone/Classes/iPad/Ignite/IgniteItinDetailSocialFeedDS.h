//
//  IgniteItinDetailSocialFeedDS.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExMsgRespondDelegate.h"
#import "EntityTrip.h"
#import "IgniteItinDetailSocialFeedDelegate.h"
#import "IgniteChatterCellDelegate.h"

@interface IgniteItinDetailSocialFeedDS : NSObject <NSFetchedResultsControllerDelegate, 
    UITableViewDelegate, UITableViewDataSource,
    IgniteChatterCellDelegate,
    ExMsgRespondDelegate>
{
    UITableView                         *tableList;
    
    id<IgniteItinDetailSocialFeedDelegate> __weak _delegate;
    
    NSFetchedResultsController          *fetchedResultsController;
    NSManagedObjectContext              *managedObjectContext;
    
    
    // Trip data, seeded by tripKey
	NSString                            *tripKey;
    EntityTrip                          *trip;
    NSMutableArray                      *lstContact;  // contacts
    
}



@property (nonatomic, strong) UITableView                           *tableList;

@property (nonatomic, weak) id<IgniteItinDetailSocialFeedDelegate> delegate;

@property (nonatomic, strong) NSMutableArray                        *lstContact;

@property (nonatomic, strong) NSString                              *tripKey;
@property (nonatomic, strong) EntityTrip                            *trip;

@property (nonatomic, strong) NSFetchedResultsController            *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext                *managedObjectContext;


-(void) fetchedResults;
-(void) respondToFoundData:(Msg *)msg;
-(void) setSeedData:(NSManagedObjectContext *)con withTripKey:(NSString *)tKey withTrip:(EntityTrip *)t withTable:(UITableView *)tbl withDelegate:(id<IgniteItinDetailSocialFeedDelegate>) del;
-(void) updateChatterFeed;

+(int) daysFromDate:(NSDate*)fromDate toDate:(NSDate*)toDate;
+(NSString*) ageFromDate:(NSDate*)date;

- (NSString*)getFeedLabel;

@end
