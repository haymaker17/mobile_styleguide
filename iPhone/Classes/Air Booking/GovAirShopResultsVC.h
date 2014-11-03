//
//  GovAirShopResultsVC.h
//  ConcurMobile
//
//  Created by Shifan Wu on 2/19/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "AirShop.h"
#import "EntityAirCriteria.h"
#import "GovAirShopSummaryCell.h"
#import "UIButton+Block.h"

@interface GovAirShopResultsVC : MobileViewController <NSFetchedResultsControllerDelegate, UITableViewDelegate, UITableViewDataSource>


@property (strong, nonatomic) IBOutlet UIView *itinHeaderView;
@property (strong, nonatomic) IBOutlet UILabel *lblHeading;
@property (strong, nonatomic) IBOutlet UILabel *lblDates;
@property (strong, nonatomic) IBOutlet UILabel *lblBenchmark;
@property (strong, nonatomic) IBOutlet UITableView *tableList;

@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;

@property (strong, nonatomic) AirShop *airShop;
@property (strong, nonatomic) NSMutableDictionary *vendors;
@property (strong, nonatomic) NSMutableDictionary *airportCodes;
@property (strong, nonatomic) NSMutableDictionary *equipment;
@property (strong, nonatomic) NSMutableArray *taFields;

-(void) refetchData;
-(void)configureCell:(GovAirShopSummaryCell *)cell atIndexPath:(NSIndexPath *)indexPath;
-(EntityAirCriteria *) loadEntity;

@end
