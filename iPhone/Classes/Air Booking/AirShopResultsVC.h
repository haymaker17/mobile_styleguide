//
//  AirShopResultsVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/5/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "EntityAirShopResults.h"
#import "AirShopResultsManager.h"
#import "AirShopSummaryCell.h"
#import "AirShop.h"
#import "EntityAirCriteria.h"

@interface AirShopResultsVC : MobileViewController <NSFetchedResultsControllerDelegate, UITableViewDelegate, UITableViewDataSource>
{
}

@property (nonatomic, strong) IBOutlet UILabel *lblHeading;
@property (nonatomic, strong) IBOutlet UILabel *lblDates;
@property (nonatomic, strong) IBOutlet UILabel *lblBenchmark;
@property (nonatomic, strong) IBOutlet UIImageView *imageView;
@property (nonatomic, strong) AirShop *airShop;
@property (nonatomic, strong) NSMutableDictionary *vendors;
@property (nonatomic, strong) NSMutableDictionary *airportCodes;

@property (nonatomic, strong) NSMutableDictionary *equipment;
@property (strong, nonatomic) IBOutlet UITableView *tableList;
@property (nonatomic, strong) NSFetchedResultsController    *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext        *managedObjectContext;

@property (strong, nonatomic) NSMutableArray                *taFields;  // TravelAuth fields for GOV

@property (strong, nonatomic) IBOutlet UIView *viewForTableViewHeader;
@property (weak, nonatomic) IBOutlet UIButton *priceToBeatInfoButton;
- (IBAction)priceToBeatHeaderInfoClicked:(UIButton *)sender;

-(void) refetchData;
- (void)configureCell:(AirShopSummaryCell *)cell atIndexPath:(NSIndexPath *)indexPath;
-(EntityAirCriteria *) loadEntity;
@end
