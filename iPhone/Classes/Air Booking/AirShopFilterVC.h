//
//  AirShopFilterVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/8/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "EntityAirShopResults.h"
#import "AirShopResultsManager.h"
#import "AirShopFilterCell.h"
#import "AirFilterSummaryManager.h"
#import "AirShop.h"
#import "AirRuleManager.h"

@interface AirShopFilterVC : MobileViewController <NSFetchedResultsControllerDelegate, UITableViewDelegate, UITableViewDataSource, UIActionSheetDelegate>
{
    UITableView *tableList;
    EntityAirShopResults *airShopResults;
    int sortOrder;
    AirShop *airShop;
}
@property (nonatomic, strong) IBOutlet UILabel *lblHeading;
@property (nonatomic, strong) IBOutlet UILabel *lblDates;
@property (nonatomic, strong) IBOutlet UILabel *lblBenchmark;
@property (nonatomic, strong) IBOutlet UIImageView *imageView;
@property (strong, nonatomic) IBOutlet UIView *viewForTableViewHeader;
@property (strong, nonatomic) IBOutlet UIButton *priceToBeatInfoButton;

@property (nonatomic, strong) AirShop *airShop;
@property int sortOrder;
@property (strong, nonatomic) IBOutlet UITableView *tableList;
@property (nonatomic, strong) NSFetchedResultsController    *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext        *managedObjectContext;
@property (nonatomic, strong) EntityAirShopResults *airShopResults;

@property (strong, nonatomic) NSMutableArray                *taFields;  // TravelAuth fields for GOV
- (IBAction)priceToBeatHeaderInfoClicked:(UIButton *)sender;

-(void) refetchData;
- (void)configureCell:(AirShopFilterCell *)cell atIndexPath:(NSIndexPath *)indexPath;

-(void) showAction:(id)sender;
-(void) makeToolbar;
@end
