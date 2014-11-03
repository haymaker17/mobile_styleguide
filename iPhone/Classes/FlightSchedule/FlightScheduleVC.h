//
//  FlightScheduleVC.h
//  ConcurMobile
//
//  Created by Paul Schmidt on 18 December 2012.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "EntitySegment.h"
#import "FlightScheduleData.h"

@interface FlightScheduleVC : MobileViewController <UITableViewDelegate, UITableViewDataSource>
{
    EntitySegment* segment;
    FlightScheduleData *flightData;
    NSMutableArray *flightOptions;
    
    __weak UIImageView *imgHeading;
    UITableView *tableList;
    UILabel *lblHeading, *lblDates;
    
    UINavigationBar			*navBar;
    UIBarButtonItem			*doneBtn;
}
- (IBAction)donePressed:(id)sender;

@property (nonatomic, strong) EntitySegment *segment;
@property (nonatomic, strong) FlightScheduleData *flightData;
@property (nonatomic, strong) NSMutableArray *flightOptions;

@property (nonatomic, strong) IBOutlet UINavigationBar			*navBar;
@property (nonatomic, strong) IBOutlet UIBarButtonItem			*doneBtn;

@property (weak, nonatomic) IBOutlet UIImageView *imgHeading;

@property (nonatomic, strong) IBOutlet UILabel *lblHeading;
@property (nonatomic, strong) IBOutlet UILabel *lblDates;

@property (strong, nonatomic) IBOutlet UITableView *tableList;
@property (nonatomic, strong) NSFetchedResultsController    *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext        *managedObjectContext;

-(void) refetchData;
//-(void)configureCell:(AirShopFilterCell *)cell atIndexPath:(NSIndexPath *)indexPath;



-(NSString*)titleForNoDataView;
-(NSString*) imageForNoDataView;

@end
