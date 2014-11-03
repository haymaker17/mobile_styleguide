//
//  AirShopFilteredResultsVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/10/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "EntityAirShopResults.h"
#import "AirShopResultsManager.h"
#import "AirShopFilteredCell.h"
#import "AirFilterSummaryManager.h"
#import "EntityAirFilterSummary.h"
#import "AirShop.h"
#import "CreditCard.h"
#import "EntityAirFilter.h"
#import "ViolationDetailsVC.h"
#import "AirShopMessageCell.h"
#import "MobileAlertView.h"
#import "OptionsSelectDelegate.h"
#import "AffinityProgram.h"
#import "TravelCustomFieldsManager.h"
#import "BoolEditDelegate.h"
#import "AmtrakSellData.h"
#import "PreSellOptions.h"

@interface AirShopFilteredResultsVC : MobileViewController <NSFetchedResultsControllerDelegate, UITableViewDelegate, 
    OptionsSelectDelegate,
    UITableViewDataSource, UIAlertViewDelegate, BoolEditDelegate>
{
    UITableView *tableList;
    EntityAirShopResults *airShopResults;
    EntityAirFilterSummary *airSummary;
    AirShop *airShop;
    int chosenCardIndex;
	CreditCard	*chosenCreditCard;
    
    BOOL					isDelayingFirstCard;
    NSMutableArray          *aClass;
    NSMutableDictionary     *dictClass;
    NSMutableArray          *aButtons;
    
    AffinityProgram         *chosenFrequentFlyer;
    
    NSArray				*violationReasons;
	NSArray				*violationReasonLabels;
//Custom Fields 
    BOOL                hideCustomFields;
    BOOL                editedDependentCustomField;
    BOOL                isDirty;
    AmtrakSellData			*airRezResponse;

}
//Custom Fields Properties
@property BOOL isDirty;
@property (strong, nonatomic) NSMutableDictionary *dictSections;
@property (strong, nonatomic) IBOutlet UIView                  *viewSegmentHeader;
@property BOOL editedDependentCustomField;
@property (strong, nonatomic) EntityTravelCustomFields *selectedCustomField;
@property (nonatomic, strong) NSMutableArray          *tcfRows;
@property BOOL hideCustomFields; // Hide custom fields when coming from existing trip
@property (strong, nonatomic) NSMutableArray			*aSections;

@property (nonatomic, strong) IBOutlet UILabel *lblHeading;
@property (nonatomic, strong) IBOutlet UILabel *lblDates;
@property (nonatomic, strong) IBOutlet UILabel *lblBenchmark;
@property (nonatomic, strong) IBOutlet UIImageView *imageView;

@property int			chosenCardIndex;
@property(strong, nonatomic) CreditCard				*chosenCreditCard;
@property (nonatomic, strong) NSArray *creditCards;
@property BOOL isPreSellOptionsLoaded;
@property(strong, nonatomic) NSMutableArray          *aButtons;

@property (nonatomic, strong) AirShop *airShop;
@property (strong, nonatomic) IBOutlet UITableView *tableList;
@property (nonatomic, strong) NSFetchedResultsController    *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext        *managedObjectContext;
@property (nonatomic, strong) EntityAirShopResults *airShopResults;
@property (nonatomic, strong) EntityAirFilterSummary *airSummary;

@property (strong, nonatomic) NSMutableArray          *aClass;
@property (strong, nonatomic) NSMutableDictionary     *dictClass;
@property (strong, nonatomic) AffinityProgram           *chosenFrequentFlyer;
@property (strong, nonatomic) NSArray *affinityPrograms;
@property (strong, nonatomic) NSArray			*violationReasons;
@property (strong, nonatomic) NSArray			*violationReasonLabels;
@property (strong, nonatomic) NSString			*creditCardCvvNumber;
@property (strong, nonatomic) PreSellOptions    *preSellOptions;
@property (strong, nonatomic) NSMutableArray            *taFields;  // TravelAuth fields for GOV
@property (nonatomic) BOOL reloadFlightOptionsSection;
-(void) refetchData;
- (void)configureCell:(AirShopFilteredCell *)cell atIndexPath:(NSIndexPath *)indexPath;
-(void)reserveFlight;

#pragma mark - cards
-(void) showCards:(id)sender;
-(void)chooseCard:(int)cardIndex;
-(void)chooseFirstCard;
-(void) fillClass;

#pragma mark - Reserve Button
-(void) makeReserveButton:(id)sender;
-(void) showReserveAlert;

#pragma mark - Frequent Flyer program select
-(void) showAffinityPrograms:(id)sender;

-(void) optionSelected:(NSObject*)obj withIdentifier:(NSObject*) identifier;

#pragma mark - Custom Fields Methods
-(void) processFilterDataForArray;
-(UITableViewCell *)configureCustomFieldCellAtIndexPath:(NSIndexPath *)indexPath;
-(void) onSelectLongTextOrNumericFieldCellAtIndexPath:(NSIndexPath *)indexPath;
-(BOOL) hasPendingRequiredTripFields;
-(void) reloadCustomFieldsSection;
-(void) updateDynamicCustomFields;
-(void) fetchCustomFields;

@end
